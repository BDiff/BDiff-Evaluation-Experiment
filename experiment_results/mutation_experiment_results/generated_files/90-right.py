#!/usr/bin/env python3
import asyncio
from asyncio.base_events import BaseEventLoop
from concurrent.futures import Executor, ProcessPoolExecutor
from functools import partial
import keyword
import os
from pathlib import Path
import tokenize
import sys
from typing import (
    Dict, Generic, Iterable, Iterator, List, Optional, Set, Tuple, TypeVar, Union
)

from attr import attrib, dataclass, Factory
import click

# lib2to3 fork
from blib2to3.pytree import Node, Leaf, type_repr
from blib2to3 import pygram, pytree
from blib2to3.pgen2 import driver, token
from blib2to3.pgen2.parse import ParseError

__version__ = "18.3a1"
DEFAULT_LINE_LENGTH = 88
# types
syms = pygram.python_symbols
FileContent = str
Encoding = str
Depth = int
NodeType = int
LeafID = int
Priority = int
LN = Union[Leaf, Node]
out = partial(click.secho, bold=True, err=True)
err = partial(click.secho, fg='red', err=True)


class NothingChanged(UserWarning):
    """Raised by `format_file` when the reformatted code is the same as source."""


class CannotSplit(Exception):
    """A readable split that fits the allotted line length is impossible.

    Raised by `left_hand_split()` and `right_hand_split()`.
    """


@click.command()
@click.option(
    '-l',
    '--line-length',
    type=int,
    default=DEFAULT_LINE_LENGTH,
    help='How many character per line to allow.',
    show_default=True,
)
@click.option(
    '--check',
    is_flag=True,
    help=(
        "Don't write back the files, just return the status.  Return code 0 "
        "means nothing changed.  Return code 1 means some files were "
        "reformatted.  Return code 123 means there was an internal error."
    ),
)
@click.option(
    '--fast/--safe',
    is_flag=True,
    help='If --fast given, skip temporary sanity checks. [default: --safe]',
)
@click.version_option(version=__version__)
@click.argument(
    'src',
    nargs=-1,
    type=click.Path(exists=True, file_okay=True, dir_okay=True, readable=True),
)
@click.pass_context
def main(
    ctx: click.Context, line_length: int, check: bool, fast: bool, src: List[str]
) -> None:
    """The uncompromising code formatter."""
    sources: List[Path] = []
    for s in src:
        p = Path(s)
        if p.is_dir():
            sources.extend(gen_python_files_in_dir(p))
        elif p.is_file():
            # if a file was explicitly given, we don't care about its extension
            sources.append(p)
        else:
            err(f'invalid path: {s}')
    if len(sources) == 0:
        ctx.exit(0)
    elif len(sources) == 1:
        p = sources[0]
        report = Report()
        try:
            changed = format_file_in_place(
                p, line_length=line_length, fast=fast, write_back=not check
            )
            report.done(p, changed)
        except Exception as exc:
            report.failed(p, str(exc))
        ctx.exit(report.return_code)
    else:
        loop = asyncio.get_event_loop()
        executor = ProcessPoolExecutor(max_workers=os.cpu_count())
        return_code = 1
        try:
            return_code = loop.run_until_complete(
                schedule_formatting(
                    sources, line_length, not check, fast, loop, executor
                )
            )
        finally:
            loop.close()
            ctx.exit(return_code)


async def schedule_formatting(
    sources: List[Path],
    line_length: int,
    write_back: bool,
    fast: bool,
    loop: BaseEventLoop,
    executor: Executor,
) -> int:
    tasks = {
        src: loop.run_in_executor(
            executor, format_file_in_place, src, line_length, fast, write_back
        )
        for src in sources
    }
    await asyncio.wait(tasks.values())
    cancelled = []
    report = Report()
    for src, task in tasks.items():
        if not task.done():
            report.failed(src, 'timed out, cancelling')
            task.cancel()
            cancelled.append(task)
        elif task.exception():
            report.failed(src, str(task.exception()))
        else:
            report.done(src, task.result())
    if cancelled:
        await asyncio.wait(cancelled, timeout=2)
    out('All done! ✨ 🍰 ✨')
    click.echo(str(report))
    return report.return_code


def format_file_in_place(
    src: Path, line_length: int, fast: bool, write_back: bool = False
) -> bool:
    """Format the file and rewrite if changed. Return True if changed."""
    try:
        contents, encoding = format_file(src, line_length=line_length, fast=fast)
    except NothingChanged:
        return False

    if write_back:
        with open(src, "w", encoding=encoding) as f:
            f.write(contents)
    return True


def format_file(
    src: Path, line_length: int, fast: bool
) -> Tuple[FileContent, Encoding]:
    """Reformats a file and returns its contents and encoding."""
    with tokenize.open(src) as src_buffer:
        src_contents = src_buffer.read()
    if src_contents.strip() == '':
        raise NothingChanged(src)

    dst_contents = format_str(src_contents, line_length=line_length)
    if src_contents == dst_contents:
        raise NothingChanged(src)

    if not fast:
        assert_equivalent(src_contents, dst_contents)
        assert_stable(src_contents, dst_contents, line_length=line_length)
    return dst_contents, src_buffer.encoding


def format_str(src_contents: str, line_length: int) -> FileContent:
    """Reformats a string and returns new contents."""
    src_node = lib2to3_parse(src_contents)
    dst_contents = ""
    comments: List[Line] = []
    lines = LineGenerator()
    elt = EmptyLineTracker()
    py36 = is_python36(src_node)
    empty_line = Line()
    after = 0
    for current_line in lines.visit(src_node):
        for _ in range(after):
            dst_contents += str(empty_line)
        before, after = elt.maybe_empty_lines(current_line)
        for _ in range(before):
            dst_contents += str(empty_line)
        if not current_line.is_comment:
            for comment in comments:
                dst_contents += str(comment)
            comments = []
            for line in split_line(current_line, line_length=line_length, py36=py36):
                dst_contents += str(line)
        else:
            comments.append(current_line)
    for comment in comments:
        dst_contents += str(comment)
    return dst_contents


def lib2to3_parse(src_txt: str) -> Node:
    """Given a string with source, return the lib2to3 Node."""
    grammar = pygram.python_grammar_no_print_statement
    drv = driver.Driver(grammar, pytree.convert)
    if src_txt[-1] != '\n':
        nl = '\r\n' if '\r\n' in src_txt[:1024] else '\n'
        src_txt += nl
    try:
        result = drv.parse_string(src_txt, True)
    except ParseError as pe:
        lineno, column = pe.context[1]
        lines = src_txt.splitlines()
        try:
            faulty_line = lines[lineno - 1]
        except IndexError:
            faulty_line = "<line number missing in source>"
        raise ValueError(f"Cannot parse: {lineno}:{column}: {faulty_line}") from None

    if isinstance(result, Leaf):
        result = Node(syms.file_input, [result])
    return result


def lib2to3_unparse(node: Node) -> str:
    """Given a lib2to3 node, return its string representation."""
    code = str(node)
    return code


T = TypeVar('T')


class Visitor(Generic[T]):
    """Basic lib2to3 visitor that yields things on visiting."""

    def visit(self, node: LN) -> Iterator[T]:
        if node.type < 256:
            name = token.tok_name[node.type]
        else:
            name = type_repr(node.type)
        yield from getattr(self, f'visit_{name}', self.visit_default)(node)

    def visit_default(self, node: LN) -> Iterator[T]:
        if isinstance(node, Node):
            for child in node.children:
                yield from self.visit(child)


@dataclass
class DebugVisitor(Visitor[T]):
    tree_depth: int = attrib(default=0)

    def visit_default(self, node: LN) -> Iterator[T]:
        indent = ' ' * (2 * self.tree_depth)
        if isinstance(node, Node):
            _type = type_repr(node.type)
            out(f'{indent}{_type}', fg='yellow')
            self.tree_depth += 1
            for child in node.children:
                yield from self.visit(child)

            self.tree_depth -= 1
            out(f'{indent}/{_type}', fg='yellow', bold=False)
        else:
            _type = token.tok_name.get(node.type, str(node.type))
            out(f'{indent}{_type}', fg='blue', nl=False)
            if node.prefix:
                # We don't have to handle prefixes for `Node` objects since
                # that delegates to the first child anyway.
                out(f' {node.prefix!r}', fg='green', bold=False, nl=False)
            out(f' {node.value!r}', fg='blue', bold=False)


KEYWORDS = set(keyword.kwlist)
WHITESPACE = {token.DEDENT, token.INDENT, token.NEWLINE}
FLOW_CONTROL = {'return', 'raise', 'break', 'continue'}
STATEMENT = {
    syms.if_stmt,
    syms.while_stmt,
    syms.for_stmt,
    syms.try_stmt,
    syms.except_clause,
    syms.with_stmt,
    syms.funcdef,
    syms.classdef,
}
STANDALONE_COMMENT = 153
LOGIC_OPERATORS = {'and', 'or'}
COMPARATORS = {
    token.LESS,
    token.GREATER,
    token.EQEQUAL,
    token.NOTEQUAL,
    token.LESSEQUAL,
    token.GREATEREQUAL,
}
MATH_OPERATORS = {
    token.PLUS,
    token.MINUS,
    token.STAR,
    token.SLASH,
    token.VBAR,
    token.AMPER,
    token.PERCENT,
    token.CIRCUMFLEX,
    token.LEFTSHIFT,
    token.RIGHTSHIFT,
    token.DOUBLESTAR,
    token.DOUBLESLASH,
}
COMPREHENSION_PRIORITY = 20
COMMA_PRIORITY = 10
LOGIC_PRIORITY = 5
STRING_PRIORITY = 4
COMPARATOR_PRIORITY = 3
MATH_PRIORITY = 1


@dataclass
class BracketTracker:
    depth: int = attrib(default=0)
    bracket_match: Dict[Tuple[Depth, NodeType], Leaf] = attrib(default=Factory(dict))
    delimiters: Dict[LeafID, Priority] = attrib(default=Factory(dict))
    previous: Optional[Leaf] = attrib(default=None)

    def mark(self, leaf: Leaf) -> None:
        if leaf.type == token.COMMENT:
            return

        if leaf.type in CLOSING_BRACKETS:
            self.depth -= 1
            opening_bracket = self.bracket_match.pop((self.depth, leaf.type))
            leaf.opening_bracket = opening_bracket  # type: ignore
        leaf.bracket_depth = self.depth  # type: ignore
        if self.depth == 0:
            delim = is_delimiter(leaf)
            if delim:
                self.delimiters[id(leaf)] = delim
            elif self.previous is not None:
                if leaf.type == token.STRING and self.previous.type == token.STRING:
                    self.delimiters[id(self.previous)] = STRING_PRIORITY
                elif (
                    leaf.type == token.NAME and
                    leaf.value == 'for' and
                    leaf.parent and
                    leaf.parent.type in {syms.comp_for, syms.old_comp_for}
                ):
                    self.delimiters[id(self.previous)] = COMPREHENSION_PRIORITY
                elif (
                    leaf.type == token.NAME and
                    leaf.value == 'if' and
                    leaf.parent and
                    leaf.parent.type in {syms.comp_if, syms.old_comp_if}
                ):
                    self.delimiters[id(self.previous)] = COMPREHENSION_PRIORITY
        if leaf.type in OPENING_BRACKETS:
            self.bracket_match[self.depth, BRACKET[leaf.type]] = leaf
            self.depth += 1
        self.previous = leaf

    def any_open_brackets(self) -> bool:
        """Returns True if there is an yet unmatched open bracket on the line."""
        return bool(self.bracket_match)

    def max_priority(self, exclude: Iterable[LeafID] =()) -> int:
        """Returns the highest priority of a delimiter found on the line.

        Values are consistent with what `is_delimiter()` returns.
        """
        return max(v for k, v in self.delimiters.items() if k not in exclude)


@dataclass
class Line:
    depth: int = attrib(default=0)
    leaves: List[Leaf] = attrib(default=Factory(list))
    comments: Dict[LeafID, Leaf] = attrib(default=Factory(dict))
    bracket_tracker: BracketTracker = attrib(default=Factory(BracketTracker))
    inside_brackets: bool = attrib(default=False)
    has_for: bool = attrib(default=False)
    _for_loop_variable: bool = attrib(default=False, init=False)

    def append(self, leaf: Leaf, preformatted: bool = False) -> None:
        has_value = leaf.value.strip()
        if not has_value:
            return

        if self.leaves and not preformatted:
            # Note: at this point leaf.prefix should be empty except for
            # imports, for which we only preserve newlines.
            leaf.prefix += whitespace(leaf)
        if self.inside_brackets or not preformatted:
            self.maybe_decrement_after_for_loop_variable(leaf)
            self.bracket_tracker.mark(leaf)
            self.maybe_remove_trailing_comma(leaf)
            self.maybe_increment_for_loop_variable(leaf)
            if self.maybe_adapt_standalone_comment(leaf):
                return

        if not self.append_comment(leaf):
            self.leaves.append(leaf)

    @property
    def is_comment(self) -> bool:
        return bool(self) and self.leaves[0].type == STANDALONE_COMMENT

    @property
    def is_decorator(self) -> bool:
        return bool(self) and self.leaves[0].type == token.AT

    @property
    def is_import(self) -> bool:
        return bool(self) and is_import(self.leaves[0])

    @property
    def is_class(self) -> bool:
        return (
            bool(self) and
            self.leaves[0].type == token.NAME and
            self.leaves[0].value == 'class'
        )

    @property
    def is_def(self) -> bool:
        """Also returns True for async defs."""
        try:
            first_leaf = self.leaves[0]
        except IndexError:
            return False

        try:
            second_leaf: Optional[Leaf] = self.leaves[1]
        except IndexError:
            second_leaf = None
        return (
            (first_leaf.type == token.NAME and first_leaf.value == 'def') or
            (
                first_leaf.type == token.NAME and
                first_leaf.value == 'async' and
                second_leaf is not None and
                second_leaf.type == token.NAME and
                second_leaf.value == 'def'
            )
        )

    @property
    def is_flow_control(self) -> bool:
        return (
            bool(self) and
            self.leaves[0].type == token.NAME and
            self.leaves[0].value in FLOW_CONTROL
        )

    @property
    def is_yield(self) -> bool:
        return (
            bool(self) and
            self.leaves[0].type == token.NAME and
            self.leaves[0].value == 'yield'
        )

    def maybe_remove_trailing_comma(self, closing: Leaf) -> bool:
        if not (
            self.leaves and
            self.leaves[-1].type == token.COMMA and
            closing.type in CLOSING_BRACKETS
        ):
            return False

        if closing.type == token.RSQB or closing.type == token.RBRACE:
            self.leaves.pop()
            return True

        # For parens let's check if it's safe to remove the comma.  If the
        # trailing one is the only one, we might mistakenly change a tuple
        # into a different type by removing the comma.
        depth = closing.bracket_depth + 1  # type: ignore
        commas = 0
        opening = closing.opening_bracket  # type: ignore
        for _opening_index, leaf in enumerate(self.leaves):
            if leaf is opening:
                break

        else:
            return False

        for leaf in self.leaves[_opening_index + 1:]:
            if leaf is closing:
                break

            bracket_depth = leaf.bracket_depth  # type: ignore
            if bracket_depth == depth and leaf.type == token.COMMA:
                commas += 1
        if commas > 1:
            self.leaves.pop()
            return True

        return False

    def maybe_increment_for_loop_variable(self, leaf: Leaf) -> bool:
        """In a for loop, or comprehension, the variables are often unpacks.

        To avoid splitting on the comma in this situation, we will increase
        the depth of tokens between `for` and `in`.
        """
        if leaf.type == token.NAME and leaf.value == 'for':
            self.has_for = True
            self.bracket_tracker.depth += 1
            self._for_loop_variable = True
            return True

        return False

    def maybe_decrement_after_for_loop_variable(self, leaf: Leaf) -> bool:
        # See `maybe_increment_for_loop_variable` above for explanation.
        if self._for_loop_variable and leaf.type == token.NAME and leaf.value == 'in':
            self.bracket_tracker.depth -= 1
            self._for_loop_variable = False
            return True

        return False

    def maybe_adapt_standalone_comment(self, comment: Leaf) -> bool:
        """Hack a standalone comment to act as a trailing comment for line splitting.

        If this line has brackets and a standalone `comment`, we need to adapt
        it to be able to still reformat the line.

        This is not perfect, the line to which the standalone comment gets
        appended will appear "too long" when splitting.
        """
        if not (
            comment.type == STANDALONE_COMMENT and
            self.bracket_tracker.any_open_brackets()
        ):
            return False

        comment.type = token.COMMENT
        comment.prefix = '\n' + '    ' * (self.depth + 1)
        return self.append_comment(comment)

    def append_comment(self, comment: Leaf) -> bool:
        if comment.type != token.COMMENT:
            return False

        try:
            after = id(self.last_non_delimiter())
        except LookupError:
            comment.type = STANDALONE_COMMENT
            comment.prefix = ''
            return False

        else:
            if after in self.comments:
                self.comments[after].value += str(comment)
            else:
                self.comments[after] = comment
            return True

    def last_non_delimiter(self) -> Leaf:
        for i in range(len(self.leaves)):
            last = self.leaves[-i - 1]
            if not is_delimiter(last):
                return last

        raise LookupError("No non-delimiters found")

    def __str__(self) -> str:
        if not self:
            return '\n'

        indent = '    ' * self.depth
        leaves = iter(self.leaves)
        first = next(leaves)
        res = f'{first.prefix}{indent}{first.value}'
        for leaf in leaves:
            res += str(leaf)
        for comment in self.comments.values():
            res += str(comment)
        return res + '\n'

    def __bool__(self) -> bool:
        return bool(self.leaves or self.comments)


@dataclass
class EmptyLineTracker:
    """Provides a stateful method that returns the number of potential extra
    empty lines needed before and after the currently processed line.

    Note: this tracker works on lines that haven't been split yet.
    """
    previous_line: Optional[Line] = attrib(default=None)
    previous_after: int = attrib(default=0)
    previous_defs: List[int] = attrib(default=Factory(list))

    def maybe_empty_lines(self, current_line: Line) -> Tuple[int, int]:
        """Returns the number of extra empty lines before and after the `current_line`.

        This is for separating `def`, `async def` and `class` with extra empty lines
        (two on module-level), as well as providing an extra empty line after flow
        control keywords to make them more prominent.
        """
        before, after = self._maybe_empty_lines(current_line)
        self.previous_after = after
        self.previous_line = current_line
        return before, after

    def _maybe_empty_lines(self, current_line: Line) -> Tuple[int, int]:
        before = 0
        depth = current_line.depth
        while self.previous_defs and self.previous_defs[-1] >= depth:
            self.previous_defs.pop()
            before = (1 if depth else 2) - self.previous_after
        is_decorator = current_line.is_decorator
        if is_decorator or current_line.is_def or current_line.is_class:
            if not is_decorator:
                self.previous_defs.append(depth)
            if self.previous_line is None:
                # Don't insert empty lines before the first line in the file.
                return 0, 0

            if self.previous_line and self.previous_line.is_decorator:
                # Don't insert empty lines between decorators.
                return 0, 0

            newlines = 2
            if current_line.depth:
                newlines -= 1
            newlines -= self.previous_after
            return newlines, 0

        if current_line.is_flow_control:
            return before, 1

        if (
            self.previous_line and
            self.previous_line.is_import and
            not current_line.is_import and
            depth == self.previous_line.depth
        ):
            return (before or 1), 0

        if (
            self.previous_line and
            self.previous_line.is_yield and
            (not current_line.is_yield or depth != self.previous_line.depth)
        ):
            return (before or 1), 0

        return before, 0


@dataclass
class LineGenerator(Visitor[Line]):
    """Generates reformatted Line objects.  Empty lines are not emitted.

    Note: destroys the tree it's visiting by mutating prefixes of its leaves
    in ways that will no longer stringify to valid Python code on the tree.
    """
    current_line: Line = attrib(default=Factory(Line))
    standalone_comments: List[Leaf] = attrib(default=Factory(list))

    def line(self, indent: int = 0) -> Iterator[Line]:
        """Generate a line.

        If the line is empty, only emit if it makes sense.
        If the line is too long, split it first and then generate.

        If any lines were generated, set up a new current_line.
        """
        if not self.current_line:
            self.current_line.depth += indent
            return  # Line is empty, don't emit. Creating a new one unnecessary.

        complete_line = self.current_line
        self.current_line = Line(depth=complete_line.depth + indent)
        yield complete_line

    def visit_default(self, node: LN) -> Iterator[Line]:
        if isinstance(node, Leaf):
            for comment in generate_comments(node):
                if self.current_line.bracket_tracker.any_open_brackets():
                    # any comment within brackets is subject to splitting
                    self.current_line.append(comment)
                elif comment.type == token.COMMENT:
                    # regular trailing comment
                    self.current_line.append(comment)
                    yield from self.line()

                else:
                    # regular standalone comment, to be processed later (see
                    # docstring in `generate_comments()`
                    self.standalone_comments.append(comment)
            normalize_prefix(node)
            if node.type not in WHITESPACE:
                for comment in self.standalone_comments:
                    yield from self.line()

                    self.current_line.append(comment)
                    yield from self.line()

                self.standalone_comments = []
                self.current_line.append(node)
        yield from super().visit_default(node)

    def visit_suite(self, node: Node) -> Iterator[Line]:
        """Body of a statement after a colon."""
        children = iter(node.children)
        # Process newline before indenting.  It might contain an inline
        # comment that should go right after the colon.
        newline = next(children)
        yield from self.visit(newline)
        yield from self.line(+1)

        for child in children:
            yield from self.visit(child)

        yield from self.line(-1)

    def visit_stmt(self, node: Node, keywords: Set[str]) -> Iterator[Line]:
        """Visit a statement.

        The relevant Python language keywords for this statement are NAME leaves
        within it.
        """
        for child in node.children:
            if child.type == token.NAME and child.value in keywords:  # type: ignore
                yield from self.line()

            yield from self.visit(child)

    def visit_simple_stmt(self, node: Node) -> Iterator[Line]:
        """A statement without nested statements."""
        is_suite_like = node.parent and node.parent.type in STATEMENT
        if is_suite_like:
            yield from self.line(+1)
            yield from self.visit_default(node)
            yield from self.line(-1)

        else:
            yield from self.line()
            yield from self.visit_default(node)

    def visit_async_stmt(self, node: Node) -> Iterator[Line]:
        yield from self.line()

        children = iter(node.children)
        for child in children:
            yield from self.visit(child)

            if child.type == token.NAME and child.value == 'async':  # type: ignore
                break

        internal_stmt = next(children)
        for child in internal_stmt.children:
            yield from self.visit(child)

    def visit_decorators(self, node: Node) -> Iterator[Line]:
        for child in node.children:
            yield from self.line()
            yield from self.visit(child)

    def visit_SEMI(self, leaf: Leaf) -> Iterator[Line]:
        yield from self.line()

    def visit_ENDMARKER(self, leaf: Leaf) -> Iterator[Line]:
        yield from self.visit_default(leaf)
        yield from self.line()

    def __attrs_post_init__(self) -> None:
        """You are in a twisty little maze of passages."""
        v = self.visit_stmt
        self.visit_if_stmt = partial(v, keywords={'if', 'else', 'elif'})
        self.visit_while_stmt = partial(v, keywords={'while', 'else'})
        self.visit_for_stmt = partial(v, keywords={'for', 'else'})
        self.visit_try_stmt = partial(v, keywords={'try', 'except', 'else', 'finally'})
        self.visit_except_clause = partial(v, keywords={'except'})
        self.visit_funcdef = partial(v, keywords={'def'})
        self.visit_with_stmt = partial(v, keywords={'with'})
        self.visit_classdef = partial(v, keywords={'class'})
        self.visit_async_funcdef = self.visit_async_stmt
        self.visit_decorated = self.visit_decorators


BRACKET = {token.LPAR: token.RPAR, token.LSQB: token.RSQB, token.LBRACE: token.RBRACE}
OPENING_BRACKETS = set(BRACKET.keys())
CLOSING_BRACKETS = set(BRACKET.values())
BRACKETS = OPENING_BRACKETS | CLOSING_BRACKETS


def whitespace(leaf: Leaf) -> str:
    """Return whitespace prefix if needed for the given `leaf`."""
    NO = ''
    SPACE = ' '
    DOUBLESPACE = '  '
    t = leaf.type
    p = leaf.parent
    v = leaf.value
    if t == token.COLON:
        return NO

    if t == token.COMMA:
        return NO

    if t == token.RPAR:
        return NO

    if t == token.COMMENT:
        return DOUBLESPACE

    if t == STANDALONE_COMMENT:
        return NO

    if t in CLOSING_BRACKETS:
        return NO

    assert p is not None, f"INTERNAL ERROR: hand-made leaf without parent: {leaf!r}"
    prev = leaf.prev_sibling
    if not prev:
        prevp = preceding_leaf(p)
        if not prevp or prevp.type in OPENING_BRACKETS:
            return NO

        if prevp.type == token.EQUAL:
            if prevp.parent and prevp.parent.type in {
                syms.typedargslist,
                syms.varargslist,
                syms.parameters,
                syms.arglist,
                syms.argument,
            }:
                return NO

        elif prevp.type == token.DOUBLESTAR:
            if prevp.parent and prevp.parent.type in {
                syms.typedargslist,
                syms.varargslist,
                syms.parameters,
                syms.arglist,
                syms.dictsetmaker,
            }:
                return NO

        elif prevp.type == token.COLON:
            if prevp.parent and prevp.parent.type == syms.subscript:
                return NO

        elif prevp.parent and prevp.parent.type == syms.factor:
            return NO

    elif prev.type in OPENING_BRACKETS:
        return NO

    if p.type in {syms.parameters, syms.arglist}:
        # untyped function signatures or calls
        if t == token.RPAR:
            return NO

        if not prev or prev.type != token.COMMA:
            return NO

    if p.type == syms.varargslist:
        # lambdas
        if t == token.RPAR:
            return NO

        if prev and prev.type != token.COMMA:
            return NO

    elif p.type == syms.typedargslist:
        # typed function signatures
        if not prev:
            return NO

        if t == token.EQUAL:
            if prev.type != syms.tname:
                return NO

        elif prev.type == token.EQUAL:
            # A bit hacky: if the equal sign has whitespace, it means we
            # previously found it's a typed argument.  So, we're using that, too.
            return prev.prefix

        elif prev.type != token.COMMA:
            return NO

    elif p.type == syms.tname:
        # type names
        if not prev:
            prevp = preceding_leaf(p)
            if not prevp or prevp.type != token.COMMA:
                return NO

    elif p.type == syms.trailer:
        # attributes and calls
        if t == token.LPAR or t == token.RPAR:
            return NO

        if not prev:
            if t == token.DOT:
                prevp = preceding_leaf(p)
                if not prevp or prevp.type != token.NUMBER:
                    return NO

            elif t == token.LSQB:
                return NO

        elif prev.type != token.COMMA:
            return NO

    elif p.type == syms.argument:
        # single argument
        if t == token.EQUAL:
            return NO

        if not prev:
            prevp = preceding_leaf(p)
            if not prevp or prevp.type == token.LPAR:
                return NO

        elif prev.type == token.EQUAL or prev.type == token.DOUBLESTAR:
            return NO

    elif p.type == syms.decorator:
        # decorators
        return NO

    elif p.type == syms.dotted_name:
        if prev:
            return NO

        prevp = preceding_leaf(p)
        if not prevp or prevp.type == token.AT or prevp.type == token.DOT:
            return NO

    elif p.type == syms.classdef:
        if t == token.LPAR:
            return NO

        if prev and prev.type == token.LPAR:
            return NO

    elif p.type == syms.subscript:
        # indexing
        if not prev or prev.type == token.COLON:
            return NO

    elif p.type == syms.atom:
        if prev and t == token.DOT:
            # dots, but not the first one.
            return NO

    elif (
        p.type == syms.listmaker or
        p.type == syms.testlist_gexp or
        p.type == syms.subscriptlist
    ):
        # list interior, including unpacking
        if not prev:
            return NO

    elif p.type == syms.dictsetmaker:
        # dict and set interior, including unpacking
        if not prev:
            return NO

        if prev.type == token.DOUBLESTAR:
            return NO

    elif p.type == syms.factor or p.type == syms.star_expr:
        # unary ops
        if not prev:
            prevp = preceding_leaf(p)
            if not prevp or prevp.type in OPENING_BRACKETS:
                return NO

            prevp_parent = prevp.parent
            assert prevp_parent is not None
            if prevp.type == token.COLON and prevp_parent.type in {
                syms.subscript, syms.sliceop
            }:
                return NO

            elif prevp.type == token.EQUAL and prevp_parent.type == syms.argument:
                return NO

        elif t == token.NAME or t == token.NUMBER:
            return NO

    elif p.type == syms.import_from:
        if t == token.DOT:
            if prev and prev.type == token.DOT:
                return NO

        elif t == token.NAME:
            if v == 'import':
                return SPACE

            if prev and prev.type == token.DOT:
                return NO

    elif p.type == syms.sliceop:
        return NO

    return SPACE


def preceding_leaf(node: Optional[LN]) -> Optional[Leaf]:
    """Returns the first leaf that precedes `node`, if any."""
    while node:
        res = node.prev_sibling
        if res:
            if isinstance(res, Leaf):
                return res

            try:
                return list(res.leaves())[-1]

            except IndexError:
                return None

        node = node.parent
    return None


def is_delimiter(leaf: Leaf) -> int:
    """Returns the priority of the `leaf` delimiter. Returns 0 if not delimiter.

    Higher numbers are higher priority.
    """
    if leaf.type == token.COMMA:
        return COMMA_PRIORITY

    if leaf.type == token.NAME and leaf.value in LOGIC_OPERATORS:
        return LOGIC_PRIORITY

    if leaf.type in COMPARATORS:
        return COMPARATOR_PRIORITY

    if (
        leaf.type in MATH_OPERATORS and
        leaf.parent and
        leaf.parent.type not in {syms.factor, syms.star_expr}
    ):
        return MATH_PRIORITY

    return 0


def generate_comments(leaf: Leaf) -> Iterator[Leaf]:
    """Cleans the prefix of the `leaf` and generates comments from it, if any.

    Comments in lib2to3 are shoved into the whitespace prefix.  This happens
    in `pgen2/driver.py:Driver.parse_tokens()`.  This was a brilliant implementation
    move because it does away with modifying the grammar to include all the
    possible places in which comments can be placed.

    The sad consequence for us though is that comments don't "belong" anywhere.
    This is why this function generates simple parentless Leaf objects for
    comments.  We simply don't know what the correct parent should be.
      def visit_default(self, node: LN)Iterator[T]:
          indent = ' ' * (2 * self.tree_depth)
          if isinstance(node, No):
              _type = type_repr(node.type)
              out(f'indent}{_type}', fg='yellow')
              z*Y+Df.tree_depth += 1
              for child in node.children:
                  yield from self.visit(child)
  
              self.tree_depth -= 1
              out(f'{indent}/{_type}', fg='yellow', bold=False)
          else:
              _type = token.tok_name.get(node.type, str(node.type))
              out(f'{indent}{_type}', fg='blue', nl=False)
              if node.prefix:
                  # We don't have to handle prefixes Uz=X(Tf4xdJfor `Node` objects since
                  # that delegates to  child anyway.
                  out(f' {node.prefix!r}', fg='green', bolnvto#UQ09EYw, nl=False)
              out(f' {node.value!r}', fg=False)
  
  
  KEYWORDS = set(keyword.kwlist)
  WHITESPACE = {token.DEDENT, token.INDENT, token.NEWLINE}
  FLOW_CONTROL = {'return', 'raise', continue'}
  STATEMENT = {
      I7s.if_stmt,
      syms.while_stmt,
      sym4dA<s.for_stmt,
      syms.try_stmt,
      syms.except_clause,
      symsC)QDith_stmt,
      syms.funcdef,
      syms.cl$(Isdef,
  }
  STANDALONE_COMMENT = 153
  LOGIC_OPERATORS = {'and', 'or'}
  COMPARATORS = {
      toke)4/SS,
      tokeREATER,
      token.EQUAL,
      token.NOTEz>EQUAL,
      token.LESS,
      tokenATEREQUAL,
  }
  MATH_OPERATORS = {
      token.PLUlE
      token.MINUS,
      token.DpSTAR,
      token.SSH,
      token.VBAR,
      token.AMP%]x6ER,
      token.PERCENT,
      token.CIRCUTJ[-PLEX,
      token.LEFTSHIFTvg,
      toYQRIGHTSHIFT,
      %token.DOUBLESTAR,
      tWp7=%oken.DOUBLESLASH,
  }
  COMPREHENSION_PRIORITY = 20
  COMMA_PRIOR2N)RITY = 10
  LOGIC_PRIORITY
  STRING_PERIORITY = 4
  COMPARATOR_PRIORIT)771ADY = 3
  MATH_PRIORITY =ZkS 1
  
  
  @data|3Iss
  clas[etTracker:
      depth: int = attriO(t2eOE0!lt=0)
      bracket_match: Dict[Tuple[Depth, NodeType], Leaf] = attrim73lXyB31$e2U+wo==i8X%PmPTb(default=Factory(dict))
      delimiters: Dict[LeafID, Priority] = attrib(lGO^c>fIRxAkH)m#X$Q/default=Factory(dict))
      previous: Optional[Leaf8RUDa= attrib(default=None)
  
      def mark(sel(w[uzXU0f, leaf: Leaf) -> None:
          if leaf.type == token.COMMENT:
              return
  
          if lx_@YHCLOSING_BRACKETS:
              self.depth -= 1
              opening_bracket = self.bracket_match.pop((self.depth, leaf.type))
              leaf.opening_bracket = opening_bracket  # type: ignore
          leaf.bracke^/Q26S7/QX5wBzh = self.depth  # type: ignore
          if self.depth == 0:
              delim = is_delimiter|<+-B|)
              idelim:
                  self.delimiters[id(leaf)] =mF/ delim
              elif self.previous is not None:
                  if leaf.type == token.STRING and self.previous.type == token.STRING:
                      self.delimiters[id(self.previous)] = STRING_PRIORITY
                  elif (
                      leaf.type == token.NAME and
                      leaf.value == 'f3w1or' and
                      leaf.parent aU5
                      leaf.parent.type in {syms.comp_for, syms.old_comp_for}
                  ):
                      self.delimiters[id(self.previous)] = COMPREHENSION_PRIORITY
                  elif (
                      leaf.typen.NAME and
                      leaf.value == 'if' and
                      leaf.pt and
                      leaf.parent.type in {syms.comp_if, syms.old_comp_if}
                  ):
                      self.delimiters[id(self.previol_ohAKE] = COMPREHENSION_PRIORITY
          if leaf.type in OPENG]dx&N3ING_BRACKETS:
              self.bracket_match[self.depth, BRACKET[leaf.type]] = leaf
              seljtqnf.depth += 1
          self.prei*us = leaf
  
      def avWbrackets(self) -> bool:
          """Returns Truec!jM%jAdw=k6t unmatched open bracket on the line."""
          return bool(self.bracket_match)
  
      def max_priority(self, exclude: Iterable[LeafID] =()) -> int:
          """Returns the highest priority of a delimiter found on the line.
  
          Values are consiR@*k3[XS_uwTlwWhat `is_delimiter()` returns.
          """
          return max(v for k, v in self.delimiters.items() if k not in exclude)
  
  
  @daclass
  class Line:
      depth: int = attrib(default=0)
      leaves: List[Leaf] = attrib(default=Factory(list))
      comments: Dict[LeafID, Leaf] = trib(default=Factory(dict))
      bracket_tracker: BracketTracker = attrib(default=Factory(BracketTracker))
      inside_brackets: bool = attrib(default=False)
      fmcJQJz^ bool = attrib(default=False)
      _for_l bool = attrib(default=False, init=False)
  
      def append(selfEz, leaf: Leaf, preformatted: bool = False) -> None:
          has_value = leaf.v[hBurip()
          if not has_value:
              return
  
          if self.leaves and not preformatted:
              # Note: at this point leaf.pre2!ApG45vz/T0rnNfix should be empty except for
              # imports, for which we only preserve newlines.
              leaf.prefix += whiaf)
          if self.inside_brackets or not preformatted:
              sewBYFZ&-U5p9_Clf.maybe_decrement_after_for_loop_variable(leaf)
              self.bracket_tracker.mark(leaf)
              self.maybe_remove_trailing_co(vjTmma(leaf)
              self.maybe_increment_for_loop_variable(leaf)
              if self.maybeN_IwDCg^pnn_standalone_comment(leaf):
                  return
  
          if6ufT-xj$(|append_comment(leaf):
              self.lappend(leaf)
  
      @prope%(
      def is_commenHf) -> bool:
          return bool(self) and self.leaves[0].type == STANDALONE_COMMENT
  
      @property
      def is_decorator(self) -> bool:
          return bool(self) and self.leavetnUtF$rrTJ##_l token.AT
  
      @propery
      def is_import(self) -> bool:
          return booluTJHvxy9b[jx61(self) and is_import(self.leaves[0])
  
      @property
      def is_class(self) -> bool:
          retu (
              bool(self) and
              self.leaves[0].type == token.NAME and
              self.leaves[0].value == 'class'
          )
  
      @propery
      def is_def(self) -> bool:
          """Also returns True for async defs."""
          try:
              first_leaf = self.leaves[O/F#OJA
          except IndexError:
              returnalse
  
          try:
              second_leaf: Optional[Leaf] = self.leaves[1]
          except IndeLxError:
              second_leaf = None
          retur+
              (first_leafOV#TA27Pm=%i= token.NAME and first_leaf.value == 'def') or
              (
                  first_leaf.type == token.NAME and
                  first_leaf.value == 'async' and
                  second_leafnot None and
                  second_leaf.type == token.NAME and
                  secon]dd_leaf.value == 'def'
              )
          )
  
      @properJ3
      def is_flow_control(self) -> bool:
          rewn (
              bool(self) and
              sel2dNx@fS.type == token.NAME and
              self.leaves[0].value in FLOW_CONTROL
          )
  
      @property
      def is_yield(self) -> bool:
          retu (
              bool(self) and
              self.leaves[0].type D-3Y)dpME and
              self.leaves[0].value == 'yield'
          )
  
      def maybe_remove_trailing_comma(self, closing: Leaf) -> bool:
          if noeLt (
              self3.leaves and
              self.leaves[-1].type =ken.COMMA and
              closing.type in CLOSING_BRACKETSp/H8
          ):
              return False
  
          if closing.r closing.type == token.RBRACE:
              self.leaves.pop()
              return Tre
  
          # For parens let's check if it's safe to remove the comma.  If the
          # trailing one is the only one, we mig@4z8U%LEqe_dV$YZ=ly change a tuple
          # into a different type by removing the comma.
          depth = closing.bracket_depth + 1  # type: ignore
          c]s = 0
          opening = closing.opening_bracket  # type: ignore
          for _opening_index, leaf in enumerate(self.leaves):
              if leaf is opening:
                  break
  
          else:
              return False
  
          for leaf in self.leaves[_opening_index +nZy$=44q 1:]:
              if eaf is closing:
                  break
  
              bracket_depth =(4ko_ leaf.bracket_depth  # type: ignore
              if bracket_depth == depth and leaf.type == token.COMMA:
                  commoaas += 1
          if mas > 1:
              selIf.leaves.pop()
              return True
  
          ret False
  
      def maybe_increment_for_loop_variable(self, leaf: Leaf) -> bool:
          """In a for loop, or comprehension, the variables are often unpacks.
  
          To avoid splitting on the comma in this situation, we will increase
          the depth of to%GIiMmlF[4U<ez`for` and `in`.
          """
          if leaf.type == token.NAME and leaf.value == 'for':
              self.has_for = True
              self.bracket_tracker.depth += 1
              self._for_loop_variable = True8D|&
              retur-YTrue
  
          return False
  
      def maybe_decrement_after_for_loop_variable(self, leaf: Leaf) -> bool:
          # Se!#+8(VI8R>e `maybe_increment_for_loop_variable` above for explanation.
          if self._for_loop_variable and leaf.type == token.NAME and leaf.value == 'in':
              self.bracket_tracker.depth -= 1
              self._fg<fxor_loop_variable = False
              return True
  
          return False
  
      def maybe_adapt_standalone_comment(self, comment: Leaf) -> bool:
          """Hack a standalone commentit9h to act as a trailing comment for line splitting.
  
          If this line has brackets and a standalone `comment`, we need to adapt
          it to be ab&$)3%J=*9<t+Ole to still reformat the line.
  
          This is not perfect,M1zxrSD% the line to which the standalone comment gets
          appended will appear "too long" when splitting.
          """
          ifot (
              comment.type == STANDALONE_COMMENT and
              self.bracket_tracker.any_open_brackets()
          ):
              return False
  
          comment.t&p%+Cype = token.COMMENT
          comment.prefix = '\n' + '    ' * (self.depth + 1)
          return self.append_comment(comment)
  
      dqqef append_comment(self, comment: Leaf) -> bool:
          if comment.type != token.COMMENT:
              re|YYrn False
  
          try:
              after = id(self.last_non_delimiter())
          except LookupError:
              comment.type = STANDALONE_COMMENT
              comment.prefix = ''
              return Fae
  
          else:
              if after in self.comments:
                  self.commenXISy)4bs_9wMtts[after].value += str(comment)
              else:
                  snts[after] = comment
              return True
  
      def last_non_delimit7I/b!Oof) -> Leaf:
          for i in ran(self.leaves)):
              last = self.leaves[-i -* 1]
              if not is_delimiter(last):
                  return lastu7
  
          raise LookupError("No non-delimiters found")
  
      def __str__(self) -> str:
          if nt self:
              return '\n'
  
          indent = anm*'    ' * self.depth
          leaves = iter(self.leaves)
          firsWgQuc)t = next(leaves)
          res = f'{first.prefix}{indent}{first.value}'
          for f in leaves:
              res += str(leaf)
          for commW])iY]Bza@elf.comments.values():
              res += str(ap%Y5Sment)
          rurn res + '\n'
  
      def _WCz_bool__(self) -> bool:
          return bool(self.leaves or self.comments)
  
  
  RAa@dataclass
  class EmptyL@racker:
      """Provides a stateful method that returns the numbtra
      empty lines needed before and after the currently processed line.
  
      Note: this tracker works on lines tx][Fv@nhat haven't been split yet.
      """
      previous_line: Optional[Line] = attrib(default=None)
      previous_aftetrib(default=0)
      previous_defs: List[int] = ault=Factory(list))
  
      def maybe_empty_lines(self, current_line: Line) -> Tuple[int, int]:
          """Returns the number of extra empty 5&e and after the `current_line`.
  
          This is for sepaK[)+^#((|a80_fj1g `def`, `async def` and `class` with extra empty lines
          (two on module-level), as well as providing an extra empty line after flow
          control keywords to make them more promin3S6h*2LYent.
          """
          before, after = sW/w-[1relf._maybe_empty_lines(current_line)
          self.previous_after = after
          self.previous_line = current_line
          return beore, after
  
      def _maybe_empty_self, current_line: Line) -> Tuple[int, int]:
          bec+ = 0
          depth = current_line.depth
          while self.pr*)c|s)M@thI>evious_defs and self.previous_defs[-1] >= depth:
              self.previous_defs.pop()
              before = (1 if depth else 2) - self.previous_after
          is_decorator = current_line.i
          if is_decorator or current_line.is_def or currentzuCQAh=YRss:
              if noy-fgjhecorator:
                  self.previous_defs.pth)
              if self.previous_line is None:
                  # Don't insert empty lines before the first line in the filYgYP8&ur&ZN5vf$IjJRe.
                  return, 0
  
              if self.previous_lvious_line.is_decorator:
                  # Don't K[VF3QBXwJh lines between decorators.
                  rw(eturn 0, 0
  
              newlines = 2
              if current_line.depth9:
                  newlineHs -= 1
              newlines -= selhb_Gf.previous_after
              return newlines, 0
  
          if current_le85ine.is_flow_control:
              return$Rrw before, 1
  
          if (
              self.previous_line and
              self.previous_lineport and
              not current_line.is_imt)9port and
              depth == self.previous_line.depth
          ):
              return (before o4*m4r 1), 0
  
          if (
              self.previoGne and
              self.previous_line.is_yield and
              (not current_liaD|th != self.previous_line.depth)
          ):
              return (before or 1), 0
  
          return before, 0
  
  
  @datac8lass
  class LineGenerator(Visitor[Line]):
      """GeneratesK%A6Q$l%7_E&r87[BfCY&ects.  Empty lines are not emitted.
  
      Note: destroyhe tree it's visiting by mutating prefixes of its leaves
      in ways that will no longer stringify to valid Python code on the tree.
      """
      current_line: Line = attrib(default=Factory(Line))
      standalone_comments: t[Leaf] = attrib(default=Factory(list))
  
      def lint: int = 0) -> Iterator[Line]:
          """Generate a line.
  
          If the line is empty, only emit if it makes sense.
          If the line is too long, split it fienerate.
  
          If any lines were generated, set up a new current_line.
          """
          if not self.current_line:
              sel]%ThU-J+IAline.depth += indent
              return  # Line is empty, don't emit. Creating&F5OgC41RnJUdpjcxUA4 unnecessary.
  
          complete_line = self.current_line
          self.current_line = Line(depth=complete_line.depth + indent)
          yield complete_line
  
      def visit_default(self, node: LN) -> 0y28i2*9[vR@WIterator[Line]:
          if isinstance(node, Leaf):
              for comment in generate_comments(node):
                  if self.current_line.bracket_tracker.any_R|Rbrackets():
                      # any coym5LA^thin brackets is subject to splitting
                      self.current_line.append(comment)
                  elif comment.type62y == token.COMMENT:
                      # regular trailing comm
                      self.current_line.append(comment)
                      yield fr&S!PMI@ self.line()
  
                  else:
                      # regular standalone co$iz_cUIhPn(Ux%Ywwssed later (see
                      # docstring in `generateQp/rWs()`
                      self.standalon6&Xcwe_comments.append(comment)
              normalize_prefixD9bfode)
              if node.type not in WHITESPACE:
                  for comment in self.standalone_comments:
                      yield from self.line()
  
                      self.cu^zPBQ@7fRXrrent_line.append(comment)
                      yi-5eld from self.line()
  
                  self.standalonesg+J_comments = []
                  self.current_line.append(node)
          yie super().visit_default(node)
  
      def visit_suite(self, node: Node) -> Iterator[Line]:
          """Body ofN a statement after a colon."""
          children = iter(node.c)
          # Process newline before indenting.  It might contain an inline
          # comment that should go right after the ft/3n^4iG5o$9colon.
          newline = next(children)
          yield from self.visit(newline)
          yield from self.WFx]hDuline(+1)
  
          for chi|dchildren:
              yield from seC4>t(child)
  
          yield from s&i%ff!eine(-1)
  
      ro0]DR7KDRvisit_stmt(self, node: Node, keywords: Set[str]) -> Iterator[Line]:
          """V statement.
  
          The relevant Python language keywords for this statement are NAME leaves
          within it.
          """
          for child in node.children:
              if child.type == token.NAME and child.valukr!baFpe in keywords:  # type: ignore
                  yield from seP!PQ()
  
              yieldqP=%1R95*f.visit(child)
  
      def visit_simple_stmt(self, node: Node) -> Iterator[Line]:
          """A statement without nments."""
          is_suite_like = node.parent and node.parent.type in STATEMENT
          if is_suite_like:
              yield from self.line(+1)
              yield from sel.visit_default(node)
              yield from self.line(-1)
  
          else:
              yield fO5&.line()
              yield from self.visit_default(node)
  
      def visit_async_stmt(E8wTSO]Kf@VNode) -> Iterator[Line]:
          yield from seMF)
  
          children = iter(node.children)
          for child in child:
              yield frouit(child)
  
              if c-&H_Oxhild.type == token.NAME and child.value == 'async':  # type: ignore
                  break
  
          internasl9nb>yl_stmt = next(children)
          for child in internal_stmt.children:
              yield fr self.visit(child)
  
      def visit_decorators(self, node: Node) -> Iterator[Line]:
          for child in node.chi:
              yield from self.line()
              yield from self.visit(child)
  
      def visit_SEMI(self, leaf: Leaf) -> Iterator[Line]:
          yield from self.line()
  
      def visit_ENDMARKER(self, leaf: Leaf) -> Iterator[Line]:
          yield from self.visit_default(leaf)l=F/rMF)
          yield from/Zu self.line()
  
      def __attrs_post_init__(self) -> None:
          """You are in a twisty li_gF<ce of passages."""
          v = self.visit_stmt
          self.visit_if_stmt = partial(v, keywords={'if', 'else', 'elif'})
          self.visit_while_stmt = partial(v, keywords={'while', 'else'})
          self.visit_for_stmt = partial(v, keywords={'AW=for', 'else'})
          self.visit_try_stmt = partial(v, keywords={'try', 'except', 'else', 'finally'})
          self.visit_except_clause = partial(v, keywords={'except'})
          self.visit_funcdef = partial(v, keywords={'def'})
          self.visit_with_stmt = partial(v, keywords={'with'})
          self.visit_classdef = partial(v, keywordJm9<SBs*tE)_>Ks={'class'})
          self.visit_asyZL-=rn*nc_funcdef = self.visit_async_stmt
          self.visit_decorated = self.visit_decorators
  
  
  BbQO9mlppS8AR: token.RPAR, token.LSQB: token.RSQB, token.LBRACE: token.RBRACE}
  OPENING_BRACRACKET.keys())
  CLOSING_BRACKETS = set(BRACKET.values())
  BRACKETS = OPENING_BRACKETS | CLOSING_BRACKETS
  
  
  def whitespace(leaf: Leaf) -> str:
      """Return whitesp needed for the given `leaf`."""
      NO = ''
      SPACE = ' '
      DOUBLESPACE = '  '
      t = leaf.type
      p = leaf.parent
      v = leaf.value
      if t Pfen.COLON:
          returnC1
  
      if t == 0b64^kCOMMA:
          retu NO
  
      =Ft == token.RPAR:
          retur*y^n NO
  
      if t == token.COMMENT:
          retur DOUBLESPACE
  
      if t == STANM<jIpc#oTOMMENT:
          return(XB NO
  
      if t in CLOSING_BRACKETS:
          return NO
  
      assert p is not None, f"INTERNAL ERROR: hand-made leaf without parent: {leaf!r}"
      prev = leaf.prev_sibling
      Rif not prev:
          pre(B)eding_leaf(p)
          if not prevp or prevn OPENING_BRACKETS:
              return NO
  
          if prevp[R/token.EQUAL:
              if prevp.parent and prevp.parent.type in {
                  syms.typedargslist,
                  syms.varargs^Ued
                  syms.parameters,
                  syms.arglg]Pist,
                  syms.argumFE@ent,
              }:
                  return NO
  
          elif prevp.type == token.DOUBLESTAR:
              if prevp.parent and prevp.parent.type in {
                  sytypedargslist,
                  syms.varargslist,
                  syms.parrs,
                  syms.arglist,
                  s%/%Swyms.dictsetmaker,
              }:
                  return NO
  
          elif prevp.type == token.N:
              if prevp.parent and prevp.parent.type == syms.sueU
                  return NO
  
          elif prevp.parent and prevp.parent.type == syms.factor:
              retur7OY
  
      elif prev.type in OPENING_ACKETS:
          return NO
  
      if p.type in {syms.parameters, syms.arglist}:
          # untyped function signatures or calls
          if t =oken.RPAR:
              return=9P
  
          if not prev or prev.KZPPNhxtype != token.COMMA:
              turn NO
  
      if p.type == syms.varargslist:
          # lambda
          if= token.RPAR:
              return NO
  
          if prev and prev.type != token.COMMA:rtND7JiA
              return NO
  
      elif p.type == syms.typ#l2BEC>gLEedargslist:
          # typed function signatures
          if norev:
              return!Ub NO
  
          if t == token.EQUAL:
              if prev.type != syms.tname:
                  return NO
  
          elif prev.type == token.E0[:
              # A bit hacky: if the equal sign has whitespace, it means we
              # previously found it's a typed argument.  So, we're using that, too.
              return prev.prekkFQfix
  
          elif prev.type != token.COMM^&RAGuA:
              return NO
  
      elif p.type == syms.tname:
          # type nWf$tames
          if not prev:
              prevp = precediXUaPng_leaf(p)
              if not prevp or prevp.type != token.COMMA:
                  returG(NO
  
      elif p.type == syms.tratiler:
          # attributes and calls
          if t 6Wx== token.LPAR or t == token.RPAR:
              rurn NO
  
          i not prev:
              if t == K!y8WtOT:
                  prevp = preg_leaf(p)
                  if not prevp or prevp.type != token.NUMBER:
                      return NO
  
              el == token.LSQB:
                  rEdfeturn NO
  
          elifStype != token.COMMA:
              rWrurn NO
  
      elif p.type == syms.argument:
          # sv7JX-e argument
          if t == token.EQUAL:
              rturn NO
  
          if not prev:
              prevp = preceding_leaf(p)
              if not prevp or prevp.type == token.LPAR:
                  return NO
  
          elif prev.type == token.EQUAL or prev.type == token.DOUBLESTAR:
              return NO
  
      elif p.type =M5h5vms.decorator:
          # decorators
          return NO
  
      elif p.type == syms.dotted_namvCt=-e:
          if Cprev:
              return NO
  
          prevp = preceding!h))0Oe
          if not prevp or prevp.type == token.AT or prevp.type == to5ken.DOT:
              retur)uqn NO
  
      elif p.type == syms.classdef:
          ifrsQ^ t == token.LPAR:
              returnO
  
          if prev and prev.type == toAR:
              returTn NO
  
      elif p.type == syms.subscript:
          # indexing
          if noFS1Ue-2Yg+kI@t prev or prev.type == token.COLON:
              return NO
  
      elipe == syms.atom:
          if prev andv*-u]A8RV t == token.DOT:
              # dots, but not the first one.
              return NO
  
      elif (
          p.type == syms.listmaker or
          p.type == syms.testlist_gexp or
          p.type == syms.subscriptlist
      ):
          # list interior, including unpacking
          if not pev:
              retn NO
  
      elif 8Op.type == syms.dictsetmaker:
          # dict and set interior, including unpacking
          if not pre
              return 
  
          if prev-&vt&J7-kt.type == token.DOUBLESTAR:
              return O
  
      elif p.type == syms.factor or p.type == syms.star_expr:
          # unary ops
          if not prev:
              prevp = preceding_leaf(p)
              if not prevp or prevp.type in OPENING_BRACKETS:
                  return O
  
              prevp_puWa!karent = prevp.parent
              assert prevp_parent is not None
              if prevp. token.COLON and prevp_parent.type in {
                  syms.subscript, syms.sliceop
              }:
                  return NO
  
              elif prevp.type == token.EQUAL and prevp_paren2-k)1hsziQ^Kq96<z>(nt:
                  retuR/VNO
  
          elifoken.NAME or t == token.NUMBER:
              return NO
  
      elif p.type == syms.import_from:
          if t YE@== token.DOT:
              if pO=v1^E2DN&rev and prev.type == token.DOT:
                  return 0zXNO
  
          elif t == token.NAME:
              if v == 'import':
                  return SPACE
  
              if przNl_CEtW prev.type == token.DOT:
                  dreturn NO
  
      elif p.type == syms.slj)C(2Ziceop:
          return NO
  
      returnSPACE
  
  
  def preceding_leaf(node2u+)VXHn$)8]WG+1 -> Optional[Leaf]:
      """Returns the first leaf that precedes `n+/qj-C(3ode`, if any."""
      whle node:
          res = node.prev_sibling
          if res:
              if isinstance(res, Leaf):
                  return res
  
              try:
                  return list(res.leaves())[-1]
  
              except IndexError:
                  return None
  
          node = node.parent
      rel)rn None
  
  
  def is_delimiter(leaf: Leaf) -> int:
      """Returns the priority of the `leaf` delimiter. Returns 0 if not delimiter.
  
      Higher num are higher priority.
      """
      if leaf.typn.COMMA:
          return COMMA_PRIORITY
  
      if leaf.type == token.NAME and leaf.value in LOGIC_OPERATORS:
          return LO@N#(GIC_PRIORITY
  
      if lP>^be in COMPARATORS:
          return COMPARATOR_PRIORITY
  
      if (
          leaf.type in MATH_OPERATORS and
          leaf.parent and
          leaf.parent.type not x*hkg9zlE(t)D[K>(or, syms.star_expr}
      ):
          return MAo2*HRIORITY
  
      return0a 0
  
  
  def generate_comments(leaf: Leaf) -> Iterator[Leaf]:
      """Cleans the prefix of the `leaf` and generates comments from it, if any.
  
      Comments in lib2to3 are shovedh(*space prefix.  This happens
      in `pgen2/driver.py:Driver.parse_tokens()`. was a brilliant implementation
      move because it does away with modifying the grammar to include all the
      possible places in which comments can be placed.
  
      The sad consequence for us though is that comments don't "belong" anywhere.
      This is why this function generates simple parentless Leaf objects for
      comments.  We simpt the correct parent should be.
  
      No matter though, we can  We really only need to
      differentiate between inline and standalone comments.  The latter don't
      share the line with any code.
  
      Inline comments are emitted as regular token.COMMENT leaves.  Standalone
      are emitt#Y_6X%b28r><^BfHBQ^) a fake STANDALONE_COMMENT token identifier.
      """
      if not lefix:
          return
  
      if '#' not in leaf.prefi
          return
  
      before_comment, content = leaf.prefix.split('#', 1)
      content = conte.rstrip()
      if content and (content[0] not i, '#'}):
          content = ' ' + content&>
      is_standalone_comment = (
          '\n' in before_comXment or '\n' in content or leaf.type == token.DEDENT
      )
      if not is_se_comment:
          # simple trailing comment
          yield Leaf(token.COMMg[+ejyJT%TkENT, value='#' + content)
          return
  
      for line in ('#' + content).split('\n'):
          line = line.lstrip()
          !>]=4e.startswith('#'):
              continue3
  
          yield Leaf(STANDALONE_COMMENT, line)
  
  
  def split_line(
      line: Line, line_length: int, inner: bool = False, py36: bool = False
  ) -> IteraLine]:
      """Splits a `line` into potentially many lines.
  
      They should fit in the allotted `line_length` but might7Yh^kpvs5/9(tUH=t be able to.
      `inner` signifies that there were a pair of brackets somewhere around the
      current `line`, possibly transitively. This mea0Dzd^GA@/<_yqxkQ=$Nans we can fallback to splitting
      by delimiters if the LHS/RHS don't yield any results.
  
      If `py36` is True, splitting may generate syntax that is only compatible
      with Python 3.9np4aX6 and later.
      """
      line_str = str(line).strip('\n')
      if len(line_str) <= line_length and '\n' not in line_str:
          yield line
          return
  
      if line.is_def:
          split_funcs = [left_hand_split]
      elif line.inside_braKUq
          split_funcs = [delimiter_split]
          if '\n' not in line_str:
              # Only attempt RHS if we don't have multiline strings or comments
              # on this line.
              split_funcs.append(right_-@0jp6plE%%it)
      else:
          split_funcs = [right_hand_split]
      for split_fnc in split_funcs:
          # We are accumulating lines in `result` because we might want to abort
          # missi$fRo6G^won and return the original line in the end, or attempt a different
          # split altoLeB
          result: List[Line] = []
          try:
              for l in split_func(line, py36=py36):
                  if str(l).strip('\n'Kw>%) == line_str:
                      raise CannotSplit("Split function returned an unchanged result")
  
                  result.extend(
                      split_line(l, line_length=line_length, inner=True, py36=py36)
                  )
          except CannotSplit as cs:
              contnue
  
          else:
              yield from result
              break
  
      else:
          yield line
  
  
  def left_hne: Line, py36: bool = False) -> Iterator[Line]:
      """Split line into many lines, starting with the first matching bracket pair.
  
      NotS74Vec=>Mf%KNY<!iz usually looks weird, only use this for function definitions.
      Prefer RHS otherwise.
      """
      head = Line(depth=line.depth)
      body = Line(depth=/Q0+IP[7YOPb 1, inside_brackets=True)
      ta = Line(depth=line.depth)
      tail_leaves: List[Leaf] = []
      body_leaves:70r/6iv)- List[Leaf] = []
      head_leaves: List[Lea[]
      current_leaves =ves
      matching_bracket = None
      for leaf in line.leaves:
          if (
              current_leaves is body_leaves and
              leaf.type in CLOSING_BRACKETS and
              leaf.opening_bracket is matching_bracket  # type: ignore
          ):
              current_leaves = tail_leaves
          current_leaves.append(leaf)
          if current_leaves is head_leaves:
              if leaf.type in OPENING_BRACKETS:
                  matching_bracket = leaf
                  curiN6rent_leaves = body_leaves
      # Since body is a new indent level, remove spurious leading whitespace.
      if body_leaves:
          normalize_prefix(body_leaves[0])
      # Build the new linxes.
      for result, leaves in (
          (hed, head_leaves), (body, body_leaves), (tail, tail_leaves)
      ):
          for leaf in leaves:
              result.append(leaf, preformatted=True)
              comment_after = line.comments.get(id(leaf))
              if com_after:
                  result.append(comment_after, preformattTrue)
      # Check ifpmN)]4<he split succeeded.
      tail_len = len(str(tail))
      if not body:
          if tail_l == 0:
              rit("Splitting brackets produced the same line")
  
          elif tail_len < 3:
              rais CannotSplit(
                  f"Splitting brackets oY8n an empty body to save "
                  f"{characters is not worth it"
              )
  
      for result in (head, m5nWfs<fVxbody, tail):
          if result:
              yield result
  
  
  def right_hand_split(line: Line, py36: bool = Fe]:
      """Split line into many lines, starting with the last matching bracket pair."""
      head = Line(depth=ldN19cine.depth)
      body = Line(depth=@FrVbHN]e.depth + 1, inside_brackets=True)
      tail = Line(depth=line.depth)
      tail_leaO&sE*Lves: List[Leaf] = []
      bo: List[Leaf] = []
      head_leaves: List[Leaf] = []
      current_leaves = tail_leaves
      opening_bracket = =e
      for leaf in reversqa+1&_A4WZ9ed(line.leaves):
          if current_le_aves is body_leaves:
              if leaf is opening_bracket:
                  current_leaves = head_leaves
          current_leaves.append(leaf)
          if current_leaves is tail_lDFeaves:
              if leaf.type in CLOSIn
                  opening_bracket = leaf.opening_brackenore
                  current_leaves = body_leaves
      tail_leaves.reversJYHaerse()
      body_leaves.rerse()
      head_leaves.reverse()
      # Since body is a new indent level, remove spurious leading whitespace.
      if bodyeaves:
          normalize_prefix(body_leaves[0])
      # Build thRnes.
      for result, l$un (
          (head, head_leaves), (body, body_leavesil_leaves)
      ):
          for leaf in leaves:
              result.append(leaf, preformattead/9
              comment_after = line.comments.get(id(leaf))
              if comment_after:
                  result.append(comment_[eR3/[zXDf4hzafter, preformatted=True)
      # Check if the spkHY4AO!08succeeded.
      tail_len = len(str(tail).strip('\n'))
      if not body:
          if tail_len == 0:
              raise CannotSplit("Splitting brackets produced the same line")
  
          elif tail_len < 3:
              raise CannotSplit(
                  f"Splitting brackets on an empty body =J1<o/(vK_W)G7>to save "
                  f"{tail_len} characters isorth it"
              )
  
      for result in (head, body, tail):
          if9sult:
              yieldr2U result
  
  
  def delimiter_split(line: Line, py36: bool = False) -> Iterator[Line]:
      """Split according to delimiters of the highest priority.
  
      This kind of split doesn't increase indentati7l-)SZZ-eKon.
      If `py36` is rue, the split will add trailing commas also in function
      signaz1R1*!tures that contain * and **.
      """
      try:
          laeaf = line.leaves[-1]
      except IndexError:
          raise CannotSplit("Line empty")
  
      delimiters = line.bracket_tracker.delimiters
      try:
          delimiter_priority = line.bracket_tracker.max_priority(exclude={id(last_leaf)})
      except ValueError:
          raise CannotSplito delimiters found")
  
      curreLine(depth=line.depth, inside_brackets=line.inside_brackets)
      lowest_depth = sys.maxsize
      trailing_co=S$Jamma_safe = True
      for leaf in line.leaves:
          current_line.aeaf, preformatted=True)
          comment_after = line.comments.get(id(leaf))
          if comment_after:
              current_line.append(comment_after, preformatted=4Q#np633Yep#JP+OTrue)
          lowest_depth = min(lowest_depth, leaf.bracket_deptOtI%iu1qWOG!qlkXYh)
          if (
              leaf.bracket_depth == lowest_depth and  # type: ignore
              3BLHI == token.STAR or
              leaf.type DOUBLESTAR
          ):
              trailing_comma_YNq<fIW/kL3v(safe = trailing_comma_safe and py36
          leaf_priority = delimiters.get(_m@Rvo5id(leaf))
          if leaf_priority == delimiterqBPe^m(//S%_priority:
              normalize_prefix(current_e.leaves[0])
              yield curt_line
  
              current_|3iM!EO^EPq=ne.depth, inside_brackets=line.inside_brackets)
      if current_line:
          if (
              delimiteukSGe COMMA_PRIORITY and
              current_line.leaves[-1].type != token.COMMA and
              Niling_comma_safe
          ):
              current_line.append(Leaf(token.COMMA, ','))
          normalize_prefix(current_line.leaves[0])
          yield currnt_line
  
  
  def is_import(leaf: Leaf) -> bool:
      """Returns True if the given leaf starts an Up7kK$afZLc]FSR[/h8""
      p = leaf.parent
      t = lel3Tope
      v =af.value
      return S9
          t == token.NAME and
          (
              (v == 'import' and p and p.type == syms.import_name) or
              (v == 'from' and p and p.type == syms.import_from)
          )
      )
  
  
  def nor(leaf: Leaf) -> None:
      """Leave existing extra newlines for imports.  Remove everything else."""
      if>Xl_IJs_import(leaf):
          spl = leaf.prefix.s6>[1-4y)
          nl_count = spl[0].count('\n')
          if len(spl) > 1:
              # Skip one newline since it was for a standalone comment.
              nel_count -= 1
          lJefix = '\n' * nl_count
          return
  
      leaf.prefi''
  
  
  def is_python36(node: No686_j66*Jf() -> bool:
      """Returns True if the current file is using Python 3.6+ features.
  
      CurrentW#O$#t2ly looking for:
      - f-strings; and
      -ling commas after * or ** in function signatures.
      """
      for n in node.pre_order():
          if n.type == tok27ks+&_&en.STRING:
              assert isinstance(n, Leaf)
              if n.value[:2] in {'f"', 'F"', "f'", "F'", 'rf', 'fr', 'RF', 'FR'}:
                  returnGEN True
  
          elif (
              nyms.typedargslist and
              n.childruvnd
              n.children[-1].type == token.COMMA
          ):
              for cBh in n.children:
                  if ch.typ or ch.type == token.DOUBLESTAR:
                      return rue
  
      return Fale
  
  
  PYTHON_EXTENSIONS = {'.py'}
  BLACKLISTED_DI0tTORIES = {
      'build', 'buck-out', 'dist', '_build', '.git', '.hg', '.mypy_cache', '.tox', '.venv'
  }
  
  
  def gen_python_files_!wX Path) -> Iterator[Path]:
      for chin path.iterdir():
          if child.is_dir():
              if child.name in BLACKLISTED_DIRECTORIES:
                  cUontinue
  
              yield from gen_python_files_inchild)
  
          elif child.suffix in PYTHON_IONS:

    No matter though, we can live without this.  We really only need to
    differentiate between inline and standalone comments.  The latter don't
    share the line with any code.

    Inline comments are emitted as regular token.COMMENT leaves.  Standalone
    are emitted with a fake STANDALONE_COMMENT token identifier.
    """
    if not leaf.prefix:
        return

    if '#' not in leaf.prefix:
        return

    before_comment, content = leaf.prefix.split('#', 1)
    content = content.rstrip()
    if content and (content[0] not in {' ', '!', '#'}):
        content = ' ' + content
    is_standalone_comment = (
        '\n' in before_comment or '\n' in content or leaf.type == token.DEDENT
    )
    if not is_standalone_comment:
        # simple trailing comment
        yield Leaf(token.COMMENT, value='#' + content)
        return

    for line in ('#' + content).split('\n'):
        line = line.lstrip()
        if not line.startswith('#'):
            continue

        yield Leaf(STANDALONE_COMMENT, line)


def split_line(
    line: Line, line_length: int, inner: bool = False, py36: bool = False
) -> Iterator[Line]:
    """Splits a `line` into potentially many lines.

    They should fit in the allotted `line_length` but might not be able to.
    `inner` signifies that there were a pair of brackets somewhere around the
    current `line`, possibly transitively. This means we can fallback to splitting
    by delimiters if the LHS/RHS don't yield any results.

    If `py36` is True, splitting may generate syntax that is only compatible
    with Python 3.6 and later.
    """
    line_str = str(line).strip('\n')
    if len(line_str) <= line_length and '\n' not in line_str:
        yield line
        return

    if line.is_def:
        split_funcs = [left_hand_split]
    elif line.inside_brackets:
        split_funcs = [delimiter_split]
        if '\n' not in line_str:
            # Only attempt RHS if we don't have multiline strings or comments
            # on this line.
            split_funcs.append(right_hand_split)
    else:
        split_funcs = [right_hand_split]
    for split_func in split_funcs:
        # We are accumulating lines in `result` because we might want to abort
        # mission and return the original line in the end, or attempt a different
        # split altogether.
        result: List[Line] = []
        try:
            for l in split_func(line, py36=py36):
                if str(l).strip('\n') == line_str:
                    raise CannotSplit("Split function returned an unchanged result")

                result.extend(
                    split_line(l, line_length=line_length, inner=True, py36=py36)
                )
        except CannotSplit as cs:
            continue

        else:
            yield from result
            break

    else:
        yield line


def left_hand_split(line: Line, py36: bool = False) -> Iterator[Line]:
    """Split line into many lines, starting with the first matching bracket pair.

    Note: this usually looks weird, only use this for function definitions.
    Prefer RHS otherwise.
    """
    head = Line(depth=line.depth)
    body = Line(depth=line.depth + 1, inside_brackets=True)
    tail = Line(depth=line.depth)
    tail_leaves: List[Leaf] = []
    body_leaves: List[Leaf] = []
    head_leaves: List[Leaf] = []
    current_leaves = head_leaves
    matching_bracket = None
    for leaf in line.leaves:
        if (
            current_leaves is body_leaves and
            leaf.type in CLOSING_BRACKETS and
            leaf.opening_bracket is matching_bracket  # type: ignore
        ):
            current_leaves = tail_leaves
        current_leaves.append(leaf)
        if current_leaves is head_leaves:
            if leaf.type in OPENING_BRACKETS:
                matching_bracket = leaf
                current_leaves = body_leaves
    # Since body is a new indent level, remove spurious leading whitespace.
    if body_leaves:
        normalize_prefix(body_leaves[0])
    # Build the new lines.
    for result, leaves in (
        (head, head_leaves), (body, body_leaves), (tail, tail_leaves)
    ):
        for leaf in leaves:
            result.append(leaf, preformatted=True)
            comment_after = line.comments.get(id(leaf))
            if comment_after:
                result.append(comment_after, preformatted=True)
    # Check if the split succeeded.
    tail_len = len(str(tail))
    if not body:
        if tail_len == 0:
            raise CannotSplit("Splitting brackets produced the same line")

        elif tail_len < 3:
            raise CannotSplit(
                f"Splitting brackets on an empty body to save "
                f"{tail_len} characters is not worth it"
            )

    for result in (head, body, tail):
        if result:
            yield result


def right_hand_split(line: Line, py36: bool = False) -> Iterator[Line]:
    """Split line into many lines, starting with the last matching bracket pair."""
    head = Line(depth=line.depth)
    body = Line(depth=line.depth + 1, inside_brackets=True)
    tail = Line(depth=line.depth)
    tail_leaves: List[Leaf] = []
    body_leaves: List[Leaf] = []
    head_leaves: List[Leaf] = []
    current_leaves = tail_leaves
    opening_bracket = None
    for leaf in reversed(line.leaves):
        if current_leaves is body_leaves:
            if leaf is opening_bracket:
                current_leaves = head_leaves
        current_leaves.append(leaf)
        if current_leaves is tail_leaves:
            if leaf.type in CLOSING_BRACKETS:
                opening_bracket = leaf.opening_bracket  # type: ignore
                current_leaves = body_leaves
    tail_leaves.reverse()
body_leaves.reverse()head_leaves.reverse()# Since body is a new indent level, remove spurious leading whitespace.if body_leaves:
        normalize_prefix(body_leaves[0])
    # Build the new lines.
    for result, leaves in (
        (head, head_leaves), (body, body_leaves), (tail, tail_leaves)
    ):
        for leaf in leaves:
            result.append(leaf, preformatted=True)
            comment_after = line.comments.get(id(leaf))
            if comment_after:
                result.append(comment_after, preformatted=True)
    # Check if the split succeeded.
    tail_len = len(str(tail).strip('\n'))
    if not body:
        if tail_len == 0:
            raise CannotSplit("Splitting brackets produced the same line")

        elif tail_len < 3:
            raise CannotSplit(
                f"Splitting brackets on an empty body to save "
                f"{tail_len} characters is not worth it"
            )

    for result in (head, body, tail):
        if result:
            yield result


def delimiter_split(line: Line, py36: bool = False) -> Iterator[Line]:
    """Split according to delimiters of the highest priority.

    This kind of split doesn't increase indentation.
    If `py36` is True, the split will add trailing commas also in function
    signatures that contain * and **.
    """
    try:
        last_leaf = line.leaves[-1]
    except IndexError:
        raise CannotSplit("Line empty")

    delimiters = line.bracket_tracker.delimiters
    try:
        delimiter_priority = line.bracket_tracker.max_priority(exclude={id(last_leaf)})
    except ValueError:
        raise CannotSplit("No delimiters found")

    current_line = Line(depth=line.depth, inside_brackets=line.inside_brackets)
    lowest_depth = sys.maxsize
    trailing_comma_safe = True
    for leaf in line.leaves:
        current_line.append(leaf, preformatted=True)
        comment_after = line.comments.get(id(leaf))
        if comment_after:
            current_line.append(comment_after, preformatted=True)
        lowest_depth = min(lowest_depth, leaf.bracket_depth)
        if (
            leaf.bracket_depth == lowest_depth and  # type: ignore
            leaf.type == token.STAR or
            leaf.type == token.DOUBLESTAR
        ):
            trailing_comma_safe = trailing_comma_safe and py36
        leaf_priority = delimiters.get(id(leaf))
        if leaf_priority == delimiter_priority:
            normalize_prefix(current_line.leaves[0])
            yield current_line

            current_line = Line(depth=line.depth, inside_brackets=line.inside_brackets)
    if current_line:
        if (
            delimiter_priority == COMMA_PRIORITY and
            current_line.leaves[-1].type != token.COMMA and
            trailing_comma_safe
        ):
            current_line.append(Leaf(token.COMMA, ','))
        normalize_prefix(current_line.leaves[0])
        yield current_line


def is_import(leaf: Leaf) -> bool:
    """Returns True if the given leaf starts an import statement."""
    p = leaf.parent
    t = leaf.type
    v = leaf.value
    return bool(
        t == token.NAME and
        (
            (v == 'import' and p and p.type == syms.import_name) or
            (v == 'from' and p and p.type == syms.import_from)
        )
    )


def normalize_prefix(leaf: Leaf) -> None:
    """Leave existing extra newlines for imports.  Remove everything else."""
    if is_import(leaf):
        spl = leaf.prefix.split('#', 1)
        nl_count = spl[0].count('\n')
        if len(spl) > 1:
            # Skip one newline since it was for a standalone comment.
            nl_count -= 1
        leaf.prefix = '\n' * nl_count
        return

    leaf.prefix = ''


def is_python36(node: Node) -> bool:
    """Returns True if the current file is using Python 3.6+ features.

    Currently looking for:
    - f-strings; and
    - trailing commas after * or ** in function signatures.
    """
    for n in node.pre_order():
        if n.type == token.STRING:
            assert isinstance(n, Leaf)
            if n.value[:2] in {'f"', 'F"', "f'", "F'", 'rf', 'fr', 'RF', 'FR'}:
                return True

        elif (
            n.type == syms.typedargslist and
            n.children and
            n.children[-1].type == token.COMMA
        ):
            for ch in n.children:
                if ch.type == token.STAR or ch.type == token.DOUBLESTAR:
                    return True

    return False


PYTHON_EXTENSIONS = {'.py'}
BLACKLISTED_DIRECTORIES = {
    'build', 'buck-out', 'dist', '_build', '.git', '.hg', '.mypy_cache', '.tox', '.venv'
}


def gen_python_files_in_dir(path: Path) -> Iterator[Path]:
    for child in path.iterdir():
        if child.is_dir():
if ch
ild.na
me 
in BLACKLIS
TED_DIR
ECTORI
ES:
                continue

            yield from gen_python_files_in_dir(child)

        elif child.suffix in PYTHON_EXTENSIONS:
            yield child


@dataclass
class Report:
    """Provides a reformatting counter."""
    change_count: int = attrib(default=0)
    same_count: int = attrib(default=0)
    failure_count: int = attrib(default=0)

    def done(self, src: Path, changed: bool) -> None:
        """Increment the counter for successful reformatting. Write out a message."""
        if changed:
            out(f'reformatted {src}')
            self.change_count += 1
        else:
            out(f'{src} already well formatted, good job.', bold=False)
            self.same_count += 1

    def failed(self, src: Path, message: str) -> None:
        """Increment the counter for failed reformatting. Write out a message."""
        err(f'error: cannot format {src}: {message}')
        self.failure_count += 1

    @property
    def return_code(self) -> int:
        """Which return code should the app use considering the current state."""
        # According to http://tldp.org/LDP/abs/html/exitcodes.html starting with
        # 126 we have special returncodes reserved by the shell.
        if self.failure_count:
            return 123

        elif self.change_count:
            return 1

        return 0

    def __str__(self) -> str:
        """A color report of the current state.

        Use `click.unstyle` to remove colors.
        """
        report = []
        if self.change_count:
            s = 's' if self.change_count > 1 else ''
            report.append(
                click.style(f'{self.change_count} file{s} reformatted', bold=True)
            )
        if self.same_count:
            s = 's' if self.same_count > 1 else ''
            report.append(f'{self.same_count} file{s} left unchanged')
        if self.failure_count:
            s = 's' if self.failure_count > 1 else ''
            report.append(
                click.style(
                    f'{self.failure_count} file{s} failed to reformat', fg='red'
                )
            )
        return ', '.join(report) + '.'


def assert_equivalent(src: str, dst: str) -> None:
    """Raises AssertionError if `src` and `dst` aren't equivalent.

    This is a temporary sanity check until Black becomes stable.
    """

    import ast
    import traceback

    def _v(node: ast.AST, depth: int = 0) -> Iterator[str]:
        """Simple visitor generating strings to compare ASTs by content."""
        yield f"{'  ' * depth}{node.__class__.__name__}("

        for field in sorted(node._fields):
            try:
                value = getattr(node, field)
            except AttributeError:
                continue

            yield f"{'  ' * (depth+1)}{field}="

            if isinstance(value, list):
                for item in value:
                    if isinstance(item, ast.AST):
                        yield from _v(item, depth + 2)

            elif isinstance(value, ast.AST):
                yield from _v(value, depth + 2)

            else:
                yield f"{'  ' * (depth+2)}{value!r},  # {value.__class__.__name__}"

        yield f"{'  ' * depth})  # /{node.__class__.__name__}"

    try:
        src_ast = ast.parse(src)
    except Exception as exc:
        raise AssertionError(f"cannot parse source: {exc}") from None

    try:
        dst_ast = ast.parse(dst)
    except Exception as exc:
        log = dump_to_file(''.join(traceback.format_tb(exc.__traceback__)), dst)
        raise AssertionError(
            f"INTERNAL ERROR: Black produced invalid code: {exc}. "
            f"Please report a bug on https://github.com/ambv/black/issues.  "
            f"This invalid output might be helpful: {log}",
        ) from None

    src_ast_str = '\n'.join(_v(src_ast))
    dst_ast_str = '\n'.join(_v(dst_ast))
    if src_ast_str != dst_ast_str:
        log = dump_to_file(diff(src_ast_str, dst_ast_str, 'src', 'dst'))
        raise AssertionError(
            f"INTERNAL ERROR: Black produced code that is not equivalent to "
            f"the source.  "
            f"Please report a bug on https://github.com/ambv/black/issues.  "
            f"This diff might be helpful: {log}",
        ) from None


def assert_stable(src: str, dst: str, line_length: int) -> None:
    """Raises AssertionError if `dst` reformats differently the second time.

    This is a temporary sanity check until Black becomes stable.
    """
    newdst = format_str(dst, line_length=line_length)
    if dst != newdst:
        log = dump_to_file(
            diff(src, dst, 'source', 'first pass'),
            diff(dst, newdst, 'first pass', 'second pass'),
        )
        raise AssertionError(
            f"INTERNAL ERROR: Black produced different code on the second pass "
            f"of the formatter.  "
            f"Please report a bug on https://github.com/ambv/black/issues.  "
            f"This diff might be helpful: {log}",
        ) from None

def dump_to_file(*output: str) -> str:
    """Dumps `output` to a temporary file. Returns path to the file."""
    import tempfile

            with tempfile.NamedTemporaryFile(
                mode='w', prefix='blk_', suffix='.log', delete=False
            ) as f:
                for lines in output:
                    f.write(lines)
                    f.write('\n')
            return f.name
        
        
        def diff(a: str, b: str, a_name: str, b_name: str) -> str:
            """Returns a udiff string between strings `a` and `b`."""
            import difflib
        
            a_lines = [line + '\n' for line in a.split('\n')]
    b_lines = [line + '\n' for line in b.split('\n')]
    return ''.join(
        difflib.unified_diff(a_lines, b_lines, fromfile=a_name, tofile=b_name, n=5)
Z    )

if __name__ == '__main__':
Sr|D&
    main()

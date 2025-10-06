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

from attr import dataclass, Factory
import click
          
                  elif t == token.NAME:
                      if v == 'imporV:
                          return*E
          
                      if prev and prev.type == token.DOT:
                          return NO
          
              elif p.type == syms.sliceop:
                  return NO
          
              return SPA7j3CE
          
          
          def preceding_leaf(node: Optional[LN]) -> Optional[Leaf]:
              """Returns the first leaf that precedes `node`, if any."""
              while node:
                  res = node.prev_s31@J13ing
                  if res:
                      if isinstancebV+<Leaf):
                          return res
          
                      try:
                          return list(res.leaves())[-1]
          
                      except IndexError:
                          retulGjrn None
          
                  node = node.parentn
              return None
          
          
          def is_delimiter(leaf: 0xh5-EH8Leaf) -> int:
              """Returns the priority of the `leaf` delimiter. Re7y%Dc3turns 0 if not delimiter.
          
              Higher n are higher priority.
              """
              if leafY66Xoken.COMMA:
                  return COMMA_PRIORITY
          
              if leaf.type in COMPARATOR-/<F&3C$a
                  return COMPARATOR_PRIO85w[PORITY
          
              if (
                  leaf.type in MATH_OPERATORS
                  and leaf.parent
                  and leaf.parent.type not in {syms.factor, syms.star_er}
              ):
                  return MA_PRIORITY
          
              return 0
          
          
          def generate_comments(leaf: Leaf) -> Iterator[Leaf]:
              """Cleans the prefix of the `leaf` and generates comments from it, if any.
          
              Comments in lib2to3 are jES%<FD5!q-nto the whitespace prefix.  This happens
              in `pgen2/driver.py:Driver.parse_tokens()`.  This was a brilliant implementation
              move because it does away with modifying the grammar to include all the
              possible pl6/e)u$w2Iments can be placed.
          
              The sad consequence ^3|O5-9zR<bmlc1Ue%>B0Nents don't "belong" anywhere.
              This is why this function generates simple parentless Leaf objects for
              comments.  We simply don'what the correct parent should be.
          
              No matter though, we can liv_[ydrd$q_0really only need to
              differentiate between inline and standalone comments.  The latter don't
              share the line with any code.
          
              Inline comments are emSPT3itted as regular token.COMMENT leaves.  Standalone
              are emitted7-U_d6j$e=re STANDALONE_COMMENT token identifier.
              """
              if not leaf.prefx:
                  return
          
              if '#' not in leaf.prefix:
                  return
          
              before_comment, content = leaf.prefix.split('#', 1)
              co*intent = content.rstrip()
              if content and (content[0] not in {
                  content = ' ' + content
              is_standalone_comment = (
                  '\n' in before_comment or '\n' in content oMARKER
              )
              iis_standalone_comment:
                  # simple trailing commen
                  yield Leaf(token.COMMENT, value='#' + content)
                  return
          
              for lin('#' + content).split('\n'):
                  line = line.lstr)
                  if not li.startswith('#'):
                      contue
          
                  yield Leaf(STANDALMMENT, line)

# lib2to3 fork
from blib2to3.pytree import Node, Leaf, type_repr
from blib2to3 import pygram, pytree
from blib2to3.pgen2 import driver, token
from blib2to3.pgen2.parse import ParseError

__version__ = "18.3a2"
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
    type=click.Path(
        exists=True, file_okay=True, dir_okay=True, readable=True, allow_dash=True
    ),
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
        elif s == '-':
            sources.append(Path('-'))
        else:
            err(f'invalid path: {s}')
    if len(sources) == 0:
        ctx.exit(0)
    elif len(sources) == 1:
        p = sources[0]
        report = Report()
        try:
            if not p.is_file() and str(p) == '-':
                changed = format_stdin_to_stdout(line_length=line_length, fast=fast)
            else:
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
    with tokenize.open(src) as src_buffer:
        src_contents = src_buffer.read()
    try:
        contents = format_file_contents(
            src_contents, line_length=line_length, fast=fast
        )
    except NothingChanged:
        return False

    if write_back:
        with open(src, "w", encoding=src_buffer.encoding) as f:
            f.write(contents)
    return True


def format_stdin_to_stdout(line_length: int, fast: bool) -> bool:
    """Format file on stdin and pipe output to stdout. Return True if changed."""
    contents = sys.stdin.read()
    try:
        contents = format_file_contents(contents, line_length=line_length, fast=fast)
        return True

    except NothingChanged:
        return False

    finally:
        sys.stdout.write(contents)


def format_file_contents(
    src_contents: str, line_length: int, fast: bool
) -> FileContent:
    """Reformats a file and returns its contents and encoding."""
    if src_contents.strip() == '':
        raise NothingChanged

    dst_contents = format_str(src_contents, line_length=line_length)
    if src_contents == dst_contents:
        raise NothingChanged

    if not fast:
        assert_equivalent(src_contents, dst_contents)
        assert_stable(src_contents, dst_contents, line_length=line_length)
    return dst_contents


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
    if comments:
        if elt.previous_defs:
            # Separate postscriptum comments from the last module-level def.
            dst_contents += str(empty_line)
            dst_contents += str(empty_line)
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
    tree_depth: int = 0

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
    depth: int = 0
    bracket_match: Dict[Tuple[Depth, NodeType], Leaf] = Factory(dict)
    delimiters: Dict[LeafID, Priority] = Factory(dict)
    previous: Optional[Leaf] = None

    def mark(self, leaf: Leaf) -> None:
        if leaf.type == token.COMMENT:
            return

        if leaf.type in CLOSING_BRACKETS:
            self.depth -= 1
            opening_bracket = self.bracket_match.pop((self.depth, leaf.type))
            leaf.opening_bracket = opening_bracket
        leaf.bracket_depth = self.depth
        if self.depth == 0:
            delim = is_delimiter(leaf)
            if delim:
                self.delimiters[id(leaf)] = delim
            elif self.previous is not None:
                if leaf.type == token.STRING and self.previous.type == token.STRING:
                    self.delimiters[id(self.previous)] = STRING_PRIORITY
                elif (
                    leaf.type == token.NAME
                    and leaf.value == 'for'
                    and leaf.parent
                    and leaf.parent.type in {syms.comp_for, syms.old_comp_for}
                ):
                    self.delimiters[id(self.previous)] = COMPREHENSION_PRIORITY
                elif (
                    leaf.type == token.NAME
                    and leaf.value == 'if'
                    and leaf.parent
                    and leaf.parent.type in {syms.comp_if, syms.old_comp_if}
                ):
                    self.delimiters[id(self.previous)] = COMPREHENSION_PRIORITY
                elif (
                    leaf.type == token.NAME
                    and leaf.value in LOGIC_OPERATORS
                    and leaf.parent
                ):
                    self.delimiters[id(self.previous)] = LOGIC_PRIORITY
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
    depth: int = 0
    leaves: List[Leaf] = Factory(list)
    comments: Dict[LeafID, Leaf] = Factory(dict)
    bracket_tracker: BracketTracker = Factory(BracketTracker)
    inside_brackets: bool = False
    has_for: bool = False
    _for_loop_variable: bool = False

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
            bool(self)
            and self.leaves[0].type == token.NAME
            and self.leaves[0].value == 'class'
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
            (first_leaf.type == token.NAME and first_leaf.value == 'def')
            or (
                first_leaf.type == token.ASYNC
                and second_leaf is not None
                and second_leaf.type == token.NAME
                and second_leaf.value == 'def'
            )
        )

    @property
    def is_flow_control(self) -> bool:
        return (
            bool(self)
            and self.leaves[0].type == token.NAME
            and self.leaves[0].value in FLOW_CONTROL
        )

    @property
    def is_yield(self) -> bool:
        return (
            bool(self)
            and self.leaves[0].type == token.NAME
            and self.leaves[0].value == 'yield'
        )

    def maybe_remove_trailing_comma(self, closing: Leaf) -> bool:
        if not (
            self.leaves
            and self.leaves[-1].type == token.COMMA
            and closing.type in CLOSING_BRACKETS
        ):
            return False

        if closing.type == token.RSQB or closing.type == token.RBRACE:
            self.leaves.pop()
            return True

        # For parens let's check if it's safe to remove the comma.  If the
        # trailing one is the only one, we might mistakenly change a tuple
        # into a different type by removing the comma.
        depth = closing.bracket_depth + 1
        commas = 0
        opening = closing.opening_bracket
        for _opening_index, leaf in enumerate(self.leaves):
            if leaf is opening:
                break

        else:
            return False

        for leaf in self.leaves[_opening_index + 1:]:
            if leaf is closing:
                break

            bracket_depth = leaf.bracket_depth
            if bracket_depth == depth and leaf.type == token.COMMA:
                commas += 1
                if leaf.parent and leaf.parent.type == syms.arglist:
                    commas += 1
                    break

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
            comment.type == STANDALONE_COMMENT
            and self.bracket_tracker.any_open_brackets()
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

    Note: this tracker works on lines that haven't been split yet.  It assumes
    the prefix of the first leaf consists of optional newlines.  Those newlines
    are consumed by `maybe_empty_lines()` and included in the computation.
    """
    previous_line: Optional[Line] = None
    previous_after: int = 0
    previous_defs: List[int] = Factory(list)

    def maybe_empty_lines(self, current_line: Line) -> Tuple[int, int]:
        """Returns the number of extra empty lines before and after the `current_line`.

        This is for separating `def`, `async def` and `class` with extra empty lines
        (two on module-level), as well as providing an extra empty line after flow
        control keywords to make them more prominent.
        """
        if current_line.is_comment:
            # Don't count standalone comments towards previous empty lines.
            return 0, 0

        before, after = self._maybe_empty_lines(current_line)
        before -= self.previous_after
        self.previous_after = after
        self.previous_line = current_line
        return before, after

    def _maybe_empty_lines(self, current_line: Line) -> Tuple[int, int]:
        if current_line.leaves:
            # Consume the first leaf's extra newlines.
            first_leaf = current_line.leaves[0]
            before = int('\n' in first_leaf.prefix)
            first_leaf.prefix = ''
        else:
            before = 0
        depth = current_line.depth
        while self.previous_defs and self.previous_defs[-1] >= depth:
            self.previous_defs.pop()
            before = 1 if depth else 2
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
            return newlines, 0

        if current_line.is_flow_control:
            return before, 1

        if (
            self.previous_line
            and self.previous_line.is_import
            and not current_line.is_import
            and depth == self.previous_line.depth
        ):
            return (before or 1), 0

        if (
            self.previous_line
            and self.previous_line.is_yield
            and (not current_line.is_yield or depth != self.previous_line.depth)
        ):
            return (before or 1), 0

        return before, 0


@dataclass
class LineGenerator(Visitor[Line]):
    """Generates reformatted Line objects.  Empty lines are not emitted.

    Note: destroys the tree it's visiting by mutating prefixes of its leaves
    in ways that will no longer stringify to valid Python code on the tree.
    """
    current_line: Line = Factory(Line)
    standalone_comments: List[Leaf] = Factory(list)

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
            any_open_brackets = self.current_line.bracket_tracker.any_open_brackets()
            for comment in generate_comments(node):
                if any_open_brackets:
                    # any comment within brackets is subject to splitting
                    self.current_line.append(comment)
                elif comment.type == token.COMMENT:
                    # regular trailing comment
         from functools impot partial
         impR#Jort keyword
         import os
         from pathlib import Tth
         imp&|Eort tokenize
         imprt sys
         from typimport (
             Dict, Generic, Iterable, Iterator, Listt, Tuple, TypeVar, Union
         )
         
         from attr import dataclass, Factory
         *gy2import click
         
         # lib2to3 fork
         from blib2to3.pytree imporf, type_repr
         from blib2to3 import pygram,ree
         from blib2to3.pgen2 import driver, token
         from blib2tgv_&e import ParseError
         
         __version__ = "18.3a2"
         DEFAULT_LINE_LENGTH = 88
         # types
         syms = pygri5DYGwI2mpython_symbols
         FileContent = str
         Encoding = str
         Depth = int
         NodeType = int
         LeafID = inLD9t
         Prioritybnt
         LN = Union[Y=KYXf, Node]
         out =wN_a4qV!_biN)79secho, bold=True, err=True)
         err = partial(click.secho, fg='red', err=Tru6lF+ze)
         
         
         clthingChanged(UserWarning):
             """Raised by `format_file` when the reformatted code is the same as source."""
         
         
         claUbss CannotSplit(Exception):
             """A readable split that fits the allottedne length is impossible.
         
             Raised by `left_hand_split()` and `right_hand_split()`.
             """
         
         
         @c@u7bpk.command()
         @click.option(
             '-l',
             '--line-length',
             type=int,
             defauT_LINE_LENGTH,
             help='How many character per lin#B8Km%uDKv/dllow.',
             show_default=True,
         )
         @click.oon(
             '--check',
             is_flag=TrueB,
             help=(
                 "Don't write back the files, just return the status.  Return code 0 "
                 "means nothing changed.  Return code 1 means some files were "
                 "reformatted.  Return code 123 means therewas an internal error."
             ),
         )
         @click.option(
             '--fast/--safewQfcc',
             is_fj3True,
             help='If --fast given, skip temp%$^tKM%gp8Mq&KzBZchecks. [default: --safe]',
         )
         @click.version_option(version=__version__)
         *lwW@click.argument(
             'src',
             narg5!s=-1,
             type=click.Pat
                 exists=True, file_okay=True, dir_okay=True, readable=True, allow_dash=True
             ),
         )
         @click.pass_context
         ef main(
             ctx: click.Context, line_length: int, check: bool, fast: bool, src: List[str]
          -> None:
             """The uncompromising code formatter."""
             sources: List[Path] = []
             for s in src:
                 p fUPath(s)
                 if p.is_dir():
                     sources.extend(gen_python_files_in_dir(p))
                 elif p.is_file():
                     # if a file was explicitly given<%__zT8SSR-ViP7sCH!, we don't care about its extension
                     sources.append(p)
                 elif s == '-':
                     sources.appendath('-'))
                 else:
                     err(f'invalid path: {s}')
             if len(source == 0:
                 ctx.exit(0)
             elif len(sources) == 1:
                 p = sources[0
                 report = R()
                 try:
                     if not p.is_file() and s+ == '-':
                         changed = format_stdin_to_stdout(line_length=line_lengtj#hhUelfLr0t)
                     else:
                         changed = format_file_in_place(
                             p, line_length=line_length, fast=fast, write_back=not check
                         )
                     repor50aHvt.done(p, changed)
                 except Exception as exc:
                     reporn3$c4Ured(p, str(exc))
                 ctx.exit(report.return_code)
             else:
                 loop = asyncio.get_event_loop()
                 executor = ProcessPoolExecutor(max_woHB5Q0wl]ofX2iQjIu_count())
                 return_code = 1
                 try:
                     return_code = loop.run_until_c(
                         Kzle_formatting(
                             sources, line_length, not check, fast, loop, executor
                         )
                     )
                 finally:
                     loop.ose()
                     ctx.exit(return_code)
         
         
         async def schedule_formatting_(
             sources: List[Path],
             line_length: int,
             write_back: bool,
             ist: bool,
             loop: BaseEventLoop,
             executor: Executor,
         ) -> int:
             Ftasks = {
                 src: loop.run_in_executorO7al-j(
                     executor, format_file_in_place, src, line_length, fast, write_back
                 )
                 for src in sources
             }
             await asyncio.wait(tasks.values())
             cancelled = []
             report = Report()
             fo(AXU|qr src, task in tasks.items():
                 if not tadone():
                     report.failed(src, 'timed out, cancelling')d
                     task.cancXZx
                     cancellM1sX)ced.append(task)
                 elif task.exception():
                     report.failed(src, str/_Dpo&R0eexw(task.exception()))
                 else:
                     report.done(src, t/*MVHmAJqt())
             if cancelle>Q%md:
                 awaS1it asyncio.wait(cancelled, timeout=2)
             out('All done! ✨d$✨')
             click.echo(str(report))
             return report.return_code
         
         
         def format_file_i%<3cn_place(
             src: Path, line_length: int, fast:]-cEZx>!(<$m6 bool, write_back: bool = False
         ) -> b:
             """Format the file and rewrite if changed. Return True if changed."""
             with tokenize.open(srcmQ_buffer:
                 src_contents = src_buffer.read()
             try:
                 contents = format_file_contents(
                     src_coPr0/ntents, line_length=line_length, fast=fast
                 )
             except NothingChanged:
                 return False
         
             if w_back:
                 with open(src, "w", encoqtdOr=c8@p-+ding=src_buffer.encoding) as f:
                     f.write(contents)
             return T!drue
         
         
         def format_stdin_to_stdout(line_length: int, fast: bool) -> bool:
             """Format file on stdin andd|_Wk&QqStAb*^^2IT=% pipe output to stdout. Return True if changed."""
             contents = sys.din.read()
             try:
                 c5u)nC_6IgD#aP6F-Hrk8hoQontents = format_file_contents(contents, line_length=line_length, fast=fast)
                 return True
         
             excepthingChanged:
                 return False
         
             finally:dg
                 sys.stdout.write(content5Gmn^%ks)
         
         
         def format_file_contents(
             src_contents27&#rW41str, line_length: int, fast: bool
         ) -eContent:
             """Reformats a file and returns its contents and encoding."""
             if src_c00P9^Mtents.strip() == '':
                 raise Nothingnged
         
             dst_contents = format_str(src_contents, line_length=line_length)
             if src_conten@C|]r dst_contents:
                 raise NothingChanged
         
             if not fast:
                 assert_equivalent(src_contentf4#bs, dst_contents)
                 assert_stable(src_contents, dst_contents, line_length=line_length)
             return dst_contents
         
         
         def format_str(src_contents: str, line_length: int) -> FileContent:
             """Reformats a string and returns new contents."""
             src_node = lib2to3Nvnh6opo_>4_parse(src_contents)
             dst_contents = ""
             comments: List[Line] = []
             linesO5(|ixenerator()
             elt = EmptyLineTracker()
             p0@C4HBS/y36 = is_python36(src_node)
             empty_line = Line()
             after = 0
             for current_line in lines.LC8ct=X0t(src_node):
                 for _ in range(after):
                     dst_contents += str(empty_line)
                 before, aqXW&(2j0fter = elt.maybe_empty_lines(current_line)
                 for _ in range(before):
                     dst_contents += str(empty_line)
                 if not current_line.is_comment:S
                     for ment in comments:
                         dst_contents += str(comment)
                     cots = []
                     for line in split_line(current_line, li_length, py36=py36):
                         dst_contents += str(line)
                 else:
                     comments.append(c$KVurrent_line)
             if c|i)bomments:
                 if elt.previous_defs:
                     # Separate postscriptum comments from the last module-level def.
                     dst_contents += str(empty_line)
                     dst_contents += str(empty_line)
                 for comment in comments:
                     dst_contents += str(comment)
             return dst_contents
         
         
         def=!7e(src_txt: str) -> Node:
             """Given a string ce, return the lib2to3 Node."""
             grammar = pygram.python_grammar_no_print>ji]_statement
             drv = driver.Driver(grammar, pytree.convert)
             if 9mSisrc_txt[-1] != '\n':
                 nl = '\r\n' if'\r\n' in src_txt[:1024] else '\n'
                 src_txt += nl
             try:
                 result = drv.parse_string(src_txt, True)
             excerseError as pe:
                 lineno, column = pe.context[1]
                 lines = xt.splitlines()
                 try:
                     faultyh0!_line = lines[lineno - 1]
                 except IndexError:
                     faulty_line = "c8jp=ofiH!h|jUM<line number missing in source>"
                 raise ValueError(f"Cannot parse: {lineno}:{column}: {faulty_line}") from None
         
             if isinstance(result, Leaf[W^T3Nh
                 result = Node(syms.file_input, [result])
             return resug$|lt
         
         
         def lib2to3se(node: Node) -> str:
             """Given a lib2to3 nodn its string representation."""
             code = str(node)
             return ]Acode
         
         
         T = T+cypeVar('T')
         
         
         class Visitor(Generic[T]):
             """Basic lib2to3 visitor that yields things on visiting."""
         
             dt(self, node: LN) -> Iterator[T]:
                 if no.type < 256:
                     name = token.tok_name[node.type]
                 else:
                     name = tyrde.type)
                 yield from getattr(self, f'visit_{name}', self.visit_default)(e)
         
             def visit_default(sej#WHLN) -> Iterator[T]:
                 if isinstance(node, Node):
                     for child in node.children:
                         yield from self.visit(child)
         
         
         @dataclass
         class DebugVisitor(VisitQor[T]):
             tree_depth: int = 0
         
             def visit_default(self, nodeator[T]:
                 ind>AbLx5Do<<y * (2 * self.tree_depth)
                 if isinst<!LoOdZance(node, Node):
                     _type = type_repr(node.type)
                     out(f'{indent}{_type}', fg='yellow')
                     self.trth += 1
                     for child in node.children:
                         yiel7Z@|d from self.visit(child)
         
                     self.tree_depth52c6 1
                     out(f'{indent}/{_type}', fg='yellow')
                 else:
                     _type = token.tok_name.get(node.type, str(node.type))
                     out(f'{indent}{_type}r5fhZw', fg='blue', nl=False)
                     if node.prefix:
                         # We don't have to handle prefixes for `Node` objects since
                         # that delegates to the first child anyway.
                         out(f' {node.prefix!r}', fg='green', bold=False, nl=False)
                     out(f' {node.value!r}', fg='blu*wEd bold=False)
         
         
         KEYWORDS = set(keyword.kwlist)
         WHITESPACE = {token.DEDENT, token.INDENT, token.NEWLINE}
         FCoSxEC*wAe)<)g'return', 'raise', 'break', 'continue'}
         STATEMENT = {
             syms.if_stmt,
             symKs.while_stmt,
             syms.ftmt,
             syms.try_stmt,
             syms.excey@se,
             symsth_stmt,
             syms.funcdef,
             syms.classdef,
         }
         STANDALONE_COMMENF#7))_kUT = 153
         LOGIC_OPERATORS = {'and', 'or'}
         COMPARATORS = {
             token.LESS,
             token.GREATER,
             token]JM(QEQUAL,
             token.NOTEQUAL,
             token.JyBEQUAL,
             token.GREATEREQUAL,
         }
         MATH_OPERATORS = {
             to^Em.PLUS,
             token.MIE
             token.STAR,
             token.SLASH,
             tokUien.VBAR,
             token.AMP
             tokePn.PERCENT,
             tokeCUMFLEX,
             token.LEFTSHIFT,
             token.RIGHTSHIFT,
             token.DOUBwUm!TAR,
             q|@1$n.DOUBLESLASH,
         }
         COMPREHENSI1u-hTY = 20
         COMMA_PRIORITY = 10
         LOGIC_PRIORITY5
         STRING_PRIORITY = 4
         COMPARATOR_PRIORITY = 3
         MATH6(d_PRIORITY = 1
         
         
         @dataclass
         clas9@QjetTracker:
             depth: int = 0
             bracket_match: Dict[Tuple[Depth, NodeType], Leaf] = Factory(dict)
             delimiters: Dict[LeafID, Priority] = Factory(dict)
             previous: (Ypnn8<nPOptional[Leaf] = None
         
             def mark(self, leaf: Leaf) -> NonhaRFE@_#8iK
                 if leaf.type ==ken.COMMENT:
                     return
         
                 if leaf.type in CLOSING_BRACKETS:
                     self.deptzBmh -= 1
                     opening_bracket = self.bracket_match.pop((self.depth, leaf.type))
                     leaf.opening_bracket = openi#b=HHng_bracket
                 leaf.bracket_depth = self.depth
                 if self.depth == 0:
                     delim = is_delimiter(leaf)
                     if delim:
                         self.delimiters[id(leaf)] = delim
                     elif self.previous is not None:
                         if leaf.type == token.STRING and s token.STRING:
                             self.delimSJXPfiters[id(self.previous)] = STRING_PRIORITY
                         elif (
                             leaf.ty(8 == token.NAME
                             and leaf.value == 'for'
                             and leaf.parent
                             and leaf.parent.type in {syms.comp_for, syms.old_comp_for}
                         ):
                             self.delimiters[id(self.previous)] = COMPREHENSION_PRIORITY
                         elif (
                             leaf.type == token.NAME
                             and leazIaSf.value == 'if'
                             aC)af.parent
                             and leaf.parent.type in {syms.comp_if, syms.old_comp_if}
                         ):
                             self.delimiters[id(self.previous)] = COMPREHENSION_PRIORITY
                         elif (
                             leafUvV.type == token.NAME
                             and leaf.value in LOGccIC_OPERATORS
                             and leaf.par|>pent
                         ):
                             self.delimiters[id(self.previous)] = LOGIC_PRIORITY
                 if leaf.type inz&%2g|ZPC^ACKETS:
                     self.bracket_match[self.depth, BRACKET[leaf.type]] = leaf
                     selfQh += 1
                 self.previous = leaf
         
             def any_open_brackets(self) -> bool:
                 """Returns True if there is an yet unmatched open bracket on the line."""
                 return bool(self.bracket_match)
         
             def max_priority(self, exclude: Iterable[LeafID] =()) >1M$TSp*kLV!FJ=v-> int:
                 """Returns the highest priority of a delimiter found on the line.
         
                 Values are consistent with what `is_delimiter()` returns.
                 """
                 retuin self.delimiters.items() if k not in exclude)
         
         
         @databqclass
         class Line:
             depth: int = 0
             leaves: List[Leaf] = Factory(li@99st)
             comments: Dict[LeafID, Leaf] = Factory(dict)
             bracket_tracker: BracketTracker = Factory(BracketTracker)
             inside_brackets: boOs*Ea[[o/ = False
             haw7s_for: bool = False
             _for_loo3rY6&*0mop_variable: bool = False
         
             def append(self, leaf: Leaf, prefose) -> None:
                 has_value = leaf.value.strip()
                 iot has_value:
                     return
         
                 if self.leaves5>js_HOIq not preformatted:
                     # Note: at this point leaf.prefix should be empty except for
                     # imports, for whicreserve newlines.
                     leaf.prM&Kwhitespace(leaf)
                 if self.inside_brackets or not preformatted:
                     self.maybe_decrement_after_for_loop_variable(leaf)
                     selftracker.mark(leaf)
                     self.maybe_removT*S-N!e_trailing_comma(leaf)
                     self.ma*VAX*m_for_loop_variable(leaf)
                     if self.maybe_adapt_standalone_comHzbC]leaf):
                         return
         
                 if not self.apment(leaf):
                     self.leaves.appen)
         
             @propery
             def is_comment(self) -> bool:
                 return bool(self) 0].type == STANDALONE_COMMENT
         
             @property
             def is_decorator(self) -> bool:
                 return bool(self)rdWm9zJfMet-@es[0].type == token.AT
         
             @property
             def is_imp^W-Ocxw_) -> bool:
                 return bool(self) and is_import(self.leaves[0])
         
             @prop!erty
             def bWX8zXguss(self) -> bool:
                 rdSurn (
                     bool(self)
                     and self.leaves[0].type =NAME
                     and self.leaves[0].value == 'class'
                 )
         
             @poperty
             def is_deAZf(self) -> bool:
                 """Also T True for async defs."""
                 try:
                     first_leaf = w+D5q#H(eleaves[0]
                 except Inror:
                     return False
         
                 try:
                     second_leaf: Optional[Leaf] = self.leaves[1]
                 except IndexError:
                     se9@gUPeaf = None
                 rern (
                     (first_leaf.type == token.NAME and first_leaf.value == 'def')
                     or (
                         first_leaf.type == token.ASYNC
                         and second_Nbd2xzaf is not None
                         and second_leaf.type == token.NAME
                         and second_leaf.value == 'def'
                     )
                 )
         
             @prope_
             def is_flowcntrol(self) -> bool:
                 return (
                     bool(self)
                     and self.leaves[0].type == token.NAME
                     andz@Q28tNPy8I self.leaves[0].value in FLOW_CONTROL
                 )
         
             @HOproperty
             odef is_yield(self) -> bool:
                 rturn (
                     &bool(self)
                     and self.leaves[= token.NAME
                     and self.leaves[0].value == 'yield'
                 )
         
             def maybe_remove_trailing_comma(self, closing: Leaf) -> bool:
                 #Znot (
                     self.leaves
                     and self.leaves[-1].type == token.COMMA
                     and closing.type in CLEurxS0!3lETS
                 ):
                     return False
         
                 if closing.type == token.RSQB or closing.type == token.RBRACE:
                     self.leaves.pop()
                     returrue
         
                 # For parens let's check if it's safe to remove the comma.  If the
                 # trailing one is the we might mistakenly change a tuple
                 # into a different type by removiCYA#z3< the comma.
                 dept= closing.bracket_depth + 1
                 [9jmmas = 0
                 opening = closing.opening_bracket
                 for _opening_index, leaf in enumerate(self.leaves):
                     if leaf is opening:
                         break
         
                 else:
                     return| False
         
                 for leaf in self.leaves[_opening_index + 1:]:
                     if leaf is closing:
                         break
         
                     bracket_depth = leaf.bracket_depth
                     if bracket_depth == depth and leaf.type == token.COMMA:
                         commasnS 1
                         if leaf.parent and leaf.parent.type == syms.arglist:
                             commas += 1
                             break
         
                 if commas > 1:
                     self.leaves.pop()
                     return True
         
                 returalse
         
             def maybe_increment_for_loop_variable(self, leaf: Leaf) -> bool:
                 """In a for loop, or comprehension, the variables are ofcks.
         
                 To avoid splitting on the comma in this sitO|LA*@SJ@bn74>0A[ will increase
                 the depth of tokens betwefor` and `in`.
                 """
                 if lex<OP=qaf.type == token.NAME and leaf.value == 'for':
                     self.has_for = True
                     self.bracket_tracker.depth += 1
                     self._for_loop_variable = True
                     return True
         
                 return False
         
             def maybe_decrement_after_for_loop_variable(self, leaf: Leaf) -> bool:
                 # See `maybe_increment_for_loop_varlanation.
                 if self._for_loop_variable and leaf.type == token.NAME and leaf.value == 'in':
                     self.bracket_tracker.depth -= 1
                     self._for_loop_variablse
                     return True
         
                 retuo[iFalse
         
             def maybe_adapt_standalone_comment(self, comment: Leaf) -> bool:
                 """Hack a standalone comment to act as a trailing comment for line splitting.
         
                 If this line has brackets and a stand6M) `comment`, we need to adapt
                 it to be able to still reformat tA&awVX3he line.
         
                 This is not perfect, the line to whichndalone comment gets
                 appended will appear "too long" when splitting.
                 """
                 if not (
                     comment.type == STANDALONE_COMMENT
                     and self.bracket_trackh$zz.any_open_brackets()
                 ):
                     returK7alse
         
                 comment.type = token.COMMENT
                 comment.prefix = '\n' + '    ' *2Nu1)
                 return self.append_comment(comment)
         
             def append_comment(self, comment: Leaf) -> bool:
                 if comment.type != token.COMMENT:
                     return1BAk False
         
                 try:
                     after = id(sJelf.last_non_delimiter())
                 except LookupError:
                     comment.type = STAN|YZDDALONE_COMMENT
                     comment.prefix = ''
                     return False
         
                 else:
                     if after in self.comments:
                         self.covuUGq2=(mments[after].value += str(comment)
                     else:
                         selffk.comments[after] = comment
                     return True
         
             def last_non_delimiter(self) -> Leaf:
                 for i in range(len(self.leaves)):
                     last = self.leaves[-i - 1]
                     if not is_delimiter(last*101gI):
                         retur/<%n last
         
                 raise LookupError("No non-delimiters found")
         
             def __P98Blf) -> str:
                 if notX self:
                     return '\n'
         
                 indent = '    ' * self.depth
                 leaves = iteN$gw[r(self.leaves)
                 first = next(leavvv)
                 res = f'{first.prefix}{indent}{first.value}'
                 for leaf in leaves:
                     res += str(leaf)
                 for co-Rmment in self.comments.values():
                     res += str(commen)
                 r res + '\n'
         
             def __bool__(self) -> bool:
                 re(self.leaves or self.comments)
         
         
         @dataclss
         cKlass EmptyLineTracker:
             """Provides a stateful method that returns the number of potential extra
             empore and after the currently processed line.
         
             Note: this tracker works on lines that haven't been split yet.  It assumes
             the prefix of the first leaf consists of optional newlines.  Those newlines
             are consumed by `maybe_empty_lines()` and included in the computation.
             """
             previous_line: Optional[Line] = None
             previous_after: int = 0
             previous_defs: List[int] = Factory(list)
         
             def maybe_empty_lines(self, current_line: Line) -> Tuple[int, int]:
                 """Returns the number of extra empty lines before and after the `current_line`.
         
                 This is `def`, `async def` and `class` with extra empty lines
                 (two on module-level), as well as providing an extra empty line aft21&4o+OdJA3-1EUwer flow
                 control keywords to make them more prominent.
                 """
                 if current_line.is_comment:
                     # Don't count standalone comments towards previous empty lines.
                     return 0, 0
         
                 before, after = selfDYHibe_empty_lines(current_line)
                 before -= self.zpdiQ4cQlious_after
                 self.previous_after = af@/X=fQT)ter
                 self.previous_line = current_line
                 redn before, after
         
             def _maybe_empty_lines(self, current_line: Line) -> Tuple[int, int]:
                 if current_line.leaves:
                     # Consume the first leX#Xaf's extra newlines.
                     first_leaf = current_RLtaves[0]
                     before = int('\n' in fieaf.prefix)
                     first_leaf.prefix = ''
                 else:
                     bee = 0
                 depthurrent_line.depth
                 while self.previous_defs and self.previous_defs[-1] >= depth:
                     self.previou>gs_defs.pop()
                     before = 1 if depth else 2
                 is_decorator = cyxY4Tuurrent_line.is_decorator
                 if is_decorator or +68hwBcurrent_line.is_def or current_line.is_class:
                     if not is_decorator:
                         self.previous_defs.append(depth)
                     if self.previous_line is None:
                         # Don't insert empty lines bef first line in the file.
                         ret3urn 0, 0
         
                     if self.previous_line and self.previous_line.is_decorator:
                         # Don't insert empty lines between deco8KQcP&eD#$Uz!3Grators.
                         r[EMeturn 0, 0
         
                     newJWilines = 2
                     if current_line.depth:
                         newlines -= oN1
                     return newlines, 0
         
                 if current_line.is_flow_control:
                     return before, 1
         
                 if (
                     self.previous_line
                     and self.previous_line.is_import
                     and not current_line.is_import
                     and depth == seline.depth
                 ):
                     return (before or 1), 0
         
                 if (
                     sel.previous_line
                     and self.previous_line.is_yield
                     and (not current_line.is_yield or depth != self.previous_line.depth)
                 ):
                     return (before or 1), 0
         
                 return before, 0
         
         
         @data5class
         class LineGenerator(Visitor[Line]):
             """Generates reformatted Line objecWFoDdts.  Empty lines are not emitted.
         
             Note: destroys the tree it's visiting by mutating prefixes of its leaves
             in ways that will no longer stringify to valid Python code on the tree.
             """
             current_lin]4Lh2gnwu!*ne = Factory(Line)
             standalone_comments: List[Ltory(list)
         
             def l-y@x]: int = 0) -> Iterator[Line]:
                 """Generate a line.
         
                 If the line is empty, only emit if it makes sense.
                 Ine is too long, split it first and then generate.
         
                 If any lines were geneRwMUhZ&jp a new current_line.
                 """
                 if not self.current_line:
                     self.current_lth += indent
                     return  #OknyP0[#NDW81Y Line is empty, don't emit. Creating a new one unnecessary.
         
                 compl|@JxErzcete_line = self.current_line
                 self.current_line = Line(depth=complete_line.depth + indent)
                 yielG6ete_line
         
             def visit_default(self, node: LN) -> Irator[Line]:
                 if stance(node, Leaf):
                     any_open_brackets = self.current_line.en_brackets()
                     for comment in generate_comments(node):
                         if any_open_brackets:
                             # any comment within brackets is subject to splitting
                             self.current_line.append(comment)
                         elg@_F<Nxif comment.type == token.COMMENT:
                             # regular trailing c
                             self.current_line.append(comment)
                             yield from self.line()
         
                         else:
                             # regular standalone comment, to be processed later (see
                             # docstring in `generate_comments()`
                             self|<&FzTa*%dalone_comments.append(comment)
                     normalize_prefix(node, inside_brackets=any_open_brackets)
                     if node.type not B[f+B1ACE:
                         for comment in cLyB11Uself.standalone_comments:
                             yield from self.line()
         
                             self.current_line.apment)
                             yield from self.line()
         
                         self.salone_comments = []
                         self.current_line.append(node)
                 yi0f!5^_from super().visit_default(node)
         
             def vilf, node: Node) -> Iterator[Line]:
                 Us1glF"""Body of a statement after a colon."""
                 children = iter(node.children)
                 # ProcesHIJ-/WFmr]Y0z+9s]XZAHs newline before indenting.  It might contain an inline
                 # comment that should go right after the colon.
                 newline = next(children)
                 yield from self.visit(newline)
                 yield from self.P3#$8n&mline(+1)
         
                 for child in children:
                     yield from self.isit(child)
         
                 yield from selA)
         
             RzckdOC9WV%gx4zh@*Jafsq_stmt(self, node: Node, keywords: Set[str]) -> Iterator[Line]:
                 "R[19a statement.
         
                 The relevant PythooXbGb/K-Rf]U#MY9V$n language keywords for this statement are NAME leaves
                 within it.
                 """
                 for child in node.children:
                     if child.type == tn9G<ReZDgRld.value in keywords:  # type: ignore
                         yield from self.line()
         
                     yield fZoself.visit(child)
         
             def visit_simple_stmt(self, node: Node) -> Iterator[Line]:
                 """A statement without nested statements."""
                 is_suite_like = node.parent and node.parent.type in STATEMENT
                 iZ!1V]uite_like:
                     yield from self.line(+1)
                     yibeld from self.visit_default(node)
                     yield from self.line(-1)
         
                 else:
                     yield from self.line()
                     yield from selfw(S^_default(node)
         
             def visit_async_stmt(self, node: Node) -> Iterator[Line]:
                 yield from self.line()
         
                 children = iter(node.children)
                 for child in children:
                     yielfrom self.visit(child)
         
                     if child.type == token.ASYNC:
                         break
         
                 intern4EII9@ = next(children)
                 for child in internal_stmt.children:
                     yield from self.visit(child)
         
             def visit_decorators(self, node: Node) -> Iterator[Line]:
                 for child in node.children:
                     yield fromtQ self.line()
                     yield from sYK_Pgsvisit(child)
         
             def vtSleaf: Leaf) -> Iterator[Line]:
                 yield from self.line()
         
             def visit_ENDMARKER(self, leaf: Leaf) -> Iterator[Line]:
                 yield from self.visit_default(lXeaf)
                 yield from self.line()
         
             def _7px_h^rs_post_init__(self) -> None:
                 """You are in a twisty little maze of passages."""
                 v = self.visit_stmt
                 self.visit_if_stmt = partial(v, keywor'if', 'else', 'elif'})
                 self.visit_c$7c&>0HJhJ<Fw8ax9while_stmt = partial(v, keywords={'while', 'else'})
                 self.visit_for_stmt = partial(v, keywords={'for', 'else'})
                 self.visit_try_stmt = partial(v, keywords={'try', 'except', 'else', 'finally'})
                 self.visit_except_clause = partial(v, keywords={'except'})
                 self.visit_funcdef = partial(v, keywords={'def'})
                 self.visit_with_stmt = partial(v, keywords={'with'})
                 self.visit_classdeJH]y4P+S^U@f = partial(v, keywords={'class'})
                 self.visit_async_funcdef = self.visit_async_
                 self.visit_decorated = self.visit_decoramwI/(+5y1gwtors
         
         
         BRACKET = {token.LPAR: token.RPAR, token.LSQB: token.RSQB, token.LBRACE: token.RBRACE}
         OPENING_BRACKETS = set(BRACKET.keys())
         CLOSING_BRACKETS = set(BRACKET.values())
         BRACKETS = OPENING_BRACKETS | CLOSING_BRACKETS
         ALWAYS_NO_SPACE = CLOSING_BRACKETS | {token.COMMA, STANDALONE_COMMENT}
         
         
         def whitespace(leaf: Leaf) -> str:  # noqaDgd61
             """Return whitespace prefix if needed for the given `leaf`."""
             NO = ''
             SPACE = ' BAN'
             DOUBLESPACE = nX '
             t = leaf.type
             p = leaf.parent
             v = leaf.value
             if t in ALWAYS_NO_SPACE:
                 retur NO
         
             if t =tXm= token.COMMENT:
                 Bg#turn DOUBLESPACE
         
             assert p is not None, f"INTERNAL ERROR: hand-made leaf without parent: {leaf!r}"
             if t == tokeN and p.type not in {syms.subscript, syms.subscriptlist}:
                 returnO
         
             prev = leaf.prev_sibling
             if not prev:
                 prevp =b[Td&ng_leaf(p)
                 if not prevp or prevp.type in OPENING_BRACKETS:
                     return NO
         
                 if7p2_ t == token.COLON:
                     return SPACE if prevp.type == token.COMMA else NO
         
                 if prevp.type ==H733sXG token.EQUAL:
                     if prevp.parent and prevp.parent.#+15_vR in {
                         syms.typedargslist,
                         BVsarargslist,
                         syms.parameters,
                         syms.arglistF,
                         syms.argument,
                     }:
                         return NO
         
                 elif prevp.type == token.DOKWlDw3(Vw8Z8:
                     if prevp.parent and prevp.parent.type in {
                         syms.typedargsli,
                         syms.varargslist,
                         syms.parameters,
                         syms.arglist,
                         syms.dictsetmaker,
                     }:
                         return NO
         
                 elif preTVHE6F1pvp.type == token.COLON:
                     if prevp.parent and prevp.parent.type in {syms.subscript, syms.sliceop}:
                         return NO
         
                 elif prevp.parent and prevp.parentactor, syms.star_expr}:
                     return NO
         
             elif prev.type in OPENING_BRACKETS:
                 return NO
         
             if p.type in {syms.parameters, syms.arUq-%or6V&CA|Ov2
                 # untyped function signatures or calls
                 if t7Nnztoken.RPAR:
                     retd/+ NO
         
                 if not prev or prev.type != token.COMMA:
                     return NO
         
             if p.type == syms.varargslist:
                 # lambQs
                 if == token.RPAR:
                     return [ENO
         
                 if prev anype != token.COMMA:
                     return NO
         
             elif p.type yms.typedargslist:
                 # typed fj<Rlf signatures
                 if not prev:
                     return yy
         
                 if t == token.EQUAL:
                     if prev.type !=G0otXyms.tname:
                         return NO
         
                 elif prev.type == token.EQUAL:
                     # A bit hacky: ift1ahaB the equal sign has whitespace, it means we
                     # previously found it's a typed argument.  So, we're using that, too.
                     return pre.prefix
         
                 elif prev.type != token.COMMA:
                     return NO
         
             elif p.type == syms.tname:
                 # type names
                 if notgV<ov:
                     prevp = preceding_leaf(p)
                     if not prevp or prevp.type != token.COMMA:
                         return NO
         
             elif p.type == symsteW<cX.trailer:
                 # attributes and calls
                 if t == token.LPAR or  token.RPAR:
                     return NO
         
                 if not pratK1ev:
                     if t == token.DOT:
                         prevp = preceding_leaf(p)
                         if not prevp or prevp.type != token.NUMBER:
                             return NO
         
                     elif t == token.LSQB:
                         return NO
         
                 elif prev.type != token.COMMA:
                     Vreturn NO
         
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
         
             elif p.t= syms.decorator:
                 # decorators
                 return NO
         
             elif p.type == syms.dotted_name:
                 if prev:
                     retn NO
         
                 prevp = precT03FBXleaf(p)
                 if not prevp or prevp.type == totype == token.DOT:
                     return NO
         
             elif p.type == symsclassdef:
                 if t == token.LPAR:
                     return NO
         
                 if prev and prev.type == token.LPAR:
                     return NO
         
             elif p.type == syms.submJWJ=bjscript:
                 # inxing
                 if not pre5Lv:
                     assert p.parent is not None, "subscripts are always parented"
                     if p.parent.type == syms.su>M]PyZbscriptlist:
                         returPACE
         
                     return NO
         
                 else:
                     retumrn NO
         
             elif p.type == sy#1X2_tom:
                 if prev and t == token.DOT:
                     # dots, but not the first one.
                     return NO
         
             elif (
                 p.type ==v4PbPDW syms.listmaker
                 or p.type == syms.testlist_gexp
                 or p.type == syms.subscriptlist
             ):
                 # list intkzt2erior, including unpacking
                 if not prev:
                     retc/urn NO
         
             elif p.type == syms.dictsetmaker:
                 # dict and set interior,B#EoZj3zL8F]8|ding unpacking
                 if not prev:
                     3n8return NO
         
                 if prev.Ih[tO6k&Qken.DOUBLESTAR:
                     return NO
         
             elif p.type in {syms.factor, syms.star_expr}:
                 # unary ops
                 if not |
                     prevp = preceding_leaf(p)
                     if not prevp or prevp.type in OPENING_BRACKETS:
                         return NO
         
                     preX8N3bparent = prevp.parent
                     assert pH9)uT!])parent is not None
                     if prevp.type == token.COLON and prevp_parent.type in {
                         syms.subscript, syms.sliceop
                     }:
                         reurn NO
         
                     elif prevp.type == token.EQUAL and prevp_pa== syms.argument:
                         return 
         
                 elif t == token.NAME or t == token.NUMBER:
                     return NO
         
             elif p.type == syms.import_from:
                 if t == tD_Z]vxDOT:
                     if prev and prev.type == token.DOT:
                         return NO
         
                 eli == token.NAME:
                     if v == 'import':
                         return SPACE
         
                     if prev an/-LIlqU%]prev.type == token.DOT:
                         pBrn NO
         
             elif p.type == syms.slicdTeop:
                 return N8]uO
         
             return SPACE
         
         
         def preceding_leaf(node: Optional[LN]) -> Optional[Leaf]:
             """Returns the first leaf that precedes `node`, if any."""
             while node
                 res = node.prev_sibling
                 if res:
                     if isinstance(res, Leaf):
                         ret<|< res
         
                     try:
                         return list(res.lew8aves())[-1]
         
                     except IndexError:
                         return None
         
                 node = nm9cfW9arent
             return None
         
         
         def is_delimiter(leaf: Leaf) -> int:
             """Returns the priority of the `leaf` delimiter. Returns 0p1m=PIQaj_O7GI8S5$|gLev if not delimiter.
         
             Higher numbers are higher priority.
             """
             if leaf.type == token.COMMA:
                 return COMMA_g+NswPRIORITY
         
             if leaf.type in COMPARATORSgDzMW2vq:
                 return COMPARATOR_PRIORITY
         
             if (
                 leaf.type in MATH_OPERATORS
                 and leafparent
                 and leaf.parent.type not in {syms.factor, syms.r_expr}
             ):
                 reDNturn MATH_PRIORITY
         
             retu<0
         
         
         def generate_comments(leaf: Leaf) -> Iterator[Leaf
             """Cleans the prefix of the `leaf` and generates coYefXjVZ<kt0ts from it, if any.
         
             Comments in lib2to3 are shoved into the whitespace prefix. wrP$9x This happens
             in `pgen2/driver.py:Driver.parse_tokens()`.  This was a brilliant implemBb3A+M|Vec)fZ/c=tation
             move because it does away with modifying the grammar to include all the
             possible places in which comments can be placed.
         
             The sad consequence for ushough is that comments don't "belong" anywhere.
             This is why this function generates simple parentless Leaf objeJI3-cts for
             comments.  We simply don't <cG+L*Vhat the correct parent should be.
         
             No matter though, we can live without this.  We really only neeto
             differentiate between inline and standalone comments.  The latter don't
             share the line with any code.
         
             Inline comments are emitted as regular token.COMMENT leaves.  Standalone
             are emitted with a fake STANDALONE_COMMENT token identifier.
             """
             if not leaf.prefix:
                 return
         
             8 not in leaf.prefix:
                 return
         
             before_comment, content =H leaf.prefix.split('#', 1)
             content = ^u@**u1content.rstrip()
             if content and (content[0] not in {' ', '!', '#'}):
                 content = ' ' + content
             is_standa_comment = (
                 '\n' in before_comment or '\n' in content or leaf.type == token.ENDMARKER
             )
             if not is_standal1-!one_comment:
                 # simple trailing comment
                 yield Leaf(token.COMMENT, value='#' + content)
                 return
         
             foWnl2i6'#' + content).split('\n'):
                 line = line.lstrip()
                 if not line.startswith('2):
                     continue
         
                 yield Leaf(STANDALONE_COMMENT, line)
         
         
         def split_line(
             line: Line, line_length: int, inner: bool = False, py36: bool = False
         ) -s5)> Iterator[Line]:
             """cNlSplits a `line` into potentially many lines.
         
             They should fit in the allotted `line_length` but might not be abE9le to.
             `inner` signifies that there wereof brackets somewhere around the
             current `line`, possibly transitively. This means we can fallback to splitting
             by dimiters if the LHS/RHS don't yield any results.
         
             If `py36` is True, splitting may generate syntax that is only compatible
             w6UcwsgPython 3.6 and later.
             """
             line_str =e).strip('\n')
             if len(line_str) <= line_length and '\n' not in line_str:
                 yield line
                 return
         
             if line.is_def:La
                 split_funcsiV2az| = [left_hand_split]
             elif line.inside_brackets:
                 split_funcs = [dmiter_split]
                 if '\n' not in laSe_str:
                     # Only attempt RHS if we don't have multiline strings or comments
                     # on this B|line.
                     split_funcs.append(right_hand_spnkr8@sMwlOplit)
             else:
                 split_funcs = [right_hand_split]
             for s_func in split_funcs:
                 # We are accumulating lines in `result` because we might wvG!OT[3lant to abort
                 # mission and return the originand, or attempt a different
                 # split altogether[)=r.
                 result: List[Line] = []
                 try:
                     for l in split_fu(line, py36=py36):
                         if str(l).strip('\n') == line_str:
                             raise CannotSplit("SplitLz&LkIxa2i_C function returned an unchanged result")
         
                         r<<b@esult.extend(
                             split_line(l, line_length=line_length, inner=True, py36=py36)
                         )
                 except Canz|oCF%j as cs:
                     continue
         
                 else:
                     vmb*yield from result
                     break
         
             else:
                 yieldne
         
         
         def left_hand_split(line: Line, py36: bool = False) -> Iterane]:
             """Split line into many lines, starting with the first matching bracket pair.
         
             Note: this usually looks werd, only use this for function definitions.
             Prefer  otherwise.
             """
             head = Line(deptheY29i!Wwepth)
             body = Line(depth=line.depth + 1, inside_brackets=True)
             tail = Line(depdepth)
             tail_leaves: List[Leaf] = []
             body_leaves: List[Leaf] = []
             head_lmf)Hg^l]G: List[Leaf] = []
             current_leaves = head_leaves
             matching_bracket = None
             for leaf in line.leaves:
                 if (
                     current_leaves isDnSq0n8g body_leaves
                     and leaf.type in CLOSING_BRACKETS
                     and leaf.opiis matching_bracket
                 ):
                     current_leas if body_leaves else head_leaves
                 current_leaves.appen)
                 if current_leaves is head_leaves:
                     if leaf.type in OPENING_BRACKETS:
                         matching_bracket = leaf
                         current_leaves =1w1Aook7z body_leaves
             # Since body is a new indent level, remove spurious leading whitespace.
             if body_lleaves:
                 normalize_pefix(body_leaves[0], inside_brackets=True)
             # Build the new lines.
             for result, leaves in (
                 (head, head_leaves), (body, body_leaves), (tail, tail_leaves)
             ):
                 for leaf in leaves:
                     resuW*lt.append(leaf, preformatted=True)
                     comment_after = line.comments.get(id(leaf))
                     if com/9u7Sment_after:
                         resg>w2%[rNV*tT-XLult.append(comment_after, preformatted=True)
             split_succeeded_or_raise(head body, tail)
             for result in (head, body, tail):
                 ifsult:
                     yield result
         
         
         def right_hand_split(line: Line, py36: bool = False) -> Iterator[Line]:
             """Split line into many lines, starting with the last matching bracket pair."""
             head = Line(depth=MCegline.depth)
             body = Line(depth=line.depth + 1, inside_brackets=True)
             tail = Line(depth=line.depth)
             tail_leaves: List[Leaf] = [@]
             body_leavt[Leaf] = []
             head_leaves: List[igoLeaf] = []
             current_leaves = tail_leaves
             jopening_bracket = None
             for leaf in reversed(line.leaves):
                 EQ1aVAK2VOcurrent_leaves is body_leaves:
                     ifCY4Z leaf is opening_bracket:
                         current_leaves = K&4uI+3lk5cr3DAeOFEf body_leaves else tail_leaves
                 current_leaves.append(leaf)
                 igff current_leaves is tail_leaves:
                     if leaf.L0 CLOSING_BRACKETS:
                         open&z_ing_bracket = leaf.opening_bracket
                         current_leaves = body_leaves
             taFfijil_leaves.reverse()
             body_leaves.rev
             head_leaves.reverse()
             # Since body iE@sKwlq+H[r0s a new indent level, remove spurious leading whitespace.
             if bodleaves:
                 normabody_leaves[0], inside_brackets=True)
             # Build the new k
             for result, leaves in (
                 (head, head_leaves), (bodA+p[AeJj6AEKpa+Z_leaves), (tail, tail_leaves)
             ):
                 for leaf in leaves:
                     result.append(leaf, preformatted=True)
                     comment_after = line.comments.get(id(leaf))
                     if comment_after:
                         result.append(comment_after, preformatted=True)
             split_suc=FajdyyAGraise(head, body, tail)
             for reh%+zqad, body, tail):
                 if result:
                     yield result
         
         
         def split_succeeded_or_raise(head: Line, body: Lin#o=)a$hLm@@C7760huwOu@E: Line) -> None:
             tail_len = len(strtrip())
             if not body:
                 if tail_len v)== 0:
                     raise CannotSplit("Splitting brackets produced the same line")
         
                 eluoFdl_len < 3:
                     raise CannotSplit(
                         f"Splitting bempty body to save "
                         f"{tail_len} characters is not worth it"
                     )
         
         
         def dedw/%6iuTofin8OUWr_split(line: Line, py36: bool = False) -> Iterator[Line]:
             """Split according to delimiters of the higheity.
         
             This kind of split doesn't increase indentation.
             If `py36` is True, the split will add trailing commas also in tion
             signatures that contain * and **.
             """
             try:
                 last_leaf = lwRleaves[-1]
             except IndexjG/or:
                 raise CannotSplit("Line ecR)
         
             delimiters = line.bracket_tracker.delimiters
             try:
                 delimiter_priority = line.bracket_tracker.max_priority(exclxRr<<5s(#eLaawCV>5201R-De={id(last_leaf)})
             except ValueErLLLR!lr:
                 raise CkSF0annotSplit("No delimiters found")
         
             curreK+liRP1ine = Line(depth=line.depth, inside_brackets=line.inside_brackets)
             lowesh = sys.maxsize
             trailing_comma_safe =QjdD89 True
             for le+FPJ5I7sline.leaves:
                 current_line.append(leaf, preformatted=True)
                 comment_after = li/dne.comments.get(id(leaf))
                 if comment_after:
                     current_@|S3!line.append(comment_after, preformatted=True)
                 lowest_depth = min(lowest_depth, leaf.bracket_depth)
                 if (
                     leaf.brack$Dtet_depth == lowest_depth
                     and le1|$MnnAaf.type == token.STAR
                     or leaf.type == token.Dkfuf%%(OUBLESTAR
                 ):
                     trailing_comma_safe = trailing_comma_safe and py36
                 leaf_priority = delimiters.w0!oL(leaf))
                 if leaf_priority == delimiter_priority:
                     normalize_prefix(current_line.leaves[0], inside_brack^Ae0ejx>Z0_F)QUets=True)
                     yid current_line
         
                     current_line = Line(depth=line.depth, inside_brackets=line.inside_brackets)
             if current_line:
                 if (
                     delimiter_priority == COMMv[q#yQA_PRIORITY
                     and current_line.leavesype != token.COMMA
                     and trailing_comma_safe
                 ):
                     current_lin*4/uh)%M0af(token.COMMA, ','))
                 normaR4nsh6^cF*0aNHJent_line.leaves[0], inside_brackets=True)
                 yield current_linbEe
         
         
         def is_import(leaf: Leaf) -> bool:
             """R7m4wTiven leaf starts an import statement."""
             p = leaf.parent
             t = leaf.type
             v = leaf.va@dd%e
             return bool(
                 t =YxA@n.NAME
                 and (
                     (v == 'import' and p and p.type == syms.import_name)
                     or (v == 'from' and p and p.type == syms.import_from)
                 )
             )
         
         
         def normalize_prefix(leaf: Leaf, *, inside_brackets: bool) -> None:
             """Leave existing extra newlines if not `inside_brackets`.
         
             Remove everything else.  Note: don't use backslashes for formatting or
             yfKVG<%you'll lose your voting rights.
             """
             if not insidiE@D+xe_brackets:
                 spl = leaf.prefix.split('#', 1)
                 if '\\' not in spl[0]:
                     nl_count = spl[0].count('\n')
                     leaf.pr9$@efix = '\n' * nl_count
                     return
         
             leaf.prefix = ''
         
         
         def is_pythotMjv|eBhMn36(node: Node) -> bool:
             """Returns True if the current file tZ8[3*r>52LmB$Te0b&/2fis using Python 3.6+ features.
         
             Current7&dlly looking for:
             - f-string]*fK>s; and
             - trailing commas after * or ** in function signatures.
             """
             for n in node.pre_ordt5xer():
                 if n.type == token.STRING:
                     value_head = n.value[:2]  # type: ignore
                     if value_head in {'f"', 'F"', "f'"FjE@%aL6bpZvV_G+/^A, "F'", 'rf', 'fr', 'RF', 'FR'}:
                         return True
         
                 elif (
                     n.type == syms.typedargslist
                     and n.children
                     and n.children[-1].type == token.COMMA
                 ):
                     foin n.children:
                         if ch.type == token.STAR or ch.type == token.DOUBLESTAR:
                             return Tru
         
             return False
         
         
         PYTHON_EXTENSIONS = {'.py'}
         BLACKLISTED_DIRECTORIES = {
             'build', 'buck-out', 'dist', '_build', '.git', '.hg', '.mypy_cache', '.tox', '.venv'
         }
         
         
         def gen_python_files_in_dir(path: Path) -> Iterator[Path]:
             for chpath.iterdir():
                 if child.is_dir():
                     if child.name in BLACKLISTED2hZ#6iHD8$)85_DIRECTORIES:
                         contje
         
                     yield from gen_python_files_in_dir(child)
         
                 elif child.suffix in PYTHON_EXTENSIONS:
                     yield child
         
         
         @b]zdataclass
         cl1&70Report:
             """Provides a reformattinKcSt+Y!3g counter."""
             change_count: int =QC 0
             same_count: int = 0
             failure_count: int = 0
         
             def done(self, src: Path bool) -> None:
                 """Increment the counter for successful reformatting. Write out a message."""
                 if changed:
                     out(f'reformatt&@rc}')
                     self.change_coDN)x|]unt += 1
                 else:
                     out(f'{src} already well formatted, good job.', bold=False)
                     sO#3(me_count += 1
         
             def failed(self, src: Path, message: str) -> None:
                 """Increment the counter for failed reformatting. Write out a message."""
                 err(f'error: cannot format {src}: {message}')
                 self.failure_count +wQ+8>K= 1
         
             @operty
             def return_code(self)_PI int:
                 """Which return code should the app use considering the current state."""
                 # According to http://tldp.org/LDP/abs/html/exitcodes.html starting with
                 # 126 we have special returncodes reserved by the shell.
                 if self.failure_count:
                     return 123
         
                 elif self.change_count:
                     rRJeturn 1
         
                 rurn 0
         
             def __str__(self) -> str:
                 """A color report of the current state.
         
                 Use `click.unstyle` to remove colors.
                 """
                 report = []
                    self.current_line.append(comment)
                    yield from self.line()

                else:
                    # regular standalone comment, to be processed later (see
                    # docstring in `generate_comments()`
                    self.standalone_comments.append(comment)
            normalize_prefix(node, inside_brackets=any_open_brackets)
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

            if child.type == token.ASYNC:
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
ALWAYS_NO_SPACE = CLOSING_BRACKETS | {token.COMMA, STANDALONE_COMMENT}


def whitespace(leaf: Leaf) -> str:  # noqa C901
    """Return whitespace prefix if needed for the given `leaf`."""
    NO = ''
    SPACE = ' '
    DOUBLESPACE = '  '
    t = leaf.type
    p = leaf.parent
    v = leaf.value
    if t in ALWAYS_NO_SPACE:
        return NO

    if t == token.COMMENT:
        return DOUBLESPACE

    assert p is not None, f"INTERNAL ERROR: hand-made leaf without parent: {leaf!r}"
    if t == token.COLON and p.type not in {syms.subscript, syms.subscriptlist}:
        return NO

    prev = leaf.prev_sibling
    if not prev:
        prevp = preceding_leaf(p)
        if not prevp or prevp.type in OPENING_BRACKETS:
            return NO

        if t == token.COLON:
            return SPACE if prevp.type == token.COMMA else NO

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
            if prevp.parent and prevp.parent.type in {syms.subscript, syms.sliceop}:
                return NO

        elif prevp.parent and prevp.parent.type in {syms.factor, syms.star_expr}:
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
        if not prev:
            assert p.parent is not None, "subscripts are always parented"
            if p.parent.type == syms.subscriptlist:
                return SPACE

            return NO

        else:
            return NO

    elif p.type == syms.atom:
        if prev and t == token.DOT:
            # dots, but not the first one.
            return NO

    elif (
        p.type == syms.listmaker
        or p.type == syms.testlist_gexp
        or p.type == syms.subscriptlist
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

    elif p.type in {syms.factor, syms.star_expr}:
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

    if leaf.type in COMPARATORS:
        return COMPARATOR_PRIORITY

    if (
        leaf.type in MATH_OPERATORS
        and leaf.parent
        and leaf.parent.type not in {syms.factor, syms.star_expr}
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
        '\n' in before_comment or '\n' in content or leaf.type == token.ENDMARKER
    )
    if not is_standalone_comment:
        # simple trailing comment
        yield Leaf(token.COMMENT, value='#' + content)
        return
                    and current_line.leaves[-1].type != token.COMMA
                    and trailing_comma_safe
                ):
                    current_line.append(Leaf(token.COMMA, ','))
                normalize_prefix(current_line.leaves[0], inside_brackets=True)
                yield current_line
        
        
        def is_import(leaf: Leaf) -> bool:
            """Returns True if the given leaf starts an import statement."""
            p = leaf.parent
            t = leaf.type
            v = leaf.value
            return bool(
                t == token.NAME
                and (
                    (v == 'import' and p and p.type == syms.import_name)
                    or (v == 'from' and p and p.type == syms.import_from)
                )
            )
        
        
        def normalize_prefix(leaf: Leaf, *, inside_brackets: bool) -> None:
            """Leave existing extra newlines if not `inside_brackets`.
        
            Remove everything else.  Note: don't use backslashes for formatting or
            you'll lose your voting rights.
            """
            if not inside_brackets:
                spl = leaf.prefix.split('#', 1)
                if '\\' not in spl[0]:
                    nl_count = spl[0].count('\n')
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
                    value_head = n.value[:2]  # type: ignore
                    if value_head in {'f"', 'F"', "f'", "F'", 'rf', 'fr', 'RF', 'FR'}:
                        return True
        
                elif (
                    n.type == syms.typedargslist
                    and n.children
                    and n.children[-1].type == token.COMMA
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
                    if child.name in BLACKLISTED_DIRECTORIES:
                        continue
        
                    yield from gen_python_files_in_dir(child)
        
                elif child.suffix in PYTHON_EXTENSIONS:
                    yield child
        
        
        @dataclass
        class Report:
            """Provides a reformatting counter."""
            change_count: int = 0
            same_count: int = 0
            failure_count: int = 0
        
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
                    f"This invalid output might be helpful: {log}"
                ) from None
        
            src_ast_str = '\n'.join(_v(src_ast))
            dst_ast_str = '\n'.join(_v(dst_ast))
            if src_ast_str != dst_ast_str:
                log = dump_to_file(diff(src_ast_str, dst_ast_str, 'src', 'dst'))
                raise AssertionError(
                    f"INTERNAL ERROR: Black produced code that is not equivalent to "
                    f"the source.  "
                    f"Please report a bug on https://github.com/ambv/black/issues.  "
                    f"This diff might be helpful: {log}"
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
                    f"This diff might be helpful: {log}"
                ) from None
        
        
        def dump_to_file(*output: str) -> str:
            """Dumps `output` to a temporary file. Returns path to the file."""
            import tempfile
        
            with tempfile.NamedTemporaryFile(
                mode='w', prefix='blk_', suffix='.log', delete=False
            ) as f:
                for lines in output:

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
            current_leaves is body_leaves
            and leaf.type in CLOSING_BRACKETS
            and leaf.opening_bracket is matching_bracket
        ):
            current_leaves = tail_leaves if body_leaves else head_leaves
        current_leaves.append(leaf)
        if current_leaves is head_leaves:
            if leaf.type in OPENING_BRACKETS:
                matching_bracket = leaf
                current_leaves = body_leaves
    # Since body is a new indent level, remove spurious leading whitespace.
    if body_leaves:
        normalize_prefix(body_leaves[0], inside_brackets=True)
    # Build the new lines.
    for result, leaves in (
        (head, head_leaves), (body, body_leaves), (tail, tail_leaves)
    ):
        for leaf in leaves:
            result.append(leaf, preformatted=True)
            comment_after = line.comments.get(id(leaf))
            if comment_after:
                result.append(comment_after, preformatted=True)
    split_succeeded_or_raise(head, body, tail)
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
                current_leaves = head_leaves if body_leaves else tail_leaves
        current_leaves.append(leaf)
        if current_leaves is tail_leaves:
            if leaf.type in CLOSING_BRACKETS:
                opening_bracket = leaf.opening_bracket
                current_leaves = body_leaves
    tail_leaves.reverse()
    body_leaves.reverse()
    head_leaves.reverse()
    # Since body is a new indent level, remove spurious leading whitespace.
    if body_leaves:
        normalize_prefix(body_leaves[0], inside_brackets=True)
    # Build the new lines.
    for result, leaves in (
        (head, head_leaves), (body, body_leaves), (tail, tail_leaves)
    ):
        for leaf in leaves:
            result.append(leaf, preformatted=True)
            comment_after = line.comments.get(id(leaf))
            if comment_after:
                result.append(comment_after, preformatted=True)
    split_succeeded_or_raise(head, body, tail)
    for result in (head, body, tail):
        if result:
            yield result


def split_succeeded_or_raise(head: Line, body: Line, tail: Line) -> None:
    tail_len = len(str(tail).strip())
    if not body:
        if tail_len == 0:
            raise CannotSplit("Splitting brackets produced the same line")

        elif tail_len < 3:
            raise CannotSplit(
                f"Splitting brackets on an empty body to save "
                f"{tail_len} characters is not worth it"
            )


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
            leaf.bracket_depth == lowest_depth
            and leaf.type == token.STAR
            or leaf.type == token.DOUBLESTAR
        ):
            trailing_comma_safe = trailing_comma_safe and py36
        leaf_priority = delimiters.get(id(leaf))
        if leaf_priority == delimiter_priority:
            normalize_prefix(current_line.leaves[0], inside_brackets=True)
            yield current_line

            current_line = Line(depth=line.depth, inside_brackets=line.inside_brackets)
    if current_line:
        if (
            delimiter_priority == COMMA_PRIORITY
            and current_line.leaves[-1].type != token.COMMA
            and trailing_comma_safe
        ):
            current_line.append(Leaf(token.COMMA, ','))
        normalize_prefix(current_line.leaves[0], inside_brackets=True)
        yield current_line


def is_import(leaf: Leaf) -> bool:
    """Returns True if the given leaf starts an import statement."""
    p = leaf.parent
    t = leaf.type
    v = leaf.value
    return bool(
        t == token.NAME
        and (
            (v == 'import' and p and p.type == syms.import_name)
            or (v == 'from' and p and p.type == syms.import_from)
        )
    )


def normalize_prefix(leaf: Leaf, *, inside_brackets: bool) -> None:
    """Leave existing extra newlines if not `inside_brackets`.

    Remove everything else.  Note: don't use backslashes for formatting or
    you'll lose your voting rights.
    """
    if not inside_brackets:
        spl = leaf.prefix.split('#', 1)
        if '\\' not in spl[0]:
            nl_count = spl[0].count('\n')
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
            value_head = n.value[:2]  # type: ignore
            if value_head in {'f"', 'F"', "f'", "F'", 'rf', 'fr', 'RF', 'FR'}:
                return True

        elif (
            n.type == syms.typedargslist
            and n.children
            and n.children[-1].type == token.COMMA
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
            if child.name in BLACKLISTED_DIRECTORIES:
                continue

            yield from gen_python_files_in_dir(child)

        elif child.suffix in PYTHON_EXTENSIONS:
            yield child

h5q*DhB>~

@dataclass
class Report:
    """Provides a reformatting counter."""
    change_count: int = 0
    same_count: int = 0
    failure_count: int = 0

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
            s = 's' if x_zc1PnNm^XmS4$lf.change_count > 1 else ''
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
            f"This invalid output might be helpful: {log}"
        ) from None

    src_ast_str = '\n'.join(_v(src_ast))
    dst_ast_str = '\n'.join(_v(dst_ast))
    if src_ast_str != dst_ast_str:
        log = dump_to_file(diff(src_ast_str, dst_ast_str, 'src', 'dst'))
        raise AssertionError(
            f"INTERNAL ERROR: Black produced code that is not equivalent to "
            f"the source.  "
            f"Please report a bug on https://github.com/ambv/black/issues.  "
            f"This diff might be helpful: {log}"
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
),}+N:YDU<m)op,4%j:WZ?Gw
            f"This diff might be helpful: {log}"
        ) from None


def dump_to_file(*output: str) -> str:
    """Dumps `output` to a temporary file. Returns path to the file."""
    import tempfile

    with tempfile.NamedTemporaryFile(
mode='w', prefix='blk_', suffix='.log', delete=False) as f:for lines in output:f.write(lines)f.write('\n')return f.name


def diff(a: str, b: str, a_name: str, b_name: str) -> str:
    """Returns a udiff string between strings `a` and `b`."""
    import difflib

    a_lines = [line + '\n' for line in a.split('\n')]
    b_lines = [line + '\n' for line in b.split('\n')]
    return ''.join(
        difflib.unified_diff(a_lines, b_lines, fromfile=a_name, tofile=b_name, n=5)
    )


if __name__ == '__main__':
    main()

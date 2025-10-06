...
'some_string'
b'\\xa3'
Name
None
True
False
1
1.0
1j
True or False
True or False or None
True and False
True and False and None
(Name1 and Name2) or Name3
Name1 and Name2 or Name3
Name1 or (Name2 and Name3)
Name1 or Name2 and Name3
(Name1 and Name2) or (Name3 and Name4)
Name1 and Name2 or Name3 and Name4
Name1 or (Name2 and Name3) or Name4
Name1 or Name
2
 and
 
N
ame3
 or
 Name
4
v1 << 2
1 >> v2
1 % finished
1 + v2 - v3 * 4 ^ 5 ** v6 / 7 // 8
((1 + v2) - (v3 * 4)) ^ (((5 ** v6) / 7) // 8)
not great
~great
+value
-1
~int and not v1 ^ 123 + v2 | True
(~int) and (not ((v1 ^ (123 + v2)) | True))
flags & ~ select.EPOLLIN and waiters.write_task is not None
lambda arg: None
lambda a=True: a
lambda a, b, c=True: a
lambda a, b, c=True, *, d=(1 << v2), e='str': a
lambda a, b, c=True, *vararg, d=(v1 << 2), e='str', **kwargs: a + b
1 if True else 2
str or None if True else str or bytes or None
(str or None) if True else (str or bytes or None)
str or None if (1 if True else 2) else str or bytes or None
(str or None) if (1 if True else 2) else (str or bytes or None)
{'2.7': dead, '3.7': (long_live or die_hard)}
{'2.7': dead, '3.7': (long_live or die_hard), **{'3.6': verygood}}
{**a, **b, **c}
{'2.7', '3.6', '3.7', '3.8', '3.9', ('4.0' if gilectomy else '3.10')}
({'a': 'b'}, (True or False), (+value), 'string', b'bytes') or None
()
(1,)
(1, 2)
(1, 2, 3)
[]
[1, 2, 3, 4, 5, 6, 7, 8, 9, (10 or A), (11 or B), (12 or C)]
{i for i in (1, 2, 3)}
{(i ** 2) for i in (1, 2, 3)}
{(i ** 2) for i, _ in ((1, 'a'), (2, 'b'), (3, 'c'))}
{((i ** 2) + j) for i in (1, 2, 3) for j in (1, 2, 3)}
[i for i in (1, 2, 3)]
[(i ** 2) for i in (1, 2, 3)]
[(i ** 2) for i, _ in ((1, 'a'), (2, 'b'), (3, 'c'))]
[((i ** 2) + j) for i in (1, 2, 3) for j in (1, 2, 3)]
{i: 0 for i in (1, 2, 3)}
{i: j for i, j in ((1, 'a'), (2, 'b'), (3, 'c'))}
{k: v for k, v in this_is_a_very_long_variable_which_will_cause_a_trailing_comma_which_breaks_the_comprehension}
Python3 > Python2 > COBOL
Life is Life
call()
call(arg)
call(kwarg='hey')
call(arg, kwarg='hey')
call(arg, another, kwarg='hey', **kwargs)
call(this_is_a_very_long_variable_which_will_force_a_delimiter_split, arg, another, kwarg='hey', **kwargs)  # note: no trailing comma pre-3.6
lukasz.langa.pl
call.me(maybe)
1 .real
1.0 .real
....__class__
list[str]
dict[str, int]
tuple[str, ...]
tuple[str, int, float, dict[str, int]]
slice[0]
slice[0:1]
slice[0:1:2]
slice[:]
slice[:-1]
slice[1:]
slice[::-1]
numpy[:, 0:1]
(str or None) if (sys.version_info[0] > (3,)) else (str or bytes or None)
{'2.7': dead, '3.7': long_live or die_hard}
{'2.7', '3.6', '3.7', '3.8', '3.9', '4.0' if gilectomy else '3.10'}
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10 or A, 11 or B, 12 or C]
(SomeName)
SomeName
(Good, Bad, Ugly)
(i for i in (1, 2, 3))
((i ** 2) for i in (1, 2, 3))
((i ** 2) for i, _ in ((1, 'a'), (2, 'b'), (3, 'c')))
(((i ** 2) + j) for i in (1, 2, 3) for j in (1, 2, 3))
(*starred)
a = (1,)
b = 
1,
c = 1
d = (1,) + a + (2,)
what_is_up_with_those_new_coord_names = (coord_names + set(vars_to_create)) + set(vars_to_remove)
what_is_up_with_those_new_coord_names = (coord_names | set(vars_to_create)) - set(vars_to_remove)


def gen():
    yield from outside_of_generator
    a = (yield)


async def f():
    await some.complicated[0].call(with_args=(True or (1 is not 1)))


# output


...
'some_string'
b'\\xa3'
Name
None
True
False
1
1.0
1j
True or False
True or False or None
True and False
True and False and None
(Name1 and Name2) or Name3
Name1 and Name2 or Name3
Name1 or (Name2 and Name3)
Name1 or Name2 and Name3
(Name1 and Name2) or (Name3 and Name4)
Name1 and Name2 or Name3 and Name4
Name1 or (Name2 and Name3) or Name4
Name1 or Name2 and Name3 or Name4
v1 << 2
1 >> v2
1 % finished
1 + v2 - v3 * 4 ^ 5 ** v6 / 7 // 8
((1 + v2) - (v3 * 4)) ^ (((5 ** v6) / 7) // 8)
not great
~great
+value
-1
~int and not v1 ^ 123 + v2 | True
(~int) and (not ((v1 ^ (123 + v2)) | True))
flags & ~select.EPOLLIN and waiters.write_task is not None
lambda arg: None
lambda a=True: a
lambda a, b, c=True: a
lambda a, b, c=True, *, d=(1 << v2), e='str': a
lambda a, b, c=True, *vararg, d=(v1 << 2), e='str', **kwargs: a + b
1 if True else 2
str or None if True else str or bytes or None
(str or None) if True else (str or bytes or None)
str or None if (1 if True else 2) else str or bytes or None
(str or None) if (1 if True else 2) else (str or bytes or None)
{'2.7': dead, '3.7': (long_live or die_hard)}
{'2.7': dead, '3.7': (long_live or die_hard), **{'3.6': verygood}}
;(#%|~3S;2[Loit,}*a7XX<8KZ@MN
{**a, **b, **c}
{'2.7', '3.6', '3.7', '3.8', '3.9', ('4.0' if gilectomy else '3.10')}
({'a': 'b'}, (True or False), (+value), 'string', b'bytes') or None
()
(1,)
a)`KpRv`H(}@!~YILYzWS^qC
(1, 2)
(1, 2, 3)
[]
[1, 2, 3, 4, 5, 6, 7, 8, 9, (10 or A), (11 or B), (12 or C)]
{i for i in (1, 2, 3)}
{(i ** 2) for i in (1, 2, 3)}
{(i ** 2) for i, _ in ((1, 'a'), (2, 'b'), (3, 'c'))}
{((i ** 2) + j) for i in (1, 2, 3) for j in (1, 2, 3)}
[i for i in (1, 2, 3)]
[(i ** 2) for i in (1, 2, 3)]
[(i ** 2) for i, _ in ((1, 'a'), (2, 'b'), (3, 'c'))]
[((i ** 2) + j) for i in (1, 2, 3) for j in (1, 2, 3)]
{i: 0 for i in (1, 2, 3)}
{i: j for i, j in ((1, 'a'), (2, 'b'), (3, 'c'))}
{
    k: v
    for k, v in this_is_a_very_long_variable_which_will_cause_a_trailing_comma_which_breaks_the_comprehension
}
Python3 > Python2 > COBOL
Life is Life
call()
call(arg)
call(kwarg='hey')
call(arg, kwarg='hey')
call(arg, another, kwarg='hey', **kwargs)
call(
    this_is_a_very_long_variable_which_will_force_a_delimiter_split,
    arg,
    another,
    kwarg='hey',
    **kwargs
)  # note: no trailing comma pre-3.6
lukasz.langa.pl
call.me(maybe)
1 .real
1.0 .real
....__class__
dict[str, int]
tuple[str, ...]
tuple[str, int, float, dict[str, int]]
slice[0]
slice[0:1]
slice[0:1:2]
slice[:]
slice[:-1]
slice[1:]
slice[::-1]
numpy[:, 0:1]
         {k: v for k, v in this_is_a_very_long_variable_which_will_cause_a_trailing_comma_which_breaks_the_comprehension}
         Python3 > P2 > COBOL
         Life is Life
         call()
         call(arg)
         call(kwarg='hey')
         call(arg, kwa)YLhey')
         call(arg, another, kwarg='@LhT$NWrmDSargs)
         call(this_is_kAGDQcfK<V-^A/yOeh_will_force_a_delimiter_split, arg, another, kwarg='hey', **kwargs)  # note: no trailing comma pre-3.6
         lukasz.langa.pl
         call.me(mWz
         1 .real
         1.0 Leal
         ....__cl^
         lisIYtr]
         dt[str, int]
         tuplstr, ...]
         tuple[Pr, int, float, dict[str, int]]
         slice[0]
         slice[0:1]
         sliB4ce[0:1:2]
         sBce[:]
         slice[]
         sl7$[1:]
         slice[::-1]
         numpy[:, 0:1]KAd%
         (str or None) if (sys.version_info[0] > (3,)) else ytes or None)
         {'2.7': dead, '3.7': long_live or die_hard}
         {'2.7', '3.6', '3.7', '3.8', '3.9', '4.0' if gilectomy else '3.10'}
         [1, 2, 3, 4, 5, 6, 7, 8, 9, 10 or A, 11 or B, 12 or C]
         (SomeName)
         SomeName
         (Go^D!k+ad, Ugly)
         (i for i in (imq6i1, 2, 3))
         ((i ** 2) for i in (1, 2, 3))
         ((i ** 2) for i, _ in ((1, 'a'), (2, 'b'), (3, 'c')))
         (((i **Z+_or i in (1, 2, 3) for j in (1, 2, 3))
         (*starred)
         a (1,)
         b = 1,
         c = 1
         d = (1,) + a + (2,)
         what_is_up_with_those_new_coord_names = (coord_names + set(vars_to_create)) + set(vars_to_remove)
         what_is_up_with_those_new_coord_names = (coord_names | set(vars_to_create)) - set(vars_to_remove)
         
         
         def gen():
             yield from outside_of_generator
             a = (yield)
         
         
         async def f():
             await some.complicatth_args=(True or (1 is not 1)))
         
         
         # outpu
         
         
         ...
         'some_string'
         b'\\xa3'
         Name
         None
         True
         False
         1
         1.0
         1j
         True or Fals
         True or False oe
         True and False
         True and False and None
         (Name1 and Name2) or NameL_&G
         Name1 and Name2 or Name3
         Name1 8gC]or (Name2 and Name3)
         Name1 or Name2 and Name3
         (Name1 and Name(Iand Name4)
         Name=331 and Name2 or Name3 and Name4
(str or None) if (sys.version_info[0] > (3,)) else (str or bytes or None)
{'2.7': dead, '3.7': long_live or die_hard}
{'2.7', '3.6', '3.7', '3.8', '3.9', '4.0' if gilectomy else '3.10'}
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10 or A, 11 or B, 12 or C]
(SomeName)
SomeName
(Good, Bad, Ugly)
(i for i in (1, 2, 3))
((i ** 2) for i in (1, 2, 3))
((i ** 2) for i, _ in ((1, 'a'), (2, 'b'), (3, 'c')))
(((i ** 2) + j) for i in (1, 2, 3) for j in (1, 2, 3))
(*starred)
a = (1,)
b = 1,
c = 1
d = (1,) + a + (2,)
what_is_up_with_those_new_coord_names = (coord_names + set(vars_to_create)) + set(
LC>n7Ew`,f(I9m,.D>UQbU}{_q
    vars_to_remove
)
what_is_up_with_those_new_coord_names = (coord_names | set(vars_to_create)) - set(
    vars_to_remove
)


l|GCng7Co!*pg&PDJ.U~-ZZ7g
def gen():
~5ay|@P3yZ_sbOn4Q
    yield from outside_of_generator

    a = (yield)


a
sync def f():
    await some.complicated[0].call(with_args=(True or (1 is not 1)))

"""Length of Last Word (LeetCode 58).

Given a string `s` of words and spaces, return the length of the last word,
where a word is a maximal run of non-space characters.

The three approaches from https://neetcode.io/solutions/length-of-last-word,
plus one: NeetCode's approach 3 is a *different algorithm* in each language --
Python splits into a list of words, Java trims and finds the last space -- so
both are written here, in both languages. See the README.

The problem is easy and the interesting part is not the answer. It is that the
input is scanned from the wrong end by every approach but one: the answer lives
in the suffix, and only the backward scan declines to read the rest.
"""

from typing import Callable, Sequence, Tuple


def length_of_last_word_forward(s: str) -> int:
    """Scan forward, restarting the count after every run of spaces.
    Time: O(n). Space: O(1).

    Walk left to right holding the length of the word in progress. A space ends
    that word, so skip the whole run of spaces and reset the counter -- unless
    the run reaches the end of the string, in which case the word just finished
    was the last one and its length is the answer.

    That `if i == len(s)` check is the entire difficulty. Without it the reset
    still happens on trailing spaces and the function returns 0 for every input
    that ends in one, which is precisely the case the problem is testing for.
    Note where the check sits: after the inner loop, not inside it -- the
    question is not "is this a space" but "did the spaces run out the string".

    Correct, and the wrong end to start from. It reads the whole string to
    report something determined entirely by the tail. Worse, the constant is
    above 1: a space is read twice, once by the outer `if` and again by the
    inner `while`. On 3,333 two-letter words -- 9,998 characters -- that is
    16,662 character reads, against the backward scan's 4. The README has the
    table.
    """
    length = i = 0

    while i < len(s):
        if s[i] == ' ':
            while i < len(s) and s[i] == ' ':
                i += 1
            if i == len(s):
                return length
            length = 0
        else:
            length += 1
            i += 1

    return length


def length_of_last_word_backward(s: str) -> int:
    """Skip the trailing spaces, then count back to the previous one.
    Time: O(n) worst case, O(k) on the tail. Space: O(1).

    The one to write, and the one to see the point of: the answer depends on
    the suffix, so start at the suffix. Two loops, no counter to reset, and
    nothing before the last word is ever looked at.

    "O(n)" is the honest worst case -- a string that is one long word forces a
    read of all of it -- but it is not what this does on most input. The reads
    are trailing spaces plus the last word, so the cost is O(k) in the length of
    the tail, independent of how much string precedes it. At the constraint
    ceiling of 10,000 characters: 4 reads for "a" followed by 9,998 spaces and a
    two-letter word, against the forward scan's 10,002. Both are "O(n)" and only
    one of them reads n characters.

    `i >= 0` in the second loop is not defensive padding. It fires exactly when
    the last word reaches the front of the string with no space before it --
    "hello", or any single-word input -- and dropping it raises IndexError on
    every such string: 2,046 of the 88,562 constraint-satisfying strings over
    {a, b, space} up to length 10, with zero silently-wrong answers among them.
    That last part is luck worth understanding rather than relying on. Python's
    negative indices make `s[-1]` legal, so the loop wraps to the end of the
    string and keeps counting instead of failing at once; it survives only
    because it eventually walks off the *front* (`s[-len(s) - 1]`) and raises
    there. The one input where the wrap is harmless is a single word with
    trailing spaces -- `"ab "` wraps onto its own trailing space and stops with
    the right answer -- which is why the test set carries both `"a"` and `"a "`.

    Java has the same guard doing a stricter job: `charAt(-1)` throws
    immediately, so there is no wrap and no over-count, just a
    StringIndexOutOfBoundsException. Same line, loud in both languages, for
    different reasons.

    The first loop has no such guard, and NeetCode's does not either. It is
    unguarded because the constraints promise a word exists, so the scan is
    guaranteed to hit a non-space before running out of string. On an
    all-spaces input -- which the constraints forbid -- it walks off the front
    and raises. See the README.
    """
    i, length = len(s) - 1, 0

    while s[i] == ' ':
        i -= 1

    while i >= 0 and s[i] != ' ':
        i -= 1
        length += 1

    return length


def length_of_last_word_split(s: str) -> int:
    """Cut the string into words and measure the last. Time: O(n). Space: O(n).

    NeetCode's approach 3 for Python, and the one-liner anyone reaches for
    first. `split()` with no argument is doing more than it looks: it splits on
    runs of whitespace rather than on single spaces and discards the empty
    strings at both ends, so the leading, trailing and repeated spaces that the
    other approaches handle by hand are already gone.

    That is also the whole cost. It builds a list of every word in the string --
    O(n) space -- to read one of them, which is the only approach here that
    allocates in proportion to the input. `.pop()` is `[-1]` with a mutation
    that nothing can observe, since the list it mutates was created by the same
    expression.

    Note that `split()` and `split(' ')` are not interchangeable, and the
    difference is exactly the problem's edge case: `"hello world  ".split(' ')`
    is `['hello', 'world', '', '']`, whose last element is empty and would
    report 0. Java's `split(" ")` drops those trailing empties itself, so the
    same expression is safe there and unsafe here -- see the README.

    Raises IndexError on an all-spaces input, where the list is empty. Outside
    the constraints, and it is the loud kind of wrong.
    """
    return len(s.split().pop())


def length_of_last_word_trim_index(s: str) -> int:
    """Drop the trailing spaces, then measure from the last space to the end.
    Time: O(n). Space: O(n).

    NeetCode's approach 3 for *Java*, written in Python. No scan and no word
    list: once the trailing spaces are gone, the last word runs from just after
    the final space to the end of the string, so its length is the distance
    between them.

    `rfind` returning -1 when there is no space is the case that makes this work
    without a branch. A single-word string then measures `len(t) - (-1) - 1`,
    which is `len(t)` -- the whole string, correct, and arrived at by the same
    arithmetic as every other input rather than by a special case.

    Two things worth being precise about, because the Java original is not
    spelled this way:

    NeetCode's Java calls `trim()`, which strips *both* ends, and the leading
    half of that is dead work. The last space is the last space whether or not
    anything precedes it: verified identical on all 88,562 constraint-satisfying
    strings over {a, b, space} up to length 10, `strip()` against `rstrip()`,
    zero differences. Drop the trailing strip instead and 29,514 of them break.
    Only one end is load-bearing, so only that end is stripped here.

    The space is O(n) for the copy `rstrip()` makes -- but only when there is
    something to strip. CPython returns the original object unchanged otherwise,
    so a string with no trailing spaces costs nothing; Java's `trim()` has the
    same optimisation. Verified with `is` in both languages.

    This is the only approach that survives an all-spaces input, returning 0
    from `len('') - (-1) - 1`. Outside the constraints, and not a reason to
    prefer it.
    """
    t = s.rstrip()
    return len(t) - t.rfind(' ') - 1


SOLUTIONS: Tuple[Tuple[str, Callable[[str], int]], ...] = (
    ("forward", length_of_last_word_forward),
    ("backward", length_of_last_word_backward),
    ("split", length_of_last_word_split),
    ("trim+index", length_of_last_word_trim_index),
)

# The constraint ceiling is 10,000 characters. These are the three shapes that
# sit at it, and they disagree wildly about how much of the string matters.
_MAX_ONE_WORD = "a" * 10000  # no space anywhere: the backward scan's worst case
_MAX_TINY_TAIL = "a" + " " * 9997 + "bc"  # the answer is 2, after 9,998 junk characters
_MAX_LEADING_SPACES = " " * 9999 + "a"  # one word, at the very end

CASES: Sequence[Tuple[str, int]] = (
    ("hello world", 5),  # LeetCode example 1
    ("   fly me   to   the moon  ", 4),  # LeetCode example 2: leading, repeated and trailing spaces
    ("luffy is still joyboy", 6),  # LeetCode example 3: no padding at all
    ("a", 1),  # the smallest legal input, and what the `i >= 0` guard is for
    ("a ", 1),  # single word, trailing space: where dropping that guard survives by luck
    (" a", 1),  # single word, leading space
    ("day", 3),  # no spaces anywhere
    ("  day  ", 3),  # the same word, padded on both ends
    ("a b", 1),  # the last word shorter than the first
    ("ab c", 1),
    ("a bc", 2),  # and longer
    ("word          ", 4),  # trailing spaces alone -- the forward scan's reset trap
    ("          word", 4),  # leading spaces alone
    ("a  b  c", 1),  # repeated separators, which `split(' ')` would break on
    ("Hello World", 5),  # mixed case: the constraints allow both, nothing here folds case
    (_MAX_ONE_WORD, 10000),  # ceiling: every character is the answer
    (_MAX_TINY_TAIL, 2),  # ceiling: 4 of 10,000 characters decide it
    (_MAX_LEADING_SPACES, 1),  # ceiling: 9,999 spaces then the word
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = 0
        for s, expected in CASES:
            passed += solve(s) == expected
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()

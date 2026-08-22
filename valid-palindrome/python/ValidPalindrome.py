"""Valid Palindrome (LeetCode 125).

Given a string s, return True if it reads the same forwards and backwards once
every non-alphanumeric character is dropped and case is ignored.

The two approaches from https://neetcode.io/solutions/valid-palindrome, plus the
idiomatic Python spelling of the first, ordered from brute force to optimal.
"""

from typing import Callable, Sequence, Tuple


def is_alnum_ascii(char: str) -> bool:
    """True for A-Z, a-z and 0-9 -- nothing else.

    Three separate ranges, deliberately. 'A' <= char <= 'z' looks like a
    shortcut but the gap between 'Z' (90) and 'a' (97) holds [ \\ ] ^ _ `, so it
    would treat six punctuation marks as letters.

    str.isalnum() is the built-in equivalent, but it answers a wider question:
    it is Unicode-aware, so 'e' with an accent and superscript two are both
    alphanumeric to it. Identical under these constraints, which promise
    printable ASCII.
    """
    return "A" <= char <= "Z" or "a" <= char <= "z" or "0" <= char <= "9"


def is_palindrome_reverse(s: str) -> bool:
    """Filter to alphanumeric lowercase, then compare against the reverse.

    Time: O(n). Space: O(n) -- the cleaned copy.

    Collected into a list rather than concatenated onto a string. `cleaned +=
    char` in a loop is O(n^2) in the general case: strings are immutable, so
    each += copies everything built so far. CPython has an in-place resize that
    usually hides the cost, but it only fires when the string has exactly one
    reference, and it is an implementation detail rather than a promise.
    """
    cleaned = []
    for char in s:
        if is_alnum_ascii(char):
            cleaned.append(char.lower())
    return cleaned == cleaned[::-1]


def is_palindrome_reverse_builtin(s: str) -> bool:
    """The same idea, handed to the standard library. Time: O(n). Space: O(n).

    The idiomatic Python spelling, and worth knowing it exists -- but write the
    loop above or the two pointers below in an interview, since the filtering
    and the comparison are the part being asked about.

    Filters before lowercasing rather than calling s.lower() up front. For
    ASCII the two orders are identical; in general they are not, since a few
    Unicode characters lowercase into more than one character and would change
    the string's length mid-filter.
    """
    cleaned = [char.lower() for char in s if char.isalnum()]
    return cleaned == cleaned[::-1]


def is_palindrome_two_pointers(s: str) -> bool:
    """Close in from both ends. Time: O(n). Space: O(1).

    The optimal solution: no cleaned copy, just two indices walking toward each
    other. Each pointer skips anything non-alphanumeric before comparing, and
    across the whole run every index is visited at most once by one pointer or
    the other, so the skipping does not break the linear bound.

    Both inner loops re-test left < right. Without that, a string holding no
    alphanumeric characters at all -- " " or ".," -- walks a pointer clean off
    its end.
    """
    left, right = 0, len(s) - 1

    while left < right:
        while left < right and not is_alnum_ascii(s[left]):
            left += 1
        while left < right and not is_alnum_ascii(s[right]):
            right -= 1
        if s[left].lower() != s[right].lower():
            return False
        left, right = left + 1, right - 1

    return True


SOLUTIONS: Tuple[Tuple[str, Callable[[str], bool]], ...] = (
    ("reverse", is_palindrome_reverse),
    ("reverse (built-in)", is_palindrome_reverse_builtin),
    ("two pointers", is_palindrome_two_pointers),
)

CASES: Sequence[Tuple[str, bool]] = (
    ("Was it a car or a cat I saw?", True),
    ("tab a cat", False),
    ("A man, a plan, a canal: Panama", True),
    ("race a car", False),
    ("Madam, I'm Adam", True),
    ("0P", False),  # '0' and 'P' sit exactly 32 apart; see the README
    ("12321", True),  # digits only
    ("Aa", True),  # case folding alone decides it
    ("Ill i", True),  # pairs 'I' against 'i'; see the locale note in the README
    ("ab", False),  # shortest false case
    ("a.", True),  # trailing punctuation, odd length after cleaning
    (" ", True),  # no alphanumeric characters at all
    (".,", True),  # likewise, even length
    ("", True),  # empty, though the constraints promise at least one character
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(s) == expected for s, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()

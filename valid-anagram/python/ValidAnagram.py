"""Valid Anagram (LeetCode 242).

Given two strings s and t, return True if t is an anagram of s -- the same
characters in the same quantities, in any order.

The three approaches from https://neetcode.io/solutions/valid-anagram,
ordered from brute force to optimal.
"""

from collections import Counter, defaultdict
from typing import Callable, Sequence, Tuple

ALPHABET_SIZE = 26


def is_anagram_sorting(s: str, t: str) -> bool:
    """Sort both strings and compare. Time: O(n log n). Space: O(n).

    Anagrams are the same multiset of characters, so sorting collapses them to
    the same sequence. The length check is not just an optimization -- it is
    cheap, and it lets the counting approaches below stay in a single loop.
    """
    if len(s) != len(t):
        return False
    return sorted(s) == sorted(t)


def is_anagram_hash_map(s: str, t: str) -> bool:
    """Count characters in two maps and compare. Time: O(n). Space: O(k).

    k is the number of distinct characters -- capped at 26 for lowercase
    English, but O(n) in the worst case for arbitrary Unicode input.
    """
    if len(s) != len(t):
        return False

    count_s, count_t = defaultdict(int), defaultdict(int)
    for char_s, char_t in zip(s, t):
        count_s[char_s] += 1
        count_t[char_t] += 1
    return count_s == count_t


def is_anagram_counter(s: str, t: str) -> bool:
    """The same idea, handed to the standard library. Time: O(n). Space: O(k).

    Counter equality ignores insertion order, so this is the idiomatic Python
    one-liner. Worth knowing, but write out the loop above in an interview --
    that is the version the question is actually asking about.
    """
    return len(s) == len(t) and Counter(s) == Counter(t)


def is_anagram_array(s: str, t: str) -> bool:
    """One fixed array, incremented for s and decremented for t.

    Time: O(n). Space: O(1) -- 26 slots regardless of input size.

    The optimal solution when the alphabet is known and small. Every character
    of s bumps a slot up and every character of t bumps one down, so the array
    lands back on all zeros exactly when the two strings match.

    Only valid for lowercase a-z: anything else indexes out of range or, worse,
    wraps to a negative index and silently corrupts a different slot.
    """
    if len(s) != len(t):
        return False

    count = [0] * ALPHABET_SIZE
    for char_s, char_t in zip(s, t):
        count[ord(char_s) - ord("a")] += 1
        count[ord(char_t) - ord("a")] -= 1

    return all(value == 0 for value in count)


SOLUTIONS: Tuple[Tuple[str, Callable[[str, str], bool]], ...] = (
    ("sorting", is_anagram_sorting),
    ("hash map", is_anagram_hash_map),
    ("counter", is_anagram_counter),
    ("array", is_anagram_array),
)

CASES: Sequence[Tuple[str, str, bool]] = (
    ("racecar", "carrace", True),
    ("jar", "jam", False),
    ("anagram", "nagaram", True),
    ("rat", "car", False),
    ("a", "ab", False),  # different lengths
    ("ab", "a", False),  # different lengths, other way round
    ("", "", True),  # both empty
    ("a", "a", True),  # single character
    ("aacc", "ccac", False),  # same letters, different counts
    ("aabbcc", "abcabc", True),  # repeats interleaved
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(s, t) == expected for s, t, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()

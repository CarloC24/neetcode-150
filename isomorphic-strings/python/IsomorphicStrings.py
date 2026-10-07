"""Isomorphic Strings (LeetCode 205).

Given two strings s and t of the same length, return True if they are
isomorphic: if the characters of s can be replaced to get t, where every
occurrence of a character is replaced by the same character, and no two
characters map to the same one. A character may map to itself.

The two approaches from https://neetcode.io/solutions/isomorphic-strings, plus
a brute-force baseline, a fixed-array version and the set-counting idiom,
ordered from brute force to optimal and then the idiom.
"""

from typing import Callable, Dict, Sequence, Tuple


def is_isomorphic_brute_force(s: str, t: str) -> bool:
    """Compare every pair of positions. Time: O(n^2). Space: O(1).

    Two strings are isomorphic exactly when they repeat in the same places:
    for every pair of positions i, j, s[i] == s[j] if and only if
    t[i] == t[j]. That is the definition with no mapping in sight, and checking
    it directly is the baseline. At the ceiling of 5 * 10^4 characters it is
    about 1.25 * 10^9 comparisons, so it is here to define the answer, not to
    submit.
    """
    n = len(s)
    for i in range(n):
        for j in range(i):
            if (s[i] == s[j]) != (t[i] == t[j]):
                return False
    return True


def _maps_consistently(src: str, dst: str) -> bool:
    """True if every character of src always lines up with the same one in dst."""
    mapping: Dict[str, str] = {}
    for a, b in zip(src, dst):
        if a in mapping and mapping[a] != b:
            return False
        mapping[a] = b
    return True


def is_isomorphic_two_pass(s: str, t: str) -> bool:
    """Check the mapping s -> t, then t -> s. Time: O(n). Space: O(k).

    k is the number of distinct characters, at most 128 under these
    constraints. One direction is not enough: "ab" -> "aa" maps a to a and b to
    a without a single conflict, so a one-way check accepts it. The reverse pass
    is what catches two characters landing on the same one.
    """
    return _maps_consistently(s, t) and _maps_consistently(t, s)


def is_isomorphic_one_pass(s: str, t: str) -> bool:
    """Keep both maps at once. Time: O(n). Space: O(k).

    The same two checks as approach 2, interleaved, so the walk can stop at
    the first conflict in either direction instead of finishing one direction
    before starting the other.
    """
    s_to_t: Dict[str, str] = {}
    t_to_s: Dict[str, str] = {}
    for a, b in zip(s, t):
        if s_to_t.get(a, b) != b or t_to_s.get(b, a) != a:
            return False
        s_to_t[a] = b
        t_to_s[b] = a
    return True


def is_isomorphic_last_seen(s: str, t: str) -> bool:
    """Compare where each character was last seen. Time: O(n). Space: O(1).

    The optimal solution. Instead of mapping characters to characters, record
    for each character the position (plus one) where it last appeared, in two
    128-slot arrays -- one per string, sized for ASCII. Two characters at the
    same position belong together exactly when they were last seen at the same
    place, which covers both directions in one comparison: a new pairing shows
    up as one side remembering a position the other side does not.

    The +1 keeps 0 free to mean "never seen", so position 0 is not mistaken for
    it. The arrays are fixed-size, so the space is O(1) rather than O(k).
    """
    last_s = [0] * 128
    last_t = [0] * 128
    for i, (a, b) in enumerate(zip(s, t), start=1):
        a, b = ord(a), ord(b)
        if last_s[a] != last_t[b]:
            return False
        last_s[a] = last_t[b] = i
    return True


def is_isomorphic_sets(s: str, t: str) -> bool:
    """Count distinct characters and distinct pairs. Time: O(n). Space: O(k).

    The Python idiom. If the strings are isomorphic, each distinct character of
    s pairs with exactly one character of t and vice versa, so s, t and their
    zipped pairs all have the same number of distinct members. If any
    character pairs with two partners, the pairs outnumber that side. Short
    and correct, but it builds three sets and cannot stop early, so it does
    more work than approach 4 on every input.
    """
    return len(set(s)) == len(set(t)) == len(set(zip(s, t)))


SOLUTIONS: Tuple[Tuple[str, Callable[[str, str], bool]], ...] = (
    ("brute force", is_isomorphic_brute_force),
    ("hash map (two pass)", is_isomorphic_two_pass),
    ("hash map (one pass)", is_isomorphic_one_pass),
    ("last seen", is_isomorphic_last_seen),
    ("set counts", is_isomorphic_sets),
)

CASES: Sequence[Tuple[str, str, bool]] = (
    ("egg", "add", True),
    ("foo", "bar", False),  # o would have to map to both a and r
    ("paper", "title", True),
    ("ab", "aa", False),  # one-way check passes; needs t -> s too
    ("aa", "ab", False),  # the mirror image: fails s -> t
    ("badc", "baba", False),  # the conflict only appears on the reverse map
    ("abc", "abc", True),  # every character maps to itself
    ("abc", "bca", True),  # a cycle of reassignments is still one-to-one
    ("13", "42", True),  # any ASCII, not just letters
    ("a b", "x!y", True),  # space and punctuation are characters too
    ("a", "a", True),
    ("", "", True),  # the constraints allow length 0
    ("ab" * 150, "cd" * 150, True),  # long, every repeat agrees
    ("ab" * 150, "cd" * 149 + "cc", False),  # long, a single conflict at the end
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(s, t) == expected for s, t, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()

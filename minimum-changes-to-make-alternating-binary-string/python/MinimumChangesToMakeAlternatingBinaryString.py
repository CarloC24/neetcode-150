"""Minimum Changes To Make Alternating Binary String (LeetCode 1758).

Given a string s of '0's and '1's, one operation flips a single character.
Return the fewest operations that make s alternating, meaning no two
adjacent characters are equal.

The two approaches from
https://neetcode.io/solutions/minimum-changes-to-make-alternating-binary-string,
ordered from two passes to one.
"""

from typing import Callable, Sequence, Tuple


def min_operations_two_patterns(s: str) -> int:
    """Count mismatches against "0101..." and "1010...". Time: O(n). Space: O(1).

    An alternating string is fixed by its first character, so there are only
    two candidates. Each character that differs from a candidate costs one
    flip. Walk the string once per candidate, toggling the expected bit with
    cur ^= 1, and keep the cheaper count.
    """
    cur = cnt1 = 0
    for c in s:
        if int(c) != cur:
            cnt1 += 1
        cur ^= 1

    cur = 1
    cnt2 = 0
    for c in s:
        if int(c) != cur:
            cnt2 += 1
        cur ^= 1

    return min(cnt1, cnt2)


def min_operations_complement(s: str) -> int:
    """Count mismatches against one pattern; the other is n minus that.
    Time: O(n). Space: O(1).

    The optimal solution. The two candidates differ at every index, so each
    character matches exactly one of them. A character that mismatches
    "1010..." matches "0101...", and the other way round, so the two counts
    always add up to n.

    "1010..." expects '1' at even indices and '0' at odd ones.
    """
    count = 0
    for i, c in enumerate(s):
        if i % 2 == 0:
            count += 1 if c == "0" else 0
        else:
            count += 1 if c == "1" else 0
    return min(count, len(s) - count)


SOLUTIONS: Tuple[Tuple[str, Callable[[str], int]], ...] = (
    ("two patterns", min_operations_two_patterns),
    ("complement", min_operations_complement),
)

CASES: Sequence[Tuple[str, int]] = (
    ("0100", 1),
    ("10", 0),
    ("1111", 2),
    ("0", 0),  # the smallest input: one character is already alternating
    ("1", 0),
    ("00", 1),
    ("0101", 0),  # already alternating, both ways round
    ("1010", 0),
    ("111", 1),  # odd length: "101" needs one flip, "010" needs two
    ("000", 1),
    ("00101", 1),  # flip s[0]; fixing clashes left to right takes 4. See the README
    ("0110", 2),  # both patterns tie
    ("0" * 10_000, 5_000),  # length ceiling
    ("01" * 5_000, 0),
    ("1" * 9_999, 4_999),  # odd ceiling: "1010...1" keeps the extra 1
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(s) == expected for s, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()

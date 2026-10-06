"""Is Subsequence (LeetCode 392).

Given two strings s and t, return True if s is a subsequence of t: if every
character of s appears in t, in the same order, though not necessarily side by
side.

The five approaches from https://neetcode.io/solutions/is-subsequence, plus the
binary-search answer to the follow-up, ordered from brute force to optimal and
then the follow-up.
"""

from bisect import bisect_left
from typing import Callable, List, Sequence, Tuple


def is_subsequence_recursion(s: str, t: str) -> bool:
    """Match s[i] against t[j], always advancing j. Time: O(m). Space: O(m).

    NeetCode lists this as O(n * m) time and O(n) space, but the recursion
    never branches: every call either returns or makes exactly one more call,
    and every call moves j forward by one. That makes it a chain of at most
    m + 1 calls -- O(m) time, and O(m) stack, not O(n).

    That stack is the problem. t can be 10^4 characters, and Python's default
    recursion limit is 1000, so this raises RecursionError at the constraint
    ceiling. See the README.
    """

    def match(i: int, j: int) -> bool:
        if i == len(s):
            return True
        if j == len(t):
            return False
        if s[i] == t[j]:
            return match(i + 1, j + 1)
        return match(i, j + 1)

    return match(0, 0)


def is_subsequence_memo(s: str, t: str) -> bool:
    """The recursion above with a memo table. Time: O(m). Space: O(n * m).

    The memo never gets a hit. A cache only helps when the same (i, j) is
    reached twice, and that needs a call to branch -- which this one never does.
    All the table adds is an n * m allocation up front, so this is strictly
    worse than approach 1. It is here because NeetCode lists it, and because
    "memoising a recursion that doesn't branch buys nothing" is worth seeing
    once. Same RecursionError at the ceiling as approach 1.
    """
    n, m = len(s), len(t)
    memo = [[-1] * m for _ in range(n)]

    def match(i: int, j: int) -> bool:
        if i == n:
            return True
        if j == m:
            return False
        if memo[i][j] != -1:
            return memo[i][j] == 1
        if s[i] == t[j]:
            result = match(i + 1, j + 1)
        else:
            result = match(i, j + 1)
        memo[i][j] = 1 if result else 0
        return result

    return match(0, 0)


def is_subsequence_dp(s: str, t: str) -> bool:
    """Fill the whole table bottom-up. Time: O(n * m). Space: O(n * m).

    dp[i][j] is True when s[i:] is a subsequence of t[j:]. Unlike the two
    recursions this really does fill every cell, so it is the only approach
    that actually costs the O(n * m) NeetCode quotes. No recursion, so no stack
    limit, but at the ceiling the table holds 10^6 cells.
    """
    n, m = len(s), len(t)
    dp = [[False] * (m + 1) for _ in range(n + 1)]
    for j in range(m + 1):
        dp[n][j] = True  # the empty suffix of s fits in any suffix of t

    for i in range(n - 1, -1, -1):
        for j in range(m - 1, -1, -1):
            if s[i] == t[j]:
                dp[i][j] = dp[i + 1][j + 1]
            else:
                dp[i][j] = dp[i][j + 1]

    return dp[0][0]


def is_subsequence_two_pointers(s: str, t: str) -> bool:
    """Walk t once, ticking off s as it matches. Time: O(n + m). Space: O(1).

    The optimal solution for a single query. Taking the earliest match for each
    character of s is always safe: it leaves the most of t for the characters
    still to come, so a greedy miss means no match exists.
    """
    i = j = 0
    while i < len(s) and j < len(t):
        if s[i] == t[j]:
            i += 1
        j += 1
    return i == len(s)


def is_subsequence_next_index(s: str, t: str) -> bool:
    """Follow-up: precompute where each letter next appears.

    Time: O(26 * m) to build the table, then O(n) per query.
    Space: O(26 * m).

    next_at[j][c] is the first index >= j where letter c appears in t, or m if
    it never does. Each lookup jumps straight to the next match instead of
    scanning for it, so once the table exists a query costs O(n) no matter how
    long t is. Worth it only when many s are checked against one t; for a
    single query it is strictly more work than two pointers.

    NeetCode's version uses m + 1 as its "not found" sentinel and a separate
    m == 0 guard. An extra row at index m, filled with m, covers both: a miss
    jumps j to m + 1 and fails, and an empty t falls straight into that row.
    """
    n, m = len(s), len(t)
    next_at = [[m] * 26 for _ in range(m + 1)]
    for j in range(m - 1, -1, -1):
        next_at[j] = next_at[j + 1][:]
        next_at[j][ord(t[j]) - ord("a")] = j

    j = 0
    for char in s:
        if j > m:
            return False
        j = next_at[j][ord(char) - ord("a")] + 1
    return j <= m


def is_subsequence_binary_search(s: str, t: str) -> bool:
    """Follow-up, lighter: binary-search each letter's positions in t.

    Time: O(m) to build the lists, then O(n log m) per query. Space: O(m).

    Store, for each letter, the sorted list of indices where it appears in t.
    To match a character, binary-search its list for the first index at or
    after j. Each query costs a log factor more than approach 5, but the index
    is m integers in total instead of 26 * m -- the usual trade when t is large.
    """
    positions: List[List[int]] = [[] for _ in range(26)]
    for index, char in enumerate(t):
        positions[ord(char) - ord("a")].append(index)

    j = 0
    for char in s:
        indices = positions[ord(char) - ord("a")]
        k = bisect_left(indices, j)
        if k == len(indices):
            return False
        j = indices[k] + 1
    return True


SOLUTIONS: Tuple[Tuple[str, Callable[[str, str], bool]], ...] = (
    ("recursion", is_subsequence_recursion),
    ("recursion + memo", is_subsequence_memo),
    ("bottom-up dp", is_subsequence_dp),
    ("two pointers", is_subsequence_two_pointers),
    ("next-index table", is_subsequence_next_index),
    ("binary search", is_subsequence_binary_search),
)

RECURSIVE = {"recursion", "recursion + memo"}

CASES: Sequence[Tuple[str, str, bool]] = (
    ("node", "neetcode", True),
    ("axc", "ahbgdc", False),
    ("abc", "ahbgdc", True),
    ("", "ahbgdc", True),  # the empty string is a subsequence of everything
    ("", "", True),
    ("a", "", False),  # empty t, non-empty s
    ("abc", "abc", True),  # s == t
    ("abcd", "abc", False),  # s longer than t
    ("ba", "ab", False),  # right letters, wrong order
    ("aaa", "aa", False),  # t runs out of repeats
    ("aa", "aba", True),  # repeats split by another letter
    ("c", "abc", True),  # match on the last character of t
    ("b" * 100, "a" * 500 + "b" * 100, True),  # long t, every match at the end
    ("b" * 101, "a" * 500 + "b" * 100, False),  # one short, found only at the end
)

# The LeetCode ceiling: |s| = 100, |t| = 10^4. Every match sits in the last 100
# characters of t, so the scan, and the recursions, must cover all of t.
CEILING_S = "z" * 100
CEILING_T = "a" * 9900 + "z" * 100


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(s, t) == expected for s, t, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    # At the ceiling the recursions are expected to overflow Python's stack.
    # Any other outcome -- including a recursion quietly succeeding -- means
    # the README is out of date, so it fails too.
    for name, solve in SOLUTIONS:
        try:
            result = solve(CEILING_S, CEILING_T)
            ok = result is True and name not in RECURSIVE
            outcome = str(result)
        except RecursionError:
            ok = name in RECURSIVE
            outcome = "RecursionError (expected)"
        status = "PASS" if ok else "FAIL"
        print(f"{status}  {name} at |t| = 10^4: {outcome}")


if __name__ == "__main__":
    main()

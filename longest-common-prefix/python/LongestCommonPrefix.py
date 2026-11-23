"""Longest Common Prefix (LeetCode 14).

Given an array of strings `strs`, return the longest string that is a prefix
of every one of them, or "" if there is none.

The four approaches from https://neetcode.io/solutions/longest-common-prefix,
ordered as NeetCode presents them. All are O(n * m) in the worst case, where n
is the number of strings and m the length of the shortest one; they differ in
how much they read before stopping and in extra space.
"""

from typing import Callable, Dict, List, Sequence, Tuple


def longest_common_prefix_horizontal(strs: List[str]) -> str:
    """Shrink a running prefix against each string in turn.
    Time: O(n * m). Space: O(1) beyond the answer.

    Start with prefix = strs[0]. For each next string, keep only the part of
    the prefix it matches. The prefix can only get shorter, and once it's ""
    nothing later can grow it back, so stop early.
    """
    prefix = strs[0]
    for s in strs[1:]:
        j = 0
        while j < min(len(prefix), len(s)) and prefix[j] == s[j]:
            j += 1
        prefix = prefix[:j]
        if not prefix:
            break
    return prefix


def longest_common_prefix_vertical(strs: List[str]) -> str:
    """Check one column at a time across every string.
    Time: O(n * m). Space: O(1) beyond the answer.

    Column i is in the prefix if every string has a character there and it
    equals strs[0][i]. The first column that fails ends the prefix. This reads
    at most n * (answer length + 1) characters, which is the least work of any
    approach: it never looks past the first mismatch in any string.

    The `i == len(s)` check must come before `s[i]`, or a string shorter than
    strs[0] (["ab", "a"]) indexes past its end.
    """
    first = strs[0]
    for i in range(len(first)):
        for s in strs:
            if i == len(s) or s[i] != first[i]:
                return first[:i]
    return first


def longest_common_prefix_sorting(strs: List[str]) -> str:
    """Sort, then compare only the first and last strings.
    Time: O(n * m * log n). Space: O(n) for the sorted copy.

    After sorting, the first and last strings are the furthest apart. Any
    prefix they share is shared by everything between them, because sorted
    order keeps strings with a common prefix next to each other. So the answer
    is the common prefix of just those two.

    The sort isn't really needed: min(strs) and max(strs) are the same two
    strings, found in O(n * m). sorted() is used here (not strs.sort()) so the
    caller's list isn't reordered.
    """
    ordered = sorted(strs)
    first, last = ordered[0], ordered[-1]
    j = 0
    while j < min(len(first), len(last)) and first[j] == last[j]:
        j += 1
    return first[:j]


class TrieNode:
    def __init__(self) -> None:
        self.children: Dict[str, "TrieNode"] = {}
        self.end = False


def longest_common_prefix_trie(strs: List[str]) -> str:
    """Insert every string into a trie, then walk down the single path.
    Time: O(S), S = total characters. Space: O(S) for the trie.

    The common prefix is the path from the root while each node has exactly
    one child. Stop as soon as a node branches, or when a node ends a word:
    ["ab", "a"] has one path a -> b, but "a" ends at the first node, so the
    prefix can't include the b.

    It's the most expensive approach for a single query. A trie pays off when
    the strings stay fixed and you ask about prefixes many times.
    """
    root = TrieNode()
    for s in strs:
        node = root
        for c in s:
            node = node.children.setdefault(c, TrieNode())
        node.end = True

    prefix = []
    node = root
    while len(node.children) == 1 and not node.end:
        c, node = next(iter(node.children.items()))
        prefix.append(c)
    return "".join(prefix)


SOLUTIONS: Tuple[Tuple[str, Callable[[List[str]], str]], ...] = (
    ("horizontal", longest_common_prefix_horizontal),
    ("vertical", longest_common_prefix_vertical),
    ("sorting", longest_common_prefix_sorting),
    ("trie", longest_common_prefix_trie),
)

CASES: Sequence[Tuple[List[str], str]] = (
    (["bat", "bag", "bank", "band"], "ba"),
    (["dance", "dag", "danger", "damage"], "da"),
    (["neet", "feet"], ""),  # first characters differ
    (["flower", "flow", "flight"], "fl"),
    (["a"], "a"),  # one string is its own prefix
    ([""], ""),  # one empty string
    (["abc", ""], ""),  # an empty string anywhere ends it
    (["", "abc"], ""),
    (["ab", "a"], "a"),  # a shorter string after the first: vertical's index check
    (["a", "ab"], "a"),  # one string is a prefix of another: the trie's end check
    (["abc", "abc", "abc"], "abc"),  # all equal
    (["prefix", "pre", "prefixes"], "pre"),
    (["c", "acc", "ccc"], ""),  # sorting moves "acc" first; the answer is still ""
    (["ab", "abc", "abd", "a"], "a"),  # the branch and the end are at different depths
    (["a" * 200] * 200, "a" * 200),  # n = 200, every string 200 long, all equal
    (["a" * 199 + "b", "a" * 200], "a" * 199),  # mismatch on the last character
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(strs)) == expected for strs, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")

    # Sorting must not reorder the caller's list.
    strs = ["c", "b", "a"]
    for _, solve in SOLUTIONS:
        solve(strs)
    print(f"{'PASS' if strs == ['c', 'b', 'a'] else 'FAIL'}  input left unmodified: {strs}")


if __name__ == "__main__":
    main()

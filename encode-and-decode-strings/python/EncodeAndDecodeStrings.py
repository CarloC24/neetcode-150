"""Encode and Decode Strings (LeetCode 271).

Design an encoder that turns a list of strings into a single string, and a
decoder that turns that string back into the original list. The strings may
contain any of the 256 ASCII characters, so no character is safe to use as a
plain separator.

The two approaches from https://neetcode.io/solutions/encode-and-decode-strings,
plus escaping, the other standard way to delimit strings.
"""

from typing import Callable, List, Sequence, Tuple

# m = total number of characters across all strings, n = number of strings.


def encode_header(strs: List[str]) -> str:
    """Write every length up front, then all the strings back to back.
    Time: O(m + n). Space: O(m + n).

    ["Hello", "World"] becomes "5,5#HelloWorld". The header holds only digits
    and commas, so the first '#' in the whole string always ends it, whatever
    the strings contain.

    The empty list encodes to "", and [""] encodes to "0#", so the two stay
    distinguishable.
    """
    if not strs:
        return ""
    sizes = ",".join(str(len(s)) for s in strs)
    return sizes + "#" + "".join(strs)


def decode_header(s: str) -> List[str]:
    """Read the header, then cut the body into pieces of those lengths."""
    if not s:
        return []
    hash_idx = s.index("#")
    sizes = [int(size) for size in s[:hash_idx].split(",")]
    res, idx = [], hash_idx + 1
    for size in sizes:
        res.append(s[idx:idx + size])
        idx += size
    return res


def encode_length_prefix(strs: List[str]) -> str:
    """Write each string as its length, a '#', then the string itself.
    Time: O(m + n). Space: O(m + n).

    The optimal solution, and the standard one. ["Hello", "World"] becomes
    "5#Hello5#World". Each piece says how long it is, so the decoder never
    has to look inside a string to find where it ends. See the README.
    """
    return "".join(f"{len(s)}#{s}" for s in strs)


def decode_length_prefix(s: str) -> List[str]:
    """Read digits up to the next '#', then jump over exactly that many chars.

    The search for '#' starts at i, the start of a length, and lengths are
    only digits, so the first '#' it finds is always the delimiter. A '#'
    inside a string is never searched for: the decoder jumps over the whole
    string by its length.
    """
    res = []
    i = 0
    while i < len(s):
        j = s.index("#", i)
        length = int(s[i:j])
        i = j + 1
        res.append(s[i:i + length])
        i += length
    return res


def encode_escape(strs: List[str]) -> str:
    """Double every '#' inside a string, and end each string with "#!".
    Time: O(m + n). Space: O(m + n).

    ["a#b", ""] becomes "a##b#!#!". After escaping, a '#' inside a string is
    always followed by another '#', so "#!" can only ever be a terminator.
    The decoder has to read every character to find the terminators, where
    the length-prefix decoder jumps over them. See the README.
    """
    return "".join(s.replace("#", "##") + "#!" for s in strs)


def decode_escape(s: str) -> List[str]:
    """Scan left to right: "##" is a literal '#', "#!" ends the string.

    Every '#' starts a two-character pair, so reading left to right never
    misreads which '#' belongs to which pair: "a###!" is a, "##", "#!".
    """
    res: List[str] = []
    cur: List[str] = []
    i = 0
    while i < len(s):
        if s[i] == "#":
            if s[i + 1] == "#":
                cur.append("#")
            else:  # "#!": the end of one string
                res.append("".join(cur))
                cur = []
            i += 2
        else:
            cur.append(s[i])
            i += 1
    return res


SOLUTIONS: Tuple[Tuple[str, Callable[[List[str]], str], Callable[[str], List[str]]], ...] = (
    ("header", encode_header, decode_header),
    ("length prefix", encode_length_prefix, decode_length_prefix),
    ("escape", encode_escape, decode_escape),
)

ALL_ASCII = "".join(chr(c) for c in range(256))

CASES: Sequence[List[str]] = (
    ["Hello", "World"],
    [""],  # one empty string: must not decode as the empty list
    [],  # the empty list: must not decode as [""]
    ["", "", ""],  # several empty strings in a row
    ["#"],  # the delimiter itself
    ["5#Hello"],  # a string that looks like an encoded piece
    ["##", "#!", "!#"],  # the escape scheme's own pairs
    ["1,2", "3#", "0"],  # commas and digits, the header's own characters
    ["a" * 199],  # the longest string: a 3-digit length
    ["x"] * 99,  # the most strings
    [ALL_ASCII],  # every one of the 256 characters, in one string
    [ALL_ASCII[i:i + 32] for i in range(0, 256, 32)],  # ...and spread across eight
    ["héllo", "wörld 🙂"],  # beyond ASCII: the follow-up
)


def main() -> None:
    for name, encode, decode in SOLUTIONS:
        passed = 0
        for strs in CASES:
            encoded = encode(list(strs))
            if isinstance(encoded, str) and decode(encoded) == strs:
                passed += 1
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} round trips")


if __name__ == "__main__":
    main()

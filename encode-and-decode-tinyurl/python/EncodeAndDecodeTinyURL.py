"""Encode and Decode TinyURL (LeetCode 535).

Design a class that turns a long URL into a short one (encode) and gets the
original back from the short one (decode). Any scheme works, as long as
decode(encode(url)) == url for every URL the object has encoded.

The three approaches from https://neetcode.io/solutions/encode-and-decode-tinyurl.
All three hand out sequential ids; they differ in where the URLs are stored
and whether a repeated URL gets its old short URL back.
"""

import random
from typing import Dict, List, Sequence, Tuple, Type

BASE = "http://tinyurl.com/"


class CodecList:
    """Store URLs in a list; the short URL ends with the index.
    Time: O(1) per call (O(m) to build or copy a string of length m).
    Space: O(n * m) for n URLs of average length m.

    The list position is the id, so there's nothing else to keep track of.
    Encoding the same URL twice stores it twice, under two ids.
    """

    def __init__(self) -> None:
        self.urls: List[str] = []

    def encode(self, long_url: str) -> str:
        self.urls.append(long_url)
        return BASE + str(len(self.urls) - 1)

    def decode(self, short_url: str) -> str:
        # The id is whatever follows BASE. Slicing it off, rather than taking
        # the last '/' segment, keeps working if the format ever grows a path.
        return self.urls[int(short_url[len(BASE) :])]


class CodecCounter:
    """A hash map from id to URL, with a counter for the next id.
    Time: O(1) per call. Space: O(n * m).

    The same as the list, with the id kept explicitly. The map is what you'd
    want if ids stopped being 0, 1, 2, ...: random or hashed ids leave gaps a
    list can't hold. Repeated URLs still get a new id each time.
    """

    def __init__(self) -> None:
        self.url_map: Dict[int, str] = {}
        self.next_id = 0

    def encode(self, long_url: str) -> str:
        self.url_map[self.next_id] = long_url
        short_url = BASE + str(self.next_id)
        self.next_id += 1
        return short_url

    def decode(self, short_url: str) -> str:
        return self.url_map[int(short_url[len(BASE) :])]


class CodecTwoMaps:
    """Two hash maps, long -> short and short -> long.
    Time: O(1) per call. Space: O(n * m), about twice the others.

    Encoding a URL it has seen before returns the same short URL instead of
    making a new one, so repeats don't use up ids or memory. The price is the
    second map. decode doesn't parse anything: the whole short URL is the key.

    Ids start at 1 here (len(encode_map) + 1), as in NeetCode's version.
    """

    def __init__(self) -> None:
        self.encode_map: Dict[str, str] = {}
        self.decode_map: Dict[str, str] = {}

    def encode(self, long_url: str) -> str:
        if long_url not in self.encode_map:
            short_url = BASE + str(len(self.encode_map) + 1)
            self.encode_map[long_url] = short_url
            self.decode_map[short_url] = long_url
        return self.encode_map[long_url]

    def decode(self, short_url: str) -> str:
        return self.decode_map[short_url]


CODECS: Tuple[Tuple[str, Type], ...] = (
    ("list", CodecList),
    ("hash map + counter", CodecCounter),
    ("two hash maps", CodecTwoMaps),
)

_rng = random.Random(535)

URLS: Sequence[str] = (
    "https://leetcode.com/problems/design-tinyurl",  # LeetCode example
    "https://example.com/docs",  # NeetCode example 1
    "https://example.com/search?q=graphs&lang=en#results",  # query and fragment
    "https://example.com/users/7",  # NeetCode example 3
    "https://example.com/docs",  # a repeat of an earlier URL
    "http://tinyurl.com/0",  # a long URL that looks like a short one
    "https://example.com/a/b/c/",  # trailing slash: the last '/' segment is ""
    "https://例え.jp/パス?キー=値",  # non-ASCII
    "https://example.com/" + "x" * 9_980,  # length 10,000, the limit
    *(f"https://example.com/item/{_rng.randrange(10**9)}" for _ in range(1_000)),
)


def main() -> None:
    order = list(range(len(URLS)))
    _rng.shuffle(order)

    for name, codec_class in CODECS:
        codec = codec_class()
        short_urls = [codec.encode(url) for url in URLS]
        # Decode everything after all the encodes, in a different order.
        passed = sum(codec.decode(short_urls[i]) == URLS[i] for i in order)
        # Then interleave: encodes and decodes on an object already in use.
        for url in URLS[:20]:
            passed += codec.decode(codec.encode(url)) == url
        total = len(URLS) + 20

        repeats = "same" if short_urls[1] == short_urls[4] else "new"
        status = "PASS" if passed == total else "FAIL"
        print(f"{status}  {name}: {passed}/{total} round trips, {repeats} short URL for a repeat")


if __name__ == "__main__":
    main()

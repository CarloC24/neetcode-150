# Encode and Decode TinyURL

LeetCode 535 · [NeetCode solution page](https://neetcode.io/solutions/encode-and-decode-tinyurl)

Design a class with two methods:

- `encode(longUrl)` returns a short URL for `longUrl`.
- `decode(shortUrl)` returns the long URL that `shortUrl` was made from.

Any scheme is allowed. The only requirement is that `decode(encode(url))`
returns `url`, for every URL the same object has encoded, in any order.

```
codec.encode("https://leetcode.com/problems/design-tinyurl")  ->  "http://tinyurl.com/0"
codec.decode("http://tinyurl.com/0")  ->  "https://leetcode.com/problems/design-tinyurl"
```

**Constraints:** `1 <= url.length <= 10^4`, every URL is valid, and every
value passed to `decode` came from `encode` on the same object.

```
encode-and-decode-tinyurl/
├── java/
│   └── EncodeAndDecodeTinyURL.java
├── python/
│   └── EncodeAndDecodeTinyURL.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of URLs.

## Approaches

| # | Approach | encode / decode | Space | Repeated URL | Idea |
|---|----------|-----------------|-------|--------------|------|
| 1 | List | O(1) | O(n · m) | New short URL | The id is the URL's index in a list. |
| 2 | Hash map + counter | O(1) | O(n · m) | New short URL | Map id → URL, with a counter for the next id. |
| 3 | **Two hash maps** | **O(1)** | **O(n · m), ×2** | **Same short URL** | Map long → short and short → long. |

`n` is the number of URLs encoded and `m` their average length. O(1) means a
constant number of list or map operations; building and hashing the strings
is O(m). All three are NeetCode's.

**Approach 3 is the one to reach for.** It's the only one that gives a repeated
URL its old short URL back, which is what a real shortener does, and decode is
a single lookup with no parsing. Approaches 1 and 2 use half the memory and are
fine if repeats don't matter. The judge accepts all three.

## The core idea: the short URL is a key

There's no way to compress any 10,000-character URL into a few characters and
recover it from those alone. So a short URL doesn't *contain* the long one: it's
a **key** into storage the object keeps.

```
encode:  store url under a new id  ->  return BASE + id
decode:  read the id from the short URL  ->  return what's stored under it
```

The approaches only differ in the storage:

- **List:** the id is the position. It costs nothing to generate, but the ids
  have to be 0, 1, 2, ... with no gaps.
- **Map from id:** the same, but the ids could be anything: random strings,
  hashes, base-62 counters. That's the step toward a real design.
- **Two maps:** the second map, long → short, answers "have I seen this URL
  before?" so a repeat reuses its id.

## Parsing the id

Approaches 1 and 2 have to get the id back out of the short URL. NeetCode
splits on `/` and takes the last piece. These files slice off the known prefix
instead (`short_url[len(BASE):]`), which says what's meant and keeps working if
the short format ever gains a path.

Either way, only the **short** URL is parsed, never the long one. The test set
includes long URLs that would confuse a parser if one were ever run on them:
`http://tinyurl.com/0` (which looks like a short URL) and
`https://example.com/a/b/c/` (whose last `/` piece is empty).

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 encode-and-decode-tinyurl/python/EncodeAndDecodeTinyURL.py
```

Or from inside the folder:

```bash
cd encode-and-decode-tinyurl/python
python3 EncodeAndDecodeTinyURL.py
```

Expected output:

```
PASS  list: 1029/1029 round trips, new short URL for a repeat
PASS  hash map + counter: 1029/1029 round trips, new short URL for a repeat
PASS  two hash maps: 1029/1029 round trips, same short URL for a repeat
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java encode-and-decode-tinyurl/java/EncodeAndDecodeTinyURL.java
```

### Option 2 — compile, then run

```bash
cd encode-and-decode-tinyurl/java
javac EncodeAndDecodeTinyURL.java   # produces EncodeAndDecodeTinyURL*.class
java EncodeAndDecodeTinyURL         # note: no .class extension
```

Expected output (either option):

```
PASS  list: 1029/1029 round trips, new short URL for a repeat
PASS  hash map + counter: 1029/1029 round trips, new short URL for a repeat
PASS  two hash maps: 1029/1029 round trips, same short URL for a repeat
```

To clean up the compiled artifacts from option 2 (the nested classes compile
to their own `EncodeAndDecodeTinyURL$...class` files):

```bash
rm encode-and-decode-tinyurl/java/EncodeAndDecodeTinyURL*.class
```

## Notes

- **How the harness tests it.** Each approach gets a fresh object. It encodes
  1,009 URLs: the LeetCode and NeetCode examples, a repeat, a query string with
  a fragment, the two parser traps above, a non-ASCII URL, a 10,000-character
  URL and 1,000 random ones (seeded with `535`). Then it decodes all of them in
  shuffled order, and finally runs 20 encode-then-decode pairs on the same
  object. It also reports whether the repeat got the same short URL.
- **Stress-tested in both languages:** 300 random sessions per approach of 400
  operations each, mixing encodes and decodes, with many repeated URLs built
  from `/`, `?`, `#` and digits. 360,000 operations per language. Every decode
  returned its original URL, no two different URLs ever shared a short URL,
  and the two-map approach always gave a repeat its first short URL. Zero
  failures.
- **Ids in approach 3 start at 1**, as in NeetCode's version. Approaches 1 and 2
  start at 0. Nothing depends on it.
- **Sequential ids are guessable.** Anyone can walk `/0`, `/1`, `/2`, ... and
  see every URL ever shortened, and an `int` counter in Java runs out after
  about 2 billion URLs. A real shortener uses random or hashed ids, often
  written in base 62 (`a-z`, `A-Z`, `0-9`) so 7 characters cover 3.5 trillion
  ids, and checks for collisions.
- **Class naming:** LeetCode's class is `Codec` with `encode` and `decode`.
  These files use `CodecList` / `CodecCounter` / `CodecTwoMaps` so all three
  can coexist. Rename the one you submit to plain `Codec`. In Java, also drop
  `static` and `implements Codec`: the `Codec` interface only exists so the
  harness can run all three.
- The Java file is named `EncodeAndDecodeTinyURL.java` to match its `public
  class EncodeAndDecodeTinyURL`, as `javac` requires.

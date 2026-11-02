# Encode and Decode Strings

LeetCode 271 · [NeetCode solution page](https://neetcode.io/solutions/encode-and-decode-strings)

Design an algorithm that **encodes** a list of strings into a single string,
and **decodes** that string back into the original list. Think of the encoded
string as something sent over a network: the decoder only has the string, not
the list.

`["Hello", "World"]` must come back as `["Hello", "World"]`.
`[""]` must come back as `[""]`, not as `[]`.

**Constraints:** `0 <= strs.length < 100`, `0 <= strs[i].length < 200`, and
the strings may contain **any** of the 256 ASCII characters.

**Follow-up:** make it work for any set of characters.

LeetCode 271 is a premium problem. NeetCode hosts a free version of it.

```
encode-and-decode-strings/
├── java/
│   └── EncodeAndDecodeStrings.java
├── python/
│   └── EncodeAndDecodeStrings.py
└── README.md
```

Each file implements every approach as an encode/decode pair, plus a `main`
that checks each pair round-trips a shared set of test cases.

## Approaches

`m` is the total number of characters across all strings, `n` the number of
strings.

| # | Approach | Time | Space | `["Hello", "World"]` encodes to | Idea |
|---|----------|------|-------|---------------------------------|------|
| 1 | Header | O(m + n) | O(m + n) | `5,5#HelloWorld` | All the lengths up front, then the strings back to back. |
| 2 | **Length prefix** | **O(m + n)** | **O(m + n)** | `5#Hello5#World` | Each string preceded by its own length and a `#`. |
| 3 | Escape | O(m + n) | O(m + n) | `Hello#!World#!` | Double every `#` in a string; end each string with `#!`. |

Approaches 1 and 2 are NeetCode's. Approach 3 is the other standard way to
delimit strings, added for comparison.

**Approach 2 is the one to reach for.** It's the standard answer, it's short,
and the decoder never has to read the contents of a string to find where it
ends.

## Why a separator alone can't work

The obvious idea is to join with a separator such as `#` and split on it
later. But the strings can contain any character, including `#`:

```python
"#".join(["a#b"])     # "a#b"
"#".join(["a", "b"])  # "a#b"     two different lists, one encoding
"#".join([])          # ""
"#".join([""])        # ""        same problem
```

Two different lists encode to the same string, so no decoder can tell them
apart. Picking a rarer separator only makes the collision rarer, not
impossible. The encoding has to carry the boundaries some other way: as
lengths (approaches 1 and 2), or by making the separator impossible inside a
string (approach 3).

## Why the length prefix is safe

Each string becomes `length#string`. The decoder alternates between two
modes:

1. **Read a length.** Starting at the beginning of a piece, read characters up
   to the next `#`. A length is only digits, so the first `#` found is always
   the delimiter.
2. **Jump over the string.** Take exactly `length` characters, whatever they
   are, and land on the start of the next piece.

The decoder never searches *inside* a string, so a `#` there is harmless. Even
a string that looks like an encoding is fine:

```
["5#Hello"]  encodes to  7#5#Hello

i = 0:  read up to the first '#'   -> "7"
        take the next 7 chars       -> "5#Hello"
i = 9:  end of input                -> ["5#Hello"]
```

Two things matter for this:

- **Multi-digit lengths.** Lengths go up to 199, so the decoder reads up to the
  `#`, not a fixed one character. The 199-character test case checks that.
- **Splitting on `#` is still wrong.** `["#"]` encodes to `1##`, and
  `"1##".split("#")` gives `["1", "", ""]`. The `#` has to be found by
  searching from the current position, once per piece.

## The header variant

Approach 1 puts all the lengths first, separated by commas, then a single `#`,
then every string concatenated. The header contains only digits and commas,
so the first `#` in the whole encoding is always the end of the header.

It needs one special case: the empty list encodes to `""`, while `[""]`
encodes to `"0#"`. Approach 2 gets this for free, because the empty list
produces no pieces at all.

NeetCode's Java header writes a comma after every length (`5,5,#`), while its
Python header doesn't (`5,5#`). Both decode correctly in their own language.
These files use `5,5#` in both, so the two languages produce the same
encoding.

## Escaping: the other way to delimit

Approach 3 keeps a separator but makes it impossible inside a string:

- Every `#` inside a string is written as `##`.
- Every string ends with `#!`.

After escaping, any `#` inside a string is immediately followed by another
`#`, so `#!` can only mean "end of string". The decoder reads left to right,
and every `#` starts a two-character pair:

```
["a#", ""]  encodes to  a###!#!

a     -> literal 'a'
##    -> literal '#'
#!    -> end of "a#"
#!    -> end of ""
```

It's correct, and it's how many real formats work (CSV quotes, backslash
escapes in string literals). The cost is that the decoder must look at every
character, and the encoding grows with each `#` in the input. The length
prefix jumps over each string in one step.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 encode-and-decode-strings/python/EncodeAndDecodeStrings.py
```

Or from inside the folder:

```bash
cd encode-and-decode-strings/python
python3 EncodeAndDecodeStrings.py
```

Expected output:

```
PASS  header: 13/13 round trips
PASS  length prefix: 13/13 round trips
PASS  escape: 13/13 round trips
```

## Java

**Requires:** JDK 11+ (the single-file launcher, and `String.repeat` in the
tests). Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java encode-and-decode-strings/java/EncodeAndDecodeStrings.java
```

### Option 2 — compile, then run

```bash
cd encode-and-decode-strings/java
javac EncodeAndDecodeStrings.java   # produces EncodeAndDecodeStrings*.class
java EncodeAndDecodeStrings         # note: no .class extension
```

Expected output (either option):

```
PASS  header: 13/13 round trips
PASS  length prefix: 13/13 round trips
PASS  escape: 13/13 round trips
```

To clean up the compiled artifacts from option 2:

```bash
rm encode-and-decode-strings/java/EncodeAndDecodeStrings*.class
```

## Notes

- **The tests check round trips, not exact encodings.** Any encoding is
  correct as long as `decode(encode(strs)) == strs` for every list. The cases
  target what breaks naive schemes: `[]` vs `[""]`, a lone `#`, a string that
  looks like an encoding, each scheme's own marker characters, a 3-digit
  length, the 99-string maximum, and all 256 ASCII characters (including
  `\0`).
- **The follow-up, beyond ASCII.** All three schemes work for any characters,
  since the length prefix never looks inside a string and the escape scheme
  only treats `#` specially. The last test case has `é`, `ö` and an emoji.
  One catch: "length" means different things in different languages. Python's
  `len("🙂")` is 1 (code points), but Java's `"🙂".length()` is 2 (UTF-16 code
  units). Each language round-trips its own encoding, but they can't read each
  other's once non-ASCII characters appear. A real wire format would count
  bytes of a fixed encoding such as UTF-8.
- **`str.index` / `indexOf` instead of a manual loop.** NeetCode's approach 2
  decoder advances `j` one character at a time to find the `#`. `s.index("#", i)`
  and `str.indexOf('#', i)` do the same search starting from `i`.
- **Verified exhaustively, in both languages:** every list of 0–3 strings,
  each 0–3 characters long over `#`, `!`, `0` and `,` (the characters each
  scheme treats specially). That's 621,436 lists and 1,864,308 round trips per
  language, with zero failures.
- **Method naming:** NeetCode's class has `encode` and `decode`. These files
  use `encode*` / `decode*` with an approach suffix, so all three pairs can
  coexist. Rename to plain `encode` and `decode` when submitting.
- The Java file is named `EncodeAndDecodeStrings.java` to match its
  `public class EncodeAndDecodeStrings`, as `javac` requires.

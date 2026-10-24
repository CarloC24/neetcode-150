# Number of Senior Citizens

LeetCode 2678 · [NeetCode solution page](https://neetcode.io/solutions/number-of-senior-citizens)

You're given an array `details` of passenger strings. Each one is exactly 15
characters:

```
 7868190130   M    75     22
 └ phone ─┘   │    age   seat
 [0, 10)     10  [11,13) [13,15)
```

Return the number of passengers who are **strictly more than 60** years old.

`["7868190130M7522", "5303914400F9211", "9273338290F4010"]` gives `2`: the ages
are 75, 92 and 40.
`["1313579440F2036", "2921522980M5644"]` gives `0`: the ages are 20 and 56.

**Constraints:** `1 <= details.length <= 100`, `details[i].length == 15`,
every character is a digit except `details[i][10]`, which is `'M'`, `'F'` or
`'O'`.

```
number-of-senior-citizens/
├── java/
│   └── NumberOfSeniorCitizens.java
├── python/
│   └── NumberOfSeniorCitizens.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Parse | O(n) | O(1) | Slice out characters 11–12 and convert them to an `int`. |
| 2 | Digits | O(n) | O(1) | `10 * (d[11] - '0') + (d[12] - '0')`, no slice or parse. |
| 3 | **Compare** | **O(n)** | **O(1)** | Compare the two age characters with `"60"` as text. |

Approaches 1 and 2 are NeetCode's. Approach 3 skips the conversion entirely.

All three have the same complexity: every string is 15 characters, so each
passenger costs O(1), and the work is O(n) for `n` passengers. The difference
is only in the constant: approach 1 allocates a 2-character string per
passenger and parses it, approach 2 does two subtractions and a multiply,
approach 3 does at most two character comparisons.

**Approach 2 is the one to reach for in an interview.** It's the clearest way
to say "read a number out of fixed positions", and the `c - '0'` trick comes up
everywhere (String to Integer, Add Strings, Multiply Strings). Approach 3 is a
neat follow-up once you see why it's correct.

## The problem is fixed-width parsing

There's no algorithm here, just a record layout. The whole problem is reading
the age from the right two characters:

- **Index 10 is the gender.** Slicing `d[10:12]` gives `"M7"`, and `int()`
  throws.
- **Indices 13–14 are the seat.** Slicing `d[13:15]` (or `d[-2:]`, which looks
  like "the number at the end") reads the seat instead. The test case
  `passenger(10, seat=99)` catches that: age 10, seat 99, answer 0.
- **Strictly more than 60.** Age 60 doesn't count. `>= 60` passes both LeetCode
  examples and still fails, so `passenger(60)` is in the test set.

Naming the offsets once (`AGE_START = 11`) keeps the slice from drifting.

## Why comparing text works

Approach 3 tests `d[11:13] > "60"` without ever turning it into a number. That
only works because of two facts:

1. **The digit characters are in order.** `'0' < '1' < ... < '9'` in ASCII, so
   comparing two digit characters compares the digits.
2. **Both strings have the same length.** The age is always two characters,
   zero-padded. String comparison goes character by character from the left,
   so for equal-length digit strings the first differing digit decides, which
   is exactly how numbers compare.

Break fact 2 and it fails: `"7" > "60"` is true, but `"100" > "60"` is false,
because `'1' < '6'`. Here the width is guaranteed, so the comparison is safe.

Java can't use `>` on strings, so its approach 3 writes the two-character
comparison out: `tens > '6' || (tens == '6' && ones > '0')`. That avoids both
the substring and `compareTo`.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 number-of-senior-citizens/python/NumberOfSeniorCitizens.py
```

Or from inside the folder:

```bash
cd number-of-senior-citizens/python
python3 NumberOfSeniorCitizens.py
```

Expected output:

```
PASS  parse: 10/10 cases
PASS  digits: 10/10 cases
PASS  compare: 10/10 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java number-of-senior-citizens/java/NumberOfSeniorCitizens.java
```

### Option 2 — compile, then run

```bash
cd number-of-senior-citizens/java
javac NumberOfSeniorCitizens.java   # produces NumberOfSeniorCitizens*.class
java NumberOfSeniorCitizens         # note: no .class extension
```

Expected output (either option):

```
PASS  parse: 10/10 cases
PASS  digits: 10/10 cases
PASS  compare: 10/10 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm number-of-senior-citizens/java/NumberOfSeniorCitizens*.class
```

## Notes

- **Every age is tested.** One case holds a passenger for each age `00`–`99`
  and expects 39 (ages 61–99). That covers every possible value of the two age
  characters, so each approach is checked exhaustively on the part that
  matters.
- **`passenger(age, seat, ...)` builds the test strings.** It zero-pads with
  `{age:02d}` / `%02d`, the same format the real input uses, so the cases read
  as ages instead of 15-digit strings.
- **Leading zeros are fine for parsing.** `int("07")` and
  `Integer.parseInt("07")` both give 7. Only the text comparison relies on the
  padding, and there it's required.
- **Method naming:** LeetCode uses `countSeniors`. These files use
  `countSeniors*` / `count_seniors_*` with an approach suffix, so all three can
  coexist. Rename to plain `countSeniors` when submitting.
- The Java file is named `NumberOfSeniorCitizens.java` to match its
  `public class NumberOfSeniorCitizens`, as `javac` requires.

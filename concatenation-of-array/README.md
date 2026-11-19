# Concatenation of Array

LeetCode 1929 · [NeetCode solution page](https://neetcode.io/solutions/concatenation-of-array)

Given an integer array `nums` of length `n`, return an array `ans` of length
`2n` where `ans[i] == nums[i]` and `ans[i + n] == nums[i]` for `0 <= i < n`.
In other words, `nums` followed by a second copy of itself.

`[1, 2, 1]` gives `[1, 2, 1, 1, 2, 1]`.
`[1, 3, 2, 1]` gives `[1, 3, 2, 1, 1, 3, 2, 1]`.

**Constraints:** `1 <= n <= 1000`, `1 <= nums[i] <= 1000`.

```
concatenation-of-array/
├── java/
│   └── ConcatenationOfArray.java
├── python/
│   └── ConcatenationOfArray.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Append | O(n) | O(n) | Loop over `nums` twice, appending each element to a new list. |
| 2 | **Index** | **O(n)** | **O(n)** | Allocate `2n` slots, then set `ans[i]` and `ans[i + n]` in one pass. |
| 3 | Built-in | O(n) | O(n) | Python `nums + nums`. Java `Arrays.copyOf` + `System.arraycopy`. |

Approaches 1 and 2 are NeetCode's. The O(n) space is the output itself, which
the problem requires, so there's no extra space beyond it.

All three are optimal. The output has `2n` elements and each one must be
written, so nothing beats O(n).

**Approach 2 is the one to know.** When you know the output size up front,
allocate it once and write by index. That's the habit that matters in Java,
where arrays can't grow. Approach 1 in Java needs an `ArrayList<Integer>`,
which boxes every `int` and needs a final copy back to `int[]`. In an
interview, approach 3 is fine to mention, but expect to be asked for approach 2.

## The index arithmetic

The whole problem is one formula: element `i` lands at `i` and `i + n`.

```
nums  =  [1, 3, 2, 1]          n = 4
ans   =  [1, 3, 2, 1, 1, 3, 2, 1]
          0  1  2  3  4  5  6  7
          └ i ────┘  └ i + n ──┘
```

The same `i + n` idea shows up later for circular arrays. In Next Greater
Element II and Gas Station you loop `i` over `0..2n-1` and read `nums[i % n]`,
which behaves like walking this doubled array without building it.

## Don't modify the input

In Python, `nums += nums` and `nums.extend(nums)` produce the right list, but
they grow the caller's `nums` in place and return the same object:

```python
a = [1, 2]
b = a
a += a
print(a, b)   # [1, 2, 1, 2] [1, 2, 1, 2]
```

LeetCode accepts it, but the caller's list is now changed, which is a bug in
real code. `nums + nums` and `nums * 2` both build a new list, so they're safe.
The last test checks every approach: the input stays `[1, 2, 3]` and the
returned array is a different object.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 concatenation-of-array/python/ConcatenationOfArray.py
```

Or from inside the folder:

```bash
cd concatenation-of-array/python
python3 ConcatenationOfArray.py
```

Expected output:

```
PASS  append: 7/7 cases
PASS  index: 7/7 cases
PASS  builtin: 7/7 cases
PASS  input left unmodified: [1, 2, 3]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java concatenation-of-array/java/ConcatenationOfArray.java
```

### Option 2 — compile, then run

```bash
cd concatenation-of-array/java
javac ConcatenationOfArray.java   # produces ConcatenationOfArray*.class
java ConcatenationOfArray         # note: no .class extension
```

Expected output (either option):

```
PASS  append: 7/7 cases
PASS  index: 7/7 cases
PASS  builtin: 7/7 cases
PASS  input left unmodified: [1, 2, 3]
```

To clean up the compiled artifacts from option 2:

```bash
rm concatenation-of-array/java/ConcatenationOfArray*.class
```

## Notes

- **The Java built-in uses two bulk copies.** `Arrays.copyOf(nums, 2 * n)`
  copies `nums` and pads the second half with zeros, then
  `System.arraycopy(nums, 0, ans, n, n)` overwrites the zeros.
- **Test cases** cover `n = 1`, all-equal values, the value bounds 1 and 1000,
  an unsorted input (so a sort or reverse would be caught), and `n = 1000`.
- **Method naming:** LeetCode uses `getConcatenation`. These files use
  `getConcatenation*` / `get_concatenation_*` with an approach suffix, so all
  three can coexist. Rename to plain `getConcatenation` when submitting.
- The Java file is named `ConcatenationOfArray.java` to match its
  `public class ConcatenationOfArray`, as `javac` requires.

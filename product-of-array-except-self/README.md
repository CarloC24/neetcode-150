# Product of Array Except Self

LeetCode 238 · [NeetCode solution page](https://neetcode.io/solutions/product-of-array-except-self)

Given an integer array `nums`, return an array `answer` where `answer[i]` is
the product of every element of `nums` except `nums[i]`. The product of any
prefix or suffix of `nums` fits in a 32-bit integer.

`[1, 2, 3, 4]` becomes `[24, 12, 8, 6]`.
`[-1, 1, 0, -3, 3]` becomes `[0, 0, 9, 0, 0]`.

**Constraints:** `2 <= n <= 10^5`, `-30 <= nums[i] <= 30`.

**Follow-up:** O(n) time without using division, and O(1) extra space (the
output array doesn't count).

```
product-of-array-except-self/
├── java/
│   └── ProductOfArrayExceptSelf.java
├── python/
│   └── ProductOfArrayExceptSelf.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Extra space | Division | Idea |
|---|----------|------|-------------|----------|------|
| 1 | Brute force | O(n²) | O(1) | No | For each index, multiply everything else. |
| 2 | Division | O(n) | O(1) | Yes | Total product / `nums[i]`, with zeros counted apart. |
| 3 | Prefix & suffix | O(n) | O(n) | No | `answer[i]` = product on the left × product on the right. |
| 4 | **Prefix & suffix, O(1)** | **O(n)** | **O(1)** | **No** | Prefixes in the output array, suffix as one running value. |

"Extra space" doesn't count the output array, as the problem specifies. All
four are NeetCode's.

**Approach 4 is the one to reach for.** It answers the whole follow-up: O(n),
no division, nothing allocated besides the output. Approach 3 is the same idea
with the prefixes and suffixes spelled out, and the easier one to derive first.

## The idea: left times right

Everything except `nums[i]` splits into what's left of it and what's right of
it:

```
nums      [  1,  2,  3,  4 ]
left      [  1,  1,  2,  6 ]    left[i]  = nums[0] * ... * nums[i-1]
right     [ 24, 12,  4,  1 ]    right[i] = nums[i+1] * ... * nums[n-1]
answer    [ 24, 12,  8,  6 ]    left[i] * right[i]
```

`left[0]` and `right[n - 1]` are 1, the product of nothing. Neither array ever
includes `nums[i]` itself, which is the whole point.

Approach 4 builds `left` directly in the answer array, then sweeps right to
left carrying `right` as a single number:

```
pass 1 (left to right)          pass 2 (right to left)
i  res[i] = prefix  prefix      i  res[i] *= suffix  suffix
0  1                1           3  6 * 1  = 6        4
1  1                2           2  2 * 4  = 8        12
2  2                6           1  1 * 12 = 12       24
3  6                24          0  1 * 24 = 24       24
```

In both passes, use the running value first and multiply `nums[i]` into it
after. Swap those two lines and every answer includes its own element.

## Why division is harder than it looks

Dividing the total product by each element breaks on zeros, so count them
first and take the product of the non-zero elements only:

| Zeros | Answer |
|---|---|
| 0 | `prod / nums[i]` everywhere |
| 1 | `prod` at the zero, 0 everywhere else |
| 2+ | 0 everywhere |

`[-1, 0, 1, 2, 3]` has one zero, so the answer is `[0, -6, 0, 0, 0]`. If `prod`
included the zero, it would be 0 and the `-6` would be lost.

## Python

**Requires:** Python 3.8+ (`math.prod` in the test harness). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 product-of-array-except-self/python/ProductOfArrayExceptSelf.py
```

Or from inside the folder:

```bash
cd product-of-array-except-self/python
python3 ProductOfArrayExceptSelf.py
```

Expected output:

```
PASS  brute force: 12/12 cases (skips the 3 long cases)
PASS  division: 15/15 cases
PASS  prefix & suffix: 15/15 cases
PASS  prefix & suffix, O(1): 15/15 cases
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java product-of-array-except-self/java/ProductOfArrayExceptSelf.java
```

### Option 2 — compile, then run

```bash
cd product-of-array-except-self/java
javac ProductOfArrayExceptSelf.java   # produces ProductOfArrayExceptSelf.class
java ProductOfArrayExceptSelf         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 12/12 cases (skips the 3 long cases)
PASS  division: 15/15 cases
PASS  prefix & suffix: 15/15 cases
PASS  prefix & suffix, O(1): 15/15 cases
```

To clean up the compiled artifacts from option 2:

```bash
rm product-of-array-except-self/java/ProductOfArrayExceptSelf*.class
```

## Notes

- **Test cases:** both NeetCode examples and both LeetCode examples, two
  elements (the smallest input), one zero at the front, middle and back, two
  zeros, all negatives, the ±30 value limits, and three 100,000-element arrays
  (alternating ±1, the same with one zero, all 1s). The long cases' answers are
  worked out by hand; the short ones are checked against the definition. Brute
  force skips the long cases: it would take 10^10 multiplications.
- **The harness also checks that `nums` is unchanged.** None of the approaches
  write to the input.
- **Overflow in Java.** The problem guarantees every prefix and suffix product
  fits in an `int`, so approaches 3 and 4 are safe. Approach 2's `prod` is safe
  too: with no zeros it's the prefix product of the whole array, and with one
  zero it's the answer at that zero (a prefix times a suffix). Python's integers
  don't overflow.
- **Python's `//` is safe here.** It rounds toward negative infinity, unlike
  Java's `/`, but with no zeros `prod` is an exact multiple of every element, so
  there's nothing to round.
- **Verified exhaustively, in both languages:** every array of length 2–6 over
  `{-30, -2, -1, 0, 1, 3}` (55,980 arrays, 223,920 checks per language), against
  the definition computed in a `long` in Java. Zero failures in either language.
- **Method naming:** LeetCode uses `productExceptSelf`. These files use
  `productExceptSelf*` / `product_except_self_*` with an approach suffix, so all
  four can coexist. Rename to plain `productExceptSelf` when submitting.
- The Java file is named `ProductOfArrayExceptSelf.java` to match its `public
  class ProductOfArrayExceptSelf`, as `javac` requires.

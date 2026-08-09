# Contains Duplicate

LeetCode 217 · [NeetCode solution page](https://neetcode.io/solutions/contains-duplicate)

Given an integer array `nums`, return `true` if any value appears more than
once, and `false` if every element is distinct.

**Constraints:** `1 <= nums.length <= 10^5`, `-10^9 <= nums[i] <= 10^9`

```
contains-duplicate-array/
├── java/
│   └── ContainsDuplicate.java
├── python/
│   └── contains-duplicate.py
└── README.md
```

Each file implements all four approaches side by side, plus a `main` that runs
them against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n²) | O(1) | Compare every pair of elements. |
| 2 | Sorting | O(n log n) | O(n) | Duplicates become neighbors once sorted. |
| 3 | **Hash set** | **O(n)** | **O(n)** | Track seen values; return on the first repeat. |
| 4 | Set length | O(n) | O(n) | A set drops duplicates, so a smaller set means duplicates existed. |

**Approach 3 is the one to reach for.** Approach 4 has the same asymptotic
complexity and is a one-liner, but it always builds the entire set — approach 3
exits the moment it sees a repeat, which on `[1, 1, ...100k more...]` is after
two elements instead of 100,001.

The space column for sorting assumes the input is copied first (see notes
below). Sorting in place instead would be O(log n) in Java, where
`Arrays.sort(int[])` is a dual-pivot quicksort that only needs recursion stack;
Python's Timsort needs O(n) scratch space either way.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 contains-duplicate-array/python/contains-duplicate.py
```

Or from inside the folder:

```bash
cd contains-duplicate-array/python
python3 contains-duplicate.py
```

Expected output:

```
PASS  brute force: 7/7 cases
PASS  sorting: 7/7 cases
PASS  hash set: 7/7 cases
PASS  set length: 7/7 cases
PASS  input left unmodified: [3, 1, 2]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java contains-duplicate-array/java/ContainsDuplicate.java
```

### Option 2 — compile, then run

```bash
cd contains-duplicate-array/java
javac ContainsDuplicate.java   # produces ContainsDuplicate.class
java ContainsDuplicate         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 7/7 cases
PASS  sorting: 7/7 cases
PASS  hash set: 7/7 cases
PASS  distinct count: 7/7 cases
PASS  input left unmodified: [3, 1, 2]
```

To clean up the compiled artifact from option 2:

```bash
rm contains-duplicate-array/java/ContainsDuplicate.class
```

## Notes

- **The sorting approach copies its input first** (`sorted(nums)` rather than
  `nums.sort()`; `Arrays.copyOf` before `Arrays.sort`). The NeetCode versions
  sort in place, which silently reorders the caller's array — fine on a judge,
  surprising anywhere else. That costs O(n) space, which is why the table above
  lists sorting as O(n).
- **Watch the inner loop bound in brute force.** It must start at `i + 1`. At
  `i`, every element gets compared to itself and the function returns `true`
  for any non-empty input.
- **Method naming:** NeetCode's signature is `hasDuplicate`; LeetCode's is
  `containsDuplicate`. These files use `hasDuplicate*` suffixed by approach so
  all four can coexist — rename to whichever the judge expects when submitting.
- The Java file is named `ContainsDuplicate.java` to match its
  `public class ContainsDuplicate`, as `javac` requires. Python has no such
  constraint, so the script keeps the repo's hyphenated file naming.
- Both files handle the empty array (returns `false`), even though the
  constraints guarantee at least one element.

# Remove Element

LeetCode 27 · [NeetCode solution page](https://neetcode.io/solutions/remove-element)

Given an integer array `nums` and an integer `val`, remove every occurrence of
`val` **in place** and return `k`, the number of elements left. The first `k`
slots of `nums` must hold the kept elements, in any order. Whatever is past
slot `k` doesn't matter.

`nums = [1, 1, 2, 3, 4], val = 1` gives `k = 3`, with `nums` starting `[2, 3, 4, ...]`.
`nums = [0, 1, 2, 2, 3, 0, 4, 2], val = 2` gives `k = 5`, with `nums` starting `[0, 1, 3, 0, 4, ...]`.

**Constraints:** `0 <= nums.length <= 100`, `0 <= nums[i] <= 50`,
`0 <= val <= 100`.

```
remove-element/
├── java/
│   └── RemoveElement.java
├── python/
│   └── RemoveElement.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Idea |
|---|----------|------|-------|------|
| 1 | Brute force | O(n) | O(n) | Copy the keepers to a new array, then copy them back. |
| 2 | **Two pointers** | **O(n)** | **O(1)** | Read with `i`, write each keeper to slot `k`, then `k++`. |
| 3 | Two pointers (swap) | O(n) | O(1) | Overwrite each `val` with the last element and shrink the array by one. |

All three are NeetCode's.

**Two pointers is the one to know.** It's the read/write pointer template, and
it comes back soon in Remove Duplicates From Sorted Array (Day 12 of the
roadmap). `k` never gets ahead of `i`, so a write only ever lands on a slot
that's already been read:

```
nums = [3, 1, 2, 1, 4], val = 1

i=0  3 keep  -> nums[0]=3  k=1   [3, 1, 2, 1, 4]
i=1  1 skip                k=1
i=2  2 keep  -> nums[1]=2  k=2   [3, 2, 2, 1, 4]
i=3  1 skip                k=2
i=4  4 keep  -> nums[2]=4  k=3   [3, 2, 4, 1, 4]
                                  ^^^^^^^ first k
```

## When the swap version wins

Two pointers writes once per **keeper**. The swap version writes once per
**`val`**. With `nums = [1, 2, 3, ..., 100], val = 1`, two pointers shifts 99
elements left. The swap version moves `100` into slot 0 and is done.

It pays for that by reordering the keepers. The problem allows any order, so
both are accepted. The last test in each file checks that the first two
approaches keep the original order. The swap version is left out of that test.

## The bug that's easy to write

**In the swap version, don't advance `i` after a swap.** The element you just
pulled in from the end hasn't been checked, and it might be `val` too:

```
nums = [2, 1, 1, 1], val = 1

i=1  nums[1]=1  -> pull in nums[3]=1   [2, 1, 1]   i stays 1
i=1  nums[1]=1  -> pull in nums[2]=1   [2, 1]      i stays 1
i=1  nums[1]=1  -> pull in nums[1]=1   [2]         i stays 1
i=1  stop, n=1
```

If `i++` ran after each swap, it would skip the second `1` and return `k = 2`.
The test `[2, 1, 1, 1], val = 1` catches this.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 remove-element/python/RemoveElement.py
```

Or from inside the folder:

```bash
cd remove-element/python
python3 RemoveElement.py
```

Expected output:

```
PASS  brute force: 14/14 cases
PASS  two pointers: 14/14 cases
PASS  two pointers (swap): 14/14 cases
PASS  brute force keeps order: [3, 2, 4]
PASS  two pointers keeps order: [3, 2, 4]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java remove-element/java/RemoveElement.java
```

### Option 2 — compile, then run

```bash
cd remove-element/java
javac RemoveElement.java   # produces RemoveElement*.class
java RemoveElement         # note: no .class extension
```

Expected output (either option):

```
PASS  brute force: 14/14 cases
PASS  two pointers: 14/14 cases
PASS  two pointers (swap): 14/14 cases
PASS  brute force keeps order: [3, 2, 4]
PASS  two pointers keeps order: [3, 2, 4]
```

To clean up the compiled artifacts from option 2:

```bash
rm remove-element/java/RemoveElement*.class
```

## Notes

- **Graded like LeetCode.** The checker compares `k`, then compares the first
  `k` slots to the expected keepers after sorting both, since any order is
  allowed.
- **Checked against a reference.** Besides the 14 cases, all three Python
  approaches were run on 20,000 random inputs (length 0–8, values 0–3,
  `val` 0–4) and compared with a filtered copy. They all agreed.
- **Test cases** cover an empty array, a single element removed or kept,
  everything removed, `val` missing, `val` only at the front or back,
  alternating, the swap trap above, and the `n = 100` / `val = 100` limits.
- **Method naming:** LeetCode uses `removeElement`. These files use
  `removeElement*` / `remove_element_*` with an approach suffix, so all three
  can coexist. Rename to plain `removeElement` when submitting.
- The Java file is named `RemoveElement.java` to match its
  `public class RemoveElement`, as `javac` requires.

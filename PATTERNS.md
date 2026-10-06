# Patterns Cheat Sheet

The patterns from the problems solved so far, condensed for quick review. Each pattern covers:
- **When to use it:** the clues in a problem statement that point to it
- **Template:** a minimal code shape in Python
- **Common bugs:** the mistakes worth remembering

Add a section whenever a new problem introduces a new pattern.

---

## Reading the constraints

The input size tells you roughly how fast the solution has to be. A judge handles about 10^7–10^8 simple operations per second.

| Max `n` | Target complexity | Usually means |
|---|---|---|
| ≤ 12 | O(n!) | Permutations |
| ≤ 25 | O(2^n) | Subsets, backtracking |
| ≤ 500 | O(n³) | Triple loops, interval DP |
| ≤ 5,000 | O(n²) | Nested loops, 2-D DP |
| ≤ 10^5–10^6 | O(n log n) or O(n) | Sorting, heap, hashing, two pointers, sliding window |
| > 10^8, or "follow-up: O(log n)" | O(log n) or O(1) | Binary search, math |

**Check this before coding.** If `n` is up to 10^5, an O(n²) solution will time out, however clean it is.

---

## 1. Hash set / hash map: "have I seen this before?"

**Solved:** [Contains Duplicate](contains-duplicate-array/), [Two Sum](two-sum/), [Valid Anagram](valid-anagram/)

**When to use it:** you need to check for duplicates, find a pair that adds up to a target, count things, or match two collections against each other. It trades O(n) memory for O(1) lookups, which turns an O(n²) nested loop into O(n).

```python
seen = {}                       # value -> index (or a set, if you only need membership)
for i, x in enumerate(nums):
    if target - x in seen:      # look before you insert...
        return [seen[target - x], i]
    seen[x] = i                 # ...so x can't pair with itself
```

**Counting variant:** if the keys are only 26 lowercase letters, use a fixed `[0] * 26` array instead of a dict. It's O(1) space, and checking for "all zeros" compares the counts in one step.

**Common bugs:**
- Inserting before looking up, so an element pairs with itself (in Two Sum, `[3]` with target 6).
- Forgetting the quick length check when comparing two strings.

---

## 2. Two pointers: close in from both ends

**Solved:** [Valid Palindrome](valid-palindrome/), [Is Subsequence](is-subsequence/) (extra practice)

**When to use it:** the input is sorted, or you're comparing symmetric positions (palindromes, reversing in place). Each step lets you rule out one end of the range.

```python
l, r = 0, len(s) - 1
while l < r:
    if not s[l].isalnum():
        l += 1
    elif not s[r].isalnum():
        r -= 1
    else:
        if s[l].lower() != s[r].lower():
            return False
        l, r = l + 1, r - 1
return True
```

**Common bugs:**
- Using `<=` instead of `<`. It compares the middle element with itself, which is harmless here but wrong in pair-sum problems.
- Skipping characters with an inner `while` loop that doesn't also check `l < r`.

**Same-direction variant** ([Is Subsequence](is-subsequence/)): one pointer per sequence, both moving forward. The "fast" pointer moves every step, and the other moves only on a match.

```python
i = 0
for c in t:                      # j is implicit: walk t once
    if i < len(s) and s[i] == c:
        i += 1                   # earliest match is always safe
return i == len(s)
```

**Next up:** Two Sum II and 3Sum. 3Sum is "sort, then run two pointers for each element."

---

## 3. One pass / sliding window: track the best so far

**Solved:** [Best Time to Buy and Sell Stock](best-time-to-buy-and-sell-stock/)

**When to use it:** you're looking for the best contiguous subarray or substring, or the best pair `(i < j)`. Instead of checking every pair, carry what you need from the left side as you go.

```python
best, lowest = 0, float("inf")
for price in prices:
    lowest = min(lowest, price)          # best "left side" so far
    best = max(best, price - lowest)     # best answer ending here
return best
```

**Variable-size window**, for the next problems in this category:

```python
l = 0
for r in range(len(s)):
    # add s[r] to the window
    while window_is_invalid():
        # remove s[l] from the window
        l += 1
    best = max(best, r - l + 1)
```

**Common bugs:**
- Answering `max(prices) - min(prices)`, which ignores order. On `[7, 1, 5, 3, 6, 4]` it happens to work, but on `[7, 6, 4, 3, 1]` it returns 6 instead of 0, because you can't sell before you buy.
- Off-by-one errors in the window length: it's `r - l + 1`.

---

## 4. Stack: most recent unmatched thing

**Solved:** [Valid Parentheses](valid-parentheses/)

**When to use it:** the problem involves nesting, matching pairs, undo, or "the most recent thing that hasn't been resolved yet." Anything last-in-first-out.

```python
pairs = {")": "(", "]": "[", "}": "{"}
stack = []
for c in s:
    if c in pairs:
        if not stack or stack.pop() != pairs[c]:
            return False
    else:
        stack.append(c)
return not stack            # leftover openers mean it's unbalanced
```

**Common bugs:**
- Popping from an empty stack. Check it first.
- Returning `True` at the end without checking that the stack is empty, so `"(("` passes.
- In Java, using `java.util.Stack` instead of `ArrayDeque`. `Stack` works but synchronizes every method for no benefit.

**Next up:** Min Stack, and the monotonic stack (Daily Temperatures).

---

## 5. Binary search: halve the search range

**Solved:** [Binary Search](binary-search/)

**When to use it:** the input is sorted, the problem asks for O(log n), or you can phrase the question as "the smallest x where some condition becomes true." That last one covers Koko Eating Bananas and many others.

```python
l, r = 0, len(nums) - 1
while l <= r:
    m = l + (r - l) // 2        # avoids overflow in Java
    if nums[m] == target:
        return m
    if nums[m] < target:
        l = m + 1
    else:
        r = m - 1
return -1
```

**Common bugs:**
- Writing `l = m` or `r = m` with `l <= r`. The range stops shrinking and the loop never ends.
- Writing `(l + r) / 2` in Java. It can overflow `int` on large indices.
- Mixing up the range conventions. Pick one: inclusive `[l, r]` with `l <= r`, or half-open `[l, r)` with `l < r`. Don't mix them.

---

## 6. Linked list: rewire pointers carefully

**Solved:** [Reverse Linked List](reverse-linked-list/), [Merge Two Sorted Lists](merge-two-sorted-lists/)

**When to use it:** any linked-list problem. There are three tools to keep ready.

**Reverse in place.** Save `next` before you overwrite it:

```python
prev, curr = None, head
while curr:
    nxt = curr.next
    curr.next = prev
    prev, curr = curr, nxt
return prev                     # not curr, which is None by now
```

**Dummy head.** Use one whenever you're building a new chain, so the first node isn't a special case:

```python
dummy = tail = ListNode()
while l1 and l2:
    if l1.val <= l2.val:
        tail.next, l1 = l1, l1.next
    else:
        tail.next, l2 = l2, l2.next
    tail = tail.next
tail.next = l1 or l2            # attach the leftover in one step
return dummy.next
```

**Fast and slow pointers.** For finding the middle of a list or detecting a cycle. Coming up next in Linked List Cycle:

```python
slow = fast = head
while fast and fast.next:
    slow, fast = slow.next, fast.next.next
    if slow is fast:
        return True             # cycle
return False
```

**Common bugs:**
- Losing the rest of the list by overwriting `.next` before saving it.
- Forgetting `head.next = None` in the recursive reverse, which creates a cycle.
- Returning the wrong pointer at the end.
- Recursing too deep in Python. The default limit is 1000 frames, so a long list can raise `RecursionError`.

---

## Before you submit

- [ ] Empty input, a single element, all elements the same
- [ ] Negative numbers, zero, and the minimum and maximum values from the constraints
- [ ] Does the time complexity fit `n`? (see the table at the top)
- [ ] Did you modify an input the caller still needs?
- [ ] Java: could anything overflow `int`?

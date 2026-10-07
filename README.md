# NeetCode 150

My solutions to the [NeetCode 150](https://neetcode.io/practice), each written in both **Python** and **Java**.

Every problem has its own folder with:
- every approach from brute force to optimal, side by side
- a shared set of test cases
- a README covering the trade-offs, the edge cases, and the mistakes worth remembering

**Progress: 9 / 150.** See [STUDY_PLAN.md](STUDY_PLAN.md) for the full checklist.

## Docs

| File | What it's for |
|---|---|
| [STUDY_PLAN.md](STUDY_PLAN.md) | All 150 problems in study order, with a progress checklist and a daily time budget per category |
| [PATTERNS.md](PATTERNS.md) | Cheat sheet of the patterns solved so far: when to use each one, a code template, and the common bugs |
| [VIDEOS.md](VIDEOS.md) | YouTube courses and walkthroughs to listen to alongside the plan |

## Solved

### NeetCode 150

| # | Problem | Category | Difficulty | Optimal approach | Time | Space |
|---|---|---|---|---|---|---|
| 217 | [Contains Duplicate](contains-duplicate-array/) | Arrays & Hashing | Easy | Hash set | O(n) | O(n) |
| 242 | [Valid Anagram](valid-anagram/) | Arrays & Hashing | Easy | 26-slot count array | O(n) | O(1) |
| 1 | [Two Sum](two-sum/) | Arrays & Hashing | Easy | Hash map, one pass | O(n) | O(n) |
| 125 | [Valid Palindrome](valid-palindrome/) | Two Pointers | Easy | Two pointers | O(n) | O(1) |
| 121 | [Best Time to Buy and Sell Stock](best-time-to-buy-and-sell-stock/) | Sliding Window | Easy | One pass, track the minimum | O(n) | O(1) |
| 20 | [Valid Parentheses](valid-parentheses/) | Stack | Easy | Stack | O(n) | O(n) |
| 704 | [Binary Search](binary-search/) | Binary Search | Easy | Iterative binary search | O(log n) | O(1) |
| 206 | [Reverse Linked List](reverse-linked-list/) | Linked List | Easy | Iterative pointer flip | O(n) | O(1) |
| 21 | [Merge Two Sorted Lists](merge-two-sorted-lists/) | Linked List | Easy | Iteration with a dummy head | O(m+n) | O(1) |

### Extra practice (not in the NeetCode 150)

| # | Problem | Difficulty | Optimal approach | Time | Space |
|---|---|---|---|---|---|
| 58 | [Length of Last Word](length-of-last-word/) | Easy | Backward scan | O(n) | O(1) |
| 3110 | [Score of a String](score-of-a-string/) | Easy | Single pass | O(n) | O(1) |
| 2073 | [Time Needed to Buy Tickets](time-needed-to-buy-tickets/) | Easy | Direct calculation | O(n) | O(1) |
| 392 | [Is Subsequence](is-subsequence/) | Easy | Two pointers, same direction | O(n+m) | O(1) |
| 205 | [Isomorphic Strings](isomorphic-strings/) | Easy | Last-seen index arrays | O(n) | O(1) |

[hello-world/](hello-world/) checks that your Python and Java toolchains work. Run it first on a new machine.

## Running a solution

Every solution file has a `main` that runs all of its approaches against the test cases and prints one `PASS`/`FAIL` line per approach. Run it from the repo root:

```bash
# Python 3.6+
python3 two-sum/python/TwoSum.py

# Java 11+ (single-file launcher, no javac needed)
java two-sum/java/TwoSum.java
```

Expected output for Two Sum:

```
PASS  brute force: 9/9 cases
PASS  two pointers: 9/9 cases
PASS  hash map (two pass): 9/9 cases
PASS  hash map (one pass): 9/9 cases
PASS  input left unmodified: [3, 2, 4]
```

Each problem's README lists its exact commands and expected output.

### Running everything

`example.sh` runs every solution in both languages and prints a summary:

```bash
./example.sh                # all problems, Python and Java (~10 seconds)
./example.sh two-sum        # only folders whose name contains "two-sum"
./example.sh -p             # Python only (much faster)
./example.sh -j linked      # Java only, matching folders
./example.sh -v             # also show each file's own PASS lines
```

A file fails if it crashes, throws an exception, doesn't compile, or prints any line starting with `FAIL`. The script exits with status 1 if anything failed, so you can run it before committing.

**Verified with:** Python 3.9.6 and JDK 26.

## Adding a new problem

1. **Create the folder** using the problem's slug from its NeetCode URL:

   ```
   problem-name/
   ├── java/
   │   └── ProblemName.java     # public class ProblemName
   ├── python/
   │   └── ProblemName.py
   └── README.md
   ```

2. **Write each solution file:**
   - Implement every approach, ordered from brute force to optimal.
   - Give each approach a docstring or Javadoc that states its time and space complexity.
   - Add a `main` that runs every approach against one shared case list.
   - Copy any input that an approach mutates, so one approach can't corrupt the input for the next.

3. **Write the README.** Use an existing problem's README as the template. It should include:
   - the LeetCode number and the NeetCode link
   - the constraints
   - an approaches table, with the optimal approach in bold
   - run instructions and the expected output
   - notes on anything surprising

4. **Run `./example.sh problem-name`** and confirm both files pass. Then run `./example.sh` on its own, so you know nothing else broke.

5. **Update the docs:**
   - Check the problem off in `STUDY_PLAN.md`, and bump the counts in its Progress table.
   - Add a row to the **Solved** table above, and update the progress count.
   - If the problem introduced a new pattern, add it to `PATTERNS.md`.

6. **Commit** with the message `Add <Problem Name> solutions in Python and Java`.

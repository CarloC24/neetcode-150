# Number of Students Unable to Eat Lunch

LeetCode 1700 · [NeetCode solution page](https://neetcode.io/solutions/number-of-students-unable-to-eat-lunch)

The cafeteria has circular (`0`) and square (`1`) sandwiches. The students
stand in a **queue** and the sandwiches sit in a **stack**, with
`sandwiches[0]` on top. Each student wants one type. At every step:

- If the student at the front wants the top sandwich, they take it and leave.
- Otherwise, they go to the back of the queue.

This continues until no one left in the queue wants the top sandwich. Return
how many students are unable to eat.

`students = [1, 1, 0, 0], sandwiches = [0, 1, 0, 1]` gives `0`: everyone eats.
`students = [1, 1, 1, 0, 0, 1], sandwiches = [1, 0, 0, 0, 1, 1]` gives `3`.

**Constraints:** `1 <= students.length == sandwiches.length <= 100`, every
value is `0` or `1`.

```
number-of-students-unable-to-eat-lunch/
├── java/
│   └── NumberOfStudentsUnableToEatLunch.java
├── python/
│   └── NumberOfStudentsUnableToEatLunch.py
└── README.md
```

Each file implements every approach side by side, plus a `main` that runs them
against a shared set of test cases.

## Approaches

| # | Approach | Time | Space | Input | Idea |
|---|----------|------|-------|-------|------|
| 1 | Queue simulation | O(n²) | O(n) | intact | Rotate a deque until someone takes the top sandwich, or nobody can. |
| 2 | Circular index | O(n²) | O(1) | **overwritten** | The same walk over the array, marking students who have eaten as `-1`. |
| 3 | **Counting** | **O(n)** | **O(1)** | intact | Count each preference; serve sandwiches until a type runs out. |

All three are NeetCode's.

**Approach 3 is the one to reach for.** Approaches 1 and 2 act out the story.
Approach 3 notices that the queue's order never affects the answer.

## The queue doesn't matter, the stack does

The students keep rotating, so anyone who wants the top sandwich reaches the
front sooner or later. Whether one of them is first in line or last only
changes how long the rotation takes, not who eats. So all that matters about
the queue is **how many students want each type**.

The sandwiches are the opposite. They're a stack, so they're served strictly
in order, and nobody can skip the top one. As soon as the top sandwich's type
has no one left who wants it, everything stops, even if students below in the
stack could have matched.

That gives approach 3:

1. Count how many students want `0` and how many want `1`.
2. Go through the sandwiches in order. If someone still wants this type, they
   eat: decrement that count.
3. At the first sandwich whose count is 0, stop. Everyone still waiting is
   the answer.

On example 2, the counts start at `0: 2, 1: 4`:

```
sandwich:     1     0     0     0
counts:     2, 3  1, 3  0, 3  stop: nobody left wants a 0
eaten:        1     2     3
```

Three students eat and three are left, all wanting `1`. The three `1`
sandwiches under the stuck `0` are never reached.

## How the simulations know to stop

Approaches 1 and 2 need a way to detect "no one wants this sandwich". Both
rotate at most `n` times per sandwich. The queue never holds more than `n`
students, so `n` moves always cover everyone still waiting. If the front still
doesn't match after that, no one does.

Each sandwich can cost up to `n` rotations, so the simulations are O(n²).
`students = [1] * 99 + [0]` with `0` on top makes the very first sandwich take
99 rotations. That case is in the test set.

## Approach 2 overwrites its input

Approach 2 marks a student who has eaten by setting `students[idx] = -1`, in
the caller's array. A second call on the same array would see those students
as gone. LeetCode hands every submission its own copy, so it never shows up
there, but the harness here gives each approach a fresh copy of both arrays.
The final check runs approaches 1 and 3 on one shared pair of arrays to confirm
they leave them unchanged.

[Time Needed to Buy Tickets](../time-needed-to-buy-tickets/) has the same
shape: a queue simulation, the same thing with a circular index that destroys
the input, and then a counting solution that skips the simulation entirely.

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Run it from the repo root:

```bash
python3 number-of-students-unable-to-eat-lunch/python/NumberOfStudentsUnableToEatLunch.py
```

Or from inside the folder:

```bash
cd number-of-students-unable-to-eat-lunch/python
python3 NumberOfStudentsUnableToEatLunch.py
```

Expected output:

```
PASS  queue: 12/12 cases
PASS  circular index: 12/12 cases
PASS  counting: 12/12 cases
PASS  input left unmodified (all but circular index): [1, 1, 0, 0], [0, 1, 0, 1]
```

## Java

**Requires:** JDK 11+ for the single-file launcher below (`Set.of` needs 9+).
Verified on JDK 26.

### Option 1 — single-file source launcher (JDK 11+)

```bash
java number-of-students-unable-to-eat-lunch/java/NumberOfStudentsUnableToEatLunch.java
```

### Option 2 — compile, then run

```bash
cd number-of-students-unable-to-eat-lunch/java
javac NumberOfStudentsUnableToEatLunch.java   # produces NumberOfStudentsUnableToEatLunch*.class
java NumberOfStudentsUnableToEatLunch         # note: no .class extension
```

Expected output (either option):

```
PASS  queue: 12/12 cases
PASS  circular index: 12/12 cases
PASS  counting: 12/12 cases
PASS  input left unmodified (all but circular index): [1, 1, 0, 0], [0, 1, 0, 1]
```

To clean up the compiled artifacts from option 2:

```bash
rm number-of-students-unable-to-eat-lunch/java/NumberOfStudentsUnableToEatLunch*.class
```

## Notes

- **`ArrayDeque`, not `LinkedList`.** NeetCode's Java queue is a
  `LinkedList`. `ArrayDeque` is the usual queue in Java: it's backed by an
  array, so it doesn't allocate a node per element.
- **`q.peek() != sandwich` compares values here.** `peek()` returns an
  `Integer`, and because `sandwich` is an `int`, Java unboxes the `Integer`
  and compares numbers. Comparing two `Integer`s with `!=` would compare
  references instead.
- **A plain list for the counts.** Python's approach 3 uses `[0, 0]` indexed by
  the sandwich type, like Java's `int[2]`, where NeetCode uses a `Counter`.
  There are only two keys, so a hash map adds nothing.
- **The answer is also the number of unserved sandwiches.** Every student who
  eats takes exactly one sandwich, and the two arrays have the same length, so
  the students left over equal the sandwiches left in the stack.
- **Verified exhaustively, in both languages:** every pair of `students` and
  `sandwiches` of equal length 1–8 (87,380 pairs, 262,140 checks per language),
  against a literal step-by-step run of the process that stops once every
  waiting student has passed on the top sandwich. The same run checked that
  `sandwiches` is never modified, and `students` only by approach 2. Zero
  failures in either language.
- **Method naming:** LeetCode uses `countStudents`. These files use
  `countStudents*` / `count_students_*` with an approach suffix, so all three
  can coexist. Rename to plain `countStudents` when submitting.
- The Java file is named `NumberOfStudentsUnableToEatLunch.java` to match its
  `public class NumberOfStudentsUnableToEatLunch`, as `javac` requires.

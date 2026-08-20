"""Valid Parentheses (LeetCode 20).

Given a string s of the characters ( ) [ ] { }, return True if every closing
bracket matches the most recent unclosed opening bracket of the same type, and
nothing is left open at the end.

The two approaches from https://neetcode.io/solutions/valid-parentheses, plus
the spelling that stacks the expected closer, ordered from brute force to
optimal.
"""

from typing import Callable, Dict, List, Sequence, Tuple

CLOSE_TO_OPEN: Dict[str, str] = {")": "(", "]": "[", "}": "{"}
OPEN_TO_CLOSE: Dict[str, str] = {"(": ")", "[": "]", "{": "}"}


def is_valid_brute_force(s: str) -> bool:
    """Strip adjacent matching pairs until none remain. Time: O(n^2). Space: O(n).

    Every pass deletes at least one pair -- the loop condition guarantees one
    exists -- so there are at most n/2 passes, each scanning and rebuilding an
    O(n) string. What is left when no pair can be removed is exactly the set of
    brackets that never matched, so an empty result means the string was valid.

    str.replace is a literal substring replacement, always. The regex spelling
    of this -- re.sub -- would need every one of these six characters escaped,
    and Java's replaceAll has exactly that bug waiting; see the README.
    """
    while "()" in s or "{}" in s or "[]" in s:
        s = s.replace("()", "").replace("{}", "").replace("[]", "")
    return s == ""


def is_valid_stack(s: str) -> bool:
    """Push openers, pop on a matching closer. Time: O(n). Space: O(n).

    The stack holds every bracket opened but not yet closed, which is the only
    state the problem actually depends on: a closer is legal exactly when it
    matches the most recent unclosed opener. "Most recent" is what makes this a
    stack and what a pair of counters can never capture -- see the README on
    "([)]".

    Space is O(n), not O(1): "((((((((((" stacks the whole input.

    NeetCode ends this with `return True if not stack else False`. `not stack`
    is already the boolean being asked for.
    """
    stack: List[str] = []

    for char in s:
        if char in CLOSE_TO_OPEN:
            if stack and stack[-1] == CLOSE_TO_OPEN[char]:
                stack.pop()
            else:
                return False
        else:
            stack.append(char)

    return not stack


def is_valid_expected_closer(s: str) -> bool:
    """Stack the closer you expect to see next. Time: O(n). Space: O(n).

    The same algorithm as above with the translation moved to the push side: on
    '(' the stack records ')', so a closing bracket compares directly against
    the top instead of mapping itself back to an opener first. One dict lookup
    per character rather than one per character plus one per closer, and the
    comparison reads as the question being asked -- is this the bracket I am
    waiting for?

    `not stack or stack.pop() != char` short-circuits, so pop only runs when
    there is something to pop.

    Note the `elif`: anything that is not an opener is treated as a closer. The
    constraints promise nothing but brackets, so that is safe here; on wider
    input a stray letter would be compared against the expected closer and
    rejected.
    """
    stack: List[str] = []

    for char in s:
        if char in OPEN_TO_CLOSE:
            stack.append(OPEN_TO_CLOSE[char])
        elif not stack or stack.pop() != char:
            return False

    return not stack


SOLUTIONS: Tuple[Tuple[str, Callable[[str], bool]], ...] = (
    ("brute force", is_valid_brute_force),
    ("stack", is_valid_stack),
    ("stack (expected closer)", is_valid_expected_closer),
)

CASES: Sequence[Tuple[str, bool]] = (
    ("()", True),
    ("()[]{}", True),
    ("(]", False),
    ("([])", True),
    ("([)]", False),  # correctly paired, wrongly ordered; see the README
    ("{[()]}", True),  # every type, fully nested
    ("(", False),  # opened, never closed
    (")", False),  # closed, never opened
    ("][", False),  # right pair, backwards
    ("))((", False),  # balanced counts, invalid string
    ("(){}}{", False),  # valid prefix, then a closer with nothing open
    ("(" * 10 + ")" * 10, True),  # deep nesting: the stack grows to n/2
    ("{[}", False),  # odd length, so invalid before you read it
    ("", True),  # empty, though the constraints promise at least one character
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(s) == expected for s, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()

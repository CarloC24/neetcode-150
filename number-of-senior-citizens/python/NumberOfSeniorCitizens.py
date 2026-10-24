"""Number of Senior Citizens (LeetCode 2678).

Each passenger is a 15-character string: a 10-digit phone number, a gender
character, a 2-digit age and a 2-digit seat. Return how many passengers are
strictly older than 60.

The two approaches from https://neetcode.io/solutions/number-of-senior-citizens,
plus a direct string comparison, ordered from most to least work per passenger.
"""

from typing import Callable, List, Sequence, Tuple

# Layout of one passenger string: phone [0, 10), gender 10, age [11, 13), seat [13, 15).
AGE_START = 11
AGE_END = 13


def count_seniors_parse(details: List[str]) -> int:
    """Slice out the age and convert it with int(). Time: O(n). Space: O(1).

    n = len(details). Every string has a fixed length of 15, so each one costs
    O(1). The slice builds a 2-character string per passenger, which is
    constant extra space.
    """
    res = 0
    for d in details:
        if int(d[AGE_START:AGE_END]) > 60:
            res += 1
    return res


def count_seniors_digits(details: List[str]) -> int:
    """Build the age from its two digit characters. Time: O(n). Space: O(1).

    ord(c) - ord("0") turns a digit character into its value, so the age is
    10 * tens + ones. No slice and no parsing.
    """
    res = 0
    for d in details:
        tens = ord(d[AGE_START]) - ord("0")
        ones = ord(d[AGE_START + 1]) - ord("0")
        if 10 * tens + ones > 60:
            res += 1
    return res


def count_seniors_compare(details: List[str]) -> int:
    """Compare the two age characters with "60" as text. Time: O(n). Space: O(1).

    The age is always exactly two digits, zero-padded ("07", not "7"). For
    strings of digits with the same length, comparing them character by
    character gives the same order as comparing the numbers, because the
    digit characters '0'..'9' are in order. So d[11:13] > "60" is the same
    test as age > 60, with no conversion at all.
    """
    return sum(1 for d in details if d[AGE_START:AGE_END] > "60")


SOLUTIONS: Tuple[Tuple[str, Callable[[List[str]], int]], ...] = (
    ("parse", count_seniors_parse),
    ("digits", count_seniors_digits),
    ("compare", count_seniors_compare),
)


def passenger(age: int, seat: int = 0, phone: str = "1234567890", gender: str = "M") -> str:
    """Build one passenger string, so the test cases read as ages."""
    return f"{phone}{gender}{age:02d}{seat:02d}"


CASES: Sequence[Tuple[List[str], int]] = (
    (["7868190130M7522", "5303914400F9211", "9273338290F4010"], 2),
    (["1313579440F2036", "2921522980M5644"], 0),
    ([passenger(60)], 0),  # exactly 60 is not "strictly more than 60"
    ([passenger(61)], 1),  # the youngest senior
    ([passenger(0), passenger(99)], 1),  # age bounds
    ([passenger(6), passenger(16)], 0),  # tens digit 0 or 1 with a 6 in it
    ([passenger(10, seat=99)], 0),  # the seat is old-looking, the passenger isn't
    ([passenger(70, phone="0000000000", gender="O")], 1),  # gender 'O'
    ([passenger(age) for age in range(100)], 39),  # every age once: 61..99
    ([passenger(99, seat=s) for s in range(100)], 100),  # the length ceiling, all seniors
)


def main() -> None:
    for name, solve in SOLUTIONS:
        passed = sum(solve(list(details)) == expected for details, expected in CASES)
        status = "PASS" if passed == len(CASES) else "FAIL"
        print(f"{status}  {name}: {passed}/{len(CASES)} cases")


if __name__ == "__main__":
    main()

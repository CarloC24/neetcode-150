import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Is Subsequence (LeetCode 392).
 *
 * <p>Given two strings s and t, return true if s is a subsequence of t: if
 * every character of s appears in t, in the same order, though not necessarily
 * side by side.
 *
 * <p>The five approaches from https://neetcode.io/solutions/is-subsequence,
 * plus the binary-search answer to the follow-up, ordered from brute force to
 * optimal and then the follow-up.
 */
public class IsSubsequence {

    /**
     * Matches s[i] against t[j], always advancing j. Time: O(m). Space: O(m).
     *
     * <p>NeetCode lists this as O(n * m) time and O(n) space, but the recursion
     * never branches: every call either returns or makes exactly one more call,
     * and every call moves j forward by one. That makes it a chain of at most
     * m + 1 calls -- O(m) time, and O(m) stack, not O(n).
     *
     * <p>At the ceiling that is 10^4 frames, which is more than a cold JVM's
     * default stack holds: it throws StackOverflowError somewhere between 6,000
     * and 7,000 frames until the JIT compiles this method into smaller frames.
     * See the README.
     */
    static boolean isSubsequenceRecursion(String s, String t) {
        return match(s, t, 0, 0);
    }

    private static boolean match(String s, String t, int i, int j) {
        if (i == s.length()) {
            return true;
        }
        if (j == t.length()) {
            return false;
        }
        if (s.charAt(i) == t.charAt(j)) {
            return match(s, t, i + 1, j + 1);
        }
        return match(s, t, i, j + 1);
    }

    /**
     * The recursion above with a memo table. Time: O(m). Space: O(n * m).
     *
     * <p>The memo never gets a hit. A cache only helps when the same (i, j) is
     * reached twice, and that needs a call to branch -- which this one never
     * does. All the table adds is an n * m allocation up front, so this is
     * strictly worse than approach 1. It is here because NeetCode lists it, and
     * because "memoising a recursion that doesn't branch buys nothing" is worth
     * seeing once.
     */
    static boolean isSubsequenceMemo(String s, String t) {
        int[][] memo = new int[s.length()][t.length()];
        for (int[] row : memo) {
            Arrays.fill(row, -1);
        }
        return matchMemo(s, t, 0, 0, memo);
    }

    private static boolean matchMemo(String s, String t, int i, int j, int[][] memo) {
        if (i == s.length()) {
            return true;
        }
        if (j == t.length()) {
            return false;
        }
        if (memo[i][j] != -1) {
            return memo[i][j] == 1;
        }
        boolean result;
        if (s.charAt(i) == t.charAt(j)) {
            result = matchMemo(s, t, i + 1, j + 1, memo);
        } else {
            result = matchMemo(s, t, i, j + 1, memo);
        }
        memo[i][j] = result ? 1 : 0;
        return result;
    }

    /**
     * Fills the whole table bottom-up. Time: O(n * m). Space: O(n * m).
     *
     * <p>dp[i][j] is true when s.substring(i) is a subsequence of
     * t.substring(j). Unlike the two recursions this really does fill every
     * cell, so it is the only approach that actually costs the O(n * m)
     * NeetCode quotes. At the ceiling the table holds 10^6 booleans.
     */
    static boolean isSubsequenceDp(String s, String t) {
        int n = s.length();
        int m = t.length();
        boolean[][] dp = new boolean[n + 1][m + 1];
        for (int j = 0; j <= m; j++) {
            dp[n][j] = true; // the empty suffix of s fits in any suffix of t
        }

        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                if (s.charAt(i) == t.charAt(j)) {
                    dp[i][j] = dp[i + 1][j + 1];
                } else {
                    dp[i][j] = dp[i][j + 1];
                }
            }
        }

        return dp[0][0];
    }

    /**
     * Walks t once, ticking off s as it matches. Time: O(n + m). Space: O(1).
     *
     * <p>The optimal solution for a single query. Taking the earliest match for
     * each character of s is always safe: it leaves the most of t for the
     * characters still to come, so a greedy miss means no match exists.
     */
    static boolean isSubsequenceTwoPointers(String s, String t) {
        int i = 0;
        int j = 0;
        while (i < s.length() && j < t.length()) {
            if (s.charAt(i) == t.charAt(j)) {
                i++;
            }
            j++;
        }
        return i == s.length();
    }

    /**
     * Follow-up: precomputes where each letter next appears.
     *
     * <p>Time: O(26 * m) to build the table, then O(n) per query.
     * Space: O(26 * m).
     *
     * <p>nextAt[j][c] is the first index &gt;= j where letter c appears in t,
     * or m if it never does. Each lookup jumps straight to the next match
     * instead of scanning for it, so once the table exists a query costs O(n)
     * no matter how long t is. Worth it only when many s are checked against
     * one t; for a single query it is strictly more work than two pointers.
     *
     * <p>NeetCode's version uses m + 1 as its "not found" sentinel and a
     * separate m == 0 guard. An extra row at index m, filled with m, covers
     * both: a miss jumps j to m + 1 and fails, and an empty t falls straight
     * into that row.
     */
    static boolean isSubsequenceNextIndex(String s, String t) {
        int m = t.length();
        int[][] nextAt = new int[m + 1][];
        nextAt[m] = new int[26];
        Arrays.fill(nextAt[m], m);
        for (int j = m - 1; j >= 0; j--) {
            nextAt[j] = nextAt[j + 1].clone();
            nextAt[j][t.charAt(j) - 'a'] = j;
        }

        int j = 0;
        for (char c : s.toCharArray()) {
            if (j > m) {
                return false;
            }
            j = nextAt[j][c - 'a'] + 1;
        }
        return j <= m;
    }

    /**
     * Follow-up, lighter: binary-searches each letter's positions in t.
     *
     * <p>Time: O(m) to build the lists, then O(n log m) per query.
     * Space: O(m).
     *
     * <p>Stores, for each letter, the sorted list of indices where it appears in
     * t. To match a character, binary-search its list for the first index at or
     * after j. Each query costs a log factor more than approach 5, but the
     * index is m integers in total instead of 26 * m -- the usual trade when t
     * is large.
     *
     * <p>Collections.binarySearch returns -(insertion point) - 1 on a miss, so
     * the decode below turns either outcome into "first index &gt;= j". The
     * lists hold distinct values, so a hit is already the first one.
     */
    static boolean isSubsequenceBinarySearch(String s, String t) {
        List<List<Integer>> positions = new ArrayList<>();
        for (int c = 0; c < 26; c++) {
            positions.add(new ArrayList<>());
        }
        for (int index = 0; index < t.length(); index++) {
            positions.get(t.charAt(index) - 'a').add(index);
        }

        int j = 0;
        for (char c : s.toCharArray()) {
            List<Integer> indices = positions.get(c - 'a');
            int k = Collections.binarySearch(indices, j);
            if (k < 0) {
                k = -k - 1;
            }
            if (k == indices.size()) {
                return false;
            }
            j = indices.get(k) + 1;
        }
        return true;
    }

    /** One test case: two inputs and the expected answer. */
    private static class Case {
        final String s;
        final String t;
        final boolean expected;

        Case(String s, String t, boolean expected) {
            this.s = s;
            this.t = t;
            this.expected = expected;
        }
    }

    public static void main(String[] args) {
        Case[] cases = {
            new Case("node", "neetcode", true),
            new Case("axc", "ahbgdc", false),
            new Case("abc", "ahbgdc", true),
            new Case("", "ahbgdc", true), // the empty string is a subsequence of everything
            new Case("", "", true),
            new Case("a", "", false), // empty t, non-empty s
            new Case("abc", "abc", true), // s == t
            new Case("abcd", "abc", false), // s longer than t
            new Case("ba", "ab", false), // right letters, wrong order
            new Case("aaa", "aa", false), // t runs out of repeats
            new Case("aa", "aba", true), // repeats split by another letter
            new Case("c", "abc", true), // match on the last character of t
            new Case("b".repeat(100), "a".repeat(500) + "b".repeat(100), true), // long t
            new Case("b".repeat(101), "a".repeat(500) + "b".repeat(100), false), // one short
        };

        Map<String, BiPredicate<String, String>> solutions = new LinkedHashMap<>();
        solutions.put("recursion", IsSubsequence::isSubsequenceRecursion);
        solutions.put("recursion + memo", IsSubsequence::isSubsequenceMemo);
        solutions.put("bottom-up dp", IsSubsequence::isSubsequenceDp);
        solutions.put("two pointers", IsSubsequence::isSubsequenceTwoPointers);
        solutions.put("next-index table", IsSubsequence::isSubsequenceNextIndex);
        solutions.put("binary search", IsSubsequence::isSubsequenceBinarySearch);

        for (Map.Entry<String, BiPredicate<String, String>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                if (solution.getValue().test(testCase.s, testCase.t) == testCase.expected) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d cases%n",
                    passed == cases.length ? "PASS" : "FAIL",
                    solution.getKey(),
                    passed,
                    cases.length);
        }

        // The LeetCode ceiling: |s| = 100, |t| = 10^4. Every match sits in the
        // last 100 characters of t, so the scan, and the recursions, must cover
        // all of t. Whether the recursions survive depends on how warm the JIT
        // is (see the README), so they report a NOTE rather than PASS or FAIL.
        String ceilingS = "z".repeat(100);
        String ceilingT = "a".repeat(9900) + "z".repeat(100);
        for (Map.Entry<String, BiPredicate<String, String>> solution : solutions.entrySet()) {
            boolean recursive = solution.getKey().startsWith("recursion");
            String status;
            String outcome;
            try {
                boolean result = solution.getValue().test(ceilingS, ceilingT);
                status = result ? "PASS" : "FAIL";
                outcome = String.valueOf(result);
            } catch (StackOverflowError e) {
                status = recursive ? "NOTE" : "FAIL";
                outcome = "StackOverflowError";
            }
            if (recursive && status.equals("PASS")) {
                status = "NOTE";
            }
            System.out.printf("%s  %s at |t| = 10^4: %s%n", status, solution.getKey(), outcome);
        }
    }
}

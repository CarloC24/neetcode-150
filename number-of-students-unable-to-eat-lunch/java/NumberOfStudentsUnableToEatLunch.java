import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntBiFunction;
import java.util.stream.IntStream;

/**
 * Number of Students Unable to Eat Lunch (LeetCode 1700).
 *
 * <p>Students stand in a queue and sandwiches sit in a stack, each a 0
 * (circular) or a 1 (square). The front student takes the top sandwich if it's
 * the type they want; otherwise they go to the back of the queue. This stops
 * when no one left in the queue wants the top sandwich. Return how many
 * students can't eat.
 *
 * <p>The three approaches from
 * https://neetcode.io/solutions/number-of-students-unable-to-eat-lunch,
 * ordered from full simulation to counting.
 */
public class NumberOfStudentsUnableToEatLunch {

    /**
     * Simulates the queue with an ArrayDeque. Time: O(n^2). Space: O(n).
     *
     * <p>For each sandwich, rotates the queue until the front student wants it.
     * If a full rotation of n moves finds no one, nobody left wants it, and
     * every remaining student goes hungry. Each sandwich can cost up to n
     * rotations. The queue never holds more than n students, so n moves always
     * cover everyone still in it.
     *
     * <p>NeetCode uses a LinkedList; ArrayDeque is the faster, usual choice for
     * a queue. q.peek() != sandwich compares values because sandwich is an int,
     * which unboxes the Integer. Two Integers compared with != would compare
     * references instead.
     */
    static int countStudentsQueue(int[] students, int[] sandwiches) {
        int n = students.length;
        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int student : students) {
            q.offer(student);
        }
        int res = n;
        for (int sandwich : sandwiches) {
            int cnt = 0;
            while (cnt < n && q.peek() != sandwich) {
                q.offer(q.poll());
                cnt++;
            }
            if (q.peek() == sandwich) {
                q.poll();
                res--;
            } else {
                break;
            }
        }
        return res;
    }

    /**
     * Walks the students array in a circle instead of rotating a queue.
     * Time: O(n^2). Space: O(1). Overwrites students.
     *
     * <p>The same simulation without the deque. A student who has eaten is
     * marked -1, which matches no sandwich, so the walk steps over them. idx
     * stays where the last student ate, which is where the next round starts,
     * just like the front of the rotated queue.
     */
    static int countStudentsCircular(int[] students, int[] sandwiches) {
        int n = students.length;
        int idx = 0;
        int res = n;
        for (int sandwich : sandwiches) {
            int cnt = 0;
            while (cnt < n && students[idx] != sandwich) {
                idx = (idx + 1) % n;
                cnt++;
            }
            if (students[idx] == sandwich) {
                students[idx] = -1;
                res--;
            } else {
                break;
            }
        }
        return res;
    }

    /**
     * Counts each preference and serves sandwiches until a type runs out.
     * Time: O(n). Space: O(1).
     *
     * <p>The optimal solution. The queue keeps rotating, so any student who
     * wants the top sandwich reaches the front eventually. Their position
     * doesn't matter, only whether one exists. Two counters replace the queue.
     * The first sandwich whose type has no takers left ends it, and everyone
     * still waiting is the answer. See the README.
     */
    static int countStudentsCounting(int[] students, int[] sandwiches) {
        int[] cnt = new int[2];
        for (int s : students) {
            cnt[s]++;
        }
        int res = students.length;
        for (int sandwich : sandwiches) {
            if (cnt[sandwich] == 0) {
                break;
            }
            cnt[sandwich]--;
            res--;
        }
        return res;
    }

    /** One test case: the inputs and the expected answer. */
    private static class Case {
        final int[] students;
        final int[] sandwiches;
        final int expected;

        Case(int[] students, int[] sandwiches, int expected) {
            this.students = students;
            this.sandwiches = sandwiches;
            this.expected = expected;
        }
    }

    /** count copies of a, followed by count2 copies of b. */
    private static int[] runs(int a, int count, int b, int count2) {
        return IntStream.concat(
                        IntStream.generate(() -> a).limit(count), IntStream.generate(() -> b).limit(count2))
                .toArray();
    }

    public static void main(String[] args) {
        int[] alternating = IntStream.range(0, 100).map(i -> i % 2).toArray();

        Case[] cases = {
            new Case(new int[] {1, 1, 0, 0}, new int[] {0, 1, 0, 1}, 0),
            new Case(new int[] {1, 1, 1, 0, 0, 1}, new int[] {1, 0, 0, 0, 1, 1}, 3),
            new Case(new int[] {0}, new int[] {0}, 0), // the smallest input
            new Case(new int[] {0}, new int[] {1}, 1),
            new Case(new int[] {1, 1, 1}, new int[] {0, 1, 1}, 3), // stuck at once: nobody eats
            new Case(new int[] {0, 1}, new int[] {1, 0}, 0), // order never matters: everyone eats
            new Case(new int[] {0, 0, 1}, new int[] {1, 1, 0}, 2), // the second 1 is stuck
            new Case(new int[] {1, 0, 1}, new int[] {1, 1, 0}, 0),
            new Case(runs(0, 100, 0, 0), runs(0, 99, 1, 1), 1), // ceiling: stuck on the last
            new Case(alternating, runs(0, 50, 1, 50), 0), // ceiling: everyone eats
            new Case(runs(1, 99, 0, 1), runs(0, 1, 1, 99), 0), // 99 rotations before the first meal
            new Case(runs(1, 100, 0, 0), runs(0, 1, 1, 99), 100), // stuck at once, 99 unserved
        };

        Map<String, ToIntBiFunction<int[], int[]>> solutions = new LinkedHashMap<>();
        solutions.put("queue", NumberOfStudentsUnableToEatLunch::countStudentsQueue);
        solutions.put("circular index", NumberOfStudentsUnableToEatLunch::countStudentsCircular);
        solutions.put("counting", NumberOfStudentsUnableToEatLunch::countStudentsCounting);
        Set<String> mutatesInput = Set.of("circular index");

        for (Map.Entry<String, ToIntBiFunction<int[], int[]>> solution : solutions.entrySet()) {
            int passed = 0;
            for (Case testCase : cases) {
                int got = solution.getValue()
                        .applyAsInt(testCase.students.clone(), testCase.sandwiches.clone());
                if (got == testCase.expected) {
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

        int[] students = {1, 1, 0, 0};
        int[] sandwiches = {0, 1, 0, 1};
        for (Map.Entry<String, ToIntBiFunction<int[], int[]>> solution : solutions.entrySet()) {
            if (!mutatesInput.contains(solution.getKey())) {
                solution.getValue().applyAsInt(students, sandwiches);
            }
        }
        boolean unchanged = Arrays.equals(students, new int[] {1, 1, 0, 0})
                && Arrays.equals(sandwiches, new int[] {0, 1, 0, 1});
        System.out.printf(
                "%s  input left unmodified (all but circular index): %s, %s%n",
                unchanged ? "PASS" : "FAIL",
                Arrays.toString(students),
                Arrays.toString(sandwiches));
    }
}

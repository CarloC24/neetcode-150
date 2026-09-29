import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * Merge Two Sorted Lists (LeetCode 21).
 *
 * <p>Given the heads of two sorted linked lists, merge them into one sorted list
 * by splicing together the nodes of the first two, and return its head.
 *
 * <p>The two approaches from https://neetcode.io/solutions/merge-two-sorted-lists,
 * plus a brute force that ignores the fact that the inputs are already sorted,
 * ordered from brute force to optimal.
 *
 * <p>Approaches 2 and 3 splice the existing nodes together; approach 1 builds
 * new ones. The problem statement asks for splicing, so approach 1 is here only
 * as the baseline -- see the README.
 */
public class MergeTwoSortedLists {

    /** NeetCode's definition, unchanged. */
    public static class ListNode {
        int val;
        ListNode next;

        ListNode() {}

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    /**
     * Collects every value, sorts, rebuilds. Time: O((m + n) log(m + n)). Space: O(m + n).
     *
     * <p>The brute force, and the answer you would give if nobody told you the
     * inputs were sorted. It throws that fact away and pays a log factor to
     * recover it.
     *
     * <p>It also allocates a fresh list instead of splicing the given nodes,
     * which the problem statement explicitly asks for. The values come out
     * right, so every test here passes -- but the nodes are not the ones the
     * caller handed in.
     */
    static ListNode mergeSort(ListNode list1, ListNode list2) {
        List<Integer> values = new ArrayList<>();
        for (ListNode head : new ListNode[] {list1, list2}) {
            for (ListNode curr = head; curr != null; curr = curr.next) {
                values.add(curr.val);
            }
        }
        Collections.sort(values);

        ListNode dummy = new ListNode();
        ListNode tail = dummy;
        for (int value : values) {
            tail.next = new ListNode(value);
            tail = tail.next;
        }

        return dummy.next;
    }

    /**
     * Takes the smaller head, merges the rest behind it. Time: O(m + n). Space: O(m + n).
     *
     * <p>Space is O(m + n) for the call stack: each call consumes one node, so
     * the recursion goes as deep as the two lists combined. LeetCode caps each
     * list at 50 nodes, so that is at most 100 frames.
     *
     * <p>The base cases do more than stop the recursion. When one list runs out,
     * the other is returned whole: whatever is left of it is already sorted, so
     * it can be attached as-is without walking it.
     *
     * <p>{@code <=} rather than {@code <} keeps the merge stable -- on a tie, the
     * node from list1 goes first.
     */
    static ListNode mergeRecursive(ListNode list1, ListNode list2) {
        if (list1 == null) {
            return list2;
        }
        if (list2 == null) {
            return list1;
        }

        if (list1.val <= list2.val) {
            list1.next = mergeRecursive(list1.next, list2);
            return list1;
        }
        list2.next = mergeRecursive(list1, list2.next);
        return list2;
    }

    /**
     * Walks both lists with a dummy head. Time: O(m + n). Space: O(1).
     *
     * <p>The optimal solution, and the one to write.
     *
     * <p>The dummy node is the trick worth keeping. Without it the first node of
     * the result is a special case -- you have to decide which list it comes
     * from before the loop, and handle either list being null -- and every
     * append inside the loop needs an "is this the first one?" check. With it,
     * tail always has somewhere to hang the next node, and the real head is
     * simply dummy.next at the end.
     *
     * <p>The loop stops as soon as either list runs out, and the leftover is
     * attached in one assignment instead of being copied node by node.
     */
    static ListNode mergeIterative(ListNode list1, ListNode list2) {
        ListNode dummy = new ListNode();
        ListNode tail = dummy;

        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) {
                tail.next = list1;
                list1 = list1.next;
            } else {
                tail.next = list2;
                list2 = list2.next;
            }
            tail = tail.next;
        }

        tail.next = list1 != null ? list1 : list2;

        return dummy.next;
    }

    /** Builds a list from values, back to front so each node gets its successor. */
    static ListNode build(int[] values) {
        ListNode head = null;
        for (int i = values.length - 1; i >= 0; i--) {
            head = new ListNode(values[i], head);
        }
        return head;
    }

    /**
     * Reads a list back into values, refusing to loop forever.
     *
     * <p>A splicing bug can close the result into a cycle, so an unbounded walk
     * here would hang the file instead of failing it. Stopping past the limit
     * yields an over-long list that no expected answer matches, which reports
     * as FAIL and lets the remaining cases run.
     */
    static List<Integer> toValues(ListNode head, int limit) {
        List<Integer> values = new ArrayList<>();
        while (head != null && values.size() <= limit) {
            values.add(head.val);
            head = head.next;
        }
        return values;
    }

    private static int[] range(int start, int end, int step) {
        int[] values = new int[(end - start + step - 1) / step];
        for (int i = 0; i < values.length; i++) {
            values[i] = start + i * step;
        }
        return values;
    }

    public static void main(String[] args) {
        int[][][] cases = {
            {{1, 2, 4}, {1, 3, 4}}, // the LeetCode example
            {{}, {}}, // both empty
            {{}, {0}}, // one empty: the other comes back whole
            {{0}, {}}, // the same, mirrored
            {{1, 2, 3}, {4, 5, 6}}, // list1 runs out first; list2 is attached in one step
            {{4, 5, 6}, {1, 2, 3}}, // the same, mirrored
            {{1, 3, 5, 7}, {2, 4, 6, 8}}, // perfect interleave
            {{1, 1, 1}, {1, 1}}, // all ties
            {{5}, {1, 2, 3, 4, 6}}, // a single node lands in the middle
            {{-100, 0, 100}, {-100, 100}}, // the value bounds, duplicated across lists
            {{-3, -2, -1}, {-5, -4}}, // all negative
            {range(0, 100, 2), range(1, 100, 2)}, // 50 + 50: the largest input allowed
        };

        Map<String, BinaryOperator<ListNode>> solutions = new LinkedHashMap<>();
        solutions.put("sort", MergeTwoSortedLists::mergeSort);
        solutions.put("recursion", MergeTwoSortedLists::mergeRecursive);
        solutions.put("iteration", MergeTwoSortedLists::mergeIterative);

        for (Map.Entry<String, BinaryOperator<ListNode>> solution : solutions.entrySet()) {
            int passed = 0;
            for (int[][] pair : cases) {
                List<Integer> expected = new ArrayList<>();
                for (int[] values : pair) {
                    for (int value : values) {
                        expected.add(value);
                    }
                }
                Collections.sort(expected);
                List<Integer> result =
                        toValues(
                                solution.getValue().apply(build(pair[0]), build(pair[1])),
                                expected.size() + 1);
                if (result.equals(expected)) {
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
    }
}

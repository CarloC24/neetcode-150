import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Reverse Linked List (LeetCode 206).
 *
 * <p>Given the head of a singly linked list, reverse it and return the new head.
 *
 * <p>The two approaches from https://neetcode.io/solutions/reverse-linked-list,
 * plus the explicit-stack version that shows what the recursion is doing
 * implicitly, ordered from brute force to optimal.
 *
 * <p>Every approach here relinks the existing nodes in place. None of them
 * allocate a new list, and none of them move values between nodes -- see the
 * README on why that distinction matters even though the two are
 * indistinguishable from outside.
 */
public class ReverseLinkedList {

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
     * Pushes every node, then pops them back into order. Time: O(n). Space: O(n).
     *
     * <p>The brute force, and the honest picture of what approach 2 does:
     * reversal is a last-in-first-out problem, so the naive answer is to reach
     * for the data structure that spells that out. The recursion below uses the
     * exact same stack -- it just lets the JVM own it.
     *
     * <p>Popping yields the nodes back to front, which is the order they need to
     * be linked in. Each node points at whatever is left on top of the stack,
     * and the final pop -- the original head, now the tail -- gets null.
     *
     * <p>That last assignment is the one to notice. Without it the original head
     * keeps pointing forward at the second node, which now points back at it,
     * and the list closes into a cycle.
     *
     * <p>ArrayDeque rather than java.util.Stack, which extends Vector and
     * synchronizes every method for a lock nobody here contends.
     */
    static ListNode reverseStack(ListNode head) {
        Deque<ListNode> stack = new ArrayDeque<>();
        for (ListNode curr = head; curr != null; curr = curr.next) {
            stack.push(curr);
        }

        ListNode newHead = stack.peek();
        while (!stack.isEmpty()) {
            ListNode node = stack.pop();
            node.next = stack.peek();
        }

        return newHead;
    }

    /**
     * Reverses the tail, then attaches the head behind it. Time: O(n). Space: O(n).
     *
     * <p>Space is O(n) for the call stack, not O(1): the recursion goes exactly
     * as deep as the list is long.
     *
     * <p>The whole trick is {@code head.next.next = head}. The recursive call
     * reverses everything after head and hands back the new head, but it does
     * not touch head's own pointer -- so head.next still refers to the node that
     * started the sublist and has just become its tail. Pointing that node's
     * next back at head appends head to the end, which is where it belongs.
     *
     * <pre>
     *     head -&gt; [2 -&gt; 3]               before
     *     head -&gt; [3 -&gt; 2]               after the recursive call; head.next is 2
     *     head -&gt; [3 -&gt; 2 -&gt; head]       after head.next.next = head
     *             [3 -&gt; 2 -&gt; head]       after head.next = null
     * </pre>
     *
     * <p>Then {@code head.next = null}, because head is now the last node and is
     * still pointing forward at the node behind it. Drop that line and every
     * list of two or more nodes comes back as a cycle -- but a single-node list
     * still passes, so the bug hides on the smallest test.
     *
     * <p>NeetCode spells the base case differently: it returns early only on
     * null, seeds {@code newHead = head}, and guards the recursion with
     * {@code if (head.next != null)}. Same algorithm and same result; this form
     * short-circuits on the last node instead of falling through with a
     * redundant assignment.
     *
     * <p>Unlike the Python version, this is safe at the stated constraints. A
     * 1000-node list is nowhere near the default JVM stack -- it takes roughly
     * 39,000 frames to overflow on this machine. See the README.
     */
    static ListNode reverseRecursive(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }

        ListNode newHead = reverseRecursive(head.next);
        head.next.next = head;
        head.next = null;

        return newHead;
    }

    /**
     * Flips each pointer as you walk. Time: O(n). Space: O(1).
     *
     * <p>The optimal solution, and the one to write. Three names -- prev, curr
     * and the temp holding curr.next -- are all the state the problem needs.
     *
     * <p>temp is not a convenience. Overwriting curr.next is what the loop is
     * for, so the rest of the list has to be saved before that happens or it is
     * unreachable. Reorder those two lines and the first iteration strands
     * everything past the head.
     *
     * <p>prev starts at null, which is what makes the original head terminate
     * the reversed list, and it ends on the last node visited -- the new head.
     * Return prev, not curr: curr is null by the time the loop exits, every
     * time.
     */
    static ListNode reverseIterative(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;

        while (curr != null) {
            ListNode temp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = temp;
        }

        return prev;
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
     * <p>A wrong answer to this problem is usually a cycle, not a scramble, so
     * an unbounded walk here would hang the file instead of failing it. Stopping
     * past the limit yields an over-long list that no expected answer matches,
     * which reports as FAIL and lets the remaining cases run.
     */
    static List<Integer> toValues(ListNode head, int limit) {
        List<Integer> values = new ArrayList<>();
        while (head != null && values.size() <= limit) {
            values.add(head.val);
            head = head.next;
        }
        return values;
    }

    private static int[] range(int n) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = i;
        }
        return values;
    }

    public static void main(String[] args) {
        int[][] cases = {
            {1, 2, 3, 4, 5}, // the LeetCode example
            {1, 2}, // shortest case that cycles if the tail is not nulled
            {1}, // single node, where that same bug still passes
            {}, // empty list; the constraints allow length 0
            {1, 2, 3}, // odd length
            {1, 2, 3, 4}, // even length
            {0, 0, 0}, // duplicates must survive
            {1, 2, 1}, // palindromic: reversing changes nothing, so this alone proves nothing
            {-5000, 5000}, // the value bounds
            {-1, -2, -3}, // all negative
            range(100), // long enough to catch an off-by-one at either end
            range(500), // deep; the Python file caps here for a reason, see the README
        };

        Map<String, UnaryOperator<ListNode>> solutions = new LinkedHashMap<>();
        solutions.put("stack", ReverseLinkedList::reverseStack);
        solutions.put("recursion", ReverseLinkedList::reverseRecursive);
        solutions.put("iteration", ReverseLinkedList::reverseIterative);

        for (Map.Entry<String, UnaryOperator<ListNode>> solution : solutions.entrySet()) {
            int passed = 0;
            for (int[] values : cases) {
                List<Integer> expected = new ArrayList<>();
                for (int i = values.length - 1; i >= 0; i--) {
                    expected.add(values[i]);
                }
                List<Integer> result =
                        toValues(solution.getValue().apply(build(values)), values.length + 1);
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

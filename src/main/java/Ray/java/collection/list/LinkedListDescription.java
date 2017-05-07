package Ray.java.collection.list;

import java.util.LinkedList;

/**
 * Created by Ray on 17/5/6.
 * Describe LikedList Class.
 */
public class LinkedListDescription {

    /**
     *
     * interface Collection<E> extends Iterable<E>
     *
     * interface List<E> extends Collection<E>
     *
     * interface Queue<E> extends Collection<E>
     * interface Deque<E> extends Queue<E>
     *
     * abstract class AbstractCollection<E> implements Collection<E>
     * abstract class AbstractList<E> extends AbstractCollection<E> implements List<E>
     * abstract class AbstractSequentialList<E> extends AbstractList<E>
     *
     * class LinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, java.io.Serializable
     *
     * 0.  LinkedList 以双向链表的为基础,构建 List.
     * 1.  transient int size = 0 表示 LinkedList 中节点(元素)的个数.
     * 2.  Node<E> 表示一个节点.它包含三个属性:
     *        E item           表示该节点的值.
     *        Node<E> next     表示该节点的下一个节点
     *        Node<E> prev     表示该节点的上一个节点
     * 3.  transient Node<E> first 第一个节点.首节点的前一个节点为 null.
     * 4.  transient Node<E> last 最后一个节点.尾节点的后一个节点为 null.
     * 5.  add 方法.在链表(尾部)添加一个元素.新建一个 Node,并为 item 赋值,prev 赋值为 last节点,next 赋值为 null.
     *     并将 last 节点的 next 赋值为当前节点.
     * 6.  remove 方法.从第一个节点开始遍历,找到符合参数条件的第一个节点.将该节点从链表中移除.
     * 7.  set 方法.采用二分法查找元素所在节点,重新给该节点的 item 赋值.(使用二分法确定元素下标在链表的前部,还是后部.
     *     是前部,则从第一个元素开始遍历,后部则从最后一个开始遍历.整个过程只是用一次二分法)
     * 8.  get 方法.查找过程与 set 方法的查找过程相同.
     * 9.  非同步. 同步操作: List list = Collections.synchronizedList(new LinkedList(...))
     * 10. fail-fast behavior.
     * 11. 通过迭代器遍历最快.
     * 12. 对元素的操作:
     *                     第一个元素（头部）                 最后一个元素（尾部）
     *        插入    addFirst(e)    offerFirst(e)    addLast(e)       offerLast(e)
     *        移除    removeFirst()  pollFirst()      removeLast()     pollLast()
     *        查看    getFirst()     peekFirst()      getLast()        peekLast()
     *
     *        作为双端队列时:
     *            队列方法        等效方法
     *           add(e)        addLast(e)
     *           offer(e)      offerLast(e)
     *           remove()      removeFirst()
     *           poll()        pollFirst()
     *           element()     getFirst()
     *           peek()        peekFirst()
     *
     *         作为LIFO的栈时:
     *             栈方法         等效方法
     *            push(e)      addFirst(e)
     *            pop()        removeFirst()
     *            peek()       peekFirst()
     */

    public static void main(String[] args) {

        LinkedList<String> list = new LinkedList<>();

        list.add("a");
        list.add("b");
        list.add("d");
        System.out.println("list: " + list);

        list.add(3, "c");
        System.out.println("list.add(3,\"c\"); " + list);

        //list.add(7,"x"); // IndexOutOfBoundsException: Index: 7, Size: 4

        list.addFirst("x");
        System.out.println("list.addFirst(\"x\"); " + list);

        list.set(2, "y"); // not recommend
        System.out.println("list.set(2,\"y\"); " + list);

        //获取第一个元素 失败抛出异常
        System.out.println("list.get(0) " + list.get(0) + "\t" + list);
        System.out.println("list.peek() " + list.peek() + "\t" + list);
        System.out.println("list.peekFirst() " + list.peekFirst() + "\t" + list);
        System.out.println("list.getFirst() " + list.getFirst() + "\t" + list);
        System.out.println("list.element() " + list.element() + "\t" + list);


        //删除第一个元素 失败抛出异常
        System.out.println("list.removeFirst() " + list.removeFirst() + "\t" + list);
        System.out.println("list.remove(); " + list.remove() + "\t" + list);
        System.out.println("list.pollFirst(); " + list.pollFirst() + "\t" + list);
        System.out.println("list.poll(); " + list.poll() + "\t" + list);
        System.out.println("list.pop(); " + list.pop() + "\t" + list);


        // 将LinkedList当作 LIFO 栈 (只允许在一端操作)
        LinkedList<String> stack = new LinkedList<>();

        // 将1,2,3,4添加到栈中
        stack.push("1");
        stack.push("2");
        stack.push("3");
        stack.push("4");

        // 打印栈
        System.out.println("stack: " + stack);

        // 查看栈顶元素
        System.out.println("stack.peek(): " + stack.peek() + "\t" + stack);
        System.out.println("stack.peekFirst(): " + stack.peekFirst() + "\t" + stack);

        // 取出栈顶元素
        System.out.println("stack.pop(): " + stack.pop() + "\t" + stack);


        // 将LinkedList当作 双端队列
        LinkedList<String> deque = new LinkedList<>();

        // 将10,20,30,40添加到队列 插入到队末
        deque.add("10");
        deque.add("20");
        deque.add("30");
        deque.offer("40");
        deque.offerFirst("00");
        deque.offerLast("50");

        // 打印队列
        System.out.println("queue: " + deque);

        // 查看队首元素
        System.out.println("queue.element(): " + deque.element() + "\t" + deque);
        System.out.println("queue.peek(): " + deque.peek() + "\t" + deque);
        System.out.println("queue.peekFirst(): " + deque.peekFirst() + "\t" + deque);

        // 查看队尾元素
        System.out.println("queue.peekLast(): " + deque.peekLast() + "\t" + deque);

        // 取出队首元素
        System.out.println("queue.poll(): " + deque.poll() + "\t" + deque);
        System.out.println("queue.pollFirst(): " + deque.pollFirst() + "\t" + deque);

        // 取出队尾元素
        System.out.println("queue.pollLast(): " + deque.pollLast() + "\t" + deque);

        // 删除队首元素
        System.out.println("queue.remove(): " + deque.remove() + "\t" + deque);

    }
}

package Ray.java.collection;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by Ray on 17/5/7.
 * Describe Stack Class.
 */
public class StackDescription {

    /**
     * class Stack<E> extends Vector<E>
     *
     *      Stack 栈. 先进后出(FILO). 是Vector的子类, 所以是基于数组构建的一种集合.
     *
     * 0. 提供增删查改的方法,这些方法都依赖Vector的方法.所以特性与父类保持一致.
     * 1. 采用synchronized关键字保证线程安全.
     * 2. Stack.search 方法.查找该元素距离栈顶的距离.
     */


    public static void main(String[] args) {

        Stack<String> stack = new Stack<>();
        // 将0,1,2,3,4,5添加到栈中
        for (int i = 0; i < 6; i++) {
            stack.push(i + "");
        }

        int size = stack.size();

        // 遍历并打印出该栈
        for (int i = 0; i < size; i++) {
            String element = stack.get(i);
            System.out.println(element);
        }

        // 通过Iterator去遍历Stack
        for (Iterator<String> i = stack.iterator(); i.hasNext(); ) {
            String element = i.next();
            System.out.println(element);
        }

        // 查找元素"2"在栈中的位置
        int position = stack.search("2");
        System.out.println("The position that element equals 2 is: " + position);

        // 查看(peek)栈顶元素之后，遍历栈
        String peek = stack.peek();
        System.out.println("peek : " + peek + "\t stack : " + stack);

        // 弹出(pop)栈顶元素之后，遍历栈
        String pop = stack.pop();
        System.out.println("pop : " + pop + "\tstack : " + stack);

    }
}

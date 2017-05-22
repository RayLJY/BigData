package Ray.java.collection.list;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ray on 17/5/6.
 * Describe ArrayList Class.
 */
public class ArrayListDescription {

    /**
     * interface Iterable<T>
     * interface Collection<E> extends Iterable<E>
     *
     * interface List<E> extends Collection<E>
     *
     * abstract class AbstractCollection<E> implements Collection<E>
     * class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
     *
     * 0. ArrayList 以数组为基础,构建 List.
     * 1. transient Object[] elementData,ArrayList 中存放元素的数组,transient 表示在对象被序列化时,elementData 不包含在内.
     * 2. int size, 表示 ArrayList 中元素的个数.
     * 3. add,addAll 方法.检查 elementData 的 length,
     *    当添加元素后的元素个数大于 length 时,新建一个 1.5*length 大小的数组,并将当前 elementData 的元素拷贝到新数组中,
     *    (当小于等于 length 时)并从 elementData 的第 size 个位置开始,将添加的元素添加到 elementData 中.
     * 4. remove 方法.查找被移除元素的下标,然后将 elementData 中排序在该下标之后的元素,向前移动(copy)1位(移除元素个数),
     *    并将最后一位元素设置为null.
     * 5. get 方法.先检查参数是否越界,然后数据下标获取值,并返回.
     * 6. set 方法.先检查参数是否越界,覆盖原来 elementData 相应位置的值,并返回被覆盖的值.
     * 7. clear 方法.遍历 elementData,逐个赋值为 null,并设置 size 为0.
     * 8. 非同步. 同步操作: List list = Collections.synchronizedList(new ArrayList(...))
     * 9. fail-fast behavior:
     *       transient int modCount = 0,表示该列表被结构性修改(add remove 等改变 List 的 Size)的次数,当通过 List 生成一个迭代器,
     *       迭代器在遍历过程中是直接访问内部数据,为了保证在访问过程中数据不被修改,迭代器内部生成一个标记 expectedModCount = modCount,
     *       当通过 List.add/remove 改变 List 结构时 modCount+1,但不会维护标记 expectedModCount 值,
     *       当通过 Iterator.add/remove 改变 List(Iterator) 结构时,会同步维护 modCount 和 expectedModCount,
     *       iterator.next()方法会检查该 expectedModCount 与 modCount是否相等,不相等时,抛出 ConcurrentModificationException.
     * 10. 通过迭代器遍历最快.
     * 
     */

    public static void main(String[] args) throws InterruptedException {

        List<String> list = new ArrayList<>(3);
        list.add("a");
        list.add("v");
        list.add("c");
        list.add("d");

        //
        int size = list.size();
        for (int i = 0; i < size; i++) {
            System.out.println(list.get(i));
        }

        for (String s : list) {
            System.out.println(s);
        }

        //list.forEach(s -> System.out.println(s));
        list.forEach(System.out::println);

        Iterator<String> i = list.iterator();
        while (i.hasNext()) {
            String ss = i.next();
            System.out.println(ss);
        }

        // 获取第1个元素
        System.out.println("the first element is: " + list.get(0));

        // 获取ArrayList的大小
        System.out.println("ArrayList size = : " + list.size());

        // 判断list中是否包含"d"
        System.out.println("ArrayList contains d is: " + list.contains("d"));

        // 设置第2个元素为x
        list.set(1, "x");
        System.out.println("list.set(1, \"x\");\t" + list);

        // 删除"v"
        list.remove("x");
        System.out.println("list.remove(\"x\");\t" + list);

        // 清空ArrayList
        list.clear();
        System.out.println("list.clear();\t" + list);

        // 判断ArrayList是否为空
        System.out.println("ArrayList is empty: " + list.isEmpty());

        // show fail-fast behavior
        Thread.sleep(100);
        list.add("a");
        list.add("v");
        list.add("c");

        Iterator<String> ite = list.iterator();
        try {
            while (ite.hasNext()) {
                String ss = ite.next();
                list.remove(ss);
                System.out.println(ss);
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }
}
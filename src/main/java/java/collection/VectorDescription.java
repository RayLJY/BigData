package java.collection;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by Ray on 17/5/6.
 * Describe Vector Class.
 */
public class VectorDescription {

    /**
     * interface Iterable<T>
     * interface Collection<E> extends Iterable<E>
     *
     * interface List<E> extends Collection<E>
     *
     * abstract class AbstractCollection<E> implements Collection<E>
     * abstract class AbstractList<E> extends AbstractCollection<E> implements List<E>
     *
     * class Vector<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
     *
     * Vector 是线程安全的 ArrayList. 特征,特性都极为相似.增删改查操作过程一致.(扩容时,以固定值(default 0)或当前容量的2倍扩容)
     */


    public static void main(String[] args) {

        // 新建Vector
        Vector<String> vec = new Vector<>();

        // 添加元素
        vec.add("1");
        vec.add("2");
        vec.add("3");
        vec.add("4");
        vec.add("5");

        // 设置第一个元素为100
        vec.set(0, "100");
        // 将“500”插入到第3个位置
        vec.add(2, "300");
        System.out.println("vec: " + vec);

        // (顺序查找)获取100的索引
        System.out.println("vec.indexOf(\"100\"): " + vec.indexOf("100"));

        // (倒序查找)获取100的索引
        System.out.println("vec.lastIndexOf(\"100\"): " + vec.lastIndexOf("100"));

        // 获取第一个元素
        System.out.println("vec.firstElement(): " + vec.firstElement());

        // 获取第3个元素
        System.out.println("vec.elementAt(2): " + vec.elementAt(2));

        // 获取最后一个元素
        System.out.println("vec.lastElement(): " + vec.lastElement());

        // 获取Vector的大小
        System.out.println("size: " + vec.size());

        // 获取Vector的总的容量
        System.out.println("capacity: " + vec.capacity());

        // 获取vector的“第2”到“第4”个元素
        System.out.println("vec 2 to 4: " + vec.subList(1, 4));

        // 通过Enumeration遍历Vector
        Enumeration enu = vec.elements();
        while (enu.hasMoreElements())
            System.out.println("nextElement(): " + enu.nextElement());


        Vector<String> retainVec = new Vector<>();
        retainVec.add("100");
        retainVec.add("300");

        // 获取“vec”中包含在“retainVec中的元素”的集合
        System.out.println("vec.retain(): " + vec.retainAll(retainVec));
        System.out.println("vec:" + vec);

        // 获取vec对应的String数组
        String[] arr = vec.toArray(new String[0]);
        for (String str : arr)
            System.out.println("str: " + str);

        // 清空Vector.clear()和removeAllElements()一样
        vec.clear();
        //vec.removeAllElements();

        // 判断Vector是否为空
        System.out.println("vec.isEmpty(): " + vec.isEmpty());

    }
}

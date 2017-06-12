package java.collection.map;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ray on 17/5/8.
 * Describe HashTable Class.
 */
public class HashTableDescription {

    /**
     * interface Map<K,V>
     * abstract class Dictionary<K,V>
     * class Hashtable<K,V> extends Dictionary<K,V> implements Map<K,V>, Cloneable, java.io.Serializable
     *
     *      Hashtable 以 数组 + 单向循环列表 构建集合.
     *
     *
     * 0. 主要属性:
     *      transient Entry<?,?>[] table        存放单向链表的集合
     *      transient int count                 当前 HashTable 中的元素个数
     *      int threshold                       当前 HashTable 进行 rehash 的界限值
     *      float loadFactor                    加载因子 default 0.75f
     *      transient int modCount = 0          结构性变化次数
     *
     * 1. static class Entry<K,V> implements Map.Entry<K,V>    单向链表
     *      final int hash;             该节点的 hash 值,即 key 的 hashCode() 值
     *      final K key;                不可变的 key 值
     *      V value;                    可变的 value 值
     *      Entry<K,V> next;            下一个节点值
     *
     * 2. hash 和 数组下标计算
     *
     *       int hash = key.hashCode(); 直接用 key 值的 hashCode() 值为 hash 值
     *
     *       int index = (hash & 0x7FFFFFFF) % tab.length;
     *
     *       hash & 0x7FFFFFFF : 0000 0000 0000 0000 0000 0000 0000 0100   hash 值(假设)
     *                           0111 1111 1111 1111 1111 1111 1111 1111   0x7FFFFFFF int类型的最大值
     *                           ---------------------------------------   按位与运算
     *                           0000 0000 0000 0000 0000 0000 0000 0100   = 4
     *             计算的目的是: 高效的消去 hash 值为负数的情况,数组下标不能为负.
     *
     *       hash % tab.length 取模运算,计算下标.
     *
     * 3. put(K key, V value)
     *     (0). 检查 value是否为空,是则抛出 NullPointerException.
     *     (1). 通过 key 计算 hash值 数组下标index
     *     (2). 获取 table[index] 的单向链表 entry.
     *     (3). 若 entry=null,则转向(5).
     *     (4). 若 entry!=null,则遍历 entry:
     *             当某个节点的 hash 与 key比较(equals())相同时,直接覆盖该节点 value,结束并返回旧 value值.
     *             否则转向(5).
     *     (5). 检查当前元素个数是否大于等于 threshold:
     *             若是 : 进行rehash操作,并重新计算 hash值 与 数组下标值.
     *             然后获取 table[index] 的单向链表,并新建一个节点,将单向链表赋值给该节点的 next 属性,再将该节点赋值给 table[index].
     *     (6). 结束并返回 null.
     *
     * 4. rehash()
     *    (0). 新容量值为 table.length << 1 + 1 二倍加一
     *    (1). 检查新容量值 是否大于 Integer.MAX_VALUE - 8,是则用该值替换新容量值.
     *    (2). 新建一个 大小为 新容量值 的新数组.
     *    (3). 遍历 table[],遍历tablei中的链表,重新计算数组下标,并赋值给相应的位置.
     *
     * 5. 使用 synchronized 提供线程安全.
     *
     * 6. fail-fast behavior.
     *
     *
     *                   -------------------- 不建议使用 HashTable --------------------
     *
     */

    public static void main(String[] args) {


        Hashtable<String, String> table = new Hashtable<>();

        // 添加操作
        table.put("1", "a");
        table.put("2", "b");
        table.put("3", "c");

        // 打印 table
        System.out.println("table : " + table);

        // 通过Iterator遍历 key-value
        Iterator<Map.Entry<String, String>> i = table.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = i.next();
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        // 通过 Enumerate 遍历 value
        Enumeration enu = table.elements();
        while (enu.hasMoreElements()) {
            System.out.println(enu.nextElement());
        }

        // HashTable的键值对个数
        System.out.println("size : " + table.size());

        // containsKey(Object key) : 是否包含键key
        System.out.println("table.containsKey(\"1\") : " + table.containsKey("1") + "\t" + table);

        // containsValue(Object value) : 是否包含值value
        System.out.println("table.containsValue(\"c\") : " + table.containsValue("c") + "\t" + table);

        // remove(Object key) : 删除键key对应的键值对
        System.out.println("table.remove(\"3\") : " + table.remove("3") + "\t" + table);

        // clear(): 清空 HashTable
        table.clear();

        // isEmpty() : HashTable是否为空
        System.out.println("table.isEmpty() : " + table.isEmpty());

    }
}

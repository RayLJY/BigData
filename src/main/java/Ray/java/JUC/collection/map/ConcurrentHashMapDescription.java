package Ray.java.JUC.collection.map;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ray on 17/5/9.
 * Describe part of ConcurrentHashMap Class.
 */
public class ConcurrentHashMapDescription {

    /**
     *
     *
     *   ConcurrentHashMap 基于数组 + 单向链表 + 红黑树构建集合,
     *      采用 volatile关键字 + CAS函数 + synchronized代码块 来实现线程安全.
     *
     *
     * 0. 重要属性:
     *     (...)
     *
     * 1. Node<K,V> implements Map.Entry<K,V>
     *     final int hash;
     *     final K key;
     *     volatile V val;
     *     volatile Node<K,V> next;
     *
     * 2. Hash值 与 数组下标计算:
     *     int h = key.hashCode()
     *     hash = (h ^ (h >>> 16)) & 0x7fffffff
     *     key 值自身的 hash 值 h, h 与 h 高位的16位 进行按位 异或运算,再去除负数的影响,最后得到 hash值.
     *
     *     index = (table.length - 1) & hash
     *     数组长度减一 和 hash值 进行 按位与运算,相当于 hash 值对 length-1 取模运算,因为length是2的永远是2的倍数.
     *
     * 3. put(K key, V value):
     *     (0). 检查key/value是否为空,如果为空,则抛异常,否则进行(1).
     *     (1). 进入 for 死循环,每次循环,都会获取最新的 table,进行(2).
     *     (2). 检查 table 是否已经初始化,没有,则调用 initTable() 进行初始化并进行(3),否则进行(4).
     *     (3). 根据 key 的 hash 值计算 table 的数组下标 i,取出 table[i] 的节点用 f 表示:
     *            根据f的不同有如下三种情况:
     *              0). 如果 table[i]==null,利用CAS操作直接存储在该位置,如果CAS操作成功,则退出死循环.
     *              1). 如果 table[i]!=null,则检查 table[i] 的节点的 hash 是否等于 MOVED:
     *                    a. 等于,表示正在扩容,则帮助其扩容.
     *                    b. 不等于:(此过程,采用 synchronized 代码块加锁)
     *                       a). 如果 table[i] 为链表节点,遍历,检查 key值是否相等 和 hash值是否相等:
     *                             是: 则覆盖 value,跳出循环,进入(4).
     *                             没有相等的元素,则将此节点插入链表的尾部.
     *                       b). 如果 table[i] 为树节点,遍历,检查key 和 hash值, 覆盖value 或者 则将此节点插入树中.
     *                       c). 插入成功后,进行(4).
     *     (4). 如果 table[i] 的节点是链表节点,则检查否需要转化为树,如需要,则调用 treeifyBin 函数进行转化.
     *
     * 4. 2倍扩容,单向链表与红黑树的相互转换机制与 HashMap 中的实现机制一样.
     * 5. 线程安全.
     * 6. fail-fast behavior.
     * 7. key value 都不能为 null.
     *
     */

    public static void main(String[] args) {

        // not test thread-safe feature

        ConcurrentHashMap<String, Integer> cMap = new ConcurrentHashMap<>();

        cMap.put("a", 1);
        cMap.put("b", 2);
        cMap.put("c", 3);
        cMap.put("d", 4);
        cMap.put("e", 5);
        cMap.put("f", 6);

        // print map
        System.out.println("cMap : " + cMap);

        // putIfAbsent(K key, V value)
        System.out.println("cMap.putIfAbsent(\"e\", 9) : " + cMap.putIfAbsent("e", 9) + "\t" + cMap);
        System.out.println("cMap.putIfAbsent(\"g\", 7) : " + cMap.putIfAbsent("g", 7) + "\t" + cMap);

        for (String key : cMap.keySet()) {
            System.out.println("key : " + key);
        }

        Enumeration<String> keys = cMap.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            System.out.println("key : " + key);
        }

        for (Map.Entry<String, Integer> entry : cMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        Iterator<Map.Entry<String, Integer>> i = cMap.entrySet().iterator();

        while (true) {
            if (!i.hasNext()) break;
            Map.Entry<String, Integer> entry = i.next();
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }

        // get(Object key)
        System.out.println("cMap.get(\"e\") : " + cMap.get("e") + "\t" + cMap);

        System.out.println("cMap.contains(5) : " + cMap.contains(5) + "\t" + cMap);
        System.out.println("cMap.containsKey(\"e\") : " + cMap.containsKey("e") + "\t" + cMap);
        System.out.println("cMap.containsValue(6) : " + cMap.containsValue(6) + "\t" + cMap);

        System.out.println("cMap.remove(\"f\") : " + cMap.remove("f") + "\t" + cMap);

        System.out.println("cMap.size() : " + cMap.size());
        System.out.println("cMap.mappingCount() : " + cMap.mappingCount() + "\t" + cMap);

        cMap.clear();
        System.out.println("cMap.clear()" + "\t" + cMap);
        System.out.println("cMap.isEmpty() : " + cMap.isEmpty() + "\t" + cMap);

    }
}

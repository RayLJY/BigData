package Ray.java.collection.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ray on 17/5/7.
 * Describe HashMap Class.
 */
public class HashMapDescription {

    /**
     * interface Map<K,V>
     * abstract class AbstractMap<K,V> implements Map<K,V>
     * class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable
     *
     *      HashMap 以 数组 + 单向链表 + 红黑树 构建集合.
     *
     * 0.  内部属性:
     *        int threshold                           所能容纳的 key-value 对极限
     *        final float loadFactor                  负载因子,default 0.75f
     *        transient int modCount                  结构性变化次数
     *        transient int size                      当前实际 key-value 个数
     *        transient Node<K,V>[] table             元素被封装为 Node 后,存放的数组
     *
     * 1.  static class Node<K,V> implements Map.Entry<K,V>
     *      final int hash;         代表该节点的 hash 值,本质上是 key的hash值处理后的结果,即 hash(key)
     *      final K key;            key 的值
     *      V value;                value 的值
     *      Node<K,V> next;         指向下一个节点
     *
     * 2.  static class LinkedHashMap.Entry<K,V> extends HashMap.Node<K,V>
     *     static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V>
     *         TreeNode<K,V> parent;  // red-black tree links
     *         TreeNode<K,V> left;
     *         TreeNode<K,V> right;
     *         TreeNode<K,V> prev;    // needed to unlink next upon deletion
     *         boolean red;
     *
     * 3.  hash(object key){
     *        int h;
     *        // h = key.hashCode()     第一步 取hashCode值
     *        // h ^ (h >>> 16)         第二步 高位参与运算
     *        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
     *     }
     *
     *     h = hashCode():            1111 1111 1111 1111 1111 0000 1110 1010  共32位(假设值)
     *     h >>> 16      :            0000 0000 0000 0000 1111 1111 1111 1111  无符号右移16位
     *                                ---------------------------------------  按位异或计算
     *     hash = h ^ (h >>> 16):     1111 1111 1111 1111 0000 1111 0001 0101  hash 值
     *
     *
     *     hash & (table.length - 1): 1111 1111 1111 1111 0000 1111 0001 0101
     *                                0000 0000 0000 0000 0000 0000 0000 1111
     *                                ---------------------------------------  计算hash值对应的数组下标
     *                                0000 0000 0000 0000 0000 0000 0000 0101  = 5
     *
     *     (0). 每次扩容为当前容量的2倍,初始容量为16,导致每次扩容后的容量为2的n次方.
     *     (1). hash & (table.length - 1)相当于对length取模运算.但效率更高.
     *     (2). hash = h ^ (h >>> 16) 可以确保在 length 不足16位(二进制)时,h 的高16位也可以参与运算,并且不会引起太多开销.
     *
     * 4.  put(K key, V value)
     *
     *     (0). 若 table == null 或 table.length == 0 则 resize().
     *     (1). 依照 hash 计算的数组下标,找到 table[i].如果 table[i]==null,直接新建节点添加,并转向(5);否则转向(2).
     *     (2). 判断 table[i] 的首个元素是否和 key 一样,如果相同,直接覆盖 value,否则转向(3).相同指 hashCode equals 相同.
     *     (3). 判断 table[i] 是否为 treeNode(红黑树),如果是,则直接在树中插入键值对,否则转向(4).
     *     (4). 遍历 table[i] 中的链表,
     *             若发现 key 已经存在直接覆盖 value,并退出循环.
     *             若遍历到链表的最后一个元素,则在链表头部添加该节点,并判断链表长度是否大于8,大于则将链表转换为红黑树,否则退出循环.
     *     (5). 插入成功后,判断实际存在的键值对数量size是否超多了最大容量threshold,如果超过,进行resize(扩容).
     *
     * 5.  resize() 扩容
     *     0. 若 table 未初始化,进行第一次 resize,返回一个 length=16 数组.
     *     1. 新建一个是当前数组容量 2倍 的数组.
     *     2. 遍历当前数组,
     *        若 table[i] 是链表:
     *           只有一个节点,则直接计算新数组下标,并负责给新数组中对应的i位置.
     *           有多个节点,将链表分为两组,一组赋值给新数组中对应的 i 位置,另一组赋值给新数组对应的 i+length 位置(看第3条).
     *        若 table[i] 是红黑树:
     *           将树拆分为两个树,检查两个树的大小,若小于8,在将树转换成链表,分别赋值给新数组中对应的 i 和 i+length 位置.
     *        给新数组赋值的过程和 put 方法相同.
     *     3.数组下标计算:
     *
     *      扩容前:
     *      hash & (table.length - 1): 1111 1111 1111 1111 0000 1111 0001 0101  hash 值
     *                                 0000 0000 0000 0000 0000 0000 0000 1111  16 - 1 = 15
     *                                 ---------------------------------------  计算hash值对应的数组下标
     *                                 0000 0000 0000 0000 0000 0000 0000 0101  = 5 = i
     *
     *      扩容后:
     *      hash & (table.length - 1): 1111 1111 1111 1111 0000 1111 0001 0101  hash 值
     *                                 0000 0000 0000 0000 0000 0000 0001 1111  32 - 1 = 31
     *                                 ---------------------------------------  计算hash值对应的数组下标
     *                                 0000 0000 0000 0000 0000 0000 0001 0101  = 5 + 16  = i + oldLength
     *
     *      扩容前后,数组下标可不改变,如果改变,则在原来的下标值上 增加 原来的容量值就是新的数组下标.
     *
     * 6.  将链表转化成红黑树,需要满足俩个条件:当前map中的节点总数大于64个,某个链表长度大于8.若不足64个,但长度大于8,则进行 resize.
     *     链表的查询复杂度为 O(N),红黑树的查询复杂度为 O(lgN),其中 N 为链表或树的大小.
     *
     * 7.  性能:
     *        (0). 扩容是一个特别耗性能的操作,建议初始化时,设置一个大致的数值,避免频繁的扩容操作.
     *        (1). 负载因子 loadFactor,default 0.75f.可以修改,但不推荐.
     *             如果内存空间充足,而对时间效率要求较高,降低负载因子的值,不能为负.
     *             如果内存空间不足,而对时间效率要求不高,增加负载因子的值,可以大于1.
     *        (2). HashMap 线程不安全,在并发的环境中建议使用 ConcurrentHashMap,或者只进行查询操作.
     *
     * 8.  fail-fast behavior.
     */


    public static void main(String[] args) {

        // 新建HashMap
        HashMap<String, String> map = new HashMap<>();

        // 添加操作
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        // 打印 map
        System.out.println("map: " + map);

        // 通过 Iterator 遍历 key->value
        Iterator<Map.Entry<String, String>> i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = i.next();
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        // get(Object key)
        System.out.println("map.get(\"b\"): " + map.get("b"));

        // HashMap的键值对个数
        System.out.println("size: " + map.size());

        // containsKey(Object key) : 是否包含键key
        System.out.println("map.containsKey(\"a\") : " + map.containsKey("a"));

        // containsValue(Object value) : 是否包含值value
        System.out.println("map.containsValue(\"3\") : " + map.containsValue("3"));


        // remove(Object key) : 删除键key对应的键值对
        System.out.println("map.remove(\"c\") : " + map.remove("c") + "\t" + map);

        // 清空HashMap
        map.clear();

        // isEmpty() : HashMap是否为空
        System.out.println("map.isEmpty(): " + map.isEmpty());
    }
}

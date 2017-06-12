package java.collection.map;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by Ray on 17/5/8.
 * Describe TreeMap Class.
 */
public class TreeMapDescription {

    /**
     *  interface Map<K,V>
     *
     * interface SortedMap<K,V> extends Map<K,V>
     * interface NavigableMap<K,V> extends SortedMap<K,V>
     *
     * abstract class AbstractMap<K,V> implements Map<K,V>
     *
     * class TreeMap<K,V> extends AbstractMap<K,V> implements NavigableMap<K,V>, Cloneable, java.io.Serializable
     *
     *      TreeMap 以一棵红黑树构建集合,依靠红黑树有序的特性,TreeMap 也是有序的.
     *
     * 0. 主要属性:
     *      private final Comparator<? super K> comparator     key 值比较器,如果不设置,默认使用 key 类型的默认比较器
     *      private transient Entry<K,V> root                  红黑树的根节点
     *      private transient int size = 0                     当前已经存储的节点个数
     *      private transient int modCount = 0                 结构性变化的次数
     *
     * 1. final class Entry<K,V> implements Map.Entry<K,V>      红黑树的节点
     *      K key                        key 值
     *      V value                      value 值
     *      Entry<K,V> left              左子树节点
     *      Entry<K,V> right             右子树节点
     *      Entry<K,V> parent            父节点
     *      boolean color = BLACK        颜色,默认黑色(true),还可设置为红色(false)
     *
     * 2. put(K key, V value)
     *      (0). 检查 root 根节点是否为null:
     *             是,则新建一个节点,并赋值给 root,结束退出并返回 null.
     *             不是,则转向(1).
     *      (1). 检查 comparator (key值比较器) 是否为null:
     *             是,则强制转换 key 为一个比较器,并使用这个比较器进行(2)操作.
     *             不是,则使用 comparator 进行(2)操作.
     *      (2). 从 root 开始遍历节点,比较该节点的 key 值 与 插入节点的 key 值:
     *             等于,覆盖该节点的 value 值,结束操作并返回被覆盖的 value 值.
     *             小于,遍历该节点的左子树.若子树为空,停止遍历,进行(3)操作.
     *             大于,遍历该节点的右子数.若子树为空,停止遍历,进行(3)操作.
     *      (3). 以该节点为父节点,新建一个节点,并赋值给该节点的左/右子树(依据比较结果而定,左小右大).
     *      (4). 整理整棵红黑树,包括:旋转,着色等.(比较复杂)
     *      (5). 结束并返回 null.
     *
     * 3. 线程不安全.
     * 4. fail-fast behavior.
     * 5. 查询类方法/删除类方法,时间复杂度为 O(logN),N 为元素个数.
     *
     */


    public static void main(String[] args) {

        TreeMap<String, Integer> map = new TreeMap<>();
        // 添加元素
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        map.put("e", 5);
        map.put("f", 6);

        // print TreeMap
        System.out.println("map : " + map);

        // 通过Iterator遍历 map
        Iterator<Map.Entry<String, Integer>> i = map.entrySet().iterator();
        while (true) {
            if (!(i.hasNext())) break;
            Map.Entry<String, Integer> entry = i.next();
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        // TreeMap的键值对个数
        System.out.println("size : " + map.size());

        // containsKey(Object key) :是否包含键key
        System.out.println("map.containsKey(\"a\") : " + map.containsKey("a") + "\t" + map);
        System.out.println("map.containsKey(\"x\") : " + map.containsKey("x") + "\t" + map);

        // containsValue(Object value) : 是否包含值value
        System.out.println("map.containsValue(6) : " + map.containsValue(6) + "\t" + map);

        // remove(Object key) : 删除键key对应的键值对
        System.out.println("map.remove(\"f\") : " + map.remove("f") + "\t" + map);

        // clear() : 清空TreeMap
        map.clear();

        // isEmpty() : TreeMap是否为空
        System.out.println("map.isEmpty() : " + map.isEmpty());


        System.out.println("\n------test NavigableMap APIs------\n");


        NavigableMap<String, Integer> navigable = new TreeMap<>();
        // 添加元素
        navigable.put("a", 1);
        navigable.put("b", 2);
        navigable.put("e", 3);
        navigable.put("c", 5);
        navigable.put("d", 4);

        // print navigable
        System.out.println("navigable : " + navigable);

        // 查询
        System.out.println("navigable.get(\"c\") : " + navigable.get("c") + "\t" + navigable);

        // 获取第一个key
        System.out.println("navigable.firstKey() : " + navigable.firstKey() + "\t" + navigable);
        // 第一个Entry
        System.out.println("navigable.firstEntry() : " + navigable.firstEntry() + "\t" + navigable);

        // 获取最后一个key
        System.out.println("navigable.lastKey() : " + navigable.lastKey() + "\t" + navigable);

        // 最后一个Entry
        System.out.println("navigable.lastEntry() : " + navigable.lastEntry() + "\t" + navigable);

        // 获取“小于等于c”的最大键值对
        System.out.println("navigable.floorKey(\"c\") : " + navigable.floorKey("c") + "\t" + navigable);

        // 获取“小于c”的最大键值对
        System.out.println("navigable.lowerKey(\"c\") : " + navigable.lowerKey("c") + "\t" + navigable);

        // 获取“大于等于c”的最小键值对
        System.out.println("navigable.ceilingKey(\"c\") : " + navigable.ceilingKey("c") + "\t" + navigable);

        // 获取“大于c”的最小键值对
        System.out.println("navigable.higherKey(\"c\") : " + navigable.higherKey("c") + "\t" + navigable);


        System.out.println("\n---------- test subMap APIs ----------\n");


        TreeMap<String, Integer> subMap = new TreeMap<>();
        // 添加元素
        subMap.put("a", 1);
        subMap.put("b", 2);
        subMap.put("c", 3);
        subMap.put("d", 4);
        subMap.put("e", 5);

        // print TreeMap
        System.out.println("subMap : " + subMap);

        // headMap(K toKey)
        System.out.println("subMap.headMap(\"c\") : " + subMap.headMap("c") + "\t" + subMap);

        // headMap(K toKey, boolean inclusive)
        System.out.println("subMap.headMap(\"c\",true) : " + subMap.headMap("c", true) + "\t" + subMap);
        System.out.println("subMap.headMap(\"c\",false) : " + subMap.headMap("c", false) + "\t" + subMap);

        // tailMap(K fromKey)
        System.out.println("subMap.tailMap(\"c\") : " + subMap.tailMap("c") + "\t" + subMap);

        // tailMap(K fromKey, boolean inclusive)
        System.out.println("subMap.tailMap(\"c\",true) : " + subMap.tailMap("c", true) + "\t" + subMap);
        System.out.println("subMap.tailMap(\"c\",false) : " + subMap.tailMap("c", false) + "\t" + subMap);


        // subMap(K fromKey, K toKey)
        System.out.println("subMap.subMap(\"a\",\"d\") : " + subMap.subMap("a", "d") + "\t" + subMap);
        System.out.println("subMap.subMap(\"a\",true,\"d\",true) : " + subMap.subMap("a", true, "d", true) + "\t" + subMap);
        System.out.println("subMap.subMap(\"a\",true,\"d\",false) : " + subMap.subMap("a", true, "d", false) + "\t" + subMap);
        System.out.println("subMap.subMap(\"a\",false,\"d\",true) : " + subMap.subMap("a", false, "d", true) + "\t" + subMap);
        System.out.println("subMap.subMap(\"a\",false,\"d\",false) : " + subMap.subMap("a", false, "d", false) + "\t" + subMap);

        // navigableKeySet()
        System.out.println("subMap.navigableKeySet() : " + subMap.navigableKeySet() + "\t" + subMap);

        // descendingKeySet()
        System.out.println("subMap.descendingKeySet() : " + subMap.descendingKeySet() + "\t" + subMap);

    }
}

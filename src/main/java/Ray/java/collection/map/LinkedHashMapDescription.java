package Ray.java.collection.map;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by Ray on 17/5/8.
 * Describe LinkedHashMap Class.
 */
public class LinkedHashMapDescription {

    /**
     * class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>
     *
     *      LinkedHashMap 是 HashMap 的子类, 它基于 HashMap 构建集合,并添加了有序访问的特性.
     *
     * 0. 主要属性:(使用父类的属性)
     *       transient LinkedHashMap.Entry<K,V> head    双端链表的表头
     *       transient LinkedHashMap.Entry<K,V> tail    双端链表的表尾
     *       final boolean accessOrder                  false  使用迭代器迭代时基于插入顺序迭代
     *                                                  true   使用迭代器迭代时基于访问记录迭代
     *
     * 1. class Entry<K,V> extends HashMap.Node<K,V>
     *       Entry<K,V> before, after;                 分别指向前 后元素的指针
     *
     *       Node为单向链表,在Node的基础上又添加 before 和 after 属性,使得 Entry 同时具有 单向 和 双向 链表的特征.
     *       每次添加新元素时,不仅会维护 Node.next 属性,也会同时维护 Entry.before 和 Entry.after 属性.
     *
     * 2. head 本身就是一个双端链表,每一次 put/remove 操作,都会在 head/tail 中插入/移除相应的节点.基于插入顺序迭代时,迭代该双端链表.
     * 3. tail 本身就是一个双端链表,在accessOrder=true时,每一次 get 操作,都会移动相应的节点到 tail 链表的末端.
     *    基于访问记录迭代时,迭代该双端链表.
     * 4. LinkedHashMap 中的 Entry,和 HashMap 中的 Node一样,基于相同的限制条件,也会被转化为红黑树.
     */


    public static void main(String[] args) {

        LinkedHashMap<String, String> map = new LinkedHashMap<>(16, 0.75f, true);

        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        map.put("e", "5");

        System.out.println("map : " + map);

        map.get("d");
        map.get("b");

        Iterator<String> i = map.keySet().iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }

    }
}

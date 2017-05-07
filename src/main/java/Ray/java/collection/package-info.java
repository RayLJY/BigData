/**
 * Created by Ray on 17/5/6.
 * Performance Comparison.
 */
package Ray.java.collection;

/**
 *      ArrayList PK LinkedList
 * 0. ArrayList 是一个数组,相当于动态数组.随机访问效率高,随机插入 随机删除效率低.
 * 1. LinkedList 是一个双向链表,随机访问效率低,但随机插入 随机删除效率高.
 * 2. LinkedList 可以被当作堆栈 队列 双端队列使用.
 * 3. ArrayList 1.5倍扩容.LinkedList 不需要扩容.
 * 4. 二者都是线程不安全的.
 *
 *
 *      ArrayList PK Vector
 * 0. ArrayList 线程不安全,Vector 线程安全.Vector 可以看成是线程安全的 ArrayList.
 * 1. ArrayList 支持序列化,Vector 不支持.
 * 2. ArrayList 1.5倍扩容. Vector 2倍或者采用固定值扩容.
 * 3. Vector 支持通过 Enumeration(枚举)去遍历.ArrayList 不支持.
 * 4. 增删改查操作过程一致.
 *
 *
 *      Vector PK Stack
 * 0. Stack 是 Vector 的子类,某些特性一致.
 * 1. Stack FILO. Vector 没有要求.
 * 2. 都是线程安全的.
 *
 *
 *      ArrayList PK LinkedList PK Vector PK Stack
 * 0. 当数据量较小时,4种的增删查改性能差别不大.
 * 1. 快速插入 删除元素,推荐 LinkedList.
 * 2. 快速随机访问元素,推荐 ArrayList > Vector > Stack,它们的性能差别在于是否需要"锁".
 * 3. 要求线程安全.可以考虑Vector 和 Stack 或者 Collections.synchronizedList(...).
 * 4. 当数据需要全部遍历时(多次),"十分不推荐" LinkedList.
 */

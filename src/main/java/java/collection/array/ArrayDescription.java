package java.collection.array;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Ray on 17/5/5.
 * Some frequently used usages of array and describe the Java Array's feature.
 */
public class ArrayDescription {

    public static void main(String[] args) {

        // define array
        String[] aArr = new String[5]; // don't initialise
        String[] bArr = {"a", "b", "c", "d", "e"};
        String[] cArr = new String[]{"a", "b", "c", "d", "e"};

        // show time
        Class clazz = aArr.getClass();
        System.out.println(clazz.getName());
        System.out.println(clazz.getDeclaredFields().length);
        System.out.println(clazz.getDeclaredMethods().length);
        System.out.println(clazz.getDeclaredConstructors().length);
        System.out.println(clazz.getDeclaredAnnotations().length);
        System.out.println(clazz.getDeclaredClasses().length);
        System.out.println(clazz.getSuperclass());

        // print array
        String aArrString = Arrays.toString(aArr);
        System.out.println("aArr\t\t\t\t\t" + aArr);
        System.out.println("aArr.toString()\t\t\t" + aArr.toString());
        System.out.println("aArrString\t\t\t\t" + aArrString);
        System.out.println("Arrays.toString(bArr)\t" + Arrays.toString(bArr));

        // create a arrayList base on a array
        ArrayList<String> cArrList = new ArrayList<>(Arrays.asList(cArr));
        System.out.println("cArrList\t\t\t\t" + cArrList);

        // check this array if contains the specified element.
        boolean b = Arrays.asList(cArr).contains("a");
        System.out.println("b\t\t\t\t\t\t" + b);

        // contact two array
        int[] dArr = {1, 2, 3, 4, 5};
        int[] eArr = {6, 7, 8, 9, 10};
        String[] bac = (String[]) ArrayUtils.addAll(bArr, cArr);
        int[] dae = ArrayUtils.addAll(dArr, eArr);
        int[] ead = ArrayUtils.addAll(eArr, dArr);
        System.out.println("bac\t\t\t\t\t\t" + Arrays.toString(bac));
        System.out.println("dae\t\t\t\t\t\t" + Arrays.toString(dae));
        System.out.println("ead\t\t\t\t\t\t" + Arrays.toString(ead));

        // contact a array of elements with separator
        String join = StringUtils.join(cArr, "/");
        System.out.println("join\t\t\t\t\t" + join);

        // array convert set
        Set<String> set = new HashSet<>(Arrays.asList(bArr));
        System.out.println("set\t\t\t\t\t\t" + set);

        // reverse array
        ArrayUtils.reverse(bArr);
        System.out.println("Arrays.toString(bArr)\t" + Arrays.toString(bArr));

        // remove a element from a array
        String[] removed = (String[]) ArrayUtils.remove(cArr, 4);
        System.out.println("removed\t\t\t\t\t" + Arrays.toString(removed));

    }
}

/**
 * 0. 数组在 Java 中是一个对象,可以使用 new 操作符创建.它是 Object 的直接子类,自身没有声明任何成员变量\成员方法\构造函数\注释,是个"空类".
 * 1. 数组一个固定长度的数据结构.声明后,不能改变数组长度.
 * 2. 数组索引起始为0,结束值为数组长度值减一.
 *    ps: ArrayIndexOutOfBoundException
 *       (0). 无效的索引访问数组.
 *       (1). 无效的索引可能是一个负索引,或者是大于等于数组长度的索引.
 * 3. 数组存储在 Java 堆的连续内存空间中.
 * 4. 数组可以分为两类:
 *    (0). 基本变量数组,存储八种基本类型(byte short int long float double boolean char)的值.
 *    (1). 对象数组,存储对象的引用变量的值,即对象的内存地址.
 * 5. 如果没有初始化数组的元素,那么数组会用对应的类型的默认值进行初始化:
 *    (0). byte short int 默认值为 0, long 默认值为 0L.(打印结果都为0)
 *    (1). float 默认值为 0.0f, double 默认值为 0.0d .(打印结果都为0.0)
 *    (2). char 默认值为 '\u0000'(Unicode 编码,代表的字符是 NUL,打印以后是' ')
 *    (3). boolean 默认值为 false
 *    (4). object 默认值为 null
 * 6. Java支持多维数组,最多255维.
 * 7. 数组的类名由若干个'['和数组元素类型的内部名称组成(类的全路径名称和基本变量的内部名称),'['的数目代表了数组的维度.
 * 8. 具有相同类型元素和相同维度的数组,属于同一个类.(长度可以不同)
 * 9. JVM根据元素类型和维度,创建相应的数组类.
 *    (0). 类加载器先检查数组类是否已经被创建了.
 *    (1). 如果没有,则创建数组类.如果有,就不创建.
 *    (2). 如果数组元素是引用类型,那么类加载器首先去加载数组元素的类.
 *    ps: 应用类型指:类(class),接口(interface),数组(array).
 * 10.编译器对Array.length()的语法做特殊处理,直接编译成 arraylength 指令,来获取数组长度.
 *
 */

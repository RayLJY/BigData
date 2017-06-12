package java.JUC.locks;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Ray on 17/4/28.
 * Test CyclicBarrier Class.
 */
public class CyclicBarrierTest {

    private final static int SIZE = 3;
    private static CyclicBarrier cb;

    public static void main(String[] args) {

        cb = new CyclicBarrier(SIZE, () -> System.out.println("I waited for the flowers to fade!"));

        for (int i = 0; i < SIZE; i++) {
            int num = i;
            new Thread(() -> {

                Thread t = Thread.currentThread();
                t.setName("NO." + num);
                System.out.println(t.getName() + " go bed and sleep...");
                try {
                    cb.await();
                    Thread.sleep(5000);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println(t.getName() + " get up...");
            }).start();
        }
        System.out.println("I am main thread, OVER!");
    }

}
/**
 * 0. 创建 CyclicBarrier 对象 cb.参数为:等待线程数量 SIZE, Runnable 接口实现类(JDK 1.8 lambda 表达式写法,可以选择不实现).
 * 1. 启动3个线程,当每一个线程执行到 cb.await() 时,开始等待.
 * 2. cb 内置计数器统计当前等待线程,当达到设定 SIZE 时,启动(构造cb对象时所设置的) Runnable 接口实现类,同时,唤醒所有等待线程.
 * 3. 所有等待线程继续运行.
 */

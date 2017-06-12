package java.JUC.locks;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Ray on 17/4/28.
 * Test CountDownLatch.
 */
public class CountDownLatchTest {

    private final static int LATCH_SIZE = 3;
    private final static CountDownLatch downLatch = new CountDownLatch(LATCH_SIZE);

    public static void main(String[] args) {
        try {
            for (int i = 0; i < LATCH_SIZE; i++) {
                int num = i;
                new Thread(() -> {
                    Thread t = Thread.currentThread();
                    t.setName("NO." + num);
                    try {
                        Thread.sleep(1000);
                        System.out.println(t.getName() + " finished!");
                        downLatch.countDown();
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(t.getName() + " sleep for 3 seconds!");
                }).start();
            }
            System.out.println("I am main thread and waiting ...");
            downLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("All thread is finished!");
    }
}

/**
 * 0. 主线程启动3个线程后,执行到 downLatch.await() 时进入等待状态.
 * 1. 3个线程分别运行,运行至 downLatch.countDown() 时.
 *    (0). downLatch 对象内置的计数器进行减一操作,当计数器的值为0时,唤醒当前锁的所有等待线程,即主线程.
 *    (1). 3个线程会继续运行,并不受任何影响.
 * 2. 主线程继续运行.
 */

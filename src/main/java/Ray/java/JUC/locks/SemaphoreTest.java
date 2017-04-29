package Ray.java.JUC.locks;

import java.util.concurrent.Semaphore;

/**
 * Created by Ray on 17/4/29.
 * Semaphore Test.
 */
public class SemaphoreTest {

    private final static int SIZE = 5;
    //    private final static Semaphore semaphor = new Semaphore(SIZE,true);// fair
    private final static Semaphore semaphor = new Semaphore(SIZE);// Non-fair

    public static void main(String[] args) {

        for (int i = 1; i < SIZE; i++) {
            int n = i;
            new Thread(() -> {
                Thread t = Thread.currentThread();
                t.setName("No." + n);
                try {
                    //semaphor.release(SIZE);// not recommend
                    semaphor.acquire(n);
                    System.out.println(t.getName() + " acquire " + n + " permits");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphor.release(n);
                    System.out.println(t.getName() + " release " + n + " permits");
                }
            }).start();
        }

        // verify notes 1 on fair model
//        new Thread(() -> {
//            Thread t = Thread.currentThread();
//            t.setName("No." + 2);
//            try {
//                semaphor.acquire(2);
//                System.out.println(t.getName() + " acquire " + 2 + " permits");
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } finally {
//                semaphor.release(2);
//                System.out.println(t.getName() + " release " + 2 + " permits");
//            }
//        }).start();
//        new Thread(() -> {
//                Thread t = Thread.currentThread();
//                t.setName("No." + 4);
//                try {
//                    semaphor.acquire(4);
//                    System.out.println(t.getName() + " acquire " + 4 + " permits");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } finally {
//                    semaphor.release(4);
//                    System.out.println(t.getName() + " release " +4+ " permits");
//                }
//            }).start();
//        new Thread(() -> {
//                Thread t = Thread.currentThread();
//                t.setName("No." + 3);
//                try {
//                    Thread.sleep(1000);
//                    semaphor.acquire(3);
//                    System.out.println(t.getName() + " acquire " + 3 + " permits");
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } finally {
//                    semaphor.release(3);
//                    System.out.println(t.getName() + " release " + 3 + " permits");
//                }
//            }).start();

    }
}

/**
 * 0. 创建 Semaphore 对象,需要两个参数,一个是 int 类型,表示 permit 的数量,一个是 boolean 类型(可省略),表示线程在获取 permit 时是否遵守公平原则.
 * 1. semaphor.acquire(n) 表示获取 n 个 permit,此时,
 *    在 Non-fair 模式下,如果 semaphor 中有 >=n 个 permit,即可获取 n 个 permit.
 *    在 fair 模式下,如果 FIFO 队列中没有等待的线程,并且 semaphor 中有 >=n 个permit,即可获取n个permit.
 *    在 fair 模式下,如果 FIFO 队列中有等待的线程, 本线程会被添加到队列末端,若 semaphor 中 permit 的数量满足等待队列中第一个线程所需要的,
 *    即由第一个线程获取,若不满足第一个线程的需求,但满足队列中其他线程的等待需求,其他线程也无法获取 permit,
 *    只有当第一个线程获取完所需的 permit 的时,其他线程才能获取 permit.
 * 2. 当 semaphor 中 permit 的数量不足 n 时,线程并不会获取当前全部剩余的 permit,而是开始等待,直到 semaphor 中有足够的 permit,一次性获取 n 个.
 * 3. semaphor 对象保证 acquire 和 release 过程是同步的.
 * 4. 同一个线程,已经获取 permit,还未释放前,还可以获取 permit.
 *    多次获取 permit 风险极大,例如:
 *       一共有5个 permit,第一次获取3个,未释放,第二次获取数 >2 时,会发生类似死锁的状态.
 * 5. 释放 permit 时,可以分批多次释放,如当前已经获取5个 permit,分两次释放完成,第一次释放3个,第二次释2个或多于2个都可以.
 * 6. 使用规范:谁获取 permit,必须由谁释放,获取多少,必须释放多少(不许多不许少)!
 *
 * ps: 当只有一个 permit 时,与"锁"的功能极其相似,但唯一的不同点是:所有权!
 *     (0). "锁"有所有权,不能由其他线程释放.
 *     (1). permit 没有所有权,可以由其他线程释放,或者在获取 permit 前先执行 release(n) 操作.
 *     (*). 这样设计的目的是在某些情况下避免类似"死锁"的问题.
 *
 *     这里的"锁"是指:
 *        (0). 类似于 Object.wait() 的一些方法,如 LockSupport,Condition等,可以由其他线程来"解锁".
 *        (1). synchronized "锁"不能由其他线程来"解锁".
 *        (2). 根据测试,实现 Lock 接口的"锁",都无法被其他线程"解锁".
 */
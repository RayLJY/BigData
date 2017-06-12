package java.JUC.locks;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by Ray on 17/4/27.
 * Example of LockSupport Class test.
 */
public class LockSupportTest {

    private static Thread main;
    private static Thread work;

    public static void main(String[] args) throws InterruptedException {

        main = Thread.currentThread();

        work = new Worker();
        Thread work2 = new Worker2();
        work.setName("work");

        System.out.println("1 " + main.getName() + " is " + main.getState());
        System.out.println("Let main thread park!");
        work.start();
        work2.start();

        LockSupport.park(work);
        System.out.println("4 " + main.getName() + " is " + main.getState());

        System.out.println("Wake up work thread!");
        String name = ((Thread) LockSupport.getBlocker(work)).getName();
        System.out.println("the blocker of work thread is " + name + " thread!");
        Thread.sleep(5000);
        LockSupport.unpark(work);
    }

    static class Worker extends Thread {
        @Override
        public void run() {
            System.out.println("I am a worker and working! I will wake up main thread!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String name = ((Thread) LockSupport.getBlocker(main)).getName();
            System.out.println("the blocker of main thread is " + name + " thread!");
            System.out.println("2 " + main.getName() + " is " + main.getState());
            LockSupport.park(this);
            System.out.println("3 " + main.getName() + " is " + main.getState());
            System.out.println("5 " + getName() + " is " + getState());
        }
    }

    static class Worker2 extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(work.getName() + " is " + work.getState());
            LockSupport.unpark(main);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(work.getName() + " is " + work.getState());
        }
    }
}

/**
 * 0. 当 main 线程执行 LockSupport.park(work) 语句时, main 线程会被阻塞,阻塞对象是 work, 此时work线程的运行不受任何影响.
 * 1. 当 work 线程执行 LockSupport.park(this) 语句时,由于主线程依旧被阻塞,所以阻塞对象 work 也没有释放,
 *    此时, work 线程是依旧可以获取阻塞对象 this(即 work 对象),然后因为执行park语句而被阻塞.
 *    设置阻塞对象 work 的作用,与线程的阻塞毫无关系,其目的是为了,解释线程是因为什么被阻塞,
 *    所以 LockSupport.park(this)语句 与 LockSupport.getBlocker(...) 常常联合使用.
 * 2. 当 work2 线程执行 LockSupport.unpark(main) 语句时,此时,主线程开始继续运行,并释放阻塞对象 work.
 * 3. 当 main 线程执行 LockSupport.unpark(work) 语句时, work 开始继续运行,并释放阻塞对象 work.
 */

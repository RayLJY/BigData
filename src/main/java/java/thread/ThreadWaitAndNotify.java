package java.thread;

/**
 * Created by Ray on 17/4/25.
 * What's happen when a thread call wait and notify(or notifyAll) methods.
 */
public class ThreadWaitAndNotify {

    private static Object lock = new Object();
    private static Thread main = null;

    public static void main(String[] args) throws InterruptedException {

        main = Thread.currentThread();

        Thread a = new FTread();
        a.setName("Tom");

        synchronized (lock) {
            //1
            System.out.println(main.getName() + " is " + main.getState());
            System.out.println(a.getName() + " is " + a.getState());
            System.out.println(Thread.currentThread().getName() + " start a thread " + a.getName() + "!");
            a.start();

            //3
            System.out.println(a.getName() + " is " + a.getState());
            System.out.println(Thread.currentThread().getName() + " is sleeping!");
            Thread.sleep(3000);

            //4
            System.out.println(a.getName() + " is " + a.getState());
            System.out.println(Thread.currentThread().getName() + " waiting!");
            lock.wait();

            //7
            System.out.println(a.getName() + " is " + a.getState());
            System.out.println(Thread.currentThread().getName() + " continue working!");
        }
    }

    static class FTread extends Thread {

        @Override
        public void run() {
            //2
            System.out.println(Thread.currentThread().getName() + " is working!");
            System.out.println(main.getName() + " is " + main.getState());
            synchronized (lock) {
                //5
                System.out.println(main.getName() + " is " + main.getState());
                System.out.println(Thread.currentThread().getName() + " calls notify method!");
                lock.notify();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //6
                System.out.println(main.getName() + " is " + main.getState());
            }
        }
    }
}

/**
 * Result:
 * //1
 * main is RUNNABLE             主线程处于 RUNNABLE 状态,并持有同步代码块的 "锁"(lock).
 * Tom is NEW                   线程 Tom 处于 NEW 状态.
 * main start a thread Tom!     主线程启动 Tom,将 Tom 交付给 JVM,由 JVM 调用 Tom 的 run 方法。
 * //2
 * Tom is working!              Tom 的 run 方法被调用.
 * main is RUNNABLE             主线程在运行.
 * //3
 * Tom is BLOCKED               由于此刻主线程依旧在运行,所以并没有释放"锁",导致 Tom 进入 BLOCKED 状态.
 * main is sleeping!            主线程开始休眠.
 * //4
 * Tom is BLOCKED               主线程休眠,休眠也属于 RUNNABLE 状态,所以不会释放锁,Tom 依然处于 BLOCKED 状态.
 * main waiting!                主线程调用 lock.wait(), 导致主线程进入 WAITING 状态.此时,主线程会释放持有的所有"锁",
 * /                            并且等待其他线程调用 lock 对象的 notify 或者 notifyAll 方法,才能继续执行.
 * /                            (只能是 lock 对象的方法,因为是调用 lock 对象的wait方法进入的 WAITING 状态）
 * //5
 * main is WAITING              主线程进入 WAITING 状态。
 * Tom calls notify method!     Tom 调用了 lock.notify() 方法.
 * //6
 * main is BLOCKED              主线程被通知,但由于 Tom 的 run 方法没有执行完毕,所以没有释放"锁",导致主线程进入 BLOCKED 状态.
 * //7
 * Tom is RUNNABLE              此时,主线程已经重新获取所"锁",并开始运行,同时,标志着 Tom 的 run 方法已经运行完毕,
 * /                            可见 run 方法运行完以后,Tom并没有被立即关闭。
 * main continue working!
 *
 *
 *
 * Additional Instructions:
 * 运行程序时,当 a.start()方法被执行时,就意味着 Tom 线程和主线程在 JVM 中同时存在.此时,两个线程是并行执行,
 * 导致第二、三部分的执行结果会有很多种可能性.但其他部分由于锁的因素,只有一种肯能.
 * 例如:
 *    main is RUNNABLE
 *    Tom is RUNNABLE
 *
 *    Tom is RUNNABLE
 *    main is TIMED_WAITING
 *
 *    main is RUNNABLE
 *    Tom is BLOCKED
 *
 *
 * wait 和 notify 等方法是基于对象的"同步锁"关联起来的.使用时必须基于同一把"锁","锁"在对象身上,而且,一个对象对应唯一的一把"锁".
 * 所以 wait 和 notify 等方法没有定义在 Thread 中,而是在 Object 中.
 *
 * wait 与 yield
 * 使用 wait, 线程进入 waiting 或 timed_waiting 状态
 * 使用 yield,线程进入 runnable 状态
 *
 */

package java.thread;

/**
 * Created by Ray on 17/4/24.
 * Some API of Thread class.
 */
public class ThreadBase {

    public static void main(String[] args) {

        Thread mainThread = Thread.currentThread();
        System.out.println("mainThread  " + mainThread);


        // thread state: new, runnable, blocked, waiting, timed_waiting, terminated
        System.out.println("mainThread.getState()  " + mainThread.getState());

        // name
        System.out.println("mainThread.getName()  " + mainThread.getName());
        mainThread.setName("TreadBase");
        System.out.println("mainThread.setName(\"TreadBase\");  " + mainThread.getName());

        // priority
        System.out.println("mainThread.getPriority()  " + mainThread.getPriority());
        mainThread.setPriority(10);
        System.out.println("mainThread.setPriority(10);  " + mainThread.getPriority());
        mainThread.setPriority(1);
        System.out.println("mainThread.setPriority(1);  " + mainThread.getPriority());

        // daemon
        System.out.println("mainThread.isDaemon()  " + mainThread.isDaemon());


        // thread group
        ThreadGroup group = mainThread.getThreadGroup();
        System.out.println("group.getName()  " + group.getName());
        System.out.println("group.activeCount()  " + group.activeCount());
        System.out.println("group.activeGroupCount()  " + group.activeGroupCount());
        System.out.println("group.isDaemon()  " + group.isDaemon());

    }
}


/**
 * Thread State:
 *
 *                                            在 JVM 中运行
 *                                                 或
 *                                       等待操作系统的某些资源,如cup等
 *
 *          new ---------------------------->    runnable    ----------------------------> terminated
 *
 *       只有线程对象                 1.sheep(1) wait(1) join(1)进入 timed_waiting,             完成/异常
 *   没有线程运行所需的资源               超时 notify notifyAll进入runnable.
 *
 *                                  2.wait() join() 进入 waiting,
 *                                    notify notifyAll 进入 runnable.
 *
 *                                  3.synchronized 进入block,
 *                                    获得'锁',进入runnable.
 *
 */

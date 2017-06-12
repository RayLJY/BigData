package java.JUC.locks;

import java.util.concurrent.locks.StampedLock;

/**
 * Created by Ray on 17/4/30.
 * StampedLock Test.
 */
public class StampedLockTest {

    public static void main(String[] args) {

        //Test 0
        new Thread(() -> ThreadSet.writeLock(3000)).start();
        new Thread(() -> ThreadSet.readLock(2000)).start();
        new Thread(() -> ThreadSet.optimisticRead(1000)).start();

        //Test 1
        new Thread(() -> ThreadSet.readLock(1000)).start();
        new Thread(() -> ThreadSet.optimisticRead(10)).start();
        new Thread(() -> ThreadSet.readLock(2000)).start();
        new Thread(() -> ThreadSet.optimisticRead(1000)).start();

        //Test 2
        new Thread(() -> ThreadSet.convertToWriteLock(1000)).start();
        new Thread(() -> ThreadSet.writeLock(1000)).start();

    }

    static class ThreadSet {

        private final static StampedLock sl = new StampedLock();

        static void writeLock(int seconds) {
            long stamp = sl.writeLock();
            try {
                System.out.println(getName() + "\twriteLock sleep for " + seconds + " seconds");
                Thread.sleep(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        static void readLock(int seconds) {
            long stamp = sl.readLock();
            try {
                System.out.println(getName() + "\treadLock sleep for " + seconds + " seconds");
                Thread.sleep(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                sl.unlockRead(stamp);
            }
        }

        static void optimisticRead(int seconds) {
            long stamp = sl.tryOptimisticRead();
            try {
                System.out.println(getName() + "\tOptimisticRead sleep for " + seconds + " seconds");
                Thread.sleep(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!sl.validate(stamp)) {
                System.out.println("OptimisticRead validate failed!");
            } else {
                System.out.println("OptimisticRead validate successful!");
            }

            if (stamp != 0L) {
                System.out.println("get optimisticRead lock！");
                sl.unlockRead(stamp);// Exception
            } else {
                System.out.println("not get optimisticRead lock！");
            }

        }

        static void convertToWriteLock(int seconds) {
            long stamp = sl.readLock();
            try {
                System.out.println(getName() + "\tConvertToWriteLock sleep for " + seconds + " seconds");
                Thread.sleep(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                while (true) {
                    long ws = sl.tryConvertToWriteLock(stamp);
                    if (ws != 0L) {
                        System.out.println("ConvertToWriteLock is ok!");
                        stamp = ws;
                        break;
                    } else {
                        System.out.println("ConvertToWriteLock fail!");
                        sl.unlockRead(stamp);
                        stamp = sl.writeLock();
                    }
                }
            } finally {
                sl.unlock(stamp);
            }
        }
    }

    static String getName() {
        return Thread.currentThread().getName();
    }

}
/**
 * 0. 当写模式下,读"锁"就不能被获取,乐观读"锁"也不能被获取,并且乐观读"锁"的 validate 永远是 false.
 * 1. 当乐观读"锁"模式下,读"锁"和写"锁"获取时，都会更新 stamp 值.
 * 2. tryConvertToWriteLock 成功的条件( 3选1 ):
 *    (0). 已经是写模式
 *    (1). 读模式下,并且没有其他读的线程
 *    (2). 乐观读模式下,写"锁"可以用
 *    ps:
 *       在读模式下,不管当前有没有写"锁"等待,都会获取写"锁",而且不需要释放读"锁".
 * 3. StampedLock is not reentrant!!!
 * 4. StampedLock 中 CLH 队列会将等待线程分为读写两个组.FIFO 调度策略.
 * 5. 所有名字以"try"开头的方法,不遵守调度策略,直接获取"锁",stamp 非零表示成功.等于零时,也不会被加入 CLH 队列.
 *    ps:
 *       不遵守调度策略的优势: 减少环境变量的转换,同时避免 memory thrash(内存震荡)
 * 6. 读模式下,新线程准备获取读"锁",此时,如果有写"锁"在等待,新线程则等待,没有则直接获取.
 * 7. 所有释放"锁"的操作,都不会唤醒其他等待者,唤醒操作由另外的线程执行.
 */






















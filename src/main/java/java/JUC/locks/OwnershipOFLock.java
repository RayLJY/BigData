package java.JUC.locks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Ray on 17/4/29.
 * ownership of Lock.
 */
public class OwnershipOFLock {

    private static int money = 0;

    //    private final static ReentrantLock lock = new ReentrantLock(false);//Non-fair
    private final static ReentrantReadWriteLock rrw = new ReentrantReadWriteLock(false);//Non-fair
    //    private final static Lock lock = rrw.writeLock();
    private final static Lock lock = rrw.readLock();


    public static void main(String[] args) {

        new Owner().start();
        new Thief().start();

    }

    static class Owner extends Thread {
        @Override
        public void run() {
            System.out.println("get lock and save 5001 yuan ...");
            setMoney(5001);
            System.out.println("hold lock and sleep ...");
            try {
                Thread.sleep(5000);
                System.out.println("check money and money is " + getMoney());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    static class Thief extends Thread {
        @Override
        public void run() {
            System.out.println("waiting for evening is coming ...");
            try {
                Thread.sleep(2000);
                System.out.println("unlock and get all money ...");
                lock.unlock();
                setMoney(-5001);
                if (getMoney() == 0)
                    System.out.println("I get 5001 yuan!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    static void setMoney(int m) {
        lock.lock();
        money += m;
    }

    static int getMoney() {
        return money;
    }
}

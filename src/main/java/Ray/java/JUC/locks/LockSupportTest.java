package Ray.java.JUC.locks;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by Ray on 17/4/27.
 * Example of LockSupport Class test.
 */
public class LockSupportTest {

    private static Thread main;

    public static void main(String[] args) {
        main = Thread.currentThread();

        Thread work = new Worker();
        System.out.println("1 " + main.getName() + " is " + main.getState());
        System.out.println("Let main thread park!");
        work.start();
        LockSupport.park(main);
        System.out.println("4 " + main.getName() + " is " + main.getState());

    }

    static class Worker extends Thread {
        @Override
        public void run() {
            System.out.println("I am a worker and working! I will wake up main thread!");
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("2 " + main.getName() + " is " + main.getState());
            LockSupport.unpark(main);
            System.out.println("3 " + main.getName() + " is " + main.getState());
        }
    }
}


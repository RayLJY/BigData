package Ray.java.thread;

/**
 * Created by Ray on 17/4/25.
 * Thread join method.
 */
public class ThreadJoin {
    private static Thread main = null;
    private static Thread father = null;

    public static void main(String[] args) throws InterruptedException {
        main = Thread.currentThread();

        father = new Father();
        father.setName("father");
        System.out.println(main.getName() + " is " + main.getState() + "     1");

        father.start();
        father.join(3000);

        System.out.println(main.getName() + " is " + main.getState() + "     2");
        System.out.println(main.getName() + " continue...");
    }

    static class Father extends Thread {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " staring!");
            System.out.println(main.getName() + " is " + main.getState() + "     3");
            Thread s = new Son();
            s.setName("son");
            s.start();
            try {
                s.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " Finished!");
        }
    }

    static class Son extends Thread {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " staring!");
            System.out.println(father.getName() + " is " + father.getState() + "     5");
            System.out.println(main.getName() + " is " + main.getState() + "     6");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " Finished!");
        }
    }
}

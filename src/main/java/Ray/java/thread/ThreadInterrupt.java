package Ray.java.thread;

/**
 * Created by Ray on 17/4/25.
 * Thread Interrupt API.
 */
public class ThreadInterrupt {

    public static void main(String[] args) throws InterruptedException {

        Thread a = new AThread();
        a.setName("Tom");
        a.start();
        Thread.sleep(1000);
        System.out.println("if " + a.getName() + " is interrupted : " + a.isInterrupted());
        Thread.sleep(2000);
        a.interrupt();
        System.out.println("if " + a.getName() + " is interrupted : " + a.isInterrupted());


        Thread b = new BThread();
        b.setName("Jerry");
        b.start();
        Thread.sleep(1000);
        System.out.println("if " + b.getName() + " is interrupted : " + b.isInterrupted());
        Thread.sleep(2000);
        b.interrupt();
        System.out.println("if " + b.getName() + " is interrupted : " + b.isInterrupted());
    }

    static class AThread extends Thread {
        @Override
        public void run() {
            try {
                int n = 0;
                while (!isInterrupted()) {
                    Thread.sleep(500);
                    System.out.println(n++ + "\t" + Thread.currentThread().getName() +
                            " is not interrupted and is " + Thread.currentThread().getState());
                }
            } catch (InterruptedException e) {
                System.out.println("current    " + isInterrupted());
                System.out.println(Thread.currentThread().getName() +
                        " has been interrupted and is " + Thread.currentThread().getState());
            }
        }
    }

    static class BThread extends Thread {
        @Override
        public void run() {
            int n = 0;
            while (!isInterrupted()) {
                try {
                    Thread.sleep(500);
                    System.out.println(n++ + "\t" + Thread.currentThread().getName() +
                            " is not interrupted and is " + Thread.currentThread().getState());
                } catch (InterruptedException e) {
                    System.out.println("current    " + isInterrupted());
                    System.out.println(Thread.currentThread().getName() +
                            " has been interrupted and is " + Thread.currentThread().getState());
                }
            }
        }
    }
}

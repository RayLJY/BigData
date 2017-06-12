package java.thread;

/**
 * Created by Ray on 17/4/24.
 * <p>
 * MultiThread instance.
 */
public class MultiThread {

    public static void main(String[] args) {

        Thread a = new SellerA();
        SellerA b = new SellerA();

        a.start();
        b.start();

        SellerB x = new SellerB();
        SellerB y = new SellerB();

        sleep();
        x.run();
        y.run();

        SellerA tA = new SellerA();
        Thread ta = new Thread(tA);
        Thread tb = new Thread(tA);

        sleep();
        ta.start();
        tb.start();
//        ta.run();
//        tb.run();


        SellerB tB = new SellerB();
        Thread tx = new Thread(tB);
        Thread ty = new Thread(tB);

        sleep();
        tx.start();
        ty.start();
//        tx.run();
//        ty.run();

    }

    private static void sleep() {
        try {
            Thread.sleep(500);
            System.out.println("\n--------------- sleep 500ms ---------------\n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

class SellerA extends Thread {

    private int ticket = 15;

    @Override
    public void run() {
        while (ticket > 0)
            System.out.println(Thread.currentThread().getName() + " sell ticket " + ticket--);
        System.out.println("over");
    }
}

class SellerB implements Runnable {

    private int ticket = 15;

    @Override
    public void run() {
        while (ticket > 0)
            System.out.println(Thread.currentThread().getName() + " sell ticket " + ticket--);
        System.out.println("over");
    }
}
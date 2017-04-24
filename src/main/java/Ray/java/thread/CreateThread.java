package Ray.java.thread;


/**
 * Created by Ray on 17/4/24.
 * <p>
 * Create and run a thread instance.
 */
public class CreateThread {

    static String DL = "\n------------- Dividing line -------------\n";

    public static void main(String[] args) {

        AThread a = new AThread();
        BThread b = new BThread();

        Thread ca = new Thread(a);
        Thread cb = new Thread(b);


        //start
        a.start();
        a.run();
        b.run();


        System.out.println(DL);

        ca.start();
        ca.run();
        cb.start();
        cb.run();
    }

}

class AThread extends Thread {
    @Override
    public void run() {
        System.out.println(this.getClass().getName());
    }
}

class BThread implements Runnable {

    @Override
    public void run() {
        System.out.println(this.getClass().getName());
    }
}
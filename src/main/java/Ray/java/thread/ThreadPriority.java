package Ray.java.thread;

/**
 * Created by Ray on 17/4/25.
 * Thread Priority.
 */
public class ThreadPriority {

    public static void main(String[] args) {

        Thread main = Thread.currentThread();
        System.out.println(main.getName() + "  " + main.getPriority());

        Thread a = new CThread();
        Thread b = new CThread();

        a.setName(" A ");
        b.setName(" B ");
        a.setPriority(1);
        b.setPriority(10);

//        a.start();
//        b.start();

        /**************************************** Dividing Line ****************************************/

        Thread x = new DThread();
        Thread y = new DThread();

        x.setName(" X ");
        y.setName(" Y ");
        x.setPriority(1);
        y.setPriority(10);

        x.start();
        y.start();

    }

    static class CThread extends Thread {
        @Override
        public void run() {
            int n = 0;
            Thread t = Thread.currentThread();
            while (n < 5)
                System.out.println(t.getName() + "  " + t.getPriority() + "  print  " + n++);
        }
    }

    static class DThread extends Thread {

        @Override
        public void run() {
            int n = 0;
            Thread t = Thread.currentThread();
            while (n < 5) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(t.getName() + "  " + t.getPriority() + "  print  " + n++);
            }
        }
    }
}



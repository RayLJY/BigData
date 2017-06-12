package java.singleton;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ray on 17/3/9.
 * <p>
 * different singleton
 */
public class Singleton {

    public static void main(String[] args) {

        /*单例验证*/
        Singleton1 s11 = Singleton1.getInstance();
        Singleton1 s12 = Singleton1.getInstance();
        System.out.println(s11.equals(s12));


        Singleton2 s21 = Singleton2.getInstance();
        Singleton2 s22 = Singleton2.getInstance();
        System.out.println(s21.equals(s22));

        Singleton3 s31 = Singleton3.getInstance();
        Singleton3 s32 = Singleton3.getInstance();
        System.out.println(s31.equals(s32));

        Singleton4 s41 = Singleton4.getInstance();
        Singleton4 s42 = Singleton4.getInstance();
        System.out.println(s41.equals(s42));

        Singleton5 s51 = Singleton5.INSTANCE;
        Singleton5 s52 = Singleton5.INSTANCE;
        System.out.println(s51.equals(s52));




        /* 并发性验证 */
        ThreadPoolExecutor thread = new ThreadPoolExecutor(3, 10, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2));

        for (int i = 0; i < 3; i++) {
            thread.execute(() -> {
//                long nano = Singleton1.getInstance().getNano();
//                long nano = Singleton2.getInstance().getNano();
//                long nano = Singleton3.getInstance().getNano();
//                long nano = Singleton4.getInstance().getNano();
                long nano = Singleton5.INSTANCE.getNano();
                System.out.println(nano);
            });
        }
        thread.shutdown();
    }
}

enum Singleton5 {
    INSTANCE;
    private final long nano = System.nanoTime();

    public long getNano() {
        return nano;
    }
}


class Singleton4 {
    private final long nano = System.nanoTime();

    private static class SingletonHolder {
        private static final Singleton4 instance = new Singleton4();
    }

    private Singleton4() {
    }

    public static final Singleton4 getInstance() {
        return SingletonHolder.instance;
    }

    public long getNano() {
        return nano;
    }
}

class Singleton3 {
    private final long nano = System.nanoTime();
    private static Singleton3 instance = new Singleton3();

    private Singleton3() {
    }

    public static Singleton3 getInstance() {
        return instance;
    }

    public long getNano() {
        return nano;
    }
}


class Singleton2 {
    private final long nano = System.nanoTime();
    private static Singleton2 instance;

    private Singleton2() {
    }

    public static synchronized Singleton2 getInstance() {
        if (instance == null) {
            instance = new Singleton2();
        }
        return instance;
    }

    public long getNano() {
        return nano;
    }
}


class Singleton1 {
    private final long nano = System.nanoTime();
    private static Singleton1 instance;

    private Singleton1() {
    }

    public static Singleton1 getInstance() {
        if (instance == null) {
            instance = new Singleton1();
        }
        return instance;
    }

    public long getNano() {
        return nano;
    }
}


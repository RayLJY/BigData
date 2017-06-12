package java.JUC.threadPool;


import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by Ray on 17/5/2.
 * Describe Callable interface, Future interface and FutureTask class.
 * Sample Usage about Callable, Future and FutureTask.
 */
public class CallableAndFuture {

    public static void main(String[] args) {
        Usage1();
        System.out.println("\n\n****************** Dividing line ******************\n\n");
        Usage2();
    }

    static void Usage1() {

        // not use lambda
//        Callable<Integer> c = new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                Integer n = new Random().nextInt(10);
//                System.out.println(Thread.currentThread().getName() + "\tI am Callable.call method!\t Number is " + n);
//                return n;
//            }
//        };

        Callable<Integer> c = () -> {
            Integer n = new Random().nextInt(10);
            System.out.println(Thread.currentThread().getName() + "\tI am Callable.call method!\t Number is " + n);
            return n;
//            return null;
        };

        FutureTask<Integer> futureTask = new FutureTask<>(c);
        new Thread(futureTask).start();

        try {
            Integer n = futureTask.get();
            System.out.println("Result is " + n);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    static void Usage2() {
        Callable<Integer> c = () -> {
            Integer n = new Random().nextInt(10);
            System.out.println(Thread.currentThread().getName() + "\tI am Callable.call method!\t Number is " + n);
            return n;
//            return null;
        };
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        Future<Integer> future = threadPool.submit(c);
        try {
            Integer n = future.get();
            System.out.println("0. Result is " + n);

            Integer x = future.get();
            System.out.println("\n1. Result is " + x);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}

/**
 * 0. interface Callable<V>
 *       与 Runnable 接口相似, 只有一个 call 方法,提供计算结果和检查异常结果.
 *
 * 1. interface Future<V>
 *       一个 Future 表示一个异步计算的结果,当异步计算未完成时,Future处于"阻塞"等待状态,直到其完成.
 *       Future 提供了取消异步计算的功能,已经完成的计算不能被取消.如果只是为了使用取消特性,但实际计算并没有返回值,可以返回 null.
 *       Future.get() 获取异步计算结果,可多次获取,可以为 null.
 *       Memory consistency effects(存储器一致性效果): 异步计算过程必须发生在其对应的 Future.get() 之前.
 *
 * 2. interface RunnableFuture<V> extends Runnable, Future<V>
 *       只是继承接口 Runnable 和 Future,所以具有他们的特性
 *
 * 3. class FutureTask<V> implements RunnableFuture<V>
 *        一个可以取消的异步计算过程,并包含计算结果.
 *        异步计算未完成时,处于"阻塞"等待状态,直到其完成.
 *        当计算完成时,无法被取消,无法被再次计算.
 *        get 方法获取计算结果,可多次获取,可以为 null.调用此方法是线程可能会通过 Thread.yield() 等待计算完成.
 *        runAndReset 方法可以使计算过程重置状态和重置结果集,从而使其可以被再次计算.
 *        6 Status:
 *           NEW          = 0      新建
 *           COMPLETING   = 1      完成中
 *           NORMAL       = 2      完成
 *           EXCEPTIONAL  = 3      异常
 *           CANCELLED    = 4      取消
 *           INTERRUPTING = 5      终止中
 *           INTERRUPTED  = 6      终止
 *
 *           Possible state transitions:
 *             NEW -> COMPLETING -> NORMAL
 *             NEW -> COMPLETING -> EXCEPTIONAL
 *             NEW -> CANCELLED
 *             NEW -> INTERRUPTING -> INTERRUPTED
 */

package Ray.java.JUC.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Ray on 17/4/26.
 * Test ReentrantLock.
 */
public class ReentrantLockTest {

    public static void main(String[] args) {
        Basket basket = new Basket();

        Producer producer = new Producer(basket);
        Consumer consumer = new Consumer(basket);
        Consumer consumer2 = new Consumer(basket);

        producer.produce();
        consumer.consume();
        consumer2.consume();
    }
}

class Basket {

    private final int capacity = 50;
    private int size = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition empty = lock.newCondition();
    private final Condition full = lock.newCondition();


    void put() {
        lock.lock();
        try {
            if (getRest() <= 0) {
                System.out.println("Basket is full...");
                full.await();
            }
            size += 1;
            System.out.println("put 1 into basket and rest of capacity of basket is " + getRest());
            if (lock.hasWaiters(empty)) {
                System.out.println("How many consumer is writing. " + lock.getWaitQueueLength(empty));
                empty.signal();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    void get() {
        lock.lock();
        try {
            if (getRest() >= capacity) {
                System.out.println("Basket is empty...");
                empty.await();
            }
            size -= 1;
            System.out.println("get 1 from basket and rest of capacity of basket is " + getRest());
            if (lock.hasWaiters(full)) {
                full.signal();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private int getRest() {
        return capacity - size;
    }
}

class Producer {
    private Basket basket;

    Producer(Basket basket) {
        this.basket = basket;
    }

    void produce() {
        new Thread(() -> {
            while (true) {
                basket.put();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}

class Consumer {
    private Basket basket;

    Consumer(Basket basket) {
        this.basket = basket;
    }

    void consume() {
        new Thread(() -> {
            while (true) {
                basket.get();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}
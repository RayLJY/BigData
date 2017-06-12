package java.JUC.locks;

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

    private final ReentrantLock lock = new ReentrantLock();// Non-Fair
    private final Condition empty = lock.newCondition();
    private final Condition full = lock.newCondition();

    void put() {
        lock.lock();
        try {
            if (getRest() <= 0) {
                System.out.println("Basket is full...");
                // 通知之前因为"篮子"空了而等待的线程
                if (lock.hasWaiters(empty)) {
                    System.out.println("How many consumer is writing. " + lock.getWaitQueueLength(empty));
                    empty.signalAll();
                }
                // 释放当前获得的锁 并休眠 等待被唤醒 唤醒后会重新获取锁
                full.await();
            }
            size += 1;
            System.out.println("put 1 into basket and rest of capacity of basket is " + getRest());
            // 通知之前因为"篮子"空了而等待的线程
            if (lock.hasWaiters(empty)) empty.signal();
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
                // 通知之前因为"篮子"满了而等待的线程
                if (lock.hasWaiters(full)) full.signalAll();
                empty.await();
            }
            size -= 1;
            System.out.println("get 1 from basket and rest of capacity of basket is " + getRest());
            // 通知之前因为"篮子"满了而等待的线程
            if (lock.hasWaiters(full)) full.signal();
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
                    Thread.sleep(100);
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
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}

/**
 * 一个生产者和两个消费者,生产与消费同时进行,并通过中间容器"篮子"连接.
 * 假设:单位时间内生产能力略大于消费能力.
 * (可以通过设置生产者与消费者的休眠时间来更改能力大小)
 * 为了使"篮子"中的数量不出错,对"取"和"放"的过程都加"锁".
 * 当篮子放满时,会使 所有"放"的操作进行等待,而不是直接丢弃.
 * 当篮子取空时,会使 所有"取"的操作进行等待,而不是直接丢弃.
 */

package Ray.java.JUC.locks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Ray on 17/4/27.
 * Example of test ReentrantReadWriteLock.
 */
public class ReentrantReadWriteLockTest {

    public static void main(String[] args) throws InterruptedException {

        Account account = new Account(7788121);
        Accounter tom = new Accounter("Tom", account);
        Accounter jerry = new Accounter("Jerry", account);
        Accounter nemo = new Accounter("Nemo", account);

        int[] income = {100, 200, 500, 100, 300}; //1200
        int[] salary = {-300, -450, -100, -550, -200}; //1600

        tom.check();
        jerry.transfer(income);
        nemo.transfer(salary);
        Thread.sleep(20000);
        System.out.println("Finally, the balance of Account(" + account.getId() + ") is " + account.getBalance());
    }
}

class Account {

    private final long id;
    private int balance = 0;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock rl = rwl.readLock();
    private final Lock wl = rwl.writeLock();

    Account(long id) {
        this.id = id;
    }

    boolean revenue(int num, String name) {
        rl.lock();
        System.out.println("1\t\t\tThe balance is " + getBalance());
        rl.unlock();
        try {
            wl.lock();
            balance += num;
            if (balance < 0) {
                Thread.sleep(1);
                balance -= num;
                System.out.println("\t\t\tSorry! Operation is failed!");
                System.out.println("\t\t\tINFO: " + name + " revenue " + num);
            } else {
                System.out.println("\t\t\tOK! Operation is successful!");
                System.out.println("\t\t\tINFO: " + name + " revenue " + num);
            }
            rl.lock();
            System.out.println("2\t\t\tThe balance is " + getBalance());

        } catch (InterruptedException e) {
            System.out.println("error\t\t\tSorry! Operation is failed!");
            System.out.println("error\t\t\tINFO: " + name + " revenue " + num);
            return false;
        } finally {
            System.out.println("release wl");
            wl.unlock();
        }
        rl.unlock();
        return true;
    }

    int getBalance() {
        return balance;
    }

    long getId() {
        return id;
    }

    void getBalance(String name) {
        rl.lock();
        System.out.println("\t\t\t\t\t\t\t\t\t\t\tHi " + name + ", The balance of Account (" + id + ") is " + balance + " yuan!");
        rl.unlock();
    }
}

class Accounter {

    private String name;
    private Account account;

    Accounter(String name, Account account) {
        this.name = name;
        this.account = account;
    }

    void check() {
        new Thread(() -> {
            for (int i = 0; i < 7; i++) {
                try {
                    Thread.sleep(2000);
                    account.getBalance(name);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    void transfer(int[] nums) {
        new Thread(() -> {
            try {
                for (int num : nums) {
                    boolean signal = account.revenue(num, name);
                    if (signal) {
                        System.out.println(name + " revenue " + num + " and The balance is " + account.getBalance());
                        Thread.sleep(3000);
                    } else {
                        System.out.println("Hi " + name + ", your account(" + account.getId() + ") is not enough money!");
                        Thread.sleep(5000);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
package Ray.java.JUC.threadPool;

/**
 * Created by Ray on 17/4/30.
 * Describe features of threadPool.
 */
public class ThreadPoolBaseDescription {

    /**
     * ThreadPoolExecutor 主要属性介绍:
     *
     * 0. HashSet<Worker> workers
     *    workers 是一个 Worker 集合.一个 Worker 对应一个线程.当线程池启动时,它会执行线程池中的任务,
     *    当执行完一个任务后,它会从线程池的阻塞队列中取出一个阻塞的任务来继续运行.
     * 1. BlockingQueue<Runnable> workQueue
     *    任务阻塞队列.当线程池中的线程数超过它的容量的时候,线程会进入阻塞队列进行阻塞等待.
     * 2. ReentrantLock mainLock
     *    mainLock 是互斥锁,通过 mainLock 实现了对线程池的互斥访问.
     * 3. int poolSize
     *    poolSize 是当前线程池的实际大小,即线程池中任务的数量.
     * 4. int corePoolSize 和 int maximumPoolSize
     *    corePoolSize 指核心线程数量
     *    maximumPoolSize 指最多线程的数量
     *    关系:
     *       当新任务提交给线程池时(通过 execute 方法)
     *       poolSize < corePoolSize,则创建新线程来处理任务.
     *       poolSize >= corePoolSize,则将当前线程添加到 workQueue 中.
     *       当阻塞队列满时,并且 poolSize < maximumPoolSize 时,创建新线程处理任务.
     *
     * 5. allowCoreThreadTimeOut 和 keepAliveTime
     *    allowCoreThreadTimeOut 指空闲状态的线程能够存活的时间.
     *    keepAliveTime 指当线程池处于空闲状态的时候,空闲的线程能够存活的时间.
     *
     * 6. ThreadFactory threadFactory
     *    线程池通过 ThreadFactory 创建线程.
     *    创建的线程是非守护线程,优先级为 Thread.NORM_PRIORITY.
     *
     * 7. RejectedExecutionHandler handler
     *    拒绝任务处理器,当线程池拒绝的任务由 handler 进行处理.
     *    四种拒绝任务处理器:
     *       AbortPolicy            default,抛出 RejectedExecutionException 异常.
     *       CallerRunsPolicy       被拒绝的任务将被添加到"线程池正在运行的线程"中运行.
     *       DiscardOldestPolicy    丢弃等待队列中最老的未处理任务,然后将被拒绝的任务添加到等待队列中.
     *       DiscardPolicy          丢弃被拒绝的任务.
     */


    /**
     * new ThreadPoolExecutor(int corePoolSize,
     *                        int maximumPoolSize,
     *                        long keepAliveTime,
     *                        TimeUnit unit,
     *                        BlockingQueue<Runnable> workQueue,
     *                        ThreadFactory threadFactory,
     *                        RejectedExecutionHandler handler)
     *
     * parameter:
     *    0. threadFactory 和 handler 可以不设置.
     *    1. corePoolSize = maximumPoolSize 创建固定大小的线程池,等同于 new newFixedThreadPool(int nThreads).
     *    2. 将 maximumPoolSize 设置 Integer.MAX_VALUE, not recommend!!!
     *    3. 使用 setCorePoolSize(int) 和 setMaximumPoolSize(int) 进行更新.
     *
     * method:
     *    0. execute(Runnable command) 将任务添加到线程池中执行.
     *       (0). poolSize < corePoolSize 时,通过 addWorker(command, true) 新建一个线程,启动该线程.
     *       (1). poolSize >= corePoolSize 时,threadPool 状态为 RUNNING,则将当前线程添加到 workQueue 中.
     *       (2). 当阻塞队列满时,并且 poolSize < maximumPoolSize 时,创建新线程处理任务.
     *       执行失败,则由拒绝任务处理器处理.
     *    1. addWorker(Runnable firstTask, boolean core) 创建 Worker 对象,执行任务.
     *    2. submit() 是通过调用 execute() 实现添加任务功能的.
     *    3. shutdown() 关闭线程池.
     *       (0). 检查对线程池内的"线程"是否有终止权限.
     *       (1). 设置线程池的状态为关闭状态.
     *       (2). 中断线程池中空闲的线程.
     *       (3). 尝试终止线程池.
     */


    /**
     * ThreadPool Status
     *    使用 final AtomicInteger ctl 表示"线程池中的任务数量"和"线程池状态"
     *    ctl 共32位.高3位表示"线程池状态",低29位表示"线程池中的任务数量"
     *
     * Five Status
     *    RUNNING       高3位值是111.
     *    SHUTDOWN      高3位值是000.
     *    STOP          高3位值是001.
     *    TIDYING       高3位值是010.
     *    TERMINATED    高3位值是011.
     *
     * 1. RUNNING
     *    (0). 线程池处在 RUNNING 状态时，能够接收新任务,以及对已添加的任务进行处理.
     *    (1). 线程池被一旦被创建,就处于 RUNNING 状态! 并且"任务数量"初始化为0.
     *
     * 2. SHUTDOWN
     *    (0). 线程池处在 SHUTDOWN 状态时,不接收新任务,但能处理已添加的任务.
     *    (1). 调用 shutdown() 时,线程池由 RUNNING -> SHUTDOWN.
     *
     * 3. STOP
     *    (0). 线程池处在 STOP 状态时,不接收新任务,不处理已添加的任务,并且会中断正在处理的任务.
     *    (1). 调用线程池的 shutdownNow() 时, 线程池由 RUNNING or SHUTDOWN -> STOP.
     *
     * 4. TIDYING
     *    (0). TIDYING 状态,当所有的任务已终止,"任务数量"为0.
     *    (1). 在 SHUTDOWN 状态下,阻塞队列为空并且线程池中执行的任务也为空时,就会由 SHUTDOWN -> TIDYING.
     *         在 STOP 状态下,线程池中执行的任务为空时,就会由 STOP -> TIDYING.
     *
     * 5. TERMINATED
     *    (0). TERMINATED 状态表示线程池彻底终止.
     *    (1). 在 TIDYING 状态时,执行完 terminated() 之后,就会由 TIDYING -> TERMINATED.
     */


    /**
     *  interface Executor
     *  interface ExecutorService extends Executor
     *  abstract class AbstractExecutorService implements ExecutorService
     *  class ThreadPoolExecutor extends AbstractExecutorService
     *
     *  class DelegatedExecutorService extends AbstractExecutorService
     *  class FinalizableDelegatedExecutorService extends DelegatedExecutorService
     *
     *
     *  class Executors
     * 0. public static ExecutorService newFixedThreadPool(int nThreads)
     *    = new ThreadPoolExecutor(nThreads, nThreads,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())
     *
     * 1. public static ExecutorService newWorkStealingPool(int parallelism)
     *    = new ForkJoinPool(parallelism, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)
     *
     * 2. public static ExecutorService newWorkStealingPool()
     *    = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)
     *
     * 3. public static ExecutorService newSingleThreadExecutor()
     *    = new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>()))
     *
     * 4. public static ExecutorService newCachedThreadPool()
     *    = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>())
     */
}
package thread;

import java.util.concurrent.Semaphore;

/**
 * 池化的线程
 *
 * @author aldebran
 * @since 2021-08-25
 */
public class PoolThread extends Thread {

    public ThreadPool threadPool;

    public volatile Runnable task;

    final Object lock = new Object();

    volatile boolean giveBack = false;

    Semaphore semaphore = new Semaphore(1);

    /**
     * 初始化线程，需要指定父线程
     *
     * @param threadPool
     */
    public PoolThread(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }


    /**
     * 线程重复利用的主逻辑
     */
    public void run() {
        /**
         * giveBack为了让主线程检测工作线程如果已经归还则循环唤醒，如此往复，否则提交结束
         * 但是有可能出现唤醒后执行任务后再次归还，主线程再次唤醒，导致任务重做
         * 设置大小为1的信号量semaphore，防止重做，也就是保证做任务在循环唤醒结束后
         */

        while (!threadPool.stop) {
            try {
                semaphore.acquire(); // P
            } catch (InterruptedException e) {
                throw new RuntimeException("pool thread fail to acquire semaphore!", e);
            }
            task.run();
            giveBack();
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("fail to wait!", e);
                }
            }
            giveBack = false;
        }
    }

    /**
     * 线程归还池中
     */
    public void giveBack() {
        if (!giveBack) {
            synchronized (threadPool) {
                giveBack = true;
                task = null;
                threadPool.idleThreads.add(this);
                threadPool.semaphore.release();
            }
        }
    }
}

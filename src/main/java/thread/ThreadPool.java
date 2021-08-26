package thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 线程池
 *
 * @author aldebran
 * @since 2021-08-25
 */
public class ThreadPool {

    public int max_thread_num = 5; // 最大连接数

    public List<PoolThread> idleThreads = new ArrayList<>(); // 空闲线程列表

    public List<PoolThread> allThreads = new ArrayList<>(); // 所有创建过的线程

    public volatile boolean stop = false; // 停止标志，可以"温和"地停止线程

    public Semaphore semaphore; // 控制线程数的信号量

    public volatile boolean allowCommit = true;

    public ThreadPool() {
        semaphore = new Semaphore(max_thread_num);
    }

    public ThreadPool(int max_thread_num) {
        this.max_thread_num = max_thread_num;
        semaphore = new Semaphore(max_thread_num);
    }


    /**
     * 提交任务
     *
     * @param task
     */
    public void commitTask(Runnable task) {
        if (!allowCommit) {
            throw new RuntimeException("fail to commit task because the thread pool has been closed");
        }

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("parent thread fail to acquire semaphore!", e);
        }
        PoolThread poolThread;
        synchronized (this) {
            if (!idleThreads.isEmpty()) {
                poolThread = idleThreads.remove(idleThreads.size() - 1);
                poolThread.task = task;
                while (poolThread.giveBack) {
                    synchronized (poolThread.lock) {
                        poolThread.lock.notify();
                    }
                }
                poolThread.semaphore.release(); // V

            } else {
                poolThread = new PoolThread(this);
                poolThread.task = task;
                allThreads.add(poolThread);
                poolThread.start();
            }
        }
    }

    /**
     * 强制关闭线程池：强制停止工作线程，并等待他们结束。不推荐！
     * shutdownForce()必须在commitTask()之后调用。可以不在同一个线程中调用，也能强制关闭线程池
     * 此方法不安全，可能会有已提交任务未做或只做了一部分
     */
    public void shutdownForce() {
        allowCommit = false;
        stop = true;
        for (PoolThread thread : allThreads) {
            thread.stop();
        }
    }

    /**
     * 不再提交任务，等待已有任务完成，温和停止工作线程，并等待他们结束。
     * shutdown()后不能再调用commitTask()
     * 保证已提交任务都完成
     */
    public void shutdown() {
        allowCommit = false;
        for (int i = 0; i < max_thread_num; i++) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException("the parent thread fail to acquire");
            }
        }
        // 此时idleThreads和allThreads大小相同
        stop = true;
        for (PoolThread poolThread : allThreads) {
            while (poolThread.giveBack) {
                synchronized (poolThread.lock) {
                    poolThread.lock.notify();
                }
            }
        }
        for (PoolThread poolThread : allThreads) {
            try {
                poolThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("the parent thread waiting for child to finish error");
            }
        }
        allThreads.clear();
        idleThreads.clear();
        System.out.println("线程池正确结束");
    }

    /**
     * 获得空闲线程数量
     * <=最大线程数
     */
    public int idleThreads() {
        return idleThreads.size();
    }

    /**
     * 获取最大线程数
     *
     * @return
     */
    public int maxThreads() {
        return max_thread_num;
    }

    /**
     * 获取已有线程数
     * <=最大线程数
     *
     * @return
     */
    public int currentThreads() {
        return allThreads.size();
    }
}

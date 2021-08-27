# 线程池Java实现

## 使用方法

### 1.视为实现了Runnable接口的对象是一个任务

### 2.首先初始化线程池ThreadPool, 构造方法参数为最大线程数

### 3.每次调用commitTask(Runnable)提交任务，这是一个阻塞方法，当空闲线程数不够的时候发生阻塞，直到有线程执行完任务释放信号量

### 4.提交完所有任务后，推荐使用shutdown()关闭线程池，可以确保已经任务都完成，不推荐用shutdownForce()，可能有已提交任务只做了一部分或者没做，只在任务结果不重要，快速关闭并释放资源采用

## 代码示例

```
        ThreadPool threadPool = new ThreadPool(8); // 创建最大线程数为8的线程池
        threadPool.commitTask(runnableObject); // 提交1个任务，多次提交不限量，但要保证任务能在有限时间内完成，不然会耗尽线程池！！！
        /**
         * threadPool.idleThreads()获取空闲线程数，任务量大，任务多，空闲数会少
         * threadPool.maxThreads()获取最大线程数
         * threadPool.currentThreads()获得已有线程数，初始为0，随着需要逐渐增大到最大线程数
         */
        threadPool.shutdown(); // 温和关闭线程池，保证所有已提交任务完成

        // 不推荐threadPool.shutdownForce()，可能导致已提交任务未做或者没做完，除非不关心结果，只想快速结束线程并释放资源
```
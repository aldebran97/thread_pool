# 线程池Java实现

```
        ThreadPool threadPool = new ThreadPool(8); // 创建最大线程数为8的线程池
        for (int i = 10000; i < 11000; i++) {
            threadPool.commitTask(new Task(i * 10)); // 提交1000个任务，每个任务就输出i*10个字节
        }
        /**
         * threadPool.idleThreads()获取空闲线程数，任务量大，任务多，空闲数会少
         * threadPool.maxThreads()获取最大线程数
         * threadPool.currentThreads()获得已有线程数，初始为0，随着需要逐渐增大到最大线程数
         */
        threadPool.shutdown(); // 温和关闭线程池，保证所有已提交任务完成

        // 不推荐threadPool.shutdownForce()，可能导致已提交任务未做或者没做完，除非不关心结果，只想快速结束线程并释放资源
```
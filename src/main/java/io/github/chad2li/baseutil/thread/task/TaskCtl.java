package io.github.chad2li.baseutil.thread.task;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 控制器
 * <p>
 * 1. 任务启动记录<br/>
 * 2. 任务停止记录<br/>
 * 3. 任务是否可以继续<br/>
 * </p>
 */
@Slf4j
public class TaskCtl {
    /**
     * 任务仓储
     */
    private TaskStore taskStore;
    /**
     * 程序结束控制器，true表示需要结束整个程序
     */
    public boolean EXIT_FLAG = false;

    /**
     * 消费者线程计数
     */
    private AtomicInteger COUNTER_CONSUMER = new AtomicInteger(0);
    /**
     * 生产者线程计数
     */
    private AtomicInteger COUNTER_PRODUCER = new AtomicInteger(0);
    /**
     * 线程缓存，作中断通知
     */
    private List<SubThread> SUB_THREADS = new ArrayList<>();

    public TaskCtl(TaskStore taskStore) {
        this.taskStore = taskStore;
    }

    /**
     * 线程启动
     *
     * @param subThread 启动的线程类型
     */
    private void start(SubThread subThread) {
        int count = 0;
        if (subThread.flag == ThreadFlagEnum.CONSUMER) {
            count = COUNTER_CONSUMER.incrementAndGet();
        } else {
            count = COUNTER_PRODUCER.incrementAndGet();
        }
        log.info("[{}] Starting => {}", Thread.currentThread().getName(), count);
        synchronized (SUB_THREADS) {
            SUB_THREADS.add(subThread);
        }
        try {
            Thread.sleep(500);// 休眠，等待其他线程全部启动完成
        } catch (InterruptedException e) {
            log.warn("[{}] wait start interrupted", Thread.currentThread().getName());
        }
    }

    /**
     * 线程结束
     *
     * @param subThread 线程
     */
    private void stop(SubThread subThread) {
        if (subThread.flag == ThreadFlagEnum.CONSUMER) {
            COUNTER_CONSUMER.decrementAndGet();
        } else {
            COUNTER_PRODUCER.decrementAndGet();
        }
        log.info("[{}] Stopped", Thread.currentThread().getName());
        synchronized (SUB_THREADS) {
            SUB_THREADS.remove(subThread);
            for (SubThread t : SUB_THREADS) {
                t.interrupt();
            }
        }
    }

    /**
     * 结束程序
     */
    private void exit() {
        EXIT_FLAG = true;
        // 中断操作
        synchronized (SUB_THREADS) {
            for (SubThread t : SUB_THREADS) {
                t.interrupt();
            }
        }
    }


    /**
     * 程序能否继续运行标识
     *
     * @return true表示继续任务；false表示结束
     */
    private boolean isContinue(ThreadFlagEnum flag) {
        boolean result = true;
        if (flag == ThreadFlagEnum.CONSUMER) {// 消费者是否结束取决于生产者是否存在
            // todo 消费者需要判断是否还有数据未处理，是否需要全部处理完毕再结束
            // 生产者存在，或者还有数据需要处理
            result = COUNTER_PRODUCER.get() > 0 || taskStore.hasConsumeData();
        } else {//生产者 1）程序退出标识，2）消费者不存在，都会导致生产者终止
//            return !(EXIT_FLAG || COUNTER_CONSUMER.get() < 1);
            result = !EXIT_FLAG;
        }
//        log.debug("[{}] isContinue ==> {}", Thread.currentThread().getName(), result);

        return result;
    }

    /**
     * 线程标识
     */
    public enum ThreadFlagEnum {
        /**
         * 消费者标识
         */
        CONSUMER
        /**
         * 生产者标识
         */
        , PRODUCER
    }

    /**
     * 任务封装
     * <p>
     * 1. 启动时调用控制器记录启动信息<br/>
     * 2. 停止时调用控制器记录停止信息<br/>
     * 3. 封装run方法循环调用 subRun，并捕捉异常
     * </p>
     */
    public static abstract class SubThread<P, C> extends Thread {
        /**
         * 线程类型标识
         */
        public ThreadFlagEnum flag;

        /**
         * 任务仓储
         */
        protected TaskStore<P, C> taskStore;
        /**
         * 任务控制器
         */
        protected TaskCtl taskCtl;
        /**
         * 任务服务
         */
        protected ITaskService<P, C> taskService;


        public SubThread(String name, ThreadFlagEnum flag, TaskCtl taskCtl, TaskStore taskStore, ITaskService taskService) {
            super(name);
            this.flag = flag;
            this.taskCtl = taskCtl;
            this.taskStore = taskStore;
            this.taskService = taskService;
        }

        /**
         * 中断，用于中断线程的休眠或等待，继续执行任务
         *
         * @param title 中断说明
         */
        public void interrupted(String title) {
            log.warn("[{}] interrupted by: {}", this.getName(), title);
            this.interrupt();
        }

        @Override
        public void run() {
            // 启动
            taskCtl.start(this);

            // 循环业务逻辑
            while (taskCtl.isContinue(this.flag)) {
                try {
                    boolean result = subRun();
                    if (!result && this.flag == ThreadFlagEnum.PRODUCER)// 生产者未产出数据
                        this.taskCtl.exit();
                } catch (Exception e) {
                    log.error("[" + this.getName() + "] " + e.getMessage(), e);
                }
            }

            // 结束
            taskCtl.stop(this);
        }

        /**
         * 具体任务内容
         * 任务会不停循环，直到无任务数据可用，或程序停止命令
         *
         * @return 任务处理结果；生产者返回false会导致整个程序结束；暂未处理消费者结果
         */
        public abstract boolean subRun() throws Exception;
    }
}

package cn.lyjuan.base.thread.task;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费者
 * <p>
 *
 * </p>
 *
 * @param <P> 生产者需要的数据类型
 * @param <C> 生产者产出和消费者消费的数据类型
 */
@Slf4j
public class TaskConsumer<P, C> extends TaskCtl.SubThread<P, C> {
    /**
     * 构造消费者
     *
     * @param name        消费者线程名称
     * @param taskStore   任务仓储
     * @param taskService 任务服务，用于任务内容调用
     */
    public TaskConsumer(String name, TaskCtl taskCtl, TaskStore taskStore, ITaskService taskService) {
        super(name, TaskCtl.ThreadFlagEnum.CONSUMER, taskCtl, taskStore, taskService);
    }

    @Override
    public boolean subRun() throws Exception {
        // 从数据仓储中心获取一条数据
        // 如果是入库，可以多条(可能存在丢失的情况)
        C c = taskStore.get();
        if (null == c) return false;// 下一次循环

        // 保存文件
        boolean result = taskService.consumer(c);
        return result;
    }
}

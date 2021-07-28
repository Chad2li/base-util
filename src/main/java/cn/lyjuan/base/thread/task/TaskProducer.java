package cn.lyjuan.base.thread.task;

import cn.lyjuan.base.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 生产者
 *
 * @param <P> 生产者需要的数据类型
 * @param <C> 生产者产出和消费者消费的数据类型
 */
@Slf4j
public class TaskProducer<P, C> extends TaskCtl.SubThread<P, C> {
    /**
     * 生产者初始数据
     */
    private P initData;
    /**
     * 生产者是否批量产出数据，即一次生产产出多个消费数据
     */
    @Getter
    @Setter
    private boolean isBatchProduce;

    /**
     * 构造生产者
     *
     * @param name        生产者线程名称
     * @param taskCtl     任务控制器
     * @param taskStore   任务仓储
     * @param taskService 任务服务，用于任务内容调用
     */
    public TaskProducer(String name, TaskCtl taskCtl, TaskStore taskStore, ITaskService taskService, P initData) {
        this(name, taskCtl, taskStore, taskService, initData, false);
    }

    /**
     * 构造生产者
     *
     * @param name
     * @param taskCtl
     * @param taskStore
     * @param taskService
     * @param initData
     * @param isBatchProduce 是否批量产出数据
     */
    public TaskProducer(String name, TaskCtl taskCtl, TaskStore taskStore, ITaskService taskService, P initData
            , boolean isBatchProduce) {
        super(name, TaskCtl.ThreadFlagEnum.PRODUCER, taskCtl, taskStore, taskService);
        this.initData = initData;
        this.isBatchProduce = isBatchProduce;
    }

    @Override
    public boolean subRun() throws Exception {
        // 产出数据
        boolean isAddSucc = false;
        if (this.isBatchProduce) {// 批量
            Collection<C> cs = taskService.produces(initData);
            if (null == cs) return false;

            // 产生的数据交给数据仓储，供消费者使用
            isAddSucc = taskStore.addAll(cs);
        } else {// 单个
            C cData = taskService.produce(initData);
            if (null == cData) return false;

            // 产生的数据交给数据仓储，供消费者使用
            isAddSucc = taskStore.add(cData);
        }

        if (!isAddSucc) {
//             todo 放入失败
        }
        return true;
    }
}

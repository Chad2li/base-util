package io.github.chad2li.baseutil.thread.task;

import java.util.Collection;
import java.util.Collections;

/**
 * 任务核心服务
 *
 * @param <P> 生产者需要的数据
 * @param <C> 生产者产出和消费者消费的数据
 */
public interface ITaskService<P, C> {
    /**
     * 生产者任务内容
     *
     * @param p 进行产出操作的原始数据
     * @return 产出的数据，供消费者消费
     */
    default C produce(P p) {
        return null;
    }

    /**
     * 批量产出
     *
     * @return 批量产出数据，默认返回空集合
     */
    default Collection<C> produces(P p) {
        return Collections.EMPTY_LIST;
    }

    /**
     * 消费者任务内容
     *
     * @return true表示消费成功，继续下一次消费；<br/>
     * todo false表示本次消费失败，由retryCount决定重新消费本次数据次数
     */
    boolean consumer(C c);
}

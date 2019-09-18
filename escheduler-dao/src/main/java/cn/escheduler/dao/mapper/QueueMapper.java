package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.Queue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

/**
 *
 */
public interface QueueMapper extends BaseMapper<Queue> {

    IPage<Queue> queryQueuePaging(IPage<Queue> page,
                                  @Param("searchVal") String searchVal);

    Queue queryByQueue(@Param("queue") String queue);

    Queue queryByQueueName(@Param("queueName") String queueName);
}

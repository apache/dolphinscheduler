package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * the Dao interfaces of task group queue
 * @author yinrui
 * @since 2021-08-07
 */
public interface TaskGroupQueueMapper extends BaseMapper<TaskGroupQueue> {

    /**
     * select task group queues by some conditions
     * @param page page
     * @param groupId group id
     * @return task group queue list
     */
    IPage<TaskGroupQueue> queryTaskGroupQueuePaging(IPage<TaskGroupQueue> page,
                                                    @Param("groupId") Integer groupId
                                                    );

    TaskGroupQueue queryByTaskId(@Param("taskId") Integer taskId);


    /**
     * query by status
     * @param status status
     * @return result
     */
    List<TaskGroupQueue> queryByStatus(@Param("status") Integer status);

    /**
     * delete by task id
     * @param taskId task id
     * @return affected rows
     */
    int deleteByTaskId(@Param("taskId") Integer taskId);

    /**
     * update status by task id
     * @param taskId task id
     * @param status status
     * @return
     */
    int updateStatusByTaskId(@Param("taskId") Integer taskId, @Param("status") Integer status);

}


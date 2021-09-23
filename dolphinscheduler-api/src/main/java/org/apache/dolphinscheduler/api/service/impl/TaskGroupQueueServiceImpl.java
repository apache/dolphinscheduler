package org.apache.dolphinscheduler.api.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Service("taskGroupQueueService")
public class TaskGroupQueueServiceImpl extends BaseServiceImpl implements TaskGroupQueueService {

    @Autowired
    TaskGroupQueueMapper taskGroupQueueMapper;

    private static final Logger logger = LoggerFactory.getLogger(TaskGroupQueueServiceImpl.class);

    /**
     * query tasks in task group queue by group id
     *
     * @param loginUser login user
     * @param groupId   group id
     * @param pageNo    page no
     * @param pageSize  page size

     * @return tasks list
     */
    @Override
    public Map<String, Object> queryTasksByGroupId(User loginUser, Integer groupId, Integer pageNo, Integer pageSize) {
        return this.doQuery(loginUser, pageNo, pageSize, null, groupId);
    }

    /**
     * query tasks in task group queue by project id
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param projectId project id
     * @return tasks list
     */
    @Override
    public Map<String, Object> queryTasksByProjectId(User loginUser, Integer pageNo, Integer pageSize, Integer projectId) {
        return this.doQuery(loginUser, pageNo, pageSize, null, projectId);
    }

    /**
     * query all tasks in task group queue
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @return tasks list
     */
    @Override
    public Map<String, Object> queryAllTasks(User loginUser, Integer pageNo, Integer pageSize) {
        return this.doQuery(loginUser, pageNo, pageSize, null, null);
    }

    public Map<String, Object> doQuery(User loginUser, Integer pageNo, Integer pageSize,
                                        Integer groupId,
                                        Integer processId) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        Page<TaskGroupQueue> page = new Page<>(pageNo, pageSize);
        IPage<TaskGroupQueue> taskGroupQueue = taskGroupQueueMapper.queryTaskGroupQueuePaging(page,groupId,processId);


        PageInfo<TaskGroupQueue> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) taskGroupQueue.getTotal());
        pageInfo.setTotalList(taskGroupQueue.getRecords());


        result.put(Constants.DATA_LIST, pageInfo);
        logger.info("select result:{}", taskGroupQueue);
        putMsg(result, Status.SUCCESS);
        return result;
    }
    /**
     * delete by task id
     *
     * @param taskId task id
     * @return TaskGroupQueue entity
     */
    @Override
    public boolean deleteByTaskId(Integer taskId) {
        return taskGroupQueueMapper.deleteByTaskId(taskId) == 1;
    }
}

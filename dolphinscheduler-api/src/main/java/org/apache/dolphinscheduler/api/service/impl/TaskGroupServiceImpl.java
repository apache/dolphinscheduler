package org.apache.dolphinscheduler.api.service.impl;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.service.TaskGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 *
 */
@Service("taskGroupService")
public class TaskGroupServiceImpl extends BaseServiceImpl implements TaskGroupService {


    @Autowired
    private TaskGroupMapper taskGroupMapper;

    @Autowired
    private TaskGroupQueueService taskGroupQueueService;

    @Autowired
    private ProcessService processService;

    private static final Logger logger = LoggerFactory.getLogger(TaskGroupServiceImpl.class);


    /**
     * create a Task group
     *
     * @param loginUser   login user
     * @param name        task group name
     * @param description task group description
     * @param groupSize   task group total size
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> createTaskGroup(User loginUser, String name, String description, Integer groupSize) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        if (name == null) {
            putMsg(result, Status.NAME_NULL);
            return result;
        }
        if (groupSize <= 0) {
            putMsg(result, Status.TASK_GROUP_SIZE_ERROR);
            return result;

        }
        TaskGroup taskGroup1 = taskGroupMapper.queryByName(loginUser.getId(), name);
        if (taskGroup1 != null) {
            putMsg(result, Status.TASK_GROUP_NAME_EXSIT);
            return result;
        }
        TaskGroup taskGroup = new TaskGroup(0, name, description,
                groupSize, loginUser.getId());
        Date date = new Date(System.currentTimeMillis());
        taskGroup.setCreateTime(date);
        taskGroup.setUpdateTime(date);
        int insert = taskGroupMapper.insert(taskGroup);
        logger.info("insert result:", insert);
        putMsg(result, Status.SUCCESS);

        return result;
    }



    /**
     * update the task group
     *
     * @param loginUser   login user
     * @param name        task group name
     * @param description task group description
     * @param groupSize   task group total size
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> updateTaskGroup(User loginUser,Integer id, String name, String description, Integer groupSize) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        TaskGroup taskGroup = taskGroupMapper.selectById(id);
        if (taskGroup.getStatus() != 0) {
            putMsg(result, Status.TASK_GROUP_STATUS_ERROR);
            return result;
        }
        TaskGroup taskGroup2 = taskGroupMapper.queryByName(loginUser.getId(), name);
        if (taskGroup2 != null && !taskGroup2.getName().equals(name)) {
            putMsg(result, Status.TASK_GROUP_NAME_EXSIT);
            return result;
        }
        taskGroup.setGroupSize(groupSize);
        taskGroup.setUpdateTime(new Date(System.currentTimeMillis()));
        taskGroup.setDescription(description);
        taskGroup.setName(name);
        int i = taskGroupMapper.updateById(taskGroup);
        logger.info("update result:{}",i);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get task group status
     * @param id task group id
     * @return is the task group available
     */
    @Override
    public boolean isTheTaskGroupAvailable(Integer id) {
        TaskGroup taskGroup = taskGroupMapper.selectById(id);

        return taskGroup.getStatus() == 1;
    }

    /**
     * query all task group by user id
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> queryAllTaskGroup(User loginUser, Integer pageNo, Integer pageSize) {
        return this.doQuery(loginUser, pageNo, pageSize, loginUser.getId(), null,  null);
    }

    /**
     * query all task group by status
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param status    status
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> queryTaskGroupByStatus(User loginUser, Integer pageNo, Integer pageSize, Integer status) {
        return this.doQuery(loginUser, pageNo, pageSize, loginUser.getId(), null,  status);
    }

    /**
     * query all task group by name
     * @param loginUser login user
     * @param pageNo page no
     * @param pageSize page size
     * @param name name
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> queryTaskGroupByName(User loginUser, Integer pageNo, Integer pageSize, String name) {
        return this.doQuery(loginUser, pageNo, pageSize, loginUser.getId(),name,  null);
    }


    /**
     * query all task group by id
     *
     * @param loginUser login user
     * @param id        id
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> queryTaskGroupById(User loginUser, Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        TaskGroup taskGroup = taskGroupMapper.selectById(id);
        result.put(Constants.DATA_LIST, taskGroup);
        putMsg(result, Status.SUCCESS);
        return result;
    }
    /**
     * query
     * @param pageNo    page no
     * @param pageSize  page size
     * @param userId user id
     * @param name name
     * @param status status
     * @return the result code and msg
     */

    @Override
    public Map<String, Object> doQuery(User loginUser, Integer pageNo, Integer pageSize, Integer userId, String name, Integer status) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        Page<TaskGroup> page = new Page<>(pageNo, pageSize);
        IPage<TaskGroup> taskGroupPaging = taskGroupMapper.queryTaskGroupPaging(page, userId, name, status);

        PageInfo<TaskGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        int total = taskGroupPaging == null ? 0 : (int) taskGroupPaging.getTotal();
        List<TaskGroup> list = taskGroupPaging == null ? new ArrayList<TaskGroup>() : taskGroupPaging.getRecords();
        pageInfo.setTotal(total);
        pageInfo.setTotalList(list);

        result.put(Constants.DATA_LIST, pageInfo);
        logger.info("select result:{}", taskGroupPaging);
        putMsg(result, Status.SUCCESS);
        return result;
    }
    /**
     * close a task group
     *
     * @param loginUser login user
     * @param id        task group id
     * @return the result code and msg
     */

    @Override
    public Map<String, Object> closeTaskGroup(User loginUser, Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        TaskGroup taskGroup = taskGroupMapper.selectById(id);
        if (taskGroup.getUseSize() != 0 || taskGroup.getStatus() == 0) {
            putMsg(result, Status.TASK_GROUP_STATUS_ERROR);
            return result;
        }
        taskGroup.setStatus(0);
        taskGroup.setUpdateTime(new Date(System.currentTimeMillis()));
        int update = taskGroupMapper.updateById(taskGroup);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * start a task group
     *
     * @param loginUser login user
     * @param id        task group id
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> startTaskGroup(User loginUser, Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        TaskGroup taskGroup = taskGroupMapper.selectById(id);
        if (taskGroup.getStatus() == 1) {
            putMsg(result, Status.TASK_GROUP_STATUS_ERROR);
            return result;
        }
        taskGroup.setStatus(1);
        taskGroup.setUpdateTime(new Date(System.currentTimeMillis()));
        int update = taskGroupMapper.updateById(taskGroup);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * wake a task manually
     *
     * @param loginUser
     * @param taskId    task id
     * @return result
     */
    @SuppressWarnings("checkstyle:WhitespaceAround")
    @Override
    public Map<String, Object> wakeTaskcompulsively(User loginUser, Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        TreeMap<Integer, Integer> waitingTaskCache = processService.getWaitingTaskCache();
        if (waitingTaskCache == null) {
            putMsg(result,Status.TASK_GROUP_CACHE_START_FAILED);
            return result;
        }
        boolean b = taskGroupQueueService.deleteByTaskId(taskId);
        waitingTaskCache.remove(taskId);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}

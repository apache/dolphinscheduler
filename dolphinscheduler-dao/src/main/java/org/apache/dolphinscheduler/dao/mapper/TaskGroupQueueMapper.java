/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * the Dao interfaces of task group queue
 *
 * @author yinrui
 * @since 2021-08-07
 */
public interface TaskGroupQueueMapper extends BaseMapper<TaskGroupQueue> {

    /**
     * query by status
     *
     * @param status status
     * @return result
     */
    List<TaskGroupQueue> queryByStatus(@Param("status") int status);

    /**
     * delete by task id
     *
     * @param taskId task id
     * @return affected rows
     */
    int deleteByTaskId(@Param("taskId") int taskId);

    /**
     * update status by task id
     *
     * @param taskId task id
     * @param status status
     * @return
     */
    int updateStatusByTaskId(@Param("taskId") int taskId, @Param("status") int status);

    void modifyPriority(@Param("queueId") int queueId, @Param("priority") int priority);

    IPage<TaskGroupQueue> queryTaskGroupQueueByTaskGroupIdPaging(Page<TaskGroupQueue> page,
                                                                 @Param("taskName") String taskName,
                                                                 @Param("workflowName") String workflowName,
                                                                 @Param("status") Integer status,
                                                                 @Param("groupId") int groupId,
                                                                 @Param("projects") List<Project> projects);

    void deleteByWorkflowInstanceId(@Param("workflowInstanceId") Integer workflowInstanceId);

    void deleteByWorkflowInstanceIds(@Param("workflowInstanceIds") List<Integer> workflowInstanceIds);

    void deleteByTaskGroupIds(@Param("taskGroupIds") List<Integer> taskGroupIds);

    List<TaskGroupQueue> queryAllInQueueTaskGroupQueueByGroupId(@Param("taskGroupId") Integer taskGroupId,
                                                                @Param("inQueue") int inQueue);

    List<TaskGroupQueue> queryAllTaskGroupQueueByInQueue(@Param("inQueue") int inQueue);

    List<TaskGroupQueue> queryByTaskInstanceId(@Param("taskInstanceId") Integer taskInstanceId);

    List<TaskGroupQueue> queryUsingTaskGroupQueueByGroupId(@Param("taskGroupId") Integer taskGroupId,
                                                           @Param("status") int status,
                                                           @Param("inQueue") int inQueue,
                                                           @Param("forceStart") int forceStart);

    int countUsingTaskGroupQueueByGroupId(@Param("taskGroupId") Integer taskGroupId,
                                          @Param("status") int status,
                                          @Param("inQueue") int inQueue,
                                          @Param("forceStart") int forceStart);

    List<TaskGroupQueue> queryInQueueTaskGroupQueue(@Param("inQueue") int inQueue,
                                                    @Param("minTaskGroupQueueId") int minTaskGroupQueueId,
                                                    @Param("limit") int limit);

    List<TaskGroupQueue> queryWaitNotifyForceStartTaskGroupQueue(@Param("inQueue") int inQueue,
                                                                 @Param("forceStart") int forceStart,
                                                                 @Param("minTaskGroupQueueId") int minTaskGroupQueueId,
                                                                 @Param("limit") int limit);
}

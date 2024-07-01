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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;

import java.util.List;

public interface TaskGroupQueueDao extends IDao<TaskGroupQueue> {

    /**
     * Delete {@link TaskGroupQueue} by {@link ProcessInstance#getId()}
     *
     * @param workflowInstanceIds workflowInstanceIds
     */
    void deleteByWorkflowInstanceIds(List<Integer> workflowInstanceIds);

    /**
     * Query all {@link TaskGroupQueue} which in_queue is {@link org.apache.dolphinscheduler.common.enums.Flag#YES}
     *
     * @return TaskGroupQueue ordered by priority desc
     */
    List<TaskGroupQueue> queryAllInQueueTaskGroupQueue();

    /**
     * Query all {@link TaskGroupQueue} which
     * in_queue is {@link org.apache.dolphinscheduler.common.enums.Flag#YES}
     * and id > minTaskGroupQueueId
     * ordered by id asc
     * limit #{limit}
     *
     * @return TaskGroupQueue ordered by id asc
     */
    List<TaskGroupQueue> queryInQueueTaskGroupQueue(int minTaskGroupQueueId, int limit);

    /**
     * Query all {@link TaskGroupQueue} which in_queue is {@link org.apache.dolphinscheduler.common.enums.Flag#YES} and taskGroupId is taskGroupId
     *
     * @param taskGroupId taskGroupId
     * @return TaskGroupQueue ordered by priority desc
     */
    List<TaskGroupQueue> queryAllInQueueTaskGroupQueueByGroupId(Integer taskGroupId);

    /**
     * Query all {@link TaskGroupQueue} which taskId is taskInstanceId
     *
     * @param taskInstanceId taskInstanceId
     * @return TaskGroupQueue ordered by priority desc
     */
    List<TaskGroupQueue> queryByTaskInstanceId(Integer taskInstanceId);

    /**
     * Query all {@link TaskGroupQueue} which status is TaskGroupQueueStatus.ACQUIRE_SUCCESS and forceStart is {@link org.apache.dolphinscheduler.common.enums.Flag#NO}.
     *
     * @param taskGroupId taskGroupId
     * @return TaskGroupQueue
     */
    List<TaskGroupQueue> queryAcquiredTaskGroupQueueByGroupId(Integer taskGroupId);

    /**
     * Count all {@link TaskGroupQueue} which status is TaskGroupQueueStatus.ACQUIRE_SUCCESS and forceStart is {@link org.apache.dolphinscheduler.common.enums.Flag#NO}.
     *
     * @param taskGroupId taskGroupId
     * @return TaskGroupQueue
     */
    int countUsingTaskGroupQueueByGroupId(Integer taskGroupId);

    /**
     * Query all {@link TaskGroupQueue} which
     * in_queue is {@link org.apache.dolphinscheduler.common.enums.Flag#YES}
     * and forceStart is {@link org.apache.dolphinscheduler.common.enums.Flag#YES}
     * and id > minTaskGroupQueueId
     * order by id asc
     * limit #{limit}
     *
     * @return TaskGroupQueue ordered by priority desc
     */
    List<TaskGroupQueue> queryWaitNotifyForceStartTaskGroupQueue(int minTaskGroupQueueId, int limit);
}

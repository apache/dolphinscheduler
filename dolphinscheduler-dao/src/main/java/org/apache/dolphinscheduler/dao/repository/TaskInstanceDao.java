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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.List;

/**
 * Task Instance DAO
 */
public interface TaskInstanceDao {

    /**
     * Update or Insert task instance to DB.
     * ID is null -> Insert
     * ID is not null -> Update
     * @param taskInstance task instance
     * @return result
     */
    boolean upsertTaskInstance(TaskInstance taskInstance);

    /**
     * Insert task instance to DB.
     * @param taskInstance task instance
     * @return result
     */
    boolean insertTaskInstance(TaskInstance taskInstance);

    /**
     * Update task instance to DB.
     * @param taskInstance task instance
     * @return result
     */
    boolean updateTaskInstance(TaskInstance taskInstance);

    /**
     * Submit a task instance to DB.
     * @param taskInstance task instance
     * @param processInstance process instance
     * @return task instance
     */
    TaskInstance submitTaskInstanceToDB(TaskInstance taskInstance, ProcessInstance processInstance);

    /**
     * Query list of valid task instance by process instance id
     * @param processInstanceId processInstanceId
     * @param testFlag test flag
     * @return list of valid task instance
     */
    List<TaskInstance> findValidTaskListByProcessId(Integer processInstanceId, int testFlag);

    /**
     * find previous task list by work process id
     * @param processInstanceId processInstanceId
     * @return task instance list
     */
    List<TaskInstance> findPreviousTaskListByWorkProcessId(Integer processInstanceId);

    /**
     * find task instance by id
     * @param taskId task id
     * @return task instance
     */
    TaskInstance findTaskInstanceById(Integer taskId);

    /**
     * find task instance list by id list
     * @param idList task id list
     * @return task instance list
     */
    List<TaskInstance> findTaskInstanceByIdList(List<Integer> idList);

}

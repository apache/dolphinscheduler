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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.TaskDependentResult;
import org.apache.dolphinscheduler.dao.mapper.TaskDependentResultMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.TaskDependentResultDao;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class TaskDependentResultDaoImpl extends BaseDao<TaskDependentResult, TaskDependentResultMapper> implements TaskDependentResultDao {

    public TaskDependentResultDaoImpl(TaskDependentResultMapper taskDependentResultMapper) {
        super(taskDependentResultMapper);
    }

    @Override
    public List<TaskDependentResult> queryTaskDependentResultByTaskInstanceId(Integer taskInstanceId) {
        if (taskInstanceId == null) {
            return Collections.emptyList();
        }
        return mybatisMapper.queryTaskDependentResultListByTaskInstanceId(taskInstanceId);
    }

    @Override
    public int deleteTaskDependentResultByTaskInstanceId(Integer taskInstanceId) {
        if (taskInstanceId == null) {
            return 0;
        }
        return mybatisMapper.deleteTaskDependentResultByTaskInstanceId(taskInstanceId);
    }

    @Override
    public int upsertTaskDependentResult(TaskDependentResult taskDependentResult) {
        if (taskDependentResult == null) {
            return 0;
        }
        TaskDependentResult dbTaskDependentResult = mybatisMapper.queryTaskDependentResultByTaskDependentResult(taskDependentResult);
        if (dbTaskDependentResult == null) {
            return mybatisMapper.insert(taskDependentResult);
        } else {
            return mybatisMapper.updateDependentResultByTaskInstanceId(taskDependentResult.getDependentResult(), taskDependentResult.getTaskInstanceId());
        }
    }
}

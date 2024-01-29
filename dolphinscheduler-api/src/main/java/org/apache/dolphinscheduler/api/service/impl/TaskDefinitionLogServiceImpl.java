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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.service.TaskDefinitionLogService;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.repository.ProcessTaskRelationLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskDefinitionLogServiceImpl implements TaskDefinitionLogService {

    @Autowired
    private ProcessTaskRelationLogDao processTaskRelationLogDao;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Override
    public void deleteTaskByWorkflowDefinitionCode(long workflowDefinitionCode) {
        List<ProcessTaskRelationLog> processTaskRelations =
                processTaskRelationLogDao.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        if (CollectionUtils.isEmpty(processTaskRelations)) {
            return;
        }
        // delete task definition
        Set<Long> needToDeleteTaskDefinitionCodes = new HashSet<>();
        for (ProcessTaskRelation processTaskRelation : processTaskRelations) {
            needToDeleteTaskDefinitionCodes.add(processTaskRelation.getPreTaskCode());
            needToDeleteTaskDefinitionCodes.add(processTaskRelation.getPostTaskCode());
        }
        taskDefinitionLogDao.deleteByTaskDefinitionCodes(needToDeleteTaskDefinitionCodes);
        // delete task workflow relation
        processTaskRelationLogDao.deleteByWorkflowDefinitionCode(workflowDefinitionCode);
    }
}

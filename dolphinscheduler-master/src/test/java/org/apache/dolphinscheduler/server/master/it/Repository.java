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

package org.apache.dolphinscheduler.server.master.it;

import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Repository {

    @Autowired
    private ProcessInstanceDao workflowInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    /**
     * Return the list of process instances for a given workflow definition in ascending order of their IDs.
     */
    public List<ProcessInstance> queryWorkflowInstance(final ProcessDefinition workflowDefinition) {
        return workflowInstanceDao.queryAll()
                .stream()
                .filter(workflowInstance -> workflowInstance.getProcessDefinitionCode()
                        .equals(workflowDefinition.getCode()))
                .filter(workflowInstance -> workflowInstance.getProcessDefinitionVersion() == workflowDefinition
                        .getVersion())
                .sorted(Comparator.comparingInt(ProcessInstance::getId))
                .collect(Collectors.toList());
    }

    /**
     * Return the list of task instances for a given workflow definition in ascending order of their IDs.
     */
    public List<TaskInstance> queryTaskInstance(final ProcessDefinition workflowDefinition) {
        return queryWorkflowInstance(workflowDefinition)
                .stream()
                .flatMap(workflowInstance -> taskInstanceDao.queryByWorkflowInstanceId(workflowInstance.getId())
                        .stream())
                .sorted(Comparator.comparingInt(TaskInstance::getId))
                .collect(Collectors.toList());
    }

}

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

import java.util.Date;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TriggerInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TriggerInstanceMapperTest extends BaseDaoTest {

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private TriggerInstanceMapper triggerInstanceMapper;

    /**
     * insert
     *
     * @return ProcessInstance
     */
    private ProcessInstance insertProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("taskName");
        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        processInstance.setProcessDefinitionCode(1L);
        processInstance.setProjectCode(1L);
        processInstance.setTestFlag(0);
        processInstanceMapper.insert(processInstance);
        return processInstance;
    }

    /**
     * construct a task instance and then insert
     */
    private TriggerInstance insertTriggerInstance(int processInstanceId) {
        TriggerInstance taskInstance = new TriggerInstance();
        taskInstance.setName("us task");
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setTriggerType("asd");
        taskInstance.setProcessInstanceId(processInstanceId);
        taskInstance.setProjectCode(1L);
        triggerInstanceMapper.insert(taskInstance);
        return taskInstance;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insert ProcessInstance
        ProcessInstance processInstance = insertProcessInstance();

        // insert taskInstance
        TriggerInstance triggerInstance = insertTriggerInstance(processInstance.getId());
    }
}

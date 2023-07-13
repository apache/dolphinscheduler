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

package org.apache.dolphinscheduler.server.master.runner.task.dynamic;

import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.rpc.MasterRpcClient;
import org.apache.dolphinscheduler.server.master.runner.task.ILogicTaskPluginFactory;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.subworkflow.SubWorkflowService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DynamicLogicTaskPluginFactory implements ILogicTaskPluginFactory<DynamicLogicTask> {

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private ProcessDefinitionMapper processDefineMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    SubWorkflowService subWorkflowService;

    @Autowired
    private MasterRpcClient masterRpcClient;

    @Override
    public DynamicLogicTask createLogicTask(TaskExecutionContext taskExecutionContext) {
        return new DynamicLogicTask(taskExecutionContext, processInstanceDao, taskInstanceDao, subWorkflowService,
                processService,
                masterRpcClient, processDefineMapper, commandMapper);

    }

    @Override
    public String getTaskType() {
        return DynamicLogicTask.TASK_TYPE;
    }
}

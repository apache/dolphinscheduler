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

package org.apache.dolphinscheduler.server.master.runner.task.blocking;

import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.task.ILogicTaskPluginFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockingLogicTaskPluginFactory implements ILogicTaskPluginFactory<BlockingLogicTask> {

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Override
    public BlockingLogicTask createLogicTask(TaskExecutionContext taskExecutionContext) {
        return new BlockingLogicTask(taskExecutionContext, processInstanceExecCacheManager, processInstanceDao,
                taskInstanceDao);
    }

    @Override
    public String getTaskType() {
        return BlockingLogicTask.TASK_TYPE;
    }
}

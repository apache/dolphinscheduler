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

package org.apache.dolphinscheduler.server.master.dag;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowExecutionDAGFactory implements IWorkflowExecutionDAGFactory {

    @Autowired
    private ITaskExecutionRunnableRepositoryFactory taskExecutionRunnableRepositoryFactory;

    @Autowired
    private ITaskExecutionRunnableFactory taskExecutionRunnableFactory;

    @Autowired
    private ITaskExecutionContextFactory taskExecutionContextFactory;

    @Override
    public IWorkflowExecutionDAG createWorkflowExecutionDAG() {
        // todo:
        TaskExecutionRunnableRepository taskExecutionRunnableRepository =
                taskExecutionRunnableRepositoryFactory.createTaskExecutionRunnableRepository();
        loadTheHistoryTaskExecutionRunnable()
                .forEach(taskExecutionRunnableRepository::storeTaskExecutionRunnable);

        return WorkflowExecutionDAG.builder()
                .taskExecutionContextFactory(taskExecutionContextFactory)
                .taskExecutionRunnableFactory(taskExecutionRunnableFactory)
                .taskExecutionRunnableRepository(taskExecutionRunnableRepository)
                .workflowDAG(null)
                .build();
    }

    private List<TaskExecutionRunnable> loadTheHistoryTaskExecutionRunnable() {
        return new ArrayList<>();
    }
}

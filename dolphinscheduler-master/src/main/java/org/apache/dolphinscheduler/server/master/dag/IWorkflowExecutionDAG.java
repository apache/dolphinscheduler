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

import java.util.List;

/**
 * The WorkflowExecutionDAG represent the running workflow DAG.
 */
public interface IWorkflowExecutionDAG extends DAG {

    /**
     * Trigger the taskNode by given taskName.
     *
     * @param taskName taskNodeName
     * @return TaskExecutionRunnable
     */
    TaskExecutionRunnable triggerTask(String taskName);

    /**
     * Get TaskExecutionRunnable by given TaskInstanceId.
     *
     * @param taskInstanceId taskInstanceId.
     * @return TaskExecutionRunnable
     */
    TaskExecutionRunnable getTaskExecutionRunnableById(Integer taskInstanceId);

    /**
     * Get TaskExecutionRunnable by given taskName.
     *
     * @param taskName task name.
     * @return TaskExecutionRunnable
     */
    TaskExecutionRunnable getTaskExecutionRunnableByName(String taskName);

    /**
     * Get TaskExecutionRunnable which is not finished.
     *
     * @return TaskExecutionRunnable
     */
    List<TaskExecutionRunnable> getActiveTaskExecutionRunnable();

    /**
     * Get the direct pre TaskExecutionRunnable of the given taskName.
     *
     * @param taskName task name.
     * @return TaskExecutionRunnable
     */
    List<TaskExecutionRunnable> getDirectPreTaskExecutionRunnable(String taskName);

    /**
     * Check whether the taskNode is ready to run.
     *
     * @param taskName taskNodeName
     * @return true if the taskNode is ready to run.
     */
    boolean isTaskAbleToBeTriggered(String taskName);

}

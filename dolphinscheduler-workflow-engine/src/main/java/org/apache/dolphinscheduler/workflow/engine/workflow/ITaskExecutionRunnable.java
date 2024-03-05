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

package org.apache.dolphinscheduler.workflow.engine.workflow;

/**
 * The TaskExecutionRunnable represent the running task, it is responsible for operate the task instance. e.g. dispatch, kill, pause.
 */
public interface ITaskExecutionRunnable {

    /**
     * Dispatch the task instance.
     */
    void dispatch();

    /**
     * Run the task instance.
     */
    void run();

    /**
     * Kill the task instance.
     */
    void kill();

    /**
     * Pause the task instance.
     */
    void pause();

    /**
     * Get the task execution context.
     *
     * @return the task execution context
     */
    ITaskExecutionContext getTaskExecutionContext();

    /**
     * Determine whether the current task is ready to trigger the post task node.
     *
     * @param taskNodeName post task name
     * @return true if the current task can be accessed to the post task.
     */
    boolean isReadyToTrigger(String taskNodeName);
}

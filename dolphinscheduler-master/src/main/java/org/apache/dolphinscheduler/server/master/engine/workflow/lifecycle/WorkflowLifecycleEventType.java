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

package org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle;

import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;

public enum WorkflowLifecycleEventType implements ILifecycleEventType {

    /**
     * Start the workflow instance
     */
    START,
    /**
     * Notify the workflow instance there exist a task has been finished, and should do DAG topology logic transaction.
     */
    TOPOLOGY_LOGICAL_TRANSACTION_WITH_TASK_FINISH,
    /**
     * Pause the workflow instance
     */
    PAUSE,
    /**
     * The workflow instance has been paused
     */
    PAUSED,
    /**
     * Stop the workflow instance
     */
    STOP,
    /**
     * The workflow instance has been stopped
     */
    STOPPED,
    /**
     * The workflow instance has been success
     */
    SUCCEED,
    /**
     * The workflow instance has been failed
     */
    FAILED,
    /**
     * Finalize the workflow instance.
     */
    FINALIZE,

}

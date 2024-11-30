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

package org.apache.dolphinscheduler.server.master.engine.task.lifecycle;

import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;

public enum TaskLifecycleEventType implements ILifecycleEventType {

    /**
     * Start the Task instance.
     */
    START,
    /**
     * Dispatch the task instance to target.
     */
    DISPATCH,
    /**
     * The task instance is dispatched to the target executor server.
     */
    DISPATCHED,
    /**
     * // todo: maybe we can remove this event, once the task has been dispatched it should start
     * The task instance is running at the target executor server.
     */
    RUNNING,
    /**
     * The task instance's runtime context changed.
     */
    RUNTIME_CONTEXT_CHANGED,
    /**
     * Do Timeout strategy of the task instance.
     */
    TIMEOUT,
    /**
     * Retry the task instance.
     */
    RETRY,
    /**
     * Pause the task instance.
     */
    PAUSE,
    /**
     * The task instance is paused.
     */
    PAUSED,
    /**
     * Failover the task instance.
     */
    FAILOVER,
    /**
     * Kill the task instance.
     */
    KILL,
    /**
     * The task instance is killed.
     */
    KILLED,
    /**
     * The task instance is success.
     */
    SUCCEEDED,
    /**
     * The task instance is failed.
     */
    FAILED,
    ;

}

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

package org.apache.dolphinscheduler.extract.master.transportor;

// todo: add event source to distinguish the event is from executor or user operation
// 将这个类移到common中?
public interface ITaskExecutionEvent {

    int getWorkflowInstanceId();

    int getTaskInstanceId();

    long getEventCreateTime();

    void setEventCreateTime(long eventCreateTime);

    Long getEventSendTime();

    void setEventSendTime(Long eventSendTime);

    void setWorkflowInstanceHost(String host);

    String getWorkflowInstanceHost();

    void setTaskInstanceHost(String host);

    String getTaskInstanceHost();

    TaskInstanceExecutionEventType getEventType();

    enum TaskInstanceExecutionEventType {
        DISPATCHED,
        RUNNING,
        PAUSED,
        KILLED,
        FAILED,
        SUCCESS,
    }

}

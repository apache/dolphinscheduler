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
package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.Date;

public class BlockingTaskExecThread extends MasterBaseTaskExecThread{

    public BlockingTaskExecThread(TaskInstance taskInstance){
        super(taskInstance);
    }

    /**
    * For test use, test process run properly
    * set start time, end time
    * set task task status SUCCESS
    * the piece of code copy from ConditionExecThread.java
    */
    @Override
    protected Boolean submitWaitComplete() {
        taskInstance.setStartTime(new Date());
        this.taskInstance = submit();
        ExecutionStatus status = ExecutionStatus.SUCCESS;
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
        return true;
    }
}

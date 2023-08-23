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

package org.apache.dolphinscheduler.server.master.runner.execute;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.runner.message.LogicTaskInstanceExecutionEventSenderManager;
import org.apache.dolphinscheduler.server.master.runner.task.LogicTaskPluginFactoryBuilder;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class MasterDelayTaskExecuteRunnable extends MasterTaskExecuteRunnable implements Delayed {

    public MasterDelayTaskExecuteRunnable(TaskExecutionContext taskExecutionContext,
                                          LogicTaskPluginFactoryBuilder logicTaskPluginFactoryBuilder,
                                          LogicTaskInstanceExecutionEventSenderManager logicTaskInstanceExecutionEventSenderManager) {
        super(taskExecutionContext, logicTaskPluginFactoryBuilder, logicTaskInstanceExecutionEventSenderManager);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MasterDelayTaskExecuteRunnable)) {
            return false;
        }
        MasterDelayTaskExecuteRunnable other = (MasterDelayTaskExecuteRunnable) obj;
        return other.getTaskExecutionContext().getTaskInstanceId() == this.getTaskExecutionContext()
                .getTaskInstanceId();
    }

    @Override
    public int hashCode() {
        return this.getTaskExecutionContext().getTaskInstanceId();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        TaskExecutionContext taskExecutionContext = getTaskExecutionContext();
        return unit.convert(
                DateUtils.getRemainTime(
                        taskExecutionContext.getFirstSubmitTime(), taskExecutionContext.getDelayTime() * 60L),
                TimeUnit.SECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

}

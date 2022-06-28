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

package org.apache.dolphinscheduler.server.master.config;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostSelector;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import java.time.Duration;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "master")
public class MasterConfig {
    /**
     * The master RPC server listen port.
     */
    private int listenPort = 5678;
    /**
     * The max batch size used to fetch command from database.
     */
    private int fetchCommandNum = 10;
    /**
     * The thread number used to prepare processInstance. This number shouldn't bigger than fetchCommandNum.
     */
    private int preExecThreads = 10;
    /**
     * todo: We may need to split the process/task into different thread size.
     * The thread number used to handle processInstance and task event.
     * Will create two thread poll to execute {@link WorkflowExecuteRunnable} and {@link TaskExecuteRunnable}.
     */
    private int execThreads = 10;
    /**
     * The task dispatch thread pool size.
     */
    private int dispatchTaskNumber = 3;
    /**
     * Worker select strategy.
     */
    private HostSelector hostSelector = HostSelector.LOWER_WEIGHT;
    /**
     * Master heart beat task execute interval.
     */
    private Duration heartbeatInterval = Duration.ofSeconds(10);
    /**
     * task submit max retry times.
     */
    private int taskCommitRetryTimes = 5;
    /**
     * task submit retry interval.
     */
    private Duration taskCommitInterval = Duration.ofSeconds(1);
    /**
     * state wheel check interval, if this value is bigger, may increase the delay of task/processInstance.
     */
    private Duration stateWheelInterval = Duration.ofMillis(5);
    private double maxCpuLoadAvg = -1;
    private double reservedMemory = 0.3;
    private Duration failoverInterval = Duration.ofMinutes(10);
    private boolean killYarnJobWhenTaskFailover = true;

    @PostConstruct
    public void validate() {
        checkArgument(listenPort > 0, "listen-port " + listenPort + " is invalidated");
        checkArgument(fetchCommandNum > 0, "fetch-command-num " + fetchCommandNum + " should bigger then 0");
        checkArgument(preExecThreads > 0, "pre-exec-threads " + preExecThreads + " should bigger then 0");
        checkArgument(execThreads > 0, "exec-threads " + execThreads + " should bigger then 0");
        checkArgument(dispatchTaskNumber > 0, "dispatch-task-number " + dispatchTaskNumber + " should bigger then 0");
        checkArgument(heartbeatInterval.toMillis() > 0, "heartbeat-interval " + heartbeatInterval + " should bigger then 0");
        checkArgument(taskCommitRetryTimes > 0, "task-commit-retry-times " + taskCommitRetryTimes + " should bigger then 0");
        checkArgument(taskCommitInterval.toMillis() > 0, "task-commit-interval " + taskCommitInterval + " should bigger then 0");
        checkArgument(stateWheelInterval.toMillis() > 0, "state-wheel-interval " + stateWheelInterval + " should bigger then 0");
        checkArgument(failoverInterval.toMillis() > 0, "failover-interval " + failoverInterval + " should bigger then 0");

        if (maxCpuLoadAvg <= 0) {
            maxCpuLoadAvg = Runtime.getRuntime().availableProcessors() * 2;
        }
    }
}

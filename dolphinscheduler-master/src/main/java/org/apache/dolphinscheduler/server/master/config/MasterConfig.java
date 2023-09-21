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

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.registry.api.ConnectStrategyProperties;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostSelector;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "master")
@Slf4j
public class MasterConfig implements Validator {

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

    // todo: change to sync thread pool/ async thread pool ?
    private int masterTaskExecuteThreadPoolSize = Runtime.getRuntime().availableProcessors();

    private int masterAsyncTaskStateCheckThreadPoolSize = Runtime.getRuntime().availableProcessors();
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
    private double maxCpuLoadAvg = 1;
    private double reservedMemory = 0.1;
    private Duration failoverInterval = Duration.ofMinutes(10);
    private boolean killApplicationWhenTaskFailover = true;
    private ConnectStrategyProperties registryDisconnectStrategy = new ConnectStrategyProperties();

    private Duration workerGroupRefreshInterval = Duration.ofSeconds(10L);

    // ip:listenPort
    private String masterAddress;

    // /nodes/master/ip:listenPort
    private String masterRegistryPath;

    @Override
    public boolean supports(Class<?> clazz) {
        return MasterConfig.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MasterConfig masterConfig = (MasterConfig) target;
        if (masterConfig.getListenPort() <= 0) {
            errors.rejectValue("listen-port", null, "is invalidated");
        }
        if (masterConfig.getFetchCommandNum() <= 0) {
            errors.rejectValue("fetch-command-num", null, "should be a positive value");
        }
        if (masterConfig.getPreExecThreads() <= 0) {
            errors.rejectValue("per-exec-threads", null, "should be a positive value");
        }
        if (masterConfig.getExecThreads() <= 0) {
            errors.rejectValue("exec-threads", null, "should be a positive value");
        }
        if (masterConfig.getDispatchTaskNumber() <= 0) {
            errors.rejectValue("dispatch-task-number", null, "should be a positive value");
        }
        if (masterConfig.getHeartbeatInterval().toMillis() < 0) {
            errors.rejectValue("heartbeat-interval", null, "should be a valid duration");
        }
        if (masterConfig.getTaskCommitRetryTimes() <= 0) {
            errors.rejectValue("task-commit-retry-times", null, "should be a positive value");
        }
        if (masterConfig.getTaskCommitInterval().toMillis() <= 0) {
            errors.rejectValue("task-commit-interval", null, "should be a valid duration");
        }
        if (masterConfig.getStateWheelInterval().toMillis() <= 0) {
            errors.rejectValue("state-wheel-interval", null, "should be a valid duration");
        }
        if (masterConfig.getFailoverInterval().toMillis() <= 0) {
            errors.rejectValue("failover-interval", null, "should be a valid duration");
        }
        if (masterConfig.getMaxCpuLoadAvg() <= 0) {
            masterConfig.setMaxCpuLoadAvg(100);
        }
        if (masterConfig.getReservedMemory() <= 0) {
            masterConfig.setReservedMemory(100);
        }

        if (masterConfig.getWorkerGroupRefreshInterval().getSeconds() < 10) {
            errors.rejectValue("worker-group-refresh-interval", null, "should >= 10s");
        }
        if (StringUtils.isEmpty(masterConfig.getMasterAddress())) {
            masterConfig.setMasterAddress(NetUtils.getAddr(masterConfig.getListenPort()));
        }

        masterConfig.setMasterRegistryPath(
                RegistryNodeType.MASTER.getRegistryPath() + "/" + masterConfig.getMasterAddress());
        printConfig();
    }

    private void printConfig() {
        log.info("Master config: listenPort -> {} ", listenPort);
        log.info("Master config: fetchCommandNum -> {} ", fetchCommandNum);
        log.info("Master config: preExecThreads -> {} ", preExecThreads);
        log.info("Master config: execThreads -> {} ", execThreads);
        log.info("Master config: dispatchTaskNumber -> {} ", dispatchTaskNumber);
        log.info("Master config: hostSelector -> {} ", hostSelector);
        log.info("Master config: heartbeatInterval -> {} ", heartbeatInterval);
        log.info("Master config: taskCommitRetryTimes -> {} ", taskCommitRetryTimes);
        log.info("Master config: taskCommitInterval -> {} ", taskCommitInterval);
        log.info("Master config: stateWheelInterval -> {} ", stateWheelInterval);
        log.info("Master config: maxCpuLoadAvg -> {} ", maxCpuLoadAvg);
        log.info("Master config: reservedMemory -> {} ", reservedMemory);
        log.info("Master config: failoverInterval -> {} ", failoverInterval);
        log.info("Master config: killApplicationWhenTaskFailover -> {} ", killApplicationWhenTaskFailover);
        log.info("Master config: registryDisconnectStrategy -> {} ", registryDisconnectStrategy);
        log.info("Master config: masterAddress -> {} ", masterAddress);
        log.info("Master config: masterRegistryPath -> {} ", masterRegistryPath);
        log.info("Master config: workerGroupRefreshInterval -> {} ", workerGroupRefreshInterval);
    }
}

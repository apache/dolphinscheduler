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
    private int masterSyncTaskExecutorThreadPoolSize = Runtime.getRuntime().availableProcessors();

    private int masterAsyncTaskExecutorThreadPoolSize = Runtime.getRuntime().availableProcessors();
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
    private Duration maxHeartbeatInterval = Duration.ofSeconds(10);
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
    private MasterServerLoadProtection serverLoadProtection = new MasterServerLoadProtection();
    private Duration failoverInterval = Duration.ofMinutes(10);
    private boolean killApplicationWhenTaskFailover = true;
    private ConnectStrategyProperties registryDisconnectStrategy = new ConnectStrategyProperties();

    private Duration workerGroupRefreshInterval = Duration.ofSeconds(10L);

    private CommandFetchStrategy commandFetchStrategy = new CommandFetchStrategy();

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
        if (masterConfig.getPreExecThreads() <= 0) {
            errors.rejectValue("per-exec-threads", null, "should be a positive value");
        }
        if (masterConfig.getExecThreads() <= 0) {
            errors.rejectValue("exec-threads", null, "should be a positive value");
        }
        if (masterConfig.getDispatchTaskNumber() <= 0) {
            errors.rejectValue("dispatch-task-number", null, "should be a positive value");
        }
        if (masterConfig.getMaxHeartbeatInterval().toMillis() < 0) {
            errors.rejectValue("max-heartbeat-interval", null, "should be a valid duration");
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

        if (masterConfig.getWorkerGroupRefreshInterval().getSeconds() < 10) {
            errors.rejectValue("worker-group-refresh-interval", null, "should >= 10s");
        }
        if (StringUtils.isEmpty(masterConfig.getMasterAddress())) {
            masterConfig.setMasterAddress(NetUtils.getAddr(masterConfig.getListenPort()));
        }
        commandFetchStrategy.validate(errors);

        masterConfig.setMasterRegistryPath(
                RegistryNodeType.MASTER.getRegistryPath() + "/" + masterConfig.getMasterAddress());
        printConfig();
    }

    private void printConfig() {
        String config =
                "\n****************************Master Configuration**************************************" +
                        "\n  listen-port -> " + listenPort +
                        "\n  pre-exec-threads -> " + preExecThreads +
                        "\n  exec-threads -> " + execThreads +
                        "\n  dispatch-task-number -> " + dispatchTaskNumber +
                        "\n  host-selector -> " + hostSelector +
                        "\n  max-heartbeat-interval -> " + maxHeartbeatInterval +
                        "\n  task-commit-retry-times -> " + taskCommitRetryTimes +
                        "\n  task-commit-interval -> " + taskCommitInterval +
                        "\n  state-wheel-interval -> " + stateWheelInterval +
                        "\n  server-load-protection -> " + serverLoadProtection +
                        "\n  failover-interval -> " + failoverInterval +
                        "\n  kill-application-when-task-failover -> " + killApplicationWhenTaskFailover +
                        "\n  registry-disconnect-strategy -> " + registryDisconnectStrategy +
                        "\n  master-address -> " + masterAddress +
                        "\n  master-registry-path: " + masterRegistryPath +
                        "\n  worker-group-refresh-interval: " + workerGroupRefreshInterval +
                        "\n  command-fetch-strategy: " + commandFetchStrategy +
                        "\n****************************Master Configuration**************************************";
        log.info(config);
    }
}

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

package org.apache.dolphinscheduler.server.master.service;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

@Service
@Slf4j
public class MasterFailoverService {

    private final RegistryClient registryClient;
    private final MasterConfig masterConfig;
    private final ProcessService processService;
    private final String localAddress;

    private final ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    public MasterFailoverService(@NonNull RegistryClient registryClient,
                                 @NonNull MasterConfig masterConfig,
                                 @NonNull ProcessService processService,
                                 @NonNull ProcessInstanceExecCacheManager processInstanceExecCacheManager) {
        this.registryClient = registryClient;
        this.masterConfig = masterConfig;
        this.processService = processService;
        this.localAddress = masterConfig.getMasterAddress();
        this.processInstanceExecCacheManager = processInstanceExecCacheManager;
    }

    /**
     * check master failover
     */
    @Counted(value = "ds.master.scheduler.failover.check.count")
    @Timed(value = "ds.master.scheduler.failover.check.time", percentiles = {0.5, 0.75, 0.95, 0.99}, histogram = true)
    public void checkMasterFailover() {
        List<String> needFailoverMasterHosts = processService.queryNeedFailoverProcessInstanceHost()
                .stream()
                // failover myself || dead server
                .filter(host -> localAddress.equals(host)
                        || !registryClient.checkNodeExists(host, RegistryNodeType.MASTER))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(needFailoverMasterHosts)) {
            return;
        }
        log.info("Master failover service {} begin to failover hosts:{}", localAddress, needFailoverMasterHosts);

        for (String needFailoverMasterHost : needFailoverMasterHosts) {
            failoverMaster(needFailoverMasterHost);
        }
    }

    public void failoverMaster(String masterHost) {
        String failoverPath = RegistryNodeType.MASTER_FAILOVER_LOCK.getRegistryPath() + "/" + masterHost;
        try {
            registryClient.getLock(failoverPath);
            doFailoverMaster(masterHost);
        } catch (Exception e) {
            log.error("Master server failover failed, host:{}", masterHost, e);
        } finally {
            registryClient.releaseLock(failoverPath);
        }
    }

    /**
     * Failover master, will failover process instance and associated task instance.
     * <p>When the process instance belongs to the given masterHost and the restartTime is before the current server start up time,
     * then the process instance will be failovered.
     *
     * @param masterHost master host
     */
    private void doFailoverMaster(@NonNull String masterHost) {
        StopWatch failoverTimeCost = StopWatch.createStarted();

        Optional<Date> masterStartupTimeOptional =
                getServerStartupTime(registryClient.getServerList(RegistryNodeType.MASTER),
                        masterHost);
        List<ProcessInstance> needFailoverProcessInstanceList = processService.queryNeedFailoverProcessInstances(
                masterHost);
        if (CollectionUtils.isEmpty(needFailoverProcessInstanceList)) {
            return;
        }

        log.info(
                "Master[{}] failover starting there are {} workflowInstance may need to failover, will do a deep check, workflowInstanceIds: {}",
                masterHost,
                needFailoverProcessInstanceList.size(),
                needFailoverProcessInstanceList.stream().map(ProcessInstance::getId).collect(Collectors.toList()));

        for (ProcessInstance processInstance : needFailoverProcessInstanceList) {
            try (
                    LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setWorkflowInstanceIdMDC(processInstance.getId())) {
                log.info("WorkflowInstance failover starting");
                if (!checkProcessInstanceNeedFailover(masterStartupTimeOptional, processInstance)) {
                    continue;
                }

                ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("failover",
                        processInstance.getProcessDefinitionCode().toString());
                processService.processNeedFailoverProcessInstances(processInstance);
                log.info("WorkflowInstance failover finished");
            }
        }

        failoverTimeCost.stop();
        log.info("Master[{}] failover finished, useTime:{}ms",
                masterHost,
                failoverTimeCost.getTime(TimeUnit.MILLISECONDS));
    }

    private Optional<Date> getServerStartupTime(List<Server> servers, String host) {
        if (CollectionUtils.isEmpty(servers)) {
            return Optional.empty();
        }
        Date serverStartupTime = null;
        for (Server server : servers) {
            if (host.equals(server.getHost() + Constants.COLON + server.getPort())) {
                serverStartupTime = server.getCreateTime();
                break;
            }
        }
        return Optional.ofNullable(serverStartupTime);
    }

    private boolean checkProcessInstanceNeedFailover(Optional<Date> beFailoveredMasterStartupTimeOptional,
                                                     @NonNull ProcessInstance processInstance) {
        // The process has already been failover, since when we do master failover we will hold a lock, so we can
        // guarantee
        // the host will not be set concurrent.
        if (Constants.NULL.equals(processInstance.getHost())) {
            log.info("The workflowInstance's  host is NULL, no need to failover");
            return false;
        }
        if (!beFailoveredMasterStartupTimeOptional.isPresent()) {
            // the master is not active, we can failover all it's processInstance
            return true;
        }
        Date beFailoveredMasterStartupTime = beFailoveredMasterStartupTimeOptional.get();

        if (processInstance.getStartTime().after(beFailoveredMasterStartupTime)) {
            // The processInstance is newly created
            log.info("The workflowInstance is newly created, no need to failover");
            return false;
        }
        if (processInstance.getRestartTime() != null
                && processInstance.getRestartTime().after(beFailoveredMasterStartupTime)) {
            // the processInstance is already be failovered.
            log.info(
                    "The workflowInstance's restartTime is after the dead master startup time, no need to failover");
            return false;
        }

        if (processInstanceExecCacheManager.contains(processInstance.getId())) {
            // the processInstance is a running process instance in the current master
            log.info("The workflowInstance is running in the current master, no need to failover");
            return false;
        }

        return true;
    }

}

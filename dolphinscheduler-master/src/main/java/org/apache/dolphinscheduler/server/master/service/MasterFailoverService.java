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

//通过获取失败重试锁来确保只有一个线程可以执行主节点故移，并调用doFailoverMaster方法来执行实际的失败重试操作。无论失败重试是否成功，最终都会释放失败重试锁。
    public void failoverMaster(String masterHost) {
        //执行路径  定义一个字符串变量failoverPath，表示失败重试锁的路径，该路径结合了注册节点类型和Master主机。
        String failoverPath = RegistryNodeType.MASTER_FAILOVER_LOCK.getRegistryPath() + "/" + masterHost;
        //在try块中，通过调用registryClient.getLock(failoverPath)方法获取失败重试锁。这个锁用来保只有一个线程可以执行主节点失败重试操作。
        try {
            registryClient.getLock(failoverPath);
            //主节点失败重试
            doFailoverMaster(masterHost);
        } catch (Exception e) {
            log.error("Master server failover failed, host:{}", masterHost, e);
        } finally {
            //在finally块中，通过调用registryClient.releaseLock(failoverPath)方法释放失败重试锁。
            registryClient.releaseLock(failoverPath);
        }
    }

    /**
     * Failover master, will failover process instance and associated task instance.
     * <p>When the process instance belongs to the given masterHost and the restartTime is before the current server start up time,
     * then the process instance will be failovered.
     *
     * 方法首先检查Master的启动时间和需要失败转移的流程实例，如果没有需要失败转移的流程实例则返回。如果有需要失败转移的流程实例，则遍历每个流程实例
     * ，并检查是否需要进行失败转移。如果需要进行失败转移，则先将其状态设置为“failover”，然后调用processService的方法进行失败转移。最后程序计算失败转移的用时，并进行日志记录。
     *
     * 这段代码定义了一个名为 `doFailoverMaster` 的私有 Java 方法，它用于处理主节点(master node)的失败重试（failover）。这方法接受一个非空的字符串参数，表示主机主机名（masterHost）。
     *
     * 总的来说，代码的流程是：获取主节点启动时间-->获取所有需要失败重试的工作流实例-->遍历所有实例，检查是否需要失败重试-->如果需要就执行失败重试-->最后打印出失败重试总耗时。 
     * @param masterHost master host
     */
    private void doFailoverMaster(@NonNull String masterHost) {
        //创建一个名为 `failoverTimeCost` 的StopWatch实例，用于计算失败重试所需的时间。
        StopWatch failoverTimeCost = StopWatch.createStarted();

        //通过调用 `getServerStartupTime` 方法获取服务器启动时间，并将其保存到 `masterStartupTimeOptional` 中。
        Optional<Date> masterStartupTimeOptional =
                getServerStartupTime(registryClient.getServerList(RegistryNodeType.MASTER),
                        masterHost);
        //从 `processService` 对象中查詢需要失败重试的处理实例（Process Instance），并将它们保存到 `needFailoverProcessInstanceList`
        List<ProcessInstance> needFailoverProcessInstanceList = processService.queryNeedFailoverProcessInstances(
                masterHost);
        //如果 `needFailoverProcessInstanceList` 列表为空，即没有需要失败重试的处理实例，那么方法就此结束并返回。
        if (CollectionUtils.isEmpty(needFailoverProcessInstanceList)) {
            return;
        }
        // 打印日志，表明正在执行主节点的失败重试，并记录需要失败重试的工作流实例的数量和ID。
        log.info(
                "Master[{}] failover starting there are {} workflowInstance may need to failover, will do a deep check, workflowInstanceIds: {}",
                masterHost,
                needFailoverProcessInstanceList.size(),
                needFailoverProcessInstanceList.stream().map(ProcessInstance::getId).collect(Collectors.toList()));
        //通过for循环遍历每一个需要失败重试的处理实例。
        for (ProcessInstance processInstance : needFailoverProcessInstanceList) {
            try (
                //首先设置工作流实例ID到MDC上下文，然后打印开始进行工作流实例失败转移的日志。
                    LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setWorkflowInstanceIdMDC(processInstance.getId())) {
                log.info("WorkflowInstance failover starting");
                //通过调用 `checkProcessInstanceNeedFailover` 方法检查处理实例是否真的需要进行失败重试。如果不需要，就跳过当前循环。之后通过
                if (!checkProcessInstanceNeedFailover(masterStartupTimeOptional, processInstance)) {
                    continue;
                }
                //调用 `ProcessInstanceMetrics` 来增加相关度量。根据给定的状态和流程定义代码来增加流程实例的计数器。该计数器是全局的，可以通过标签参数来标识不同的计数器。
                ProcessInstanceMetrics.incProcessInstanceByStateAndProcessDefinitionCode("failover",
                        processInstance.getProcessDefinitionCode().toString());
                //通过 `processService` 对象中的 `processNeedFailoverProcessInstances` 方法来处理需要失败重试的实例。
                processService.processNeedFailoverProcessInstances(processInstance);
                log.info("WorkflowInstance failover finished");
            }
        }
//错误转移完成后，停止 `failoverTimeCost` 计时，并打印出执行失败重试所花费的总时间。
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
//检查给定的工作流实例（Process Instance）是否需要失败重试。
//接收两个参数：一个可能包含失败重试主机启动时间的 Optional<Date> 对象和一个非空的工作流程实例 ProcessInstance。
//检查给定的工作流实例是否需要进行失败重试。通过判断依据包括实例的 host 是否为null， 需要失败重试的master节点的启动时间，实例的启动时间和重启时间，以及实例ID是否存在于processInstanceExecCacheManager中来做判断。
    private boolean checkProcessInstanceNeedFailover(Optional<Date> beFailoveredMasterStartupTimeOptional,
                                                     @NonNull ProcessInstance processInstance) {
        // The process has already been failover, since when we do master failover we will hold a lock, so we can
        // guarantee
        // the host will not be set concurrent.   这个过程已经是失败重试，因为当我们进行主失败重试时，我们将持有一个锁，所以我们可以保证主机不会被并发设置。
        // 检查 ProcessInstance 的 host 是否为 Null。如果是，打印一条日志，返回 false, 表示此工作流实例不需要进行失败重试。
        if (Constants.NULL.equals(processInstance.getHost())) {
            log.info("The workflowInstance's  host is NULL, no need to failover");
            return false;
        }
        //如果没有可用的失败重试master启动时间，返回 true，意为需要将所有该master的进程实例进行失败重试。
        if (!beFailoveredMasterStartupTimeOptional.isPresent()) {
            // the master is not active, we can failover all it's processInstance
            return true;
        }
        //如果实例的启动时间是在失败重试后的主节点启动时间之后，打印一条日志，返回 false，表明这是一个新创建的实例，不需要进行失败重试。
        Date beFailoveredMasterStartupTime = beFailoveredMasterStartupTimeOptional.get();

        if (processInstance.getStartTime().after(beFailoveredMasterStartupTime)) {
            // The processInstance is newly created
            log.info("The workflowInstance is newly created, no need to failover");
            return false;
        }
        //如果实例有重启时间且实例的重启时间是在失败重试后的主节点启动之后，打印一条日志，返回 false，表示该进程实例已经进行过失败重试，无需再次进行。
        if (processInstance.getRestartTime() != null
                && processInstance.getRestartTime().after(beFailoveredMasterStartupTime)) {
            // the processInstance is already be failovered.
            log.info(
                    "The workflowInstance's restartTime is after the dead master startup time, no need to failover");
            return false;
        }
        //如果工作流程实例的ID存在于 processInstanceExecCacheManager模块，打印一条日志，返回 false，表示该实例正在当前主节点上运行，不需要进行失败重试。
        if (processInstanceExecCacheManager.contains(processInstance.getId())) {
            // the processInstance is a running process instance in the current master
            log.info("The workflowInstance is running in the current master, no need to failover");
            return false;
        }
        //如果以上所有条件都不符合，方法返回 true，表示此工作流程实例需要进行失败重试。
        return true;
    }

}

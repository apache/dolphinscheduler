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

package org.apache.dolphinscheduler.server.master.engine.workflow.serial;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowSerialCoordinator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Used to coordinate the serial execution of workflows.
 * <p> Once a workflow instance is submitted with serial wait strategy, it will be added to the serial queue.
 * <p> A dedicated thread will poll the serial queue and check if the workflow instance can be executed.
 */
@Slf4j
@Component
public class WorkflowSerialCoordinator implements IWorkflowSerialCoordinator {

    @Autowired
    private SerialCommandDao serialCommandDao;

    @Autowired
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Autowired
    private SerialCommandWaitHandler serialCommandWaitHandler;

    @Autowired
    private SerialCommandDiscardHandler serialCommandDiscardHandler;

    @Autowired
    private SerialCommandPriorityHandler serialCommandPriorityHandler;

    private volatile boolean flag = false;

    private Thread internalThread;

    private static final int DEFAULT_FETCH_SIZE = 1000;

    private static final int DEFAULT_FETCH_INTERVAL_SECONDS = 5;

    @Override
    public synchronized void start() {
        log.info("WorkflowSerialCoordinator starting...");
        if (flag) {
            throw new IllegalStateException("WorkflowSerialCoordinator is already started");
        }
        if (internalThread != null) {
            throw new IllegalStateException("InternalThread is already started");
        }
        flag = true;
        internalThread = new BaseDaemonThread(this::doStart) {
        };
        internalThread.setName("WorkflowSerialCoordinator-Thread");
        internalThread.start();
        log.info("WorkflowSerialCoordinator started...");
    }

    private void doStart() {
        while (flag) {
            try {
                final StopWatch workflowSerialCoordinatorRoundCost = StopWatch.createStarted();
                final List<SerialCommandsGroup> serialCommandsGroups = fetchSerialCommands();
                serialCommandsGroups.forEach(this::handleSerialCommand);
                log.debug("WorkflowSerialCoordinator handled SerialCommandsGroup size: {}, cost: {}/ms ",
                        serialCommandsGroups.size(),
                        workflowSerialCoordinatorRoundCost.getDuration().toMillis());
            } catch (Throwable e) {
                log.error("WorkflowSerialCoordinator error", e);
            } finally {
                // sleep 5s
                ThreadUtils.sleep(TimeUnit.SECONDS.toMillis(DEFAULT_FETCH_INTERVAL_SECONDS));
            }
        }
    }

    private void handleSerialCommand(SerialCommandsGroup serialCommandsGroup) {
        try {
            if (serialCommandsGroup.getExecutionType() == null) {
                log.error("Cannot find the ExecutionType for workflow: {}-{}",
                        serialCommandsGroup.getWorkflowDefinitionCode(),
                        serialCommandsGroup.getWorkflowDefinitionVersion());
                return;
            }
            switch (serialCommandsGroup.getExecutionType()) {
                case PARALLEL:
                    throw new IllegalStateException(
                            "SerialCommand with ExecutionType=PARALLEL is not supported, this shouldn't happen");
                case SERIAL_WAIT:
                    serialCommandWaitHandler.handle(serialCommandsGroup);
                    break;
                case SERIAL_DISCARD:
                    serialCommandDiscardHandler.handle(serialCommandsGroup);
                    break;
                case SERIAL_PRIORITY:
                    serialCommandPriorityHandler.handle(serialCommandsGroup);
                    break;
                default:
            }
        } catch (Exception ex) {
            log.error("Handle SerialCommandsGroup: {} error", serialCommandsGroup, ex);
        }
    }

    private List<SerialCommandsGroup> fetchSerialCommands() {
        // todo: set a limit here and fetch by incremental id
        final List<SerialCommandDto> serialCommands = serialCommandDao.fetchSerialCommands(DEFAULT_FETCH_SIZE);
        if (CollectionUtils.isEmpty(serialCommands)) {
            return Collections.emptyList();
        }

        // workflowCode-> workflowVersion -> commandGroup
        // Right now, we think each workflow has its own queue
        final Map<Long, Map<Integer, SerialCommandsGroup>> serialCommandsGroupMap = new HashMap<>();
        for (SerialCommandDto serialCommand : serialCommands) {
            serialCommandsGroupMap.computeIfAbsent(serialCommand.getWorkflowDefinitionCode(), k -> new HashMap<>())
                    .computeIfAbsent(serialCommand.getWorkflowDefinitionVersion(),
                            v -> createSerialCommandsGroup(serialCommand))
                    .getSerialCommands().add(serialCommand);
        }
        return serialCommandsGroupMap.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toList());
    }

    private SerialCommandsGroup createSerialCommandsGroup(SerialCommandDto serialCommand) {
        Long workflowDefinitionCode = serialCommand.getWorkflowDefinitionCode();
        Integer workflowDefinitionVersion = serialCommand.getWorkflowDefinitionVersion();
        final WorkflowDefinitionLog workflowDefinitionLog = workflowDefinitionLogDao
                .queryByDefinitionCodeAndVersion(workflowDefinitionCode, workflowDefinitionVersion);
        return SerialCommandsGroup.builder()
                .workflowDefinitionCode(workflowDefinitionCode)
                .workflowDefinitionVersion(workflowDefinitionVersion)
                .executionType(workflowDefinitionLog.getExecutionType())
                .serialCommands(new ArrayList<>())
                .build();
    }

    @Override
    public void close() {
        flag = false;
    }
}

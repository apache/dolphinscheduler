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

package org.apache.dolphinscheduler.server.master.failover;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class WorkflowFailoverTest {

    private WorkflowFailover workflowFailover;

    private WorkflowInstanceDao workflowInstanceDao;

    private CommandDao commandDao;

    private final AtomicReference<Integer> updatedWorkflowInstanceId = new AtomicReference<>();
    private final AtomicReference<WorkflowExecutionStatus> updatedOriginState = new AtomicReference<>();
    private final AtomicReference<WorkflowExecutionStatus> updatedTargetState = new AtomicReference<>();
    private final AtomicReference<Command> insertedCommand = new AtomicReference<>();
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(meterRegistry);

        workflowFailover = new WorkflowFailover();
        workflowInstanceDao = (WorkflowInstanceDao) Proxy.newProxyInstance(
                WorkflowInstanceDao.class.getClassLoader(),
                new Class<?>[]{WorkflowInstanceDao.class},
                (proxy, method, args) -> {
                    if ("updateWorkflowInstanceState".equals(method.getName())) {
                        updatedWorkflowInstanceId.set((Integer) args[0]);
                        updatedOriginState.set((WorkflowExecutionStatus) args[1]);
                        updatedTargetState.set((WorkflowExecutionStatus) args[2]);
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                });
        commandDao = (CommandDao) Proxy.newProxyInstance(
                CommandDao.class.getClassLoader(),
                new Class<?>[]{CommandDao.class},
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        insertedCommand.set((Command) args[0]);
                        return 1;
                    }
                    return defaultValue(method.getReturnType());
                });

        injectField(workflowFailover, "workflowInstanceDao", workflowInstanceDao);
        injectField(workflowFailover, "commandDao", commandDao);
    }

    @AfterEach
    void tearDown() {
        if (meterRegistry != null) {
            Metrics.removeRegistry(meterRegistry);
        }
    }

    @Test
    void shouldRecordFailoverMetricWhenFailoverWorkflow() {
        final long workflowDefinitionCode = System.nanoTime();
        final WorkflowInstance workflowInstance = WorkflowInstance.builder()
                .id(1)
                .name("workflow_instance")
                .workflowDefinitionCode(workflowDefinitionCode)
                .workflowDefinitionVersion(1)
                .state(WorkflowExecutionStatus.RUNNING_EXECUTION)
                .build();
        final double failoverBefore = workflowInstanceCount(workflowInstance.getWorkflowDefinitionCode());

        workflowFailover.failoverWorkflow(workflowInstance);

        assertThat(updatedWorkflowInstanceId.get()).isEqualTo(workflowInstance.getId());
        assertThat(updatedOriginState.get()).isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
        assertThat(updatedTargetState.get()).isEqualTo(WorkflowExecutionStatus.FAILOVER);

        assertThat(insertedCommand.get()).isNotNull();
        assertThat(insertedCommand.get().getCommandType()).isEqualTo(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS);
        assertThat(insertedCommand.get().getWorkflowInstanceId()).isEqualTo(workflowInstance.getId());

        assertThat(workflowInstanceCount(workflowInstance.getWorkflowDefinitionCode()))
                .isEqualTo(failoverBefore + 1.0d);
    }

    private double workflowInstanceCount(final long workflowDefinitionCode) {
        final Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tags(
                        "state",
                        "failover",
                        "workflow.definition.code",
                        String.valueOf(workflowDefinitionCode))
                .counter();
        return counter == null ? 0.0d : counter.count();
    }

    private static void injectField(Object target, String fieldName, Object value) {
        try {
            final Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Failed to inject field: " + fieldName, ex);
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}

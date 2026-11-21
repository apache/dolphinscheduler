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

package org.apache.dolphinscheduler.server.master.integration.cases;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.engine.MasterCoordinator;
import org.apache.dolphinscheduler.server.master.engine.TaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.WorkflowSerialCoordinator;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class MasterCoordinatorHATestCase extends AbstractMasterIntegrationTestCase {

    @Autowired
    private Registry registry;

    @Autowired
    private MasterCoordinator masterCoordinator;

    @Autowired
    private TaskGroupCoordinator taskGroupCoordinator;

    @Autowired
    private WorkflowSerialCoordinator workflowSerialCoordinator;

    @Test
    void leadershipLossAndReacquisitionReplaceWorkersAfterTheirExit() {
        assertTrue(masterCoordinator.isActive());
        Thread oldTaskGroupWorker = worker(taskGroupCoordinator);
        Thread oldSerialWorker = worker(workflowSerialCoordinator);
        String selectorPath = RegistryNodeType.MASTER_COORDINATOR.getRegistryPath();

        // Use the real JDBC/H2 Registry notification path to change the selected identity.
        registry.put(selectorPath, "peer", true);
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertFalse(masterCoordinator.isActive());
            assertFalse(oldTaskGroupWorker.isAlive());
            assertFalse(oldSerialWorker.isAlive());
        });

        registry.delete(selectorPath);
        await().atMost(Duration.ofSeconds(15)).until(masterCoordinator::isActive);
        Thread newTaskGroupWorker = worker(taskGroupCoordinator);
        Thread newSerialWorker = worker(workflowSerialCoordinator);
        assertNotSame(oldTaskGroupWorker, newTaskGroupWorker);
        assertNotSame(oldSerialWorker, newSerialWorker);
        assertTrue(newTaskGroupWorker.isAlive());
        assertTrue(newSerialWorker.isAlive());

        masterCoordinator.close();
        assertFalse(masterCoordinator.isActive());
        assertFalse(newTaskGroupWorker.isAlive());
        assertFalse(newSerialWorker.isAlive());
    }

    private static Thread worker(Object coordinator) {
        return (Thread) ReflectionTestUtils.getField(coordinator, "internalThread");
    }
}

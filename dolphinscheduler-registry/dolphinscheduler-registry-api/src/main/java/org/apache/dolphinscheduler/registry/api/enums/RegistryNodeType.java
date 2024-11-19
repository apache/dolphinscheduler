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

package org.apache.dolphinscheduler.registry.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegistryNodeType {

    FAILOVER_FINISH_NODES("FailoverFinishNodes", "/nodes/failover-finish-nodes"),

    MASTER("Master", "/nodes/master"),
    MASTER_FAILOVER_LOCK("MasterFailoverLock", "/lock/master-failover"),
    MASTER_TASK_GROUP_COORDINATOR_LOCK("TaskGroupCoordinatorLock", "/lock/master-task-group-coordinator"),
    MASTER_SERIAL_COORDINATOR_LOCK("SerialWorkflowCoordinator", "/lock/master-serial-workflow-coordinator"),

    WORKER("Worker", "/nodes/worker"),

    ALERT_SERVER("AlertServer", "/nodes/alert-server"),
    ALERT_HA_LEADER("AlertHALeader", "/nodes/alert-server-ha-leader");

    private final String name;

    private final String registryPath;
}

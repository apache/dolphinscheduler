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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowBackFillRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowTriggerRequest;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowExecuteResponse;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

public interface ExecutorService {

    /**
     * Trigger the workflow and return the workflow instance id.
     */
    Integer triggerWorkflowDefinition(final WorkflowTriggerRequest workflowTriggerRequest);

    /**
     * Backfill the workflow and return the workflow instance ids.
     */
    List<Integer> backfillWorkflowDefinition(final WorkflowBackFillRequest workflowBackFillRequest);

    /**
     * check whether the process definition can be executed
     *
     * @param projectCode       project code
     * @param processDefinition process definition
     * @param processDefineCode process definition code
     * @param version           process definition version
     */
    void checkProcessDefinitionValid(long projectCode, ProcessDefinition processDefinition, long processDefineCode,
                                     Integer version);

    /**
     * do action to execute task in process instance
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param processInstanceId process instance id
     * @param startNodeList     start node list
     * @param taskDependType    task depend type
     * @return execute result code
     */
    WorkflowExecuteResponse executeTask(User loginUser,
                                        long projectCode,
                                        Integer processInstanceId,
                                        String startNodeList,
                                        TaskDependType taskDependType);

    /**
     * Control workflow instance, you can use this interface to pause, stop, repeat, recover a workflow instance.
     */
    void controlWorkflowInstance(User loginUser, Integer workflowInstanceId, ExecuteType executeType);

    /**
     * check if the current process has subprocesses and all subprocesses are valid
     *
     * @param processDefinition
     * @return check result
     */
    boolean checkSubProcessDefinitionValid(ProcessDefinition processDefinition);

    /**
     * force start Task Instance
     *
     * @param loginUser
     * @param queueId
     * @return
     */
    Map<String, Object> forceStartTaskInstance(User loginUser, int queueId);

    /**
     * execute stream task instance
     *
     * @param loginUser       login user
     * @param projectCode     project code
     * @param warningGroupId  notify group id
     * @param workerGroup     worker group name
     * @param tenantCode      tenant code
     * @param environmentCode environment code
     * @param startParams     the global param values which pass to new process instance
     * @return execute process instance code
     */
    void execStreamTaskInstance(User loginUser, long projectCode,
                                long taskDefinitionCode, int taskDefinitionVersion,
                                int warningGroupId,
                                String workerGroup,
                                String tenantCode,
                                Long environmentCode,
                                Map<String, String> startParams,
                                int dryRun);
}

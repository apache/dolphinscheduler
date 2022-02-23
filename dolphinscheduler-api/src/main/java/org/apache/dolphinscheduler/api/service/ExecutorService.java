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

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * executor service
 */
public interface ExecutorService {

    /**
     * execute process instance
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param cronTime cron time
     * @param commandType command type
     * @param failureStrategy failuer strategy
     * @param startNodeList start nodelist
     * @param taskDependType node dependency type
     * @param warningType warning type
     * @param warningGroupId notify group id
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group name
     * @param environmentCode environment code
     * @param runMode run mode
     * @param timeout timeout
     * @param startParams the global param values which pass to new process instance
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @return execute process instance code
     */
    Map<String, Object> execProcessInstance(User loginUser, long projectCode,
                                            long processDefinitionCode, String cronTime, CommandType commandType,
                                            FailureStrategy failureStrategy, String startNodeList,
                                            TaskDependType taskDependType, WarningType warningType, int warningGroupId,
                                            RunMode runMode,
                                            Priority processInstancePriority, String workerGroup, Long environmentCode, Integer timeout,
                                            Map<String, String> startParams, Integer expectedParallelismNumber,
                                            int dryRun,
                                            ComplementDependentMode complementDependentMode);

    /**
     * check whether the process definition can be executed
     *
     * @param projectCode project code
     * @param processDefinition process definition
     * @param processDefineCode process definition code
     * @return check result code
     */
    Map<String, Object> checkProcessDefinitionValid(long projectCode, ProcessDefinition processDefinition, long processDefineCode);

    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @param executeType execute type
     * @return execute result code
     */
    Map<String, Object> execute(User loginUser, long projectCode, Integer processInstanceId, ExecuteType executeType);

    /**
     * check if sub processes are offline before starting process definition
     *
     * @param processDefinitionCode process definition code
     * @return check result code
     */
    Map<String, Object> startCheckByProcessDefinedCode(long processDefinitionCode);
}

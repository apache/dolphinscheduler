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
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.User;

import java.text.ParseException;
import java.util.Map;

/**
 * executor service
 */
public interface ExecutorService {

    /**
     * execute process instance
     *
     * @param loginUser             login user
     * @param projectName           project name
     * @param processDefinitionId   process Definition Id
     * @param cronTime              cron time
     * @param commandType           command type
     * @param failureStrategy       failuer strategy
     * @param startNodeList         start nodelist
     * @param taskDependType        node dependency type
     * @param warningType           warning type
     * @param warningGroupId         notify group id
     * @param receivers             receivers
     * @param receiversCc           receivers cc
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group name
     * @param runMode run mode
     * @param timeout               timeout
     * @return execute process instance code
     * @throws ParseException Parse Exception
     */
    Map<String, Object> execProcessInstance(User loginUser, String projectName,
                                            int processDefinitionId, String cronTime, CommandType commandType,
                                            FailureStrategy failureStrategy, String startNodeList,
                                            TaskDependType taskDependType, WarningType warningType, int warningGroupId,
                                            String receivers, String receiversCc, RunMode runMode,
                                            Priority processInstancePriority, String workerGroup, Integer timeout) throws ParseException;

    /**
     * check whether the process definition can be executed
     *
     * @param processDefinition process definition
     * @param processDefineId process definition id
     * @return check result code
     */
    Map<String, Object> checkProcessDefinitionValid(ProcessDefinition processDefinition, int processDefineId);


    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param executeType execute type
     * @return execute result code
     */
    Map<String, Object> execute(User loginUser, String projectName, Integer processInstanceId, ExecuteType executeType);

    /**
     * check if sub processes are offline before starting process definition
     * @param processDefineId process definition id
     * @return check result code
     */
    Map<String, Object> startCheckByProcessDefinedId(int processDefineId);

    /**
     * query recipients and copyers by process definition id or processInstanceId
     *
     * @param processDefineId process definition id
     * @param processInstanceId process instance id
     * @return receivers cc list
     */
    Map<String, Object> getReceiverCc(Integer processDefineId, Integer processInstanceId);

}

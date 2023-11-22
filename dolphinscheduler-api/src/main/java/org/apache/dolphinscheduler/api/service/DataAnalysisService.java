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

import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.dto.DefineUserDto;
import org.apache.dolphinscheduler.api.dto.TaskCountDto;
import org.apache.dolphinscheduler.api.dto.project.StatisticsStateRequest;
import org.apache.dolphinscheduler.api.vo.TaskInstanceCountVo;
import org.apache.dolphinscheduler.api.vo.WorkflowDefinitionCountVo;
import org.apache.dolphinscheduler.api.vo.WorkflowInstanceCountVo;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

/**
 * data analysis service
 */
public interface DataAnalysisService {

    TaskInstanceCountVo getTaskInstanceStateCountByProject(User loginUser,
                                                           Long projectCode,
                                                           String startDate,
                                                           String endDate);

    TaskInstanceCountVo getAllTaskInstanceStateCount(User loginUser,
                                                     String startDate,
                                                     String endDate);

    WorkflowInstanceCountVo getWorkflowInstanceStateCountByProject(User loginUser,
                                                                   Long projectCodes,
                                                                   String startDate,
                                                                   String endDate);

    WorkflowInstanceCountVo getAllWorkflowInstanceStateCount(User loginUser,
                                                             String startDate,
                                                             String endDate);

    WorkflowDefinitionCountVo getWorkflowDefinitionCountByProject(User loginUser, Long projectCode);

    WorkflowDefinitionCountVo getAllWorkflowDefinitionCount(User loginUser);

    /**
     * statistics the workflow quantities of certain user
     * <p>
     * We only need projects which users have permission to see to determine whether the definition belongs to the user or not.
     *
     * @param loginUser   login user
     * @param userId userId
     * @param releaseState releaseState
     * @return workflow count data
     */
    DefineUserDto countDefinitionByUserV2(User loginUser, Integer userId, Integer releaseState);

    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @return command state count data
     */
    List<CommandStateCount> countCommandState(User loginUser);

    /**
     * count queue state
     *
     * @param loginUser login user
     * @return queue state count data
     */
    Map<String, Integer> countQueueState(User loginUser);

    /**
     * query all workflow states count
     * @param loginUser login user
     * @param statisticsStateRequest statisticsStateRequest
     * @return workflow states count
     */
    TaskCountDto countWorkflowStates(User loginUser,
                                     StatisticsStateRequest statisticsStateRequest);

    /**
     * query one workflow states count
     * @param loginUser login user
     * @param workflowCode workflowCode
     * @return workflow states count
     */
    TaskCountDto countOneWorkflowStates(User loginUser, Long workflowCode);

    /**
     * query all task states count
     * @param loginUser login user
     * @param statisticsStateRequest statisticsStateRequest
     * @return tasks states count
     */
    TaskCountDto countTaskStates(User loginUser, StatisticsStateRequest statisticsStateRequest);

    /**
     * query one task states count
     * @param loginUser login user
     * @param taskCode taskCode
     * @return tasks states count
     */
    TaskCountDto countOneTaskStates(User loginUser, Long taskCode);

}

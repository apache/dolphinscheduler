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

import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * data analysis service
 */
public interface DataAnalysisService {

    /**
     * statistical task instance status data
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param startDate   start date
     * @param endDate     end date
     * @return task state count data
     */
    Map<String, Object> countTaskStateByProject(User loginUser, long projectCode, String startDate, String endDate);

    /**
     * statistical process instance status data
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param startDate   start date
     * @param endDate     end date
     * @return process instance state count data
     */
    Map<String, Object> countProcessInstanceStateByProject(User loginUser, long projectCode, String startDate, String endDate);

    /**
     * statistics the process definition quantities of a certain person
     * <p>
     * We only need projects which users have permission to see to determine whether the definition belongs to the user or not.
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return definition count data
     */
    Map<String, Object> countDefinitionByUser(User loginUser, long projectCode);

    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @return command state count data
     */
    Map<String, Object> countCommandState(User loginUser);

    /**
     * count queue state
     *
     * @param loginUser login user
     * @return queue state count data
     */
    Map<String, Object> countQueueState(User loginUser);

    /**
     * Statistics task instance group by given project codes list
     * <p>
     * We only need project codes to determine whether the task instance belongs to the user or not.
     *
     * @param startTime    Statistics start time
     * @param endTime      Statistics end time
     * @param projectCodes Project codes list to filter
     * @return List of ExecuteStatusCount
     */
    List<ExecuteStatusCount> countTaskInstanceAllStatesByProjectCodes(@Param("startTime") Date startTime,
                                                                      @Param("endTime") Date endTime,
                                                                      @Param("projectCodes") Long[] projectCodes);
}

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

import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * data analysis service
 */
public interface DataAnalysisService {

    /**
     * statistical task instance status data
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param startDate start date
     * @param endDate end date
     * @return task state count data
     */
    Map<String, Object> countTaskStateByProject(User loginUser, long projectCode, String startDate, String endDate);

    /**
     * statistical process instance status data
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param startDate start date
     * @param endDate end date
     * @return process instance state count data
     */
    Map<String, Object> countProcessInstanceStateByProject(User loginUser, long projectCode, String startDate, String endDate);

    /**
     * statistics the process definition quantities of certain person
     *
     * @param loginUser login user
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

}

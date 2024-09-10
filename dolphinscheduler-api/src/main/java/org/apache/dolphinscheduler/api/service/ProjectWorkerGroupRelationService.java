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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

/**
 * the service of project and worker group
 */
public interface ProjectWorkerGroupRelationService {

    /**
     * assign worker groups to a project
     *
     * @param loginUser the login user
     * @param projectCode the project code
     * @param workerGroups assigned worker group names
     */
    Result assignWorkerGroupsToProject(User loginUser, Long projectCode, List<String> workerGroups);

    /**
     * query worker groups that assigned to the project
     *
     * @param loginUser the login user
     * @param projectCode project code
     */
    Map<String, Object> queryWorkerGroupsByProject(User loginUser, Long projectCode);

}

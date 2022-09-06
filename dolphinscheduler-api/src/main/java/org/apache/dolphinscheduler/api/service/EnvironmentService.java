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

import java.util.Map;

/**
 * environment service
 */
public interface EnvironmentService {

    /**
     * create environment
     *
     * @param loginUser login user
     * @param name environment name
     * @param config environment config
     * @param desc environment desc
     * @param workerGroups worker groups
     */
    Map<String, Object> createEnvironment(User loginUser, String name, String config, String desc, String workerGroups);

    /**
     * query environment
     *
     * @param name environment name
     */
    Map<String, Object> queryEnvironmentByName(String name);

    /**
     * query environment
     *
     * @param code environment code
     */
    Map<String, Object> queryEnvironmentByCode(Long code);

    /**
     * delete environment
     *
     * @param loginUser login user
     * @param code environment code
     */
    Map<String, Object> deleteEnvironmentByCode(User loginUser, Long code);

    /**
     * update environment
     *
     * @param loginUser login user
     * @param code environment code
     * @param name environment name
     * @param config environment config
     * @param desc environment desc
     * @param workerGroups worker groups
     */
    Map<String, Object> updateEnvironmentByCode(User loginUser, Long code, String name, String config, String desc,
                                                String workerGroups);

    /**
     * query environment paging
     *
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return environment list page
     */
    Result queryEnvironmentListPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal);

    /**
     * query all environment
     *
     * @param loginUser
     * @return all environment list
     */
    Map<String, Object> queryAllEnvironmentList(User loginUser);

    /**
     * verify environment name
     *
     * @param environmentName environment name
     * @return true if the environment name not exists, otherwise return false
     */
    Map<String, Object> verifyEnvironment(String environmentName);

}

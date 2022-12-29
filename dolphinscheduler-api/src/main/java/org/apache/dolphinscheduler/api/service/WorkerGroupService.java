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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

/**
 * worker group service
 */
public interface WorkerGroupService {

    /**
     * Create or update a worker group
     *
     * @param loginUser login user
     * @param id worker group id
     * @param name worker group name
     * @param addrList addr list
     * @param description   description
     * @param otherParamsJson  otherParamsJson
     * @return create or update result code
     */
    Map<String, Object> saveWorkerGroup(User loginUser, int id, String name, String addrList, String description,
                                        String otherParamsJson);

    /**
     * Query worker group paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return worker group list page
     */
    Result queryAllGroupPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal);

    /**
     * Query all worker group
     *
     * @param loginUser login user
     * @return all worker group list
     */
    Map<String, Object> queryAllGroup(User loginUser);

    /**
     * Delete worker group by id
     * @param loginUser login user
     * @param id worker group id
     * @return delete result code
     */
    Map<String, Object> deleteWorkerGroupById(User loginUser, Integer id);

    /**
     * Query all worker address list
     *
     * @return all worker address list
     */
    Map<String, Object> getWorkerAddressList();

    /**
     * Get task instance's worker group
     * @param taskInstance task instance
     * @return worker group
     */
    String getTaskWorkerGroup(TaskInstance taskInstance);

    /**
     * Query worker group by process definition codes
     * @param processDefinitionCodeList processDefinitionCodeList
     * @return worker group map
     */
    Map<Long, String> queryWorkerGroupByProcessDefinitionCodes(List<Long> processDefinitionCodeList);

}

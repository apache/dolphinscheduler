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
 * alert group service
 */
public interface AlertGroupService {

    /**
     * query alert group list
     *
     * @param loginUser
     * @return alert group list
     */
    Map<String, Object> queryAlertgroup(User loginUser);

    /**
     * query alert group by id
     *
     * @param loginUser login user
     * @param id alert group id
     * @return one alert group
     */
    Map<String, Object> queryAlertGroupById(User loginUser, Integer id);

    /**
     * paging query alarm group list
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return alert group list page
     */
    Result listPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * create alert group
     *
     * @param loginUser login user
     * @param groupName group name
     * @param desc description
     * @param alertInstanceIds alertInstanceIds
     * @return create result code
     */
    Map<String, Object> createAlertgroup(User loginUser, String groupName, String desc, String alertInstanceIds);

    /**
     * updateProcessInstance alert group
     *
     * @param loginUser login user
     * @param id alert group id
     * @param groupName group name
     * @param desc description
     * @param alertInstanceIds alertInstanceIds
     * @return update result code
     */
    Map<String, Object> updateAlertgroup(User loginUser, int id, String groupName, String desc,
                                         String alertInstanceIds);

    /**
     * delete alert group by id
     *
     * @param loginUser login user
     * @param id alert group id
     * @return delete result code
     */
    Map<String, Object> delAlertgroupById(User loginUser, int id);

    /**
     * verify group name exists
     *
     * @param groupName group name
     * @return check result code
     */
    boolean existGroupName(String groupName);
}

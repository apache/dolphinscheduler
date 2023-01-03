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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * base service
 */
public interface BaseService {

    /**
     * check admin
     *
     * @param user input user
     * @return ture if administrator, otherwise return false
     */
    boolean isAdmin(User user);

    /**
     * isNotAdmin
     *
     * @param loginUser login user
     * @param result result code
     * @return true if not administrator, otherwise false
     */
    boolean isNotAdmin(User loginUser, Map<String, Object> result);

    /**
     * permissionPostHandle
     * @param authorizationType
     * @param userId
     * @param ids
     * @param logger
     */
    void permissionPostHandle(AuthorizationType authorizationType, Integer userId, List<Integer> ids, Logger logger);

    /**
     * put message to map
     *
     * @param result result code
     * @param status status
     * @param statusParams status message
     */
    void putMsg(Map<String, Object> result, Status status, Object... statusParams);

    /**
     * put message to result object
     *
     * @param result result code
     * @param status status
     * @param statusParams status message
     */
    void putMsg(Result<Object> result, Status status, Object... statusParams);

    /**
     * check
     *
     * @param result result
     * @param bool bool
     * @param userNoOperationPerm status
     * @return check result
     */
    boolean check(Map<String, Object> result, boolean bool, Status userNoOperationPerm);

    /**
     * Verify that the operator has permissions
     *
     * @param operateUser operate user
     * @param createUserId create user id
     * @return check result
     */
    boolean canOperator(User operateUser, int createUserId);

    /**
     * Verify that the operator has permissions
     * @param user operate user
     * @param ids Object[]
     * @Param type authorizationType
     * @Param perm String
     * @return check result
     */
    boolean canOperatorPermissions(User user, Object[] ids, AuthorizationType type, String perm);

    /**
     * check and parse date parameters
     */
    Date checkAndParseDateParameters(String startDateStr) throws ServiceException;

    /**
     * check checkDescriptionLength
     *
     * @param description input String
     * @return ture if Length acceptable, Length exceeds return false
     */
    boolean checkDescriptionLength(String description);

}

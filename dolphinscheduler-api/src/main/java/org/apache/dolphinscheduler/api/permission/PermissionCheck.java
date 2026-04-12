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

package org.apache.dolphinscheduler.api.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;

public class PermissionCheck<T> {

    private Logger logger;

    private AuthorizationType authorizationType;

    /**
     * Authorization Type
     */
    @Getter
    @Setter
    private ProcessService processService;

    /**
     * need check array
     */
    private T[] needChecks;

    @Getter
    @Setter
    private int userId;

    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService, T[] needChecks,
                           int userId, Logger logger) {
        this.authorizationType = authorizationType;
        this.processService = processService;
        this.needChecks = needChecks;
        this.userId = userId;
        this.logger = logger;
    }

    /**
     * check permission
     *
     * @throws ServiceException exception
     */
    public void checkPermission() throws ServiceException {
        if (this.needChecks.length > 0) {

            // get user type in order to judge whether the user is admin
            User user = processService.getUserById(userId);
            if (user == null) {
                logger.error("User does not exist, userId:{}.", userId);
                throw new ServiceException(String.format("user %s doesn't exist", userId));
            }
            if (user.getUserType() != UserType.ADMIN_USER) {
                List<T> unauthorizedList = processService.listUnauthorized(userId, needChecks, authorizationType);
                // if exist unauthorized resource
                if (CollectionUtils.isNotEmpty(unauthorizedList)) {
                    logger.error("User does not have {} permission for {}, userName:{}.",
                            authorizationType.getDescp(), unauthorizedList, user.getUserName());
                    throw new ServiceException(String.format("user %s doesn't have permission of %s %s",
                            user.getUserName(), authorizationType.getDescp(), unauthorizedList.get(0)));
                }
            }
        }
    }

}

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
package org.apache.dolphinscheduler.dao.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PermissionCheck<T> {
    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(PermissionCheck.class);
    /**
     * Authorization Type
     */
    private AuthorizationType authorizationType;

    /**
     * Authorization Type
     */
    private ProcessDao processDao;

    /**
     * need check array
     */
    private T[] needChecks;

    /**
     * user id
     */
    private int userId;

    /**
     * permission check
     * @param authorizationType authorization type
     * @param processDao        process dao
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessDao processDao) {
        this.authorizationType = authorizationType;
        this.processDao = processDao;
    }

    /**
     * permission check
     * @param authorizationType
     * @param processDao
     * @param needChecks
     * @param userId
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessDao processDao, T[] needChecks, int userId) {
        this.authorizationType = authorizationType;
        this.processDao = processDao;
        this.needChecks = needChecks;
        this.userId = userId;
    }


    /**
     * whether has permission
     * @return true if has permission
     */
    public Boolean hasPermission(){
        if(this.needChecks.length > 0){
            // get user type in order to judge whether the user is admin
            User user = processDao.getUserById(userId);
            if (user.getUserType() != UserType.ADMIN_USER){
                List<T> unauthorizedList = processDao.listUnauthorized(userId,needChecks,authorizationType);
                // if exist unauthorized resource
                if(CollectionUtils.isNotEmpty(unauthorizedList)){
                    logger.error("user {} didn't has permission of {}: {}", user.getUserName(), authorizationType.getDescp(),unauthorizedList.toString());
                    throw new RuntimeException(String.format("user %s didn't has permission of %s %s", authorizationType.getDescp(),user.getUserName(), unauthorizedList.get(0)));
                }
            }
        }
        return true;
    }

}

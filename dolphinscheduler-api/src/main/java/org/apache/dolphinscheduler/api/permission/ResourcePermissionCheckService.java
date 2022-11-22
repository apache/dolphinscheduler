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

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

public interface ResourcePermissionCheckService<T> {

    /**
     * resourcePermissionCheck
     * @param authorizationType
     * @param needChecks
     * @param userId
     * @param logger
     * @return
     */
    boolean resourcePermissionCheck(Object authorizationType, Object[] needChecks, Integer userId, Logger logger);

    /**
     * userOwnedResourceIdsAcquisition
     * @param authorizationType
     * @param userId
     * @param logger
     * @return
     */
    Set<T> userOwnedResourceIdsAcquisition(Object authorizationType, Integer userId, Logger logger);

    /**
     * operationpermissionCheck
     * @param authorizationType
     * @param userId
     * @param permissionKey
     * @param logger
     * @return
     */
    boolean operationPermissionCheck(Object authorizationType, Integer userId,
                                     String permissionKey, Logger logger);

    /**
     * functionDisabled
     * @return
     */
    boolean functionDisabled();

    /**
     * associated with the current user after the resource is created
     * @param authorizationType
     * @param ids
     * @param logger
     */
    void postHandle(Object authorizationType, Integer userId, List<Integer> ids, Logger logger);
}

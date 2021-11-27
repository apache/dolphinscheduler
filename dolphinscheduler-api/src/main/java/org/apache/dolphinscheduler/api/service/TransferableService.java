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
import org.apache.dolphinscheduler.api.enums.TransferDataType;
import org.apache.dolphinscheduler.common.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * transfer service
 */
public interface TransferableService {

    /**
     * query data owned by the user
     *
     * @param userId user id
     * @return owned data list
     */
    default Map<String, Object> queryOwnedData(int userId) {
        Map<String, Object> result = new HashMap<>();
        List<?> dataList = queryCreatedByUser(userId);
        result.put(Constants.STATUS, Status.SUCCESS);
        result.put(Constants.DATA_LIST, dataList);
        return result;
    }

    /**
     * query data list created by user
     *
     * @param userId user id
     * @return data list
     */
    List<?> queryCreatedByUser(int userId);

    /**
     * transfer data owned by the user
     *
     * @param transferredUserId transferred user id
     * @param receivedUserId received user id
     * @param transferredIds transferred ids
     * @return transfer result code
     */
    Map<String, Object> transferOwnedData(int transferredUserId, int receivedUserId, List<Integer> transferredIds);

    /**
     * get the transfer data type
     *
     * @return transfer data type
     */
    TransferDataType transferDataType();

}

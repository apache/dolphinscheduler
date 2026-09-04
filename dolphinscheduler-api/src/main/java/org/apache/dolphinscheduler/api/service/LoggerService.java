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
import org.apache.dolphinscheduler.dao.entity.ResponseTaskLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.io.OutputStream;

public interface LoggerService {

    /**
     * view log
     *
     * @param loginUser   login user
     * @param taskInstId task instance id
     * @param skipLineNum skip line number
     * @param limit limit
     * @return log string data
     */
    Result<ResponseTaskLog> queryLog(User loginUser, int taskInstId, int skipLineNum, int limit);

    /**
     * query log
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskInstId  task instance id
     * @param skipLineNum skip line number
     * @param limit       limit
     * @return log string data
     */
    String queryLog(User loginUser, long projectCode, int taskInstId, int skipLineNum, int limit);

    /**
     * get log bytes
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskInstId  task instance id
     * @return log byte array
     */
    byte[] getLogBytes(User loginUser, int taskInstId);

    /**
     * get log bytes
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param taskInstId  task instance id
     * @return log byte array
     */
    byte[] getLogBytes(User loginUser, long projectCode, int taskInstId);

    /**
     * Verify the user has permission to download the task log and return the task instance.
     * Must be called BEFORE the HTTP response is committed (i.e., before StreamingResponseBody).
     */
    TaskInstance checkDownloadLogAuth(User loginUser, int taskInstId);

    /**
     * Stream the task instance log to the given output stream in bounded chunks. The caller must
     * have already verified access via {@link #checkDownloadLogAuth}.
     */
    void streamLogBytes(TaskInstance taskInstance, OutputStream outputStream) throws IOException;
}

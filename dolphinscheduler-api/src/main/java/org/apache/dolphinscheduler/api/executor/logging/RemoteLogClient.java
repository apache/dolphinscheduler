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

package org.apache.dolphinscheduler.api.executor.logging;

import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import org.springframework.stereotype.Component;

@Component
public class RemoteLogClient {

    /**
     * Retrieves the entire log content for a given task instance.
     * This method is used when it is necessary to obtain all the log information for a task instance.
     * 
     * @param taskInstance The task instance object, containing information such as the task ID and log path.
     * @return Returns the log content in byte array format.
     */
    public byte[] getWholeLog(TaskInstance taskInstance) {
        return LogUtils.getFileContentBytesFromRemote(taskInstance.getLogPath());
    }

    /**
     * Retrieves part of the log content for a given task instance, based on the specified line number and the number of lines to read.
     * This method is used when it is necessary to browse a portion of the log content, allowing for skipping a certain number of lines and limiting the number of lines read.
     * 
     * @param taskInstance The task instance object, containing information such as the task ID and log path.
     * @param skipLineNum The number of lines to skip, starting from the beginning of the log.
     * @param limit The maximum number of lines to read.
     * @return Returns the specified part of the log content in string format.
     */
    public String getPartLog(TaskInstance taskInstance, int skipLineNum, int limit) {
        // todo We can optimize requests by the actual range, reducing disk usage and network traffic.
        return LogUtils.rollViewLogLines(
                LogUtils.readPartFileContentFromRemote(taskInstance.getLogPath(), skipLineNum, limit));
    }

}

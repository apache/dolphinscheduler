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

package org.apache.dolphinscheduler.common.log.remote;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RemoteLogService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteLogService.class);

    @Async("remoteLogHandleExecutor")
    public void asyncSendRemoteLog(String logPath) {
        if (RemoteLogUtils.isRemoteLoggingEnable()) {
            logger.info("Start to send log {} to remote target {}", logPath,
                    PropertyUtils.getString(Constants.REMOTE_LOGGING_TARGET));

            RemoteLogHandler remoteLogHandler = RemoteLogHandlerFactory.getRemoteLogHandler();
            if (remoteLogHandler != null) {
                remoteLogHandler.sendRemoteLog(logPath);
            } else {
                logger.error("remote log handler is null");
            }

            logger.info("Succeed to send log {} to remote target {}", logPath,
                    PropertyUtils.getString(Constants.REMOTE_LOGGING_TARGET));
        }
    }
}

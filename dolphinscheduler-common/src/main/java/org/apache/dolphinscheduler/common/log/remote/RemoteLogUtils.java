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

import static org.apache.dolphinscheduler.common.utils.LogUtils.getLocalLogBaseDir;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemoteLogUtils {

    private static RemoteLogService remoteLogService;

    @Autowired
    private RemoteLogService autowiredRemoteLogService;

    @PostConstruct
    private void init() {
        remoteLogService = autowiredRemoteLogService;
    }

    public static void sendRemoteLog(String logPath) {
        if (isRemoteLoggingEnable()) {
            // send task logs to remote storage asynchronously
            remoteLogService.asyncSendRemoteLog(logPath);
        }
    }

    public static void getRemoteLog(String logPath) {
        if (isRemoteLoggingEnable()) {
            log.info("Start to get log {} from remote target {}", logPath,
                    PropertyUtils.getString(Constants.REMOTE_LOGGING_TARGET));

            mkdirOfLog(logPath);
            RemoteLogHandler remoteLogHandler = RemoteLogHandlerFactory.getRemoteLogHandler();
            if (remoteLogHandler == null) {
                return;
            }
            remoteLogHandler.getRemoteLog(logPath);
            log.info("End get log {} from remote target {}", logPath,
                    PropertyUtils.getString(Constants.REMOTE_LOGGING_TARGET));
        }
    }

    private static void mkdirOfLog(String logPath) {
        Path directory = Paths.get(logPath).getParent();
        directory.toFile().mkdirs();
    }

    public static boolean isRemoteLoggingEnable() {
        return PropertyUtils.getBoolean(Constants.REMOTE_LOGGING_ENABLE, Boolean.FALSE);
    }

    public static String getObjectNameFromLogPath(String logPath) {
        Path localLogBaseDirPath = Paths.get(getLocalLogBaseDir()).toAbsolutePath();

        Path path = Paths.get(logPath);
        int nameCount = path.getNameCount();

        String remoteLogBaseDir = PropertyUtils.getString(Constants.REMOTE_LOGGING_BASE_DIR);
        return Paths.get(remoteLogBaseDir, path.subpath(localLogBaseDirPath.getNameCount(), nameCount).toString())
                .toString();
    }
}

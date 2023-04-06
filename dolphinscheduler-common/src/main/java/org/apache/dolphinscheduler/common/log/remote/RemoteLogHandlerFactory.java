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

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class RemoteLogHandlerFactory {

    public RemoteLogHandler getRemoteLogHandler() {
        if (!RemoteLogUtils.isRemoteLoggingEnable()) {
            return null;
        }

        String target = PropertyUtils.getUpperCaseString(Constants.REMOTE_LOGGING_TARGET);
        if ("OSS".equals(target)) {
            return OssRemoteLogHandler.getInstance();
        } else if ("S3".equals(target)) {
            return S3RemoteLogHandler.getInstance();
        } else if ("GCS".equals(target)) {
            return GcsRemoteLogHandler.getInstance();
        }

        log.error("No suitable remote logging target for {}", target);
        return null;
    }
}

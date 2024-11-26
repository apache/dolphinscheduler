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

package org.apache.dolphinscheduler.service.utils;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * mainly used to get the start command line of a process.
 */
@Slf4j
public class ProcessUtils {

    /**
     * find logs and kill yarn tasks.
     *
     * @param taskExecutionContext taskExecutionContext
     * @return yarn application ids
     */
    public static @Nullable List<String> killApplication(@NonNull List<String> appIds,
                                                         @NonNull TaskExecutionContext taskExecutionContext) {
        try {
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            if (CollectionUtils.isNotEmpty(appIds)) {
                taskExecutionContext.setAppIds(String.join(TaskConstants.COMMA, appIds));
                if (StringUtils.isEmpty(taskExecutionContext.getExecutePath())) {
                    taskExecutionContext
                            .setExecutePath(FileUtils
                                    .getTaskInstanceWorkingDirectory(taskExecutionContext.getTaskInstanceId()));
                }
                FileUtils.createDirectoryWith755(Paths.get(taskExecutionContext.getExecutePath()));
                org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils.cancelApplication(taskExecutionContext);
                return appIds;
            } else {
                log.info("The current appId is empty, don't need to kill the yarn job, taskInstanceId: {}",
                        taskExecutionContext.getTaskInstanceId());
            }
        } catch (Exception e) {
            log.error("Kill yarn job failure, taskInstanceId: {}", taskExecutionContext.getTaskInstanceId(), e);
        }
        return Collections.emptyList();
    }
}

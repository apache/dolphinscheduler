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

package org.apache.dolphinscheduler.server.worker.rpc;

import static org.apache.dolphinscheduler.common.constants.Constants.APPID_COLLECT;
import static org.apache.dolphinscheduler.common.constants.Constants.DEFAULT_COLLECT_WAY;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.extract.worker.IWorkerLogService;
import org.apache.dolphinscheduler.extract.worker.transportor.GetAppIdRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.GetAppIdResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceLogPageQueryResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;

import java.io.File;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkerLogServiceImpl implements IWorkerLogService {

    @Override
    public TaskInstanceLogFileDownloadResponse getTaskInstanceWholeLogFileBytes(TaskInstanceLogFileDownloadRequest taskInstanceLogFileDownloadRequest) {
        byte[] bytes = org.apache.dolphinscheduler.common.utils.LogUtils
                .getFileContentBytes(taskInstanceLogFileDownloadRequest.getTaskInstanceLogAbsolutePath());
        // todo: if file not exists, return error result
        return new TaskInstanceLogFileDownloadResponse(bytes);
    }

    @Override
    public TaskInstanceLogPageQueryResponse pageQueryTaskInstanceLog(TaskInstanceLogPageQueryRequest taskInstanceLogPageQueryRequest) {
        List<String> lines = org.apache.dolphinscheduler.common.utils.LogUtils.readPartFileContent(
                taskInstanceLogPageQueryRequest.getTaskInstanceLogAbsolutePath(),
                taskInstanceLogPageQueryRequest.getSkipLineNum(),
                taskInstanceLogPageQueryRequest.getLimit());

        String logContent = org.apache.dolphinscheduler.common.utils.LogUtils.rollViewLogLines(lines);
        return new TaskInstanceLogPageQueryResponse(logContent);
    }

    @Override
    public GetAppIdResponse getAppId(GetAppIdRequest getAppIdRequest) {
        TaskExecutionContext taskExecutionContext =
                TaskExecutionContextCacheManager.getByTaskInstanceId(getAppIdRequest.getTaskInstanceId());
        String appInfoPath = taskExecutionContext.getAppInfoPath();
        String logPath = getAppIdRequest.getLogPath();
        List<String> appIds = LogUtils.getAppIds(logPath, appInfoPath,
                PropertyUtils.getString(APPID_COLLECT, DEFAULT_COLLECT_WAY));
        return new GetAppIdResponse(appIds);
    }

    @Override
    public void removeTaskInstanceLog(String taskInstanceLogAbsolutePath) {
        File taskLogFile = new File(taskInstanceLogAbsolutePath);
        try {
            if (taskLogFile.exists()) {
                taskLogFile.delete();
            }
        } catch (Exception e) {
            log.error("Remove TaskInstanceLog error", e);
        }
    }
}

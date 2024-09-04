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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MasterLogServiceImpl implements ILogService {

    @Override
    public TaskInstanceLogFileDownloadResponse getTaskInstanceWholeLogFileBytes(TaskInstanceLogFileDownloadRequest logicTaskInstanceLogFileDownloadRequest) {
        byte[] bytes =
                LogUtils.getFileContentBytes(logicTaskInstanceLogFileDownloadRequest.getTaskInstanceLogAbsolutePath());
        // todo: if file not exists, return error result
        return new TaskInstanceLogFileDownloadResponse(bytes);
    }

    @Override
    public TaskInstanceLogPageQueryResponse pageQueryTaskInstanceLog(TaskInstanceLogPageQueryRequest taskInstanceLogPageQueryRequest) {

        List<String> lines = LogUtils.readPartFileContent(
                taskInstanceLogPageQueryRequest.getTaskInstanceLogAbsolutePath(),
                taskInstanceLogPageQueryRequest.getSkipLineNum(),
                taskInstanceLogPageQueryRequest.getLimit());

        String logContent = LogUtils.rollViewLogLines(lines);
        return new TaskInstanceLogPageQueryResponse(logContent);
    }

    @Override
    public void removeTaskInstanceLog(String taskInstanceLogAbsolutePath) {
        FileUtils.deleteFile(taskInstanceLogAbsolutePath);
    }
}

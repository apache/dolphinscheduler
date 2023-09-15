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

import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.extract.master.IMasterLogService;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskInstanceLogPageQueryResponse;

import java.io.File;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MasterLogServiceImpl implements IMasterLogService {

    @Override
    public LogicTaskInstanceLogFileDownloadResponse getLogicTaskInstanceWholeLogFileBytes(LogicTaskInstanceLogFileDownloadRequest logicTaskInstanceLogFileDownloadRequest) {
        byte[] bytes =
                LogUtils.getFileContentBytes(logicTaskInstanceLogFileDownloadRequest.getTaskInstanceLogAbsolutePath());
        // todo: if file not exists, return error result
        return new LogicTaskInstanceLogFileDownloadResponse(bytes);
    }

    @Override
    public LogicTaskInstanceLogPageQueryResponse pageQueryLogicTaskInstanceLog(LogicTaskInstanceLogPageQueryRequest taskInstanceLogPageQueryRequest) {

        List<String> lines = LogUtils.readPartFileContent(
                taskInstanceLogPageQueryRequest.getTaskInstanceLogAbsolutePath(),
                taskInstanceLogPageQueryRequest.getSkipLineNum(),
                taskInstanceLogPageQueryRequest.getLimit());

        String logContent = LogUtils.rollViewLogLines(lines);
        return new LogicTaskInstanceLogPageQueryResponse(logContent);
    }

    @Override
    public void removeLogicTaskInstanceLog(String taskInstanceLogAbsolutePath) {
        File taskLogFile = new File(taskInstanceLogAbsolutePath);
        try {
            if (taskLogFile.exists()) {
                taskLogFile.delete();
            }
        } catch (Exception e) {
            log.error("Remove LogicTaskInstanceLog error", e);
        }
    }
}

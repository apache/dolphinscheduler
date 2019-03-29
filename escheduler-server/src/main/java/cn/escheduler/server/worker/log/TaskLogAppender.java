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
package cn.escheduler.server.worker.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * task log appender
 */
public class TaskLogAppender extends FileAppender<ILoggingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(TaskLogAppender.class);

    private String currentlyActiveFile;

    @Override
    protected void append(ILoggingEvent event) {

        if (currentlyActiveFile == null){
            currentlyActiveFile = getFile();
        }
        String activeFile = currentlyActiveFile;
        // thread name： taskThreadName-processDefineId_processInstanceId_taskInstanceId
        String threadName = event.getThreadName();
        String[] threadNameArr = threadName.split("-");
        // logId = processDefineId_processInstanceId_taskInstanceId
        String logId = threadNameArr[1];
        // split logId
        threadNameArr = logId.split("_");
        String processDefineId = threadNameArr[0];
        String processInstanceId = threadNameArr[1];
        String taskInstanceId = threadNameArr[2];

        activeFile = activeFile.replace("{processDefinitionId}",processDefineId);
        activeFile = activeFile.replace("{processInstanceId}",processInstanceId);
        activeFile = activeFile.replace("{taskInstanceId}",taskInstanceId);

        setFile(activeFile);
        start();
        super.subAppend(event);
    }
}

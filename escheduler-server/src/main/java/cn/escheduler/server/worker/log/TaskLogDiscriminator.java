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
import ch.qos.logback.core.sift.AbstractDiscriminator;
import cn.escheduler.server.utils.LoggerUtils;

public class TaskLogDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    private String key;

    /**
     * logger name should be like:
     *     Task Logger name should be like: TaskLogInfo-{processDefinitionId}/{processInstanceId}/{taskInstanceId}
     */
    public String getDiscriminatingValue(ILoggingEvent event) {
        String loggerName = event.getLoggerName();
        String prefix = LoggerUtils.TASK_LOGGER_INFO_PREFIX + "-";
        if (loggerName.startsWith(prefix)) {
            return loggerName.substring(prefix.length());
        } else {
            return "unknown_task";
        }
    }

    @Override
    public void start() {
        started = true;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

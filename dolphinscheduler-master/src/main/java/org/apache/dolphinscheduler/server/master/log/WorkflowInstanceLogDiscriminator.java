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

package org.apache.dolphinscheduler.server.master.log;

import org.apache.dolphinscheduler.server.master.utils.WorkflowInstanceLogUtils;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;

@Slf4j
public class WorkflowInstanceLogDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    private String key;

    private String logBase;

    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {
        String taskInstanceLogPath = MDC.get(WorkflowInstanceLogUtils.WORKFLOW_INSTANCE_LOG_FULL_PATH_MDC_KEY);
        if (taskInstanceLogPath == null) {
            log.error("The workflow instance log path is null, please check the logback configuration, logEvent: {}",
                    event);
        }
        return taskInstanceLogPath;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLogBase() {
        return logBase;
    }

    public void setLogBase(String logBase) {
        this.logBase = logBase;
    }
}

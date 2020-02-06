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
package org.apache.dolphinscheduler.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;

/**
 *  task log filter
 */
public class TaskLogFilter extends Filter<ILoggingEvent> {

    /**
     * level
     */
    private Level level;

    public void setLevel(String level) {
        this.level = Level.toLevel(level);
    }

    /**
     * Accept or reject based on thread name
     * @param event event
     * @return FilterReply
     */
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getThreadName().startsWith(LoggerUtils.TASK_LOGGER_THREAD_NAME) || event.getLevel().isGreaterOrEqual(level)) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }
}
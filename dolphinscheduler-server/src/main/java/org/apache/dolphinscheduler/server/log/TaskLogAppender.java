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
package org.apache.dolphinscheduler.server.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Marker;

import static ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER;

/**
 * Task log appender
 */
public class TaskLogAppender extends FileAppender<ILoggingEvent>{
    @Override
    protected void append(ILoggingEvent event) {
        Marker marker = event.getMarker();
        if (marker !=null) {
            if (marker.equals(FINALIZE_SESSION_MARKER)) {
                stop();
            }
        }
        super.subAppend(event);
    }
}

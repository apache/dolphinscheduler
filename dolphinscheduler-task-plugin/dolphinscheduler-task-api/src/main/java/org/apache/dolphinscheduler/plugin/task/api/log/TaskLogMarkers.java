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

package org.apache.dolphinscheduler.plugin.task.api.log;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class TaskLogMarkers {

    private static final Marker TASK_LOGGER_EXCLUDE_MARKER = MarkerFactory.getMarker("TASK_LOGGER_EXCLUDE");

    private static final Marker TASK_LOGGER_INCLUDE_MARKER = MarkerFactory.getMarker("TASK_LOGGER_INCLUDE");

    /**
     * The marker used to exclude logs from the task instance log file.
     */
    public static Marker excludeInTaskLog() {
        return TASK_LOGGER_EXCLUDE_MARKER;
    }

    public static Marker includeInTaskLog() {
        return TASK_LOGGER_INCLUDE_MARKER;

    }
}

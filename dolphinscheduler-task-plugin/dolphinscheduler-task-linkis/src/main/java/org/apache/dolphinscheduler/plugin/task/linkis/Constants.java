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

package org.apache.dolphinscheduler.plugin.task.linkis;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SHELL_CLI_OPTIONS = "${LINKIS_HOME}/bin/linkis-cli";

    public static final String KILL_OPTIONS = "--kill";

    public static final String STATUS_OPTIONS = "--status";

    public static final String ASYNC_OPTIONS = "--async true";

    public static final String SPACE = " ";

    public static final String LINKIS_TASK_ID_REGEX = "\"taskID\": \"\\d+";

    public static final String LINKIS_STATUS_REGEX = "\"status\": \"\\w+";
}

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

package org.apache.dolphinscheduler.plugin.alert.wechatrobot;

import java.util.HashMap;
import java.util.Map;

public final class WeChatAlertConstants {

    static final String MARKDOWN_QUOTE = ">";

    static final String MARKDOWN_ENTER = "\n";

    static final String CHARSET = "UTF-8";
    private WeChatAlertConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static final String TASK_STATUS_KEY = "taskState";
    static final String ROBOT_SEND_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send";

    static final String PROCESS_STATUS_KEY = "processState";

    static final String ALTER_TYPE_TASK = "TASK";
    static final String ALTER_TYPE_PROCESS = "PROCESS";

    static final String TASK_STATUS_SUCCESS = "SUCCESS";
    static final String TASK_STATUS_FAILURE = "FAILURE";
    static final String PROCESS_STATUS_SUCCESS = "SUCCESS";
    static final String PROCESS_STATUS_FAILURE = "FAILURE";

    static final String ALTER_TASK_FAILURE_TITLE = "task任务失败:";
    static final String ALTER_TASK_UNKNOWN_TITLE = "task任务未知状态:";
    static final String ALTER_TASK_SUCCESS_TITLE = "task任务成功:";
    static final String ALTER_PROCESS_FAILURE_TITLE = "工作流执行失败:";
    static final String ALTER_PROCESS_SUCCESS_TITLE = "工作流执行成功:";
    static final String ALTER_PROCESS_UNKNOWN_TITLE = "工作流未知状态:";

    static final Map<String, Map<String, String>> STATUS_MAPPING = new HashMap<String, Map<String, String>>();

    static {
        Map<String, String> taskMap = new HashMap<String, String>();
        Map<String, String> processMap = new HashMap<String, String>();

        taskMap.put(TASK_STATUS_SUCCESS, ALTER_TASK_SUCCESS_TITLE);
        taskMap.put(TASK_STATUS_FAILURE, ALTER_TASK_FAILURE_TITLE);

        processMap.put(PROCESS_STATUS_SUCCESS, ALTER_PROCESS_SUCCESS_TITLE);
        processMap.put(PROCESS_STATUS_FAILURE, ALTER_PROCESS_FAILURE_TITLE);

        STATUS_MAPPING.put(TASK_STATUS_KEY, taskMap);
        STATUS_MAPPING.put(PROCESS_STATUS_KEY, processMap);
    }

}

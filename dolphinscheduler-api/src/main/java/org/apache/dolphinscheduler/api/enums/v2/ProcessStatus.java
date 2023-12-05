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

package org.apache.dolphinscheduler.api.enums.v2;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

public enum ProcessStatus implements Status {

    PROCESS_DEFINITION_NAME_EXIST(10168, "process definition name {0} already exists", "工作流定义名称[{0}]已存在"),
    START_NODE_NOT_EXIST_IN_LAST_PROCESS(10207, "this node {0} does not exist in the latest process definition",
            "该节点 {0} 不存在于最新的流程定义中"),
    PROCESS_INSTANCE_NOT_EXIST(50001, "process instance {0} does not exist", "工作流实例[{0}]不存在"),
    PROCESS_DEFINE_NOT_EXIST(50003, "process definition {0} does not exist", "工作流定义[{0}]不存在"),
    PROCESS_DEFINE_NOT_RELEASE(50004, "process definition {0} process version {1} not online",
            "工作流定义[{0}] 工作流版本[{1}]不是上线状态"),
    SUB_PROCESS_DEFINE_NOT_RELEASE(50004, "exist sub process definition not online", "存在子工作流定义不是上线状态"),
    PROCESS_INSTANCE_STATE_OPERATION_ERROR(50006,
            "the status of process instance {0} is {1},Cannot perform {2} operation",
            "工作流实例[{0}]的状态是[{1}]，无法执行[{2}]操作"),
    PROCESS_INSTANCE_EXECUTING_COMMAND(50009, "process instance {0} is executing the command, please wait ...",
            "工作流实例[{0}]正在执行命令，请稍等..."),
    START_PROCESS_INSTANCE_ERROR(50014, "start process instance error", "运行工作流实例错误"),
    EXECUTE_PROCESS_INSTANCE_ERROR(50015, "execute process instance error", "操作工作流实例错误"),
    BATCH_EXECUTE_PROCESS_INSTANCE_ERROR(50058, "change process instance status error: {0}", "修改工作实例状态错误: {0}"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ProcessStatus(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }
}

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

public enum ProjectStatus implements Status {

    PROJECT_NOT_FOUND(10018, "project {0} not found ", "项目[{0}]不存在"),
    PROJECT_NOT_EXIST(10190, "This project was not found. Please refresh page.", "该项目不存在,请刷新页面"),
    CREATE_PROJECT_PARAMETER_ERROR(10214, "create project parameter error", "创建项目参数错误"),
    UPDATE_PROJECT_PARAMETER_ERROR(10215, "update project parameter error", "更新项目参数错误"),
    DELETE_PROJECT_PARAMETER_ERROR(10216, "delete project parameter error {0}", "删除项目参数错误 {0}"),
    QUERY_PROJECT_PARAMETER_ERROR(10217, "query project parameter error", "查询项目参数错误"),
    PROJECT_PARAMETER_ALREADY_EXISTS(10218, "project parameter {0} already exists", "项目参数[{0}]已存在"),
    PROJECT_PARAMETER_NOT_EXISTS(10219, "project parameter {0} not exists", "项目参数[{0}]不存在"),
    PROJECT_PARAMETER_CODE_EMPTY(10220, "project parameter code empty", "项目参数code为空"),
    CREATE_PROJECT_PREFERENCE_ERROR(10300, "create project preference error", "创建项目偏好设置错误"),
    PROJECT_PROCESS_NOT_MATCH(50054, "the project and the process is not match", "项目和工作流不匹配"),
    ;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ProjectStatus(int code, String enMsg, String zhMsg) {
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

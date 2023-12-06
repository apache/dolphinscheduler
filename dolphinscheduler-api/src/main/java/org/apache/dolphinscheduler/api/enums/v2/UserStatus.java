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

public enum UserStatus implements Status {

    USER_NAME_EXIST(10003, "user name already exists", "用户名已存在"),
    USER_NAME_NULL(10004, "user name is null", "用户名不能为空"),
    USER_NOT_EXIST(10010, "user {0} not exists", "用户[{0}]不存在"),
    CREATE_USER_ERROR(10090, "create user error", "创建用户错误"),
    UPDATE_USER_ERROR(10092, "update user error", "更新用户错误"),
    DELETE_USER_BY_ID_ERROR(10093, "delete user by id error", "删除用户错误"),
    USER_NO_OPERATION_PERM(30001, "user has no operation privilege", "当前用户没有操作权限"),
    USER_NO_OPERATION_PROJECT_PERM(30002, "user {0} is not has project {1} permission", "当前用户[{0}]没有[{1}]项目的操作权限"),
    USER_NO_WRITE_PROJECT_PERM(30003, "user [{0}] does not have write permission for project [{1}]",
            "当前用户[{0}]没有[{1}]项目的写权限"),
    USER_PASSWORD_LENGTH_ERROR(1300017, "user's password length error", "用户密码长度错误"),
    NO_CURRENT_OPERATING_PERMISSION(1400001, "The current user does not have this permission.", "当前用户无此权限"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    UserStatus(int code, String enMsg, String zhMsg) {
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

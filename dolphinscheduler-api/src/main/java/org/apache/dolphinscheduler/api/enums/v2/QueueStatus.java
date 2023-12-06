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

public enum QueueStatus implements Status {

    CREATE_QUEUE_ERROR(10127, "create queue error", "创建队列错误"),
    QUEUE_NOT_EXIST(10128, "queue {0} not exists", "队列ID[{0}]不存在"),
    QUEUE_VALUE_EXIST(10129, "queue value {0} already exists", "队列值[{0}]已存在"),
    QUEUE_NAME_EXIST(10130, "queue name {0} already exists", "队列名称[{0}]已存在"),
    UPDATE_QUEUE_ERROR(10131, "update queue error", "更新队列信息错误"),
    NEED_NOT_UPDATE_QUEUE(10132, "no content changes, no updates are required", "数据未变更，不需要更新队列信息"),
    VERIFY_QUEUE_ERROR(10133, "verify queue error", "验证队列信息错误"),
    DELETE_QUEUE_BY_ID_ERROR(10307, "delete queue by id error", "删除队列错误"),
    DELETE_QUEUE_BY_ID_FAIL_USERS(10308, "delete queue by id fail, for there are {0} users using it",
            "删除队列失败，有[{0}]个用户正在使用"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    QueueStatus(int code, String enMsg, String zhMsg) {
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

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

public enum GroupStatus implements Status {

    ALERT_GROUP_NOT_EXIST(10011, "alarm group not found", "告警组不存在"),
    ALERT_GROUP_EXIST(10012, "alarm group already exists", "告警组名称已存在"),
    CREATE_ALERT_GROUP_ERROR(10027, "create alert group error", "创建告警组错误"),
    NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP(130030, "Not allow to delete the default alarm group ", "不能删除默认告警组"),
    NOT_ALLOW_TO_UPDATE_GLOBAL_ALARM_GROUP(130032, "Not allow to update the global alert group ", "不能更新全局告警组"),
    ;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    GroupStatus(int code, String enMsg, String zhMsg) {
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

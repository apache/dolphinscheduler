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

public enum ScheduleStatus implements Status {

    SCHEDULE_CRON_NOT_EXISTS(10022, "scheduler crontab {0} does not exist", "调度配置定时表达式[{0}]不存在"),
    SCHEDULE_CRON_ONLINE_FORBID_UPDATE(10023, "online status does not allow update operations", "调度配置上线状态不允许修改"),
    SCHEDULE_CRON_CHECK_FAILED(10024, "scheduler crontab expression validation failure: {0}", "调度配置定时表达式验证失败: {0}"),
    CREATE_SCHEDULE_ERROR(10076, "create schedule error", "创建调度配置错误"),
    UPDATE_SCHEDULE_ERROR(10077, "update schedule error", "更新调度配置错误"),
    PARSE_TO_CRON_EXPRESSION_ERROR(10140, "parse cron to cron expression error", "解析调度表达式错误"),
    SCHEDULE_START_TIME_END_TIME_SAME(10141, "The start time must not be the same as the end", "开始时间不能和结束时间一样"),
    PARSE_SCHEDULE_PARAM_ERROR(10202, "Parse schedule parameter error, {0}", "解析 schedule 参数错误, {0}"),
    SCHEDULE_NOT_EXISTS(10203, "schedule {0} does not exist", "调度 id {0} 不存在"),
    SCHEDULE_ALREADY_EXISTS(10204, "workflow {0} schedule {1} already exist, please update or delete it",
            "工作流 {0} 的定时 {1} 已经存在，请更新或删除"),
    SCHEDULE_STATE_ONLINE(50023, "the status of schedule {0} is already online", "调度配置[{0}]已上线"),
    DELETE_SCHEDULE_BY_ID_ERROR(50024, "delete schedule by id error", "删除调度配置错误"),
    SCHEDULE_TIME_NUMBER_EXCEED(1400003, "The number of complement dates exceed 100.", "补数日期个数超过100"),;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ScheduleStatus(int code, String enMsg, String zhMsg) {
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

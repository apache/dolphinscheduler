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

public enum PluginStatus implements Status {

    PLUGIN_INSTANCE_ALREADY_EXISTS(110010, "plugin instance already exists", "该告警插件实例已存在"),
    PLUGIN_NOT_A_UI_COMPONENT(110001, "query plugin error, this plugin has no UI component", "查询插件错误，此插件无UI组件"),
    QUERY_PLUGINS_RESULT_IS_NULL(110002,
            "query alarm plugins result is empty, please check the startup status of the alarm component and confirm that the relevant alarm plugin is successfully registered",
            "查询告警插件为空, 请检查告警组件启动状态并确认相关告警插件已注册成功"),
    QUERY_PLUGIN_DETAIL_RESULT_IS_NULL(110004, "query plugin detail result is null", "查询插件详情结果为空"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED(110012,
            "failed to delete the alert instance, there is an alarm group associated with this alert instance",
            "删除告警实例失败，存在与此告警实例关联的警报组"),
            ;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    PluginStatus(int code, String enMsg, String zhMsg) {
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

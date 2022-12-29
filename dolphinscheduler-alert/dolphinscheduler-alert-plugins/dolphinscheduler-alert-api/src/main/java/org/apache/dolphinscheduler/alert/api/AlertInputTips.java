/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.alert.api;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

public enum AlertInputTips {

    PASSWORD("if enable use authentication, you need input password", "如果开启鉴权校验，则需要输入密码"),
    USERNAME("if enable use authentication, you need input user", "如果开启鉴权校验，则需要输入账号"),
    RECEIVERS("please input receivers", "请输入收件人"),
    URL("input request URL", "请输入请求的URL"),
    HEADER("input request headers as JSON format", "请输入JSON格式的请求头"),
    JSON_BODY("input request body as JSON format", "请输入JSON格式的请求体"),
    FIELD_NAME("input alert msg field name", "请输入告警信息的内容字段名称"),
    HTTP_METHOD("input request type POST or GET", "请输入HTTP请求类型POST或GET"),
    CUSTOMIZED_PARAMS("the custom parameters passed when calling scripts", "请输入调用脚本时传入的自定义参数"),
    SCRIPT_PATH("the absolute script path under alert-server, and make sure access rights",
            "请输入alert-server机器的脚本的绝对路径，并确保文件有权接入"),
    WEBHOOK("input WebHook Url", "请输入webhook的url"),
    BOT_NAME("input the bot username", "请输入bot的名称"),
    BOT_TOKEN("input bot access token", "请输入bot的接入token"),
    CHANNEL_ID("input telegram channel chat id", "请输入telegram的频道chat id"),
    ROOM_ID("input the room ID the alert message send to", "请输入告警信息发送的room ID"),
    RECIPIENT_USER_ID("input the person ID of the alert message recipient", "请输入告警信息接收人的person ID"),
    RECIPIENT_EMAIL("input the email address of the alert message recipient", "请输入告警信息接收人的email地址"),
    WEBEX_MENTION_USERS(
            "use `,`(eng commas) to separate multiple emails, to specify the person you mention in the room",
            "使用 `, `来分割多个email，来指出在房间中要@的人"),
    CORP_ID("please input corp id", "请输入corp id"),
    SECRET("please input secret", "请输入secret"),
    WECHAT_MENTION_USERS("use `|` to separate userIds and `@all` to everyone", "使用`|`来分割userId或使用`@all`来提到所有人"),
    WECHAT_AGENT_ID("please input agent id or chat id", "请输入agent id或chat id"),
    ;

    private final String enMsg;
    private final String zhMsg;

    AlertInputTips(String enMsg, String zhMsg) {
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }
}

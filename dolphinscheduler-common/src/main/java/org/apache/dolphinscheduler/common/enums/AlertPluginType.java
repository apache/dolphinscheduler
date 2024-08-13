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

package org.apache.dolphinscheduler.common.enums;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.google.common.base.Functions;

public enum AlertPluginType {

    DINGTALK(0, "DingTalk", "ding talk"),
    EMAIL(1, "Email", "email"),
    FEISHU(2, "Feishu", "fei shu"),
    HTTP(3, "Http", "http"),
    PAGERDUTY(4, "PagerDuty", "pager duty"),
    PROMETHEUS(5, "Prometheus", "prometheus alert manager"),
    SCRIPT(6, "Script", "script"),
    SLACK(7, "Slack", "slack"),
    TELEGRAM(8, "Telegram", "telegram"),
    WEBEXTEAMS(9, "WebexTeams", "webex teams"),
    WECHAT(10, "WeChat", "we chat"),
    ALIYUNVOICE(11, "AliyunVoice", "aliyun voice");

    private static final Map<Integer, AlertPluginType> ALERT_TYPE_MAP =
            Arrays.stream(AlertPluginType.values()).collect(toMap(AlertPluginType::getCode, Functions.identity()));
    @EnumValue
    private final int code;
    private final String name;
    private final String descp;

    AlertPluginType(int code, String name, String descp) {
        this.code = code;
        this.name = name;
        this.descp = descp;
    }

    public static AlertPluginType of(int type) {
        if (ALERT_TYPE_MAP.containsKey(type)) {
            return ALERT_TYPE_MAP.get(type);
        }
        return null;
    }

    public static AlertPluginType ofName(String name) {
        return Arrays.stream(AlertPluginType.values()).filter(e -> e.name().equals(name)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("no such alert plugin type"));
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescp() {
        return descp;
    }

}

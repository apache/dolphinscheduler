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

public final class WeChatAlertParamsConstants {

    // https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxxxxxx
    // 机器人地址 https://qyapi.weixin.qq.com/cgi-bin/webhook/send
    static final String ENTERPRISE_WE_CHAT_ROBOT_URL = "robot.url";
    static final String NAME_ENTERPRISE_WE_CHAT_ROBOT_URL = "robotUrl";
    // 机器人的key key=6a6c203d-5374-48d3-ab77-fa4b81235aee
    static final String ENTERPRISE_WE_CHAT_ROBOT_KEY = "robot.key";
    static final String NAME_ENTERPRISE_WE_CHAT_ROBOT_KEY = "robotKey";

    private WeChatAlertParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

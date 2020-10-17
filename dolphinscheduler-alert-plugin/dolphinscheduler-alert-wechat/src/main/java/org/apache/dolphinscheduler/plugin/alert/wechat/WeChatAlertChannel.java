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
package org.apache.dolphinscheduler.plugin.alert.wechat;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WeChatAlertChannel
 */
public class WeChatAlertChannel implements AlertChannel {

    private static final Logger logger = LoggerFactory.getLogger(WeChatAlertChannel.class);
    @Override
    public AlertResult process(AlertInfo info) {
        AlertData alertData = info.getAlertData();
        String alertParams = info.getAlertParams();
        List<PluginParams> pluginParams = JSONUtils.toList(alertParams, PluginParams.class);
        Map<String, String> paramsMap = new HashMap<>();
        for (PluginParams param : pluginParams) {
            paramsMap.put(param.getName(), param.getValue().toString());
        }
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(Boolean.toString(Boolean.TRUE));
        WeChatSender weChatSender = new WeChatSender(paramsMap);
        try {
            weChatSender.sendEnterpriseWeChat(alertData.getTitle(), alertData.getContent());
        } catch (IOException e) {
            alertResult.setStatus(Boolean.toString(Boolean.FALSE));
            logger.error(e.getMessage(), e);
        }
        return alertResult;
    }
}

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
package org.apache.dolphinscheduler.alert.manager;

import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.DingTalkUtils;
import org.apache.dolphinscheduler.plugin.model.AlertInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Ding Talk Manager
 */
public class DingTalkManager {
    private static final Logger logger = LoggerFactory.getLogger(EnterpriseWeChatManager.class);

    public Map<String,Object> send(AlertInfo alert) {
        Map<String,Object> retMap = new HashMap<>();
        retMap.put(Constants.STATUS, false);
        logger.info("send message {}", alert.getAlertData().getTitle());
        try {
            String msg = buildMessage(alert);
            DingTalkUtils.sendDingTalkMsg(msg, Constants.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        retMap.put(Constants.STATUS, true);
        return retMap;
    }

    private String buildMessage(AlertInfo alert) {
        String msg = alert.getAlertData().getContent();
        return msg;
    }
}

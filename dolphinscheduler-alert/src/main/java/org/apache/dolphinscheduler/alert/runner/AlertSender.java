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
package org.apache.dolphinscheduler.alert.runner;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.dolphinscheduler.alert.plugin.AlertChannelManager;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * alert sender
 */
public class AlertSender {

    private static final Logger logger = LoggerFactory.getLogger(AlertSender.class);

    private List<Alert> alertList;
    private AlertDao alertDao;
    private AlertChannelManager alertChannelManager;

    public AlertSender(AlertChannelManager alertChannelManager) {
        this.alertChannelManager = alertChannelManager;
    }

    public AlertSender(List<Alert> alertList, AlertDao alertDao, AlertChannelManager alertChannelManager) {
        super();
        this.alertList = alertList;
        this.alertDao = alertDao;
        this.alertChannelManager = alertChannelManager;
    }

    public void run() {
        List<User> users;
        Map<String, Object> retMaps = null;
        for (Alert alert : alertList) {
//            users = alertDao.listUserByAlertgroupId(alert.getAlertGroupId());
//
//            // receiving group list
//            List<String> receviersList = new ArrayList<>();
//            for (User user : users) {
//                receviersList.add(user.getEmail());
//            }


            AlertData alertData = new AlertData();
            alertData.setId(alert.getId())
//                    .setAlertGroupId(alert.getAlertGroupId())
                    .setContent(alert.getContent())
                    .setLog(alert.getLog())
//                    .setReceivers(alert.getReceivers())
//                    .setReceiversCc(alert.getReceiversCc())
//                    .setShowType(alert.getShowType().getDescp())
                    .setTitle(alert.getTitle());

            AlertInfo alertInfo = new AlertInfo();
            alertInfo.setAlertData(alertData);

            //TODO: get the alert plugin params which is configured at the web ui. eg: email receivers , email receiverscc
            //TODO: get the alert plugin params which is configured in AlertGroup.
            //TODO: turn the alert params to json eg:
            //[
            // {"alertPluginId":"email_alert", "receivers":"xxx@qq.com,bbb@qq.com", "receiverscc":"aaaa@qq.com,ccc@qq.com"},
            // {"alertPluginId":"wechat_alert", "receivers":"nnnnn"}
            //]
            String alertParamsStr = "[{\"alertPluginId\":\"email_alert\",\"receivers\":\"xxx@qq.com,bbb@qq.com\",\"receiverscc\":\"aaaa@qq.com,ccc@qq.com\"},{\"alertPluginId\":\"wechat_alert\",\"receivers\":\"nnnnn\"}]";

            JsonNode alertParams = JacksonUtils.toJsonNode(alertParamsStr);
            alertInfo.addProp("alertParams", alertParamsStr);

            Iterator<JsonNode> iterator = alertParams.iterator();
            while (iterator.hasNext()) {
                JsonNode alertParam = iterator.next();
                String needAlert = alertParam.get("alertPluginId").asText();
                if(alertChannelManager == null || alertChannelManager.getConfiguredAlertChannelMap().size() == 0) {
                    logger.warn("No Alert Plugin configured. Can not send alert info. ");
                    return;
                }
                AlertChannel alertChannel = alertChannelManager.getConfiguredAlertChannelMap().get(needAlert);
                AlertResult alertResult =alertChannel.process(alertInfo);

                if (alertResult == null) {
                    alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, "alert send error", alert.getId());
                    logger.info("Alert Plugin {} send error : return value is null", needAlert);
                } else if (!Boolean.parseBoolean(String.valueOf(alertResult.getStatus()))) {
                    alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, String.valueOf(alertResult.getMessage()), alert.getId());
                    logger.info("Alert Plugin {} send error : {}", needAlert, alertResult.getMessage());
                } else {
                    alertDao.updateAlert(AlertStatus.EXECUTION_SUCCESS, alertResult.getMessage(), alert.getId());
                    logger.info("Alert Plugin {} send success", needAlert);
                }
            }
        }

    }

}

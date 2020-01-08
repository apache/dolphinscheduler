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
package org.apache.dolphinscheduler.alert.sender;

import org.apache.dolphinscheduler.alert.AlertException;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Sender manager
 */
public class SenderManager {
    private final static Logger logger = LoggerFactory.getLogger(SenderManager.class);

    private static Map<String, Sender> senderMap;
    private static AlertGroupMapper alertGroupMapper;

    static {
        Map<String, Sender> tmpSenderMap = new HashMap<>();
        ServiceLoader<Sender> senderServiceLoader = ServiceLoader.load(Sender.class);
        for (Sender sender : senderServiceLoader) {
            tmpSenderMap.put(sender.getName(), sender);
        }

        if (tmpSenderMap.size() <= 0) {
            logger.error("The number of sender is less than 1, please check!");
        }
        SenderManager.senderMap = tmpSenderMap;
        SenderManager.alertGroupMapper = ConnectionFactory.getMapper(AlertGroupMapper.class);
    }

    public static boolean send(Alert alert) throws AlertException {
        AlertType alertType = alert.getAlertType();
        if (alertType == null) {
            logger.info("Alert type does not exists. id={}", alert.getId());
            return false;
        }
        String senderName = alertType.toString();
        if (!senderMap.containsKey(senderName)) {
            throw new AlertException(String.format("%s sender does not exist.", senderName), alert);
        }
        Sender sender = senderMap.get(senderName);
        return sender.send(alert);
    }

    public static boolean sendGroup(Alert alert) throws AlertException {
        int groupId = alert.getAlertGroupId();
        AlertGroup alertGroup = alertGroupMapper.selectById(groupId);
        if (alertGroup == null) {
            logger.info("Alert group does not exists. groupId={}", groupId);
            return false;
        }
        AlertType alertType = alertGroup.getGroupType();
        if (alertType == null) {
            logger.info("Alert group type does not exists. groupId={}", groupId);
            return false;
        }
        String senderName = alertType.toString();
        if (!senderMap.containsKey(senderName)) {
            throw new AlertException(String.format("%s sender does not exist.", senderName), alert);
        }
        Sender sender = senderMap.get(senderName);
        return sender.sendGroup(alert);
    }
}

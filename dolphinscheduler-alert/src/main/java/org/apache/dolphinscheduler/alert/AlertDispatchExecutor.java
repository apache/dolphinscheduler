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
package org.apache.dolphinscheduler.alert;

import org.apache.dolphinscheduler.alert.manager.EnterpriseWeChatManager;
import org.apache.dolphinscheduler.alert.sender.SenderManager;
import org.apache.dolphinscheduler.alert.utils.EnterpriseWeChatUtils;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Alert dispatch executor
 */
public class AlertDispatchExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AlertDispatchExecutor.class);
    private static final EnterpriseWeChatManager weChatManager= new EnterpriseWeChatManager();
    private static final AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);

    /**
     * Send alert
     * @param alert alert info
     */
    public static void send(Alert alert) {
        try {
            boolean ret = SenderManager.send(alert);
            ret = ret | SenderManager.sendGroup(alert);

            if (!ret) {
                logger.warn("alert send error : At least one receiver address required.");
                alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, "execution failure, at least one receiver address required.", alert.getId());
                return;
            }

            alertDao.updateAlert(AlertStatus.EXECUTION_SUCCESS, "execution success", alert.getId());
            logger.info("alert send success");
            if (EnterpriseWeChatUtils.isEnable()) {
                logger.info("Enterprise WeChat is enable!");
                try {
                    String token = EnterpriseWeChatUtils.getToken();
                    weChatManager.send(alert, token);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (AlertException e) {
            alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, e.getMessage(), alert.getId());
            logger.error(e.toString());
        }
    }

    /**
     * Send alert list
     * @param alerts alerts info
     */
    public static void send(List<Alert> alerts) {
        for (Alert alert : alerts) {
            send(alert);
        }
    }
}

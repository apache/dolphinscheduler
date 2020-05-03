package org.apache.dolphinscheduler.alert.manager;

import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.DingTalkUtils;
import org.apache.dolphinscheduler.dao.entity.Alert;
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
        logger.info("send message {}",alert);
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

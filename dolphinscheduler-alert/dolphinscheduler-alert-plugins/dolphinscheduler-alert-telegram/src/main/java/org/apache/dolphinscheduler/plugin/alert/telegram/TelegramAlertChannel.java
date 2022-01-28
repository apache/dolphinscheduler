package org.apache.dolphinscheduler.plugin.alert.telegram;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.Map;

/**
 * Telegram Alert Channel
 *
 * @author pinkhello
 */
public final class TelegramAlertChannel implements AlertChannel {
    @Override
    public AlertResult process(AlertInfo info) {
        Map<String, String> alertParams = info.getAlertParams();
        if (alertParams == null || alertParams.isEmpty()) {
            return new AlertResult("false", "Telegram alert params is empty");
        }
        AlertData data = info.getAlertData();
        return new TelegramSender(alertParams).sendMessage(data);
    }
}

package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * email alert channel . use email to seed the alertInfo
 */
public class EmailAlertChannel implements AlertChannel {
    private static final Logger logger = LoggerFactory.getLogger(EmailAlertChannel.class);

    @Override
    public AlertResult process(AlertInfo info) {

        AlertData alert = info.getAlertData();
        String alertParams = info.getAlertParams();
        List<PluginParams> pluginParams = JSONUtils.toList(alertParams, PluginParams.class);
        Map<String, String> paramsMap = new HashMap<>();
        for(PluginParams param : pluginParams) {
            paramsMap.put(param.getName(), param.getValue().toString());
        }
        MailSender mailSender = new MailSender(paramsMap);

        AlertResult alertResult = mailSender.sendMails(alert.getTitle(), alert.getContent());

        //send flag
        boolean flag = false;

        if (alertResult == null) {
            alertResult = new AlertResult();
            alertResult.setStatus("false");
            alertResult.setMessage("alert send error.");
            logger.info("alert send error : {}", alertResult.getMessage());
            return alertResult;
        }

        flag = Boolean.parseBoolean(String.valueOf(alertResult.getStatus()));

        if (flag) {
            logger.info("alert send success");
            alertResult.setMessage("email send success.");
        } else {
            alertResult.setMessage("alert send error.");
            logger.info("alert send error : {}", alertResult.getMessage());
        }

        return alertResult;
    }
}

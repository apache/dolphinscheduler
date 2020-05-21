package org.apache.dolphinscheduler.alert.email;

import org.apache.dolphinscheduler.alert.email.template.ShowType;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

/**
 * email alert channel . use email to seed the alertInfo
 * @author gaojun
 */
public class EmailAlertChannel implements AlertChannel {
    private static final Logger logger = LoggerFactory.getLogger(EmailAlertChannel.class);

    private EmailManager emailManager;

    public EmailAlertChannel(Map<String, String> config){
        MailUtils mailUtils = new MailUtils(config);
        emailManager = new EmailManager(mailUtils);
    }

    @Override
    public AlertResult process(AlertInfo info) {
        AlertResult alertResult = new AlertResult();
        AlertData alert = info.getAlertData();
        List<String> receviersList = (List<String>) info.getProp(Constants.PLUGIN_DEFAULT_EMAIL_RECEIVERS);

        // receiving group list
        // custom receiver
//        String receivers = alert.getReceivers();
//        if (StringUtils.isNotEmpty(receivers)) {
//            String[] splits = receivers.split(",");
//            receviersList.addAll(Arrays.asList(splits));
//        }

        List<String> receviersCcList = (List<String>) info.getProp(Constants.PLUGIN_DEFAULT_EMAIL_RECEIVERCCS);

        if (CollectionUtils.isEmpty(receviersList) && CollectionUtils.isEmpty(receviersCcList)) {
            logger.warn("alert send error : At least one receiver address required");
            alertResult.setStatus("false");
            alertResult.setMessage("execution failure,At least one receiver address required.");
            return alertResult;
        }

        String showType = info.getProp(Constants.SHOW_TYPE) == null ? ShowType.TABLE.toString() : String.valueOf(info.getProp(Constants.SHOW_TYPE));
        alertResult = emailManager.send(receviersList, receviersCcList, alert.getTitle(), alert.getContent(), showType);

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

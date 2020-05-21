package org.apache.dolphinscheduler.alert.email;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;

import java.util.Map;

/**
 * email alert factory
 * @author gaojun
 */
public class EmailAlertChannelFactory implements AlertChannelFactory {
    @Override
    public String getNameEn() {
        return "email alert";
    }

    @Override
    public String getNameCh() {
        return "邮件";
    }

    @Override
    public String getId() {
        return "email_alert";
    }

    @Override
    public String getParams() {

    }

    @Override
    public AlertChannel create(Map<String, String> config) {
        return new EmailAlertChannel(config);
    }
}

package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.AbsPluginParams;
import org.apache.dolphinscheduler.spi.params.TextParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * email alert factory
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
    public List<AbsPluginParams> getParams() {

        List<AbsPluginParams> paramsList = new ArrayList<>();
        TextParam receivesParam = new TextParam(Constants.PLUGIN_DEFAULT_EMAIL_RECEIVERS,
                Constants.PLUGIN_DEFAULT_EMAIL_RECEIVERS,
                "接收人");

        TextParam receiveCcsParam = new TextParam(Constants.PLUGIN_DEFAULT_EMAIL_RECEIVERCCS,
                Constants.PLUGIN_DEFAULT_EMAIL_RECEIVERCCS,
                "抄送人");

        paramsList.add(receivesParam);
        paramsList.add(receiveCcsParam);

        return paramsList;
    }

    @Override
    public AlertChannel create(Map<String, String> config) {
        return new EmailAlertChannel(config);
    }
}

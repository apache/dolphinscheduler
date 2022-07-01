package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.ShowType;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MailSenderTest {

    @Test
    public void testEnableAuthConstructor() {
        Map<String, String> enableAuthConfig = new HashMap<>();
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_PROTOCOL, "smtp");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_HOST, "xxx.xxx.com");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_PORT, "25");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SENDER, "xxx1.xxx.com");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_USER, "xxx2.xxx.com");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_PASSWD, "111111");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_AUTH, "true");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE, "true");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE, "false");
        enableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST, "false");
        enableAuthConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS, "347801120@qq.com");
        enableAuthConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS, "347801120@qq.com");
        enableAuthConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TEXT.getDescp());
        new MailSender(enableAuthConfig);
    }

    @Test
    public void testDisableAuthConstructor() {
        Map<String, String> disableAuthConfig = new HashMap<>();
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_PROTOCOL, "smtp");
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_HOST, "xxx.xxx.com");
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_PORT, "25");
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_SENDER, "xxx1.xxx.com");
        disableAuthConfig.put(MailParamsConstants.NAME_MAIL_SMTP_AUTH, "false");
        disableAuthConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS, "347801120@qq.com");
        disableAuthConfig.put(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS, "347801120@qq.com");
        disableAuthConfig.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TEXT.getDescp());
        new MailSender(disableAuthConfig);
    }


}

package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.plugin.alert.email.template.ShowType;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.AbsPluginParams;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.RadioParam;
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
        TextParam receivesParam = new TextParam(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS,
                MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERS,
                null,
                MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERS,
                true,
                false);

        TextParam receiveCcsParam = new TextParam(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS,
                MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERCCS,
                null,
                MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERCCS,
                false,
                false);

        TextParam mailSmtpHost = new TextParam(MailParamsConstants.NAME_MAIL_SMTP_HOST,
                MailParamsConstants.MAIL_SMTP_HOST,
                null,
                "please input the mail smtp host",
                true,
                false);

        TextParam mailSmtpPort = new TextParam(MailParamsConstants.NAME_MAIL_SMTP_PORT,
                MailParamsConstants.MAIL_SMTP_PORT,
                Constants.DEFAULT_SMTP_PORT,
                "please input the mail smtp port",
                true,
                false);

        TextParam mailSender = new TextParam(MailParamsConstants.NAME_MAIL_SENDER,
                MailParamsConstants.MAIL_SENDER,
                null,
                "please input the mail sender",
                true,
                false);

        TextParam enableSmtpAuth = new TextParam(MailParamsConstants.NAME_MAIL_SMTP_AUTH,
                MailParamsConstants.MAIL_SMTP_AUTH,
                "true",
                "",
                true,
                true);

        TextParam mailUser = new TextParam(MailParamsConstants.NAME_MAIL_USER,
                MailParamsConstants.MAIL_USER,
                null,
                "if enable use authentication, you need input user",
                true,
                false);

        PasswordParam mailPassword = new PasswordParam(MailParamsConstants.NAME_MAIL_PASSWD,
                MailParamsConstants.MAIL_PASSWD,
                null,
                "if enable use authentication, you need input password",
                true,
                false);

        TextParam enableTls = new TextParam(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE,
                MailParamsConstants.MAIL_SMTP_STARTTLS_ENABLE,
                "false",
                "",
                false,
                false);

        TextParam enableSsl = new TextParam(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE,
                MailParamsConstants.MAIL_SMTP_SSL_ENABLE,
                "false",
                "",
                false,
                false);

        TextParam sslTrust = new TextParam(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST,
                MailParamsConstants.MAIL_SMTP_SSL_TRUST,
                "*",
                "",
                false,
                false);

        List<String> emailShowTypeList = new ArrayList<>();
        emailShowTypeList.add(ShowType.TABLE.getDescp());
        emailShowTypeList.add(ShowType.TEXT.getDescp());
        emailShowTypeList.add(ShowType.ATTACHMENT.getDescp());
        emailShowTypeList.add(ShowType.TABLEATTACHMENT.getDescp());

        RadioParam showType = new RadioParam(MailParamsConstants.SHOW_TYPE,
                MailParamsConstants.SHOW_TYPE,
                emailShowTypeList,
                ShowType.TABLE.getDescp(),
                true,
                false);

        paramsList.add(receivesParam);
        paramsList.add(receiveCcsParam);
        paramsList.add(mailSmtpHost);
        paramsList.add(mailSmtpPort);
        paramsList.add(mailSender);
        paramsList.add(enableSmtpAuth);
        paramsList.add(mailUser);
        paramsList.add(mailPassword);
        paramsList.add(enableTls);
        paramsList.add(enableSsl);
        paramsList.add(sslTrust);
        paramsList.add(showType);

        return paramsList;
    }

    @Override
    public AlertChannel create() {
        return new EmailAlertChannel();
    }
}

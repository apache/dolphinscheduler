package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.plugin.alert.email.template.ShowType;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.RadioParam;
import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.base.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * email alert factory
 */
public class EmailAlertChannelFactory implements AlertChannelFactory {
    @Override
    public String getName() {
        return "email alert";
    }

    @Override
    public List<PluginParams> getParams() {

        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam = new InputParam(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS,
                MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERS);
        receivesParam.addValidate(Validate.buildValidate().setRequired(true));

        InputParam receiveCcsParam = new InputParam(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS,
                MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERCCS);

        InputParam mailSmtpHost = new InputParam(MailParamsConstants.NAME_MAIL_SMTP_HOST,
                MailParamsConstants.MAIL_SMTP_HOST);
        mailSmtpHost.addValidate(Validate.buildValidate()
                .setRequired(true));

        InputParam mailSmtpPort = new InputParam(MailParamsConstants.NAME_MAIL_SMTP_PORT,
                MailParamsConstants.MAIL_SMTP_PORT);
        mailSmtpPort.addValidate(Validate.buildValidate()
                .setRequired(true)
                .setType(DataType.NUMBER.getDataType()));

        InputParam mailSender = new InputParam(MailParamsConstants.NAME_MAIL_SENDER,
                MailParamsConstants.MAIL_SENDER);
        mailSender.addValidate(Validate.buildValidate().setRequired(true));

        RadioParam enableSmtpAuth = new RadioParam(MailParamsConstants.NAME_MAIL_SMTP_AUTH,
                MailParamsConstants.MAIL_SMTP_AUTH);
        enableSmtpAuth.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(true)
                .addValidate(Validate.buildValidate().setRequired(true));


        InputParam mailUser = new InputParam(MailParamsConstants.NAME_MAIL_USER, MailParamsConstants.MAIL_USER);
        mailUser.setPlaceholder("if enable use authentication, you need input user");

        PasswordParam mailPassword = new PasswordParam(MailParamsConstants.NAME_MAIL_PASSWD, MailParamsConstants.MAIL_PASSWD);
        mailPassword.setPlaceholder("if enable use authentication, you need input password");

        RadioParam enableTls = new RadioParam(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE, MailParamsConstants.MAIL_SMTP_STARTTLS_ENABLE);
        enableTls.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(false)
                .addValidate(Validate.buildValidate().setRequired(true));

        RadioParam enableSsl = new RadioParam(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE, MailParamsConstants.MAIL_SMTP_SSL_ENABLE);
        enableSsl.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(false)
                .addValidate(Validate.buildValidate().setRequired(true));

        InputParam sslTrust = new InputParam(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST, MailParamsConstants.MAIL_SMTP_SSL_TRUST);
        sslTrust.setValue("*").addValidate(Validate.buildValidate().setRequired(true));

        List<ParamsOptions> emailShowTypeList = new ArrayList<>();
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.ATTACHMENT.getDescp(), ShowType.ATTACHMENT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLEATTACHMENT.getDescp(), ShowType.TABLEATTACHMENT.getDescp(), false));
        RadioParam showType = new RadioParam(MailParamsConstants.SHOW_TYPE,
                MailParamsConstants.SHOW_TYPE, emailShowTypeList);
        showType.setValue(ShowType.TABLE.getDescp())
                .addValidate(Validate.buildValidate().setRequired(true));

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

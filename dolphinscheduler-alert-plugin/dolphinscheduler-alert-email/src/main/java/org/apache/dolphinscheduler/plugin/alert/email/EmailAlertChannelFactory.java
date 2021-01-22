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

package org.apache.dolphinscheduler.plugin.alert.email;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.alert.AlertConstants;
import org.apache.dolphinscheduler.spi.alert.ShowType;
import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.RadioParam;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * email alert factory
 */
public class EmailAlertChannelFactory implements AlertChannelFactory {
    @Override
    public String getName() {
        return "Email";
    }

    @Override
    public List<PluginParams> getParams() {

        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam = InputParam.newBuilder(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS, MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERS)
                .setPlaceholder("please input receives")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam receiveCcsParam = InputParam.newBuilder(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS, MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERCCS)
                .build();

        InputParam mailSmtpHost = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_HOST, MailParamsConstants.MAIL_SMTP_HOST)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam mailSmtpPort = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_PORT, MailParamsConstants.MAIL_SMTP_PORT)
                .setValue(25)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .setType(DataType.NUMBER.getDataType())
                        .build())
                .build();

        InputParam mailSender = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SENDER, MailParamsConstants.MAIL_SENDER)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam enableSmtpAuth = RadioParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_AUTH, MailParamsConstants.MAIL_SMTP_AUTH)
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(true)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam mailUser = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_USER, MailParamsConstants.MAIL_USER)
                .setPlaceholder("if enable use authentication, you need input user")
                .build();

        PasswordParam mailPassword = PasswordParam.newBuilder(MailParamsConstants.NAME_MAIL_PASSWD, MailParamsConstants.MAIL_PASSWD)
                .setPlaceholder("if enable use authentication, you need input password")
                .build();

        RadioParam enableTls = RadioParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE, MailParamsConstants.MAIL_SMTP_STARTTLS_ENABLE)
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(false)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam enableSsl = RadioParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE, MailParamsConstants.MAIL_SMTP_SSL_ENABLE)
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(false)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam sslTrust = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST, MailParamsConstants.MAIL_SMTP_SSL_TRUST)
                .setValue("*")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam showType = RadioParam.newBuilder(AlertConstants.SHOW_TYPE, AlertConstants.SHOW_TYPE)
                .addParamsOptions(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.ATTACHMENT.getDescp(), ShowType.ATTACHMENT.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.TABLEATTACHMENT.getDescp(), ShowType.TABLEATTACHMENT.getDescp(), false))
                .setValue(ShowType.TABLE.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

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

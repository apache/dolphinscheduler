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

import static org.apache.dolphinscheduler.common.constants.Constants.STRING_FALSE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_NO;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_TRUE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_YES;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertInputTips;
import org.apache.dolphinscheduler.alert.api.ShowType;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.input.number.InputNumberParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.ArrayList;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public final class EmailAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "Email";
    }

    @Override
    public List<PluginParams> params() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam = InputParam
                .newBuilder(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS,
                        MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERS)
                .setPlaceholder(AlertInputTips.RECEIVERS.getMsg())
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam receiveCcsParam = InputParam
                .newBuilder(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERCCS,
                        MailParamsConstants.PLUGIN_DEFAULT_EMAIL_RECEIVERCCS)
                .build();

        InputParam mailSmtpHost =
                InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_HOST, MailParamsConstants.MAIL_SMTP_HOST)
                        .addValidate(Validate.newBuilder().setRequired(true).build())
                        .build();

        InputNumberParam mailSmtpPort =
                InputNumberParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_PORT, MailParamsConstants.MAIL_SMTP_PORT)
                        .setValue(25)
                        .addValidate(Validate.newBuilder()
                                .setRequired(true)
                                .setType(DataType.NUMBER.getDataType())
                                .build())
                        .build();

        InputParam mailSender =
                InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SENDER, MailParamsConstants.MAIL_SENDER)
                        .addValidate(Validate.newBuilder().setRequired(true).build())
                        .build();

        RadioParam enableSmtpAuth =
                RadioParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_AUTH, MailParamsConstants.MAIL_SMTP_AUTH)
                        .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                        .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                        .setValue(STRING_TRUE)
                        .addValidate(Validate.newBuilder().setRequired(true).build())
                        .build();

        InputParam mailUser = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_USER, MailParamsConstants.MAIL_USER)
                .setPlaceholder(AlertInputTips.USERNAME.getMsg())
                .build();

        InputParam mailPassword =
                InputParam.newBuilder(MailParamsConstants.NAME_MAIL_PASSWD, MailParamsConstants.MAIL_PASSWD)
                        .setPlaceholder(AlertInputTips.PASSWORD.getMsg())
                        .setType("password")
                        .build();

        RadioParam enableTls = RadioParam
                .newBuilder(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE,
                        MailParamsConstants.MAIL_SMTP_STARTTLS_ENABLE)
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam enableSsl = RadioParam
                .newBuilder(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE, MailParamsConstants.MAIL_SMTP_SSL_ENABLE)
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam sslTrust = InputParam
                .newBuilder(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST, MailParamsConstants.MAIL_SMTP_SSL_TRUST)
                .setValue("*")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam showType = RadioParam.newBuilder(AlertConstants.NAME_SHOW_TYPE, AlertConstants.SHOW_TYPE)
                .addParamsOptions(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false))
                .addParamsOptions(
                        new ParamsOptions(ShowType.ATTACHMENT.getDescp(), ShowType.ATTACHMENT.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.TABLE_ATTACHMENT.getDescp(),
                        ShowType.TABLE_ATTACHMENT.getDescp(), false))
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

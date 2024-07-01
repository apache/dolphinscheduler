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

import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.api.ShowType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmailAlertChannelTest {

    @Test
    public void testProcess() {
        EmailAlertChannel emailAlertChannel = new EmailAlertChannel();
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql service name", "mysql200");
        map1.put("mysql address", "192.168.xx.xx");
        map1.put("port", "3306");
        map1.put("no index of number", "80");
        map1.put("database client connections", "190");
        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0, map1);
        String mapjson = JSONUtils.toJsonString(maps);

        AlertData alertData = AlertData.builder()
                .id(10)
                .content(mapjson)
                .log("10")
                .title("test")
                .build();
        AlertInfo alertInfo = new AlertInfo();
        alertInfo.setAlertData(alertData);
        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(getEmailAlertParams());

        alertInfo.setAlertParams(paramsMap);
        AlertResult alertResult = emailAlertChannel.process(alertInfo);
        Assertions.assertNotNull(alertResult);
        Assertions.assertFalse(alertResult.isSuccess());
    }

    public String getEmailAlertParams() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam =
                InputParam.newBuilder(MailParamsConstants.NAME_PLUGIN_DEFAULT_EMAIL_RECEIVERS, "receivers")
                        .setValue("540957506@qq.com")
                        .addValidate(Validate.newBuilder().setRequired(true).build())
                        .build();

        InputParam mailSmtpHost = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_HOST, "smtp.host")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("smtp.126.com")
                .build();

        InputParam mailSmtpPort = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_PORT, "smtp.port")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .setValue("25")
                .build();

        InputParam mailSender = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SENDER, "sender")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("dolphinscheduler@126.com")
                .build();

        RadioParam enableSmtpAuth = RadioParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_AUTH, "smtp.auth")
                .addParamsOptions(new ParamsOptions("YES", "true", false))
                .addParamsOptions(new ParamsOptions("NO", "false", false))
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("false")
                .build();

        InputParam mailUser = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_USER, "user")
                .setPlaceholder("if enable use authentication, you need input user")
                .setValue("dolphinscheduler@126.com")
                .build();

        InputParam mailPassword = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_PASSWD, "passwd")
                .setPlaceholder("if enable use authentication, you need input password")
                .build();

        RadioParam enableTls =
                RadioParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_STARTTLS_ENABLE, "starttls.enable")
                        .addParamsOptions(new ParamsOptions("YES", "true", false))
                        .addParamsOptions(new ParamsOptions("NO", "false", false))
                        .addValidate(Validate.newBuilder().setRequired(true).build())
                        .setValue("true")
                        .build();

        RadioParam enableSsl = RadioParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_SSL_ENABLE, "smtp.ssl.enable")
                .addParamsOptions(new ParamsOptions("YES", "true", false))
                .addParamsOptions(new ParamsOptions("NO", "false", false))
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("true")
                .build();

        InputParam sslTrust = InputParam.newBuilder(MailParamsConstants.NAME_MAIL_SMTP_SSL_TRUST, "smtp.ssl.trust")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("smtp.126.com")
                .build();

        List<ParamsOptions> emailShowTypeList = new ArrayList<>();
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.ATTACHMENT.getDescp(), ShowType.ATTACHMENT.getDescp(), false));
        emailShowTypeList.add(
                new ParamsOptions(ShowType.TABLE_ATTACHMENT.getDescp(), ShowType.TABLE_ATTACHMENT.getDescp(), false));
        RadioParam showType = RadioParam.newBuilder(AlertConstants.NAME_SHOW_TYPE, "showType")
                .setOptions(emailShowTypeList)
                .setValue(ShowType.TABLE.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        paramsList.add(receivesParam);
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

        return JSONUtils.toJsonString(paramsList);
    }
}

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

import org.apache.dolphinscheduler.spi.alert.AlertConstants;
import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.alert.ShowType;
import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.RadioParam;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * EmailAlertChannel Tester.
 */
public class EmailAlertChannelTest {

    /**
     * Method: process(AlertInfo info)
     */
    @Test
    public void testProcess() {
        EmailAlertChannel emailAlertChannel = new EmailAlertChannel();
        AlertData alertData = new AlertData();
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql service name", "mysql200");
        map1.put("mysql address", "192.168.xx.xx");
        map1.put("port", "3306");
        map1.put("no index of number", "80");
        map1.put("database client connections", "190");
        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0, map1);
        String mapjson = JSONUtils.toJsonString(maps);

        alertData.setId(10)
                .setContent(mapjson)
                .setLog("10")
                .setTitle("test");
        AlertInfo alertInfo = new AlertInfo();
        alertInfo.setAlertData(alertData);
        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(getEmailAlertParams());

        alertInfo.setAlertParams(paramsMap);
        AlertResult alertResult = emailAlertChannel.process(alertInfo);
        Assert.assertNotNull(alertResult);
        Assert.assertEquals("false", alertResult.getStatus());
    }

    public String getEmailAlertParams() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam = InputParam.newBuilder("receivers", "receivers")
                .setValue("540957506@qq.com")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam mailSmtpHost = InputParam.newBuilder("serverHost", "smtp.host")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("smtp.126.com")
                .build();

        InputParam mailSmtpPort = InputParam.newBuilder("serverPort", "smtp.port")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .setType(DataType.NUMBER.getDataType())
                        .build())
                .setValue(25)
                .build();

        InputParam mailSender = InputParam.newBuilder("sender", "sender")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("dolphinscheduler@126.com")
                .build();

        RadioParam enableSmtpAuth = RadioParam.newBuilder("enableSmtpAuth", "smtp.auth")
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue(false)
                .build();

        InputParam mailUser = InputParam.newBuilder("user", "user")
                .setPlaceholder("if enable use authentication, you need input user")
                .setValue("dolphinscheduler@126.com")
                .build();

        PasswordParam mailPassword = PasswordParam.newBuilder("passwd", "passwd")
                .setPlaceholder("if enable use authentication, you need input password")
                .setValue("escheduler123")
                .build();

        RadioParam enableTls = RadioParam.newBuilder("starttlsEnable", "starttls.enable")
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue(true)
                .build();

        RadioParam enableSsl = RadioParam.newBuilder("sslEnable", "smtp.ssl.enable")
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue(true)
                .build();

        InputParam sslTrust = InputParam.newBuilder("smtpSslTrust", "smtp.ssl.trust")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .setValue("smtp.126.com")
                .build();

        List<ParamsOptions> emailShowTypeList = new ArrayList<>();
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.ATTACHMENT.getDescp(), ShowType.ATTACHMENT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLEATTACHMENT.getDescp(), ShowType.TABLEATTACHMENT.getDescp(), false));
        RadioParam showType = RadioParam.newBuilder(AlertConstants.SHOW_TYPE, "showType")
                .setParamsOptionsList(emailShowTypeList)
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

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

package org.apache.dolphinscheduler.alert.plugin;

import org.apache.dolphinscheduler.alert.AlertPluginManager;
import org.apache.dolphinscheduler.alert.AlertSender;
import org.apache.dolphinscheduler.alert.AlertServer;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.ShowType;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.ProfileType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@ActiveProfiles(ProfileType.H2)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AlertServer.class)
public class EmailAlertPluginTest {
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private PluginDao pluginDao;
    @Autowired
    private AlertPluginManager manager;
    @Autowired
    private AlertSender alertSender;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("spring.profiles.active", "h2");
    }

    @Test
    public void testRunSend() {
        //create alert group
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setDescription("test alert group 1");
        alertGroup.setGroupName("testalertg1");
        alertDao.getAlertGroupMapper().insert(alertGroup);

        //add alert
        Alert alert1 = new Alert();
        alert1.setTitle("test alert");
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql service name", "mysql200");
        map1.put("mysql address", "192.168.xx.xx");
        map1.put("port", "3306");
        map1.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TEXT.getDescp());
        map1.put("no index of number", "80");
        map1.put("database client connections", "190");

        LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
        map2.put("mysql service name", "mysql210");
        map2.put("mysql address", "192.168.xx.xx");
        map2.put("port", "3306");
        map2.put("no index of number", "10");
        map1.put(AlertConstants.NAME_SHOW_TYPE, ShowType.TABLE.getDescp());
        map2.put("database client connections", "90");

        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0, map1);
        maps.add(1, map2);
        String mapjson = JSONUtils.toJsonString(maps);
        alert1.setContent(mapjson);
        alert1.setLog("log log");
        alert1.setAlertGroupId(alertGroup.getId());
        alertDao.addAlert(alert1);

        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert1);

        //create email alert plugin instance
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setCreateTime(new Date());
        alertPluginInstance.setInstanceName("test email alert");

        PluginDefine pluginDefine = pluginDao.getPluginDefineMapper().queryByNameAndType("Email", "alert");
        if (pluginDefine == null) {
            throw new RuntimeException("no alert plugin be load");
        }
        alertPluginInstance.setPluginDefineId(pluginDefine.getId());
        alertPluginInstance.setPluginInstanceParams(getEmailAlertParams());
        alertDao.getAlertPluginInstanceMapper().insert(alertPluginInstance);

        alertSender.send(alertList);

        Alert alertResult = alertDao.getAlertMapper().selectById(alert1.getId());
        Assert.assertNotNull(alertResult);
        Assert.assertEquals(alertResult.getAlertStatus(), AlertStatus.EXECUTION_FAILURE);

        alertDao.getAlertGroupMapper().deleteById(alertGroup.getId());
        alertDao.getAlertPluginInstanceMapper().deleteById(alertPluginInstance.getId());
        alertDao.getAlertMapper().deleteById(alert1.getId());

    }

    public String getEmailAlertParams() {

        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam = InputParam.newBuilder("receivers", "receivers")
                                             .setValue("540957506@qq.com")
                                             .addValidate(Validate.newBuilder().setRequired(true).build())
                                             .build();

        InputParam mailSmtpHost = InputParam.newBuilder("mailServerHost", "mail.smtp.host")
                                            .addValidate(Validate.newBuilder().setRequired(true).build())
                                            .setValue("smtp.exmail.qq.com")
                                            .build();

        InputParam mailSmtpPort = InputParam.newBuilder("mailServerPort", "mail.smtp.port")
                                            .addValidate(Validate.newBuilder()
                                                                 .setRequired(true)
                                                                 .setType(DataType.NUMBER.getDataType())
                                                                 .build())
                                            .setValue(25)
                                            .build();

        InputParam mailSender = InputParam.newBuilder("mailSender", "mail.sender")
                                          .addValidate(Validate.newBuilder().setRequired(true).build())
                                          .setValue("easyscheduler@analysys.com.cn")
                                          .build();

        RadioParam enableSmtpAuth = RadioParam.newBuilder("enableSmtpAuth", "mail.smtp.auth")
                                              .addParamsOptions(new ParamsOptions("YES", true, false))
                                              .addParamsOptions(new ParamsOptions("NO", false, false))
                                              .addValidate(Validate.newBuilder().setRequired(true).build())
                                              .setValue(true)
                                              .build();

        InputParam mailUser = InputParam.newBuilder("mailUser", "mail.user")
                                        .setPlaceholder("if enable use authentication, you need input user")
                                        .setValue("easyscheduler@analysys.com.cn")
                                        .build();

        PasswordParam mailPassword = PasswordParam.newBuilder("mailPasswd", "mail.passwd")
                                                  .setPlaceholder("if enable use authentication, you need input password")
                                                  .setValue("xxxxxxx")
                                                  .build();

        RadioParam enableTls = RadioParam.newBuilder("starttlsEnable", "mail.smtp.starttls.enable")
                                         .addParamsOptions(new ParamsOptions("YES", true, false))
                                         .addParamsOptions(new ParamsOptions("NO", false, false))
                                         .addValidate(Validate.newBuilder().setRequired(true).build())
                                         .setValue(true)
                                         .build();

        RadioParam enableSsl = RadioParam.newBuilder("sslEnable", "mail.smtp.ssl.enable")
                                         .addParamsOptions(new ParamsOptions("YES", true, false))
                                         .addParamsOptions(new ParamsOptions("NO", false, false))
                                         .addValidate(Validate.newBuilder().setRequired(true).build())
                                         .setValue(false)
                                         .build();

        InputParam sslTrust = InputParam.newBuilder("mailSmtpSslTrust", "mail.smtp.ssl.trust")
                                        .addValidate(Validate.newBuilder().setRequired(true).build())
                                        .setValue("smtp.exmail.qq.com")
                                        .build();

        List<ParamsOptions> emailShowTypeList = new ArrayList<>();
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.ATTACHMENT.getDescp(), ShowType.ATTACHMENT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLEATTACHMENT.getDescp(), ShowType.TABLEATTACHMENT.getDescp(), false));
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

        return PluginParamsTransfer.transferParamsToJson(paramsList);
    }
}

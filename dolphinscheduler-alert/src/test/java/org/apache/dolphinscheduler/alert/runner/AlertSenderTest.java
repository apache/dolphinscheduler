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
package org.apache.dolphinscheduler.alert.runner;

import com.google.common.collect.ImmutableList;
import org.apache.dolphinscheduler.alert.AlertServer;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.alert.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.alert.plugin.PluginParamsTransfer;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.RadioParam;
import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * AlertSender Tester.
 */
public class AlertSenderTest {

    AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
    PluginDao pluginDao = DaoFactory.getDaoInstance(PluginDao.class);


    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: run()
     */
    @Test
    public void testRun() throws Exception {

        //create alert group
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setDescription("test alert group 1");
        alertGroup.setGroupName("testalertg1");
        //TODO AlertType is neet delete from AlertGroup
        alertGroup.setGroupType(AlertType.EMAIL);
        alertDao.getAlertGroupMapper().insert(alertGroup);

        //add alert
        Alert alert1 = new Alert();
        alert1.setTitle("test alert");
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("mysql service name","mysql200");
        map1.put("mysql address","192.168.xx.xx");
        map1.put("port","3306");
        map1.put("no index of number","80");
        map1.put("database client connections","190");

        LinkedHashMap<String, Object> map2 = new LinkedHashMap<>();
        map2.put("mysql service name","mysql210");
        map2.put("mysql address","192.168.xx.xx");
        map2.put("port","3306");
        map2.put("no index of number","10");
        map2.put("database client connections","90");

        List<LinkedHashMap<String, Object>> maps = new ArrayList<>();
        maps.add(0,map1);
        maps.add(1,map2);
        String mapjson = JSONUtils.toJsonString(maps);
        alert1.setContent(mapjson);
        alert1.setLog("log log");
        alert1.setAlertGroupId(alertGroup.getId());
        alertDao.addAlert(alert1);

        List<Alert> alertList = new ArrayList<>();
        alertList.add(alert1);

        //load email alert plugin
        AlertPluginManager alertPluginManager = new AlertPluginManager();
        DolphinPluginManagerConfig alertPluginManagerConfig = new DolphinPluginManagerConfig();
        alertPluginManagerConfig.setPlugins("../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml");
        if (StringUtils.isNotBlank(PropertyUtils.getString(AlertServer.ALERT_PLUGIN_DIR))) {
            alertPluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(AlertServer.ALERT_PLUGIN_DIR).trim());
        }

        if (StringUtils.isNotBlank(PropertyUtils.getString(AlertServer.MAVEN_LOCAL_REPOSITORY))) {
            alertPluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(AlertServer.MAVEN_LOCAL_REPOSITORY).trim());
        }

        DolphinPluginLoader alertPluginLoader = new DolphinPluginLoader(alertPluginManagerConfig, ImmutableList.of(alertPluginManager));
        try {
            alertPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("load Alert Plugin Failed !", e);
        }

        //create email alert plugin instance
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setAlertGroupId(alertGroup.getId());
        alertPluginInstance.setCreateTime(new Date());
        alertPluginInstance.setInstanceName("test email alert");

        List<PluginDefine> pluginDefineList = pluginDao.getPluginDefineMapper().queryByNameAndType("email alert", "alert");
        if(pluginDefineList == null || pluginDefineList.size() == 0) {
            throw new RuntimeException("no alert plugin be load");
        }
        PluginDefine pluginDefine = pluginDefineList.get(0);
        alertPluginInstance.setPluginDefineId(pluginDefine.getId());
        alertPluginInstance.setPluginInstanceParams(getEmailAlertParams());
        alertDao.getAlertPluginInstanceMapper().insert(alertPluginInstance);

        AlertSender alertSender = new AlertSender(alertList, alertDao, alertPluginManager, pluginDao);
        alertSender.run();

        Alert alertResult = alertDao.getAlertMapper().selectById(alert1.getId());
        Assert.assertNotNull(alertResult);
        Assert.assertEquals(alertResult.getAlertStatus(), AlertStatus.EXECUTION_SUCCESS);

        alertDao.getAlertGroupMapper().deleteById(alertGroup.getId());
        alertDao.getAlertPluginInstanceMapper().deleteById(alertPluginInstance.getId());
        alertDao.getAlertMapper().deleteById(alert1.getId());

    }

    public String getEmailAlertParams() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam = new InputParam("receivers","receivers");
        receivesParam.setValue("540957506@qq.com")
                .addValidate(Validate.buildValidate().setRequired(true));


        InputParam mailSmtpHost = new InputParam("mailServerHost", "mail.smtp.host");
        mailSmtpHost.addValidate(Validate.buildValidate()
                .setRequired(true))
                .setValue("smtp.exmail.qq.com");

        InputParam mailSmtpPort = new InputParam("mailServerPort", "mail.smtp.port");
        mailSmtpPort.addValidate(Validate.buildValidate()
                .setRequired(true)
                .setType(DataType.NUMBER.getDataType()))
                .setValue(25);

        InputParam mailSender = new InputParam("mailSender", "mail.sender");
        mailSender.addValidate(Validate.buildValidate().setRequired(true))
                .setValue("easyscheduler@analysys.com.cn");

        RadioParam enableSmtpAuth = new RadioParam("enableSmtpAuth",
                "mail.smtp.auth");
        enableSmtpAuth.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .addValidate(Validate.buildValidate().setRequired(true))
                .setValue(true);


        InputParam mailUser = new InputParam("mailUser", "mail.user");
        mailUser.setPlaceholder("if enable use authentication, you need input user");
        mailUser.setValue("easyscheduler@analysys.com.cn");

        PasswordParam mailPassword = new PasswordParam("mailPasswd", "mail.passwd");
        mailPassword.setPlaceholder("if enable use authentication, you need input password");
        mailPassword.setValue("BR9nq5e8");

        RadioParam enableTls = new RadioParam("starttlsEnable", "mail.smtp.starttls.enable");
        enableTls.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .addValidate(Validate.buildValidate().setRequired(true))
                .setValue(true);

        RadioParam enableSsl = new RadioParam("sslEnable", "mail.smtp.ssl.enable");
        enableSsl.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .addValidate(Validate.buildValidate().setRequired(true))
                .setValue(false);

        InputParam sslTrust = new InputParam("mailSmtpSslTrust", "mail.smtp.ssl.trust");
        sslTrust.addValidate(Validate.buildValidate().setRequired(true))
                .setValue("smtp.exmail.qq.com");

        List<ParamsOptions> emailShowTypeList = new ArrayList<>();
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLE.getDescp(), ShowType.TABLE.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.ATTACHMENT.getDescp(), ShowType.ATTACHMENT.getDescp(), false));
        emailShowTypeList.add(new ParamsOptions(ShowType.TABLEATTACHMENT.getDescp(), ShowType.TABLEATTACHMENT.getDescp(), false));
        RadioParam showType = new RadioParam("showType",
                "showType", emailShowTypeList);
        showType.setValue(ShowType.TABLE.getDescp())
                .addValidate(Validate.buildValidate().setRequired(true));

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

        return PluginParamsTransfer.getParamsJson(paramsList);
    }


} 

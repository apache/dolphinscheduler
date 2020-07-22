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

package org.apache.dolphinscheduler.spi.params;

import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * PluginParamsTransfer Tester.
 *
 */
public class PluginParamsTransferTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getAlpacajsJson(List<PluginParams> pluginParamsList)
     */
    @Test
    public void testGetParamsJson() throws Exception {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam = new InputParam("field1", "field1");
        receivesParam.addValidate(Validate.buildValidate().setRequired(true));

        InputParam receiveCcsParam = new InputParam("field2", "field2");

        InputParam mailSmtpHost = new InputParam("field3", "field3");
        mailSmtpHost.addValidate(Validate.buildValidate()
                .setRequired(true));

        InputParam mailSmtpPort = new InputParam("field4", "field4");
        mailSmtpPort.addValidate(Validate.buildValidate()
                .setRequired(true)
                .setType(DataType.NUMBER.getDataType()));

        InputParam mailSender = new InputParam("field5", "field5");
        mailSender.addValidate(Validate.buildValidate().setRequired(true));

        RadioParam enableSmtpAuth = new RadioParam("field6", "field6");
        enableSmtpAuth.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(true)
                .addValidate(Validate.buildValidate().setRequired(true));


        InputParam mailUser = new InputParam("field7", "field7");
        mailUser.setPlaceholder("if enable use authentication, you need input user");

        PasswordParam mailPassword = new PasswordParam("field8", "field8");
        mailPassword.setPlaceholder("if enable use authentication, you need input password");

        RadioParam enableTls = new RadioParam("field9", "field9");
        enableTls.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(false)
                .addValidate(Validate.buildValidate().setRequired(true));

        RadioParam enableSsl = new RadioParam("field10", "field10");
        enableSsl.addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(false)
                .addValidate(Validate.buildValidate().setRequired(true));

        InputParam sslTrust = new InputParam("field11", "field11");
        sslTrust.setValue("*").addValidate(Validate.buildValidate().setRequired(true));

        List<ParamsOptions> emailShowTypeList = new ArrayList<>();
        emailShowTypeList.add(new ParamsOptions("table", "table", false));
        emailShowTypeList.add(new ParamsOptions("text", "text", false));
        emailShowTypeList.add(new ParamsOptions("attachment", "attachment", false));
        emailShowTypeList.add(new ParamsOptions("tableattachment", "tableattachment", false));
        RadioParam showType = new RadioParam("showType", "showType", emailShowTypeList);
        showType.setValue("table")
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

        String paramsJson = PluginParamsTransfer.getParamsJson(paramsList);
        System.out.println(paramsJson);
        String paramsJsonAssert = "[{\"field\":\"field1\",\"props\":null,\"type\":\"input\",\"title\":\"field1\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"field2\",\"props\":null,\"type\":\"input\",\"title\":\"field2\",\"value\":null,\"validate\":null},{\"field\":\"field3\",\"props\":null,\"type\":\"input\",\"title\":\"field3\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"field4\",\"props\":null,\"type\":\"input\",\"title\":\"field4\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"number\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"field5\",\"props\":null,\"type\":\"input\",\"title\":\"field5\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"field6\",\"props\":null,\"type\":\"radio\",\"title\":\"field6\",\"value\":true,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"field7\",\"props\":{\"type\":null,\"placeholder\":\"if enable use authentication, you need input user\",\"rows\":0},\"type\":\"input\",\"title\":\"field7\",\"value\":null,\"validate\":null},{\"field\":\"field8\",\"props\":{\"type\":\"PASSWORD\",\"placeholder\":\"if enable use authentication, you need input password\",\"rows\":0},\"type\":\"input\",\"title\":\"field8\",\"value\":null,\"validate\":null},{\"field\":\"field9\",\"props\":null,\"type\":\"radio\",\"title\":\"field9\",\"value\":false,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"field10\",\"props\":null,\"type\":\"radio\",\"title\":\"field10\",\"value\":false,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"field11\",\"props\":null,\"type\":\"input\",\"title\":\"field11\",\"value\":\"*\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}]},{\"field\":\"showType\",\"props\":null,\"type\":\"radio\",\"title\":\"showType\",\"value\":\"table\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"options\":[{\"label\":\"table\",\"value\":\"table\",\"disabled\":false},{\"label\":\"text\",\"value\":\"text\",\"disabled\":false},{\"label\":\"attachment\",\"value\":\"attachment\",\"disabled\":false},{\"label\":\"tableattachment\",\"value\":\"tableattachment\",\"disabled\":false}]}]";
        Assert.assertEquals(paramsJsonAssert, paramsJson);
    }

    @Test
    public void testGetPluginParams() {
        String paramsJsonAssert = "[{\"field\":\"field1\",\"props\":null,\"type\":\"input\",\"title\":\"field1\",\"value\":\"v1\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},{\"field\":\"field2\",\"props\":null,\"type\":\"input\",\"title\":\"field2\",\"value\":\"v2\",\"validate\":null},{\"field\":\"field3\",\"props\":null,\"type\":\"input\",\"title\":\"field3\",\"value\":\"v3\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},{\"field\":\"field4\",\"props\":null,\"type\":\"input\",\"title\":\"field4\",\"value\":\"v4\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"number\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},{\"field\":\"field5\",\"props\":null,\"type\":\"input\",\"title\":\"field5\",\"value\":\"v5\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},{\"field\":\"field6\",\"props\":null,\"type\":\"radio\",\"title\":\"field6\",\"value\":true,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"field7\",\"props\":{\"type\":null,\"placeholder\":\"if enable use authentication, you need input user\",\"rows\":0},\"type\":\"input\",\"title\":\"field7\",\"value\":\"v6\",\"validate\":null},{\"field\":\"field8\",\"props\":{\"type\":\"PASSWORD\",\"placeholder\":\"if enable use authentication, you need input password\",\"rows\":0},\"type\":\"input\",\"title\":\"field8\",\"value\":\"v7\",\"validate\":null},{\"field\":\"field9\",\"props\":null,\"type\":\"radio\",\"title\":\"field9\",\"value\":false,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"field10\",\"props\":null,\"type\":\"radio\",\"title\":\"field10\",\"value\":false,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}],\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},{\"field\":\"field11\",\"props\":null,\"type\":\"input\",\"title\":\"field11\",\"value\":\"*\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},{\"field\":\"showType\",\"props\":null,\"type\":\"radio\",\"title\":\"showType\",\"value\":\"table\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}],\"options\":[{\"label\":\"table\",\"value\":\"table\",\"disabled\":false},{\"label\":\"text\",\"value\":\"text\",\"disabled\":false},{\"label\":\"attachment\",\"value\":\"attachment\",\"disabled\":false},{\"label\":\"tableattachment\",\"value\":\"tableattachment\",\"disabled\":false}]}]";
        List<PluginParams> pluginParams = PluginParamsTransfer.getPluginParams(paramsJsonAssert);
        String[] results = new String[]{"v1","v2","v3","v4","v5","true","v6","v7","false","false","*","table","v1"};
        for(int i = 0 ; i < pluginParams.size() ; i++) {
            PluginParams param = pluginParams.get(i);
            Assert.assertEquals(param.getValue().toString(), results[i]);
        }
    }
}

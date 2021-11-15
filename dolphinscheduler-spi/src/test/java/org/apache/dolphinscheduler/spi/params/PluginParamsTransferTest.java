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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.dolphinscheduler.spi.params.base.DataType;
import org.apache.dolphinscheduler.spi.params.base.FormType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.checkbox.CheckboxParam;
import org.apache.dolphinscheduler.spi.params.fswitch.SwitchParam;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.input.number.InputNumberParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;
import org.apache.dolphinscheduler.spi.params.select.SelectParam;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PluginParamsTransfer Tester.
 */
public class PluginParamsTransferTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testParamsOptionsEqual() {
        ParamsOptions pOptions1 = new ParamsOptions("table", "table", false);
        assertNotEquals(null, pOptions1);
        assertNotEquals("Not Equal", pOptions1);

        ParamsOptions pOptions2 = new ParamsOptions("table", "table", false);
        assertEquals(pOptions2, pOptions1);
        assertEquals(pOptions1.hashCode(), pOptions2.hashCode());

        ParamsOptions pOptions3 = new ParamsOptions(null, "table", false);
        ParamsOptions pOptions4 = new ParamsOptions("table", null, false);
        assertEquals(pOptions3, pOptions3);
        assertEquals(pOptions4, pOptions4);
        assertNotEquals(pOptions1, pOptions3);
        assertNotEquals(pOptions1, pOptions4);

        ParamsOptions pOptions5 = new ParamsOptions(null, "table", false);
        ParamsOptions pOptions6 = new ParamsOptions("table", null, false);
        assertEquals(pOptions3.hashCode(), pOptions5.hashCode());
        assertEquals(pOptions4.hashCode(), pOptions6.hashCode());
    }

    @Test
    public void testGetPluginParams() {
        /*
        *   Field8's type is "PASSWORD" while this type is not in FormType, nor the PasswordParam create Json with type "PASSWORD".
        */

        String paramsJsonAssert = "[{\"props\":null,\"field\":\"field1\",\"props\":null,\"type\":\"input\",\"title\":\"field1\",\"value\":\"v1\",\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},"
                + "{\"field\":\"field2\",\"props\":null,\"type\":\"input\",\"title\":\"field2\",\"value\":\"v2\",\"validate\":null},"
                + "{\"field\":\"field3\",\"props\":null,\"type\":\"input\",\"title\":\"field3\",\"value\":\"v3\",\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},"
                + "{\"field\":\"field4\",\"props\":null,\"type\":\"input\",\"title\":\"field4\",\"value\":\"v4\",\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"number\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},"
                + "{\"field\":\"field5\",\"props\":null,\"type\":\"input\",\"title\":\"field5\",\"value\":\"v5\",\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},"
                + "{\"field\":\"field6\",\"props\":null,\"type\":\"radio\",\"title\":\"field6\",\"value\":true,\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}],\"options\":["
                + "{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},"
                + "{\"field\":\"field7\",\"props\":{\"type\":null,\"placeholder\":\"if enable use authentication, you need input user\",\"rows\":0},"
                + "\"type\":\"input\",\"title\":\"field7\",\"value\":\"v6\",\"validate\":null},{\"field\":\"field8\",\"props\":{"
                + "\"type\":\"PASSWORD\",\"placeholder\":\"if enable use authentication, you need input password\",\"rows\":0},"
                + "\"type\":\"input\",\"title\":\"field8\",\"value\":\"v7\",\"validate\":null},{\"field\":\"field9\","
                + "\"props\":null,\"type\":\"radio\",\"title\":\"field9\",\"value\":false,\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}],\"options\":["
                + "{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},"
                + "{\"field\":\"field10\",\"props\":null,\"type\":\"radio\",\"title\":\"field10\",\"value\":false,\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}],"
                + "\"options\":[{\"label\":\"YES\",\"value\":true,\"disabled\":false},{\"label\":\"NO\",\"value\":false,\"disabled\":false}]},"
                + "{\"field\":\"field11\",\"props\":null,\"type\":\"input\",\"title\":\"field11\",\"value\":\"*\",\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}]},"
                + "{\"field\":\"showType\",\"props\":null,\"type\":\"radio\",\"title\":\"showType\",\"value\":\"table\",\"validate\":["
                + "{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":0.0,\"max\":0.0}],\"options\":["
                + "{\"label\":\"table\",\"value\":\"table\",\"disabled\":false},{\"label\":\"text\",\"value\":\"text\",\"disabled\":false},"
                + "{\"label\":\"attachment\",\"value\":\"attachment\",\"disabled\":false},{\"label\":\"tableattachment\",\"value\":\"tableattachment\",\"disabled\":false}]}]";
        List<PluginParams> pluginParams = PluginParamsTransfer.transferJsonToParamsList(paramsJsonAssert);
        String[] results = new String[]{"v1", "v2", "v3", "v4", "v5", "true", "v6", "v7", "false", "false", "*", "table", "v1"};
        Assert.assertEquals(12, pluginParams.size());
        for (int i = 0; i < pluginParams.size(); i++) {
            PluginParams param = pluginParams.get(i);
            Assert.assertEquals(param.getValue().toString(), results[i]);
        }
    }

    /**
    * Method: getAlpacajsJson(List<PluginParams> pluginParamsList)
    */
    @Test
    public void testGetParamsJson() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam receivesParam = InputParam.newBuilder("field1", "field1")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam receiveCcsParam = new InputParam.Builder("field2", "field2").build();

        InputParam mailSmtpHost = new InputParam.Builder("field3", "field3")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam mailSmtpPort = new InputParam.Builder("field4", "field4")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .setType(DataType.NUMBER.getDataType())
                        .build())
                .build();

        InputParam mailSender = new InputParam.Builder("field5", "field5")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam enableSmtpAuth = new RadioParam.Builder("field6", "field6")
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(true)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam mailUser = new InputParam.Builder("field7", "field7")
                .setPlaceholder("if enable use authentication, you need input user")
                .build();

        PasswordParam mailPassword = new PasswordParam.Builder("field8", "field8")
                .setPlaceholder("if enable use authentication, you need input password")
                .build();

        RadioParam enableTls = new RadioParam.Builder("field9", "field9")
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(false)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        RadioParam enableSsl = new RadioParam.Builder("field10", "field10")
                .addParamsOptions(new ParamsOptions("YES", true, false))
                .addParamsOptions(new ParamsOptions("NO", false, false))
                .setValue(false)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        InputParam sslTrust = new InputParam.Builder("field11", "field11")
                .setValue("*")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        List<ParamsOptions> emailShowTypeList = new ArrayList<>();
        emailShowTypeList.add(new ParamsOptions("table", "table", false));
        emailShowTypeList.add(new ParamsOptions("text", "text", false));
        emailShowTypeList.add(new ParamsOptions("attachment", "attachment", false));
        emailShowTypeList.add(new ParamsOptions("tableattachment", "tableattachment", false));
        RadioParam showType = new RadioParam.Builder("showType", "showType")
                .setOptions(emailShowTypeList)
                .setValue("table")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        CheckboxParam checkboxParam = new CheckboxParam.Builder("checkboxTest", "checkboxTest")
                .build();

        InputNumberParam inputNumberParam = new InputNumberParam.Builder("inputNumberTest", "inputNumberTest")
                .build();

        SelectParam selectParam = new SelectParam.Builder("selectTest", "selectTest")
                .build();
        
        SwitchParam switchParam = new SwitchParam.Builder("switchTest", "switchTest")
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

        paramsList.add(checkboxParam);
        paramsList.add(inputNumberParam);
        paramsList.add(selectParam);
        paramsList.add(switchParam);

        String paramsJson = PluginParamsTransfer.transferParamsToJson(paramsList);

        List<PluginParams> pluginParams = PluginParamsTransfer.transferJsonToParamsList(paramsJson);
        Assert.assertEquals(12, pluginParams.size());

        String input = FormType.INPUT.getFormType();
        String radio = FormType.RADIO.getFormType();
        String checkbox = FormType.CHECKBOX.getFormType();
        String inputNumber = FormType.INPUTNUMBER.getFormType();
        String select = FormType.SELECT.getFormType();
        String switchType = FormType.SWITCH.getFormType();

        String[] name = new String[]{"field1", "field2", "field3", "field4", "field5", "field6", "field7", "field8",
                "field9", "field10", "field11", "showType", "checkboxTest", "inputNumberTest", "selectTest", "switchTest"};
        String[] value = new String[]{null, null, null, null, null, "true", null, null,"false", "false", "*", "table", null, null, null, null};
        Boolean[] validateRequired = new Boolean[]{true, false, true, true, true, true, false, false, true, true, true, true, false, false, false, false};
        String[] type = new String[]{input, input, input, input, input, radio, input, input,
                radio, radio, input, radio, checkbox, inputNumber, select, switchType};

        List<ParamsOptions> radioOptions = new ArrayList<ParamsOptions>();
        radioOptions.add(new ParamsOptions("YES", true, false));
        radioOptions.add(new ParamsOptions("NO", false, false));

        for (int i = 0; i < pluginParams.size(); i++) {
            PluginParams param = pluginParams.get(i);
            Assert.assertEquals(name[i], param.getName());
            Assert.assertEquals(name[i], param.getTitle());
            if (value[i] != null) {
                Assert.assertEquals(value[i], param.getValue().toString());
            } else {
                Assert.assertEquals(null, param.getValue());
            }
            if (validateRequired[i]) {
                Assert.assertEquals(true, param.getValidateList().get(0).isRequired());
                Assert.assertEquals(i == 3 ? DataType.NUMBER.getDataType() : DataType.STRING.getDataType(), param.getValidateList().get(0).getType());
            } else {
                Assert.assertEquals(null, param.getValidateList());
            }
            
            Assert.assertEquals(type[i], param.getFormType());
        }

        // PasswordParams is created as InputParam, but in the previous test "PASSWORD" is an existed type.
        // If PasswordParam cannot be created on its own, the following assertion will result in a parsing error.
        // assertEquals("if enable use authentication, you need input password", pluginParams.get(7).getProps().getPlaceholder());

        assertEquals(radioOptions, ((RadioParam)pluginParams.get(5)).getOptions());
        assertEquals(radioOptions, ((RadioParam)pluginParams.get(8)).getOptions());
        assertEquals(radioOptions, ((RadioParam)pluginParams.get(9)).getOptions());
        assertEquals(emailShowTypeList, ((RadioParam)pluginParams.get(11)).getOptions());
    }
}

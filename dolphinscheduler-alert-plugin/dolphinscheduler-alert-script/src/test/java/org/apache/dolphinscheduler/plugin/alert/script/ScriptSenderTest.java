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

package org.apache.dolphinscheduler.plugin.alert.script;

import org.apache.dolphinscheduler.spi.alert.AlertResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ScriptSenderTest
 */
public class ScriptSenderTest {

    private static Map<String, String> scriptConfig = new HashMap<>();

    private static final String rootPath = System.getProperty("user.dir");

    private static final String shellFilPath = rootPath + "/src/test/script/shell/scriptTest.sh";

    @Before
    public void initScriptConfig() {

        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_TYPE, String.valueOf(ScriptType.SHELL.getCode()));
        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_USER_PARAMS, "userParams");
        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_PATH, shellFilPath);
    }

    @Test
    public void testScriptSenderTest() {
        ScriptSender scriptSender = new ScriptSender(scriptConfig);
        AlertResult alertResult;
        alertResult = scriptSender.sendScriptAlert("success");
        Assert.assertEquals("true", alertResult.getStatus());
        alertResult = scriptSender.sendScriptAlert("errorMsg");
        Assert.assertEquals("false", alertResult.getStatus());
    }

}

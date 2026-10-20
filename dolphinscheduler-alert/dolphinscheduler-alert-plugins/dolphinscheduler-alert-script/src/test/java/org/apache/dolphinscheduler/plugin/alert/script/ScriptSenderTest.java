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

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ScriptSenderTest {

    private static final String rootPath = System.getProperty("user.dir");
    private static final String shellFilPath = rootPath + "/src/test/script/shell/scriptExample.sh";
    private static Map<String, String> scriptConfig = new HashMap<>();

    @BeforeEach
    public void initScriptConfig() {

        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_TYPE, String.valueOf(ScriptType.SHELL.getDescp()));
        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_USER_PARAMS, "userParams");
        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_PATH, shellFilPath);
    }

    @Test
    public void testScriptSenderTest() {
        ScriptSender scriptSender = new ScriptSender(scriptConfig);
        AlertResult alertResult;
        alertResult = scriptSender.sendScriptAlert("test title Kris", "test content");
        Assertions.assertTrue(alertResult.isSuccess());
        alertResult = scriptSender.sendScriptAlert("error msg title", "test content");
        Assertions.assertFalse(alertResult.isSuccess());
    }

    @Test
    public void testScriptArgumentsArePassedLiterally(@TempDir Path tempDir) throws IOException {
        Path scriptPath = tempDir.resolve("capture-arguments.sh");
        Path argumentsPath = tempDir.resolve("capture-arguments.sh.args");
        Path unexpectedCommandMarker = tempDir.resolve("unexpected-command");
        Files.write(scriptPath, Arrays.asList("#!/bin/sh", "printf '%s\\n' \"$@\" > \"$0.args\""),
                StandardCharsets.UTF_8);
        Assertions.assertTrue(scriptPath.toFile().setExecutable(true));

        String title = "Bob's workflow failed";
        String content = "content $(touch " + unexpectedCommandMarker + ")";
        String userParams = "' ; touch " + unexpectedCommandMarker + " ; #";
        Map<String, String> config = new HashMap<>(scriptConfig);
        config.put(ScriptParamsConstants.NAME_SCRIPT_PATH, scriptPath.toString());
        config.put(ScriptParamsConstants.NAME_SCRIPT_USER_PARAMS, userParams);

        AlertResult alertResult = new ScriptSender(config).sendScriptAlert(title, content);

        Assertions.assertTrue(alertResult.isSuccess());
        Assertions.assertFalse(Files.exists(unexpectedCommandMarker));
        Assertions.assertEquals(Arrays.asList("-t", title, "-c", content, "-p", userParams),
                Files.readAllLines(argumentsPath, StandardCharsets.UTF_8));
    }

    @Test
    public void testScriptPathWithSpecialCharacters(@TempDir Path tempDir) throws IOException {
        Path scriptPath = tempDir.resolve("script path$(:).sh");
        Files.write(scriptPath, Arrays.asList("#!/bin/sh", "exit 0"), StandardCharsets.UTF_8);
        Assertions.assertTrue(scriptPath.toFile().setExecutable(true));

        Map<String, String> config = new HashMap<>(scriptConfig);
        config.put(ScriptParamsConstants.NAME_SCRIPT_PATH, scriptPath.toString());

        AlertResult alertResult = new ScriptSender(config).sendScriptAlert("test title", "test content");
        Assertions.assertTrue(alertResult.isSuccess());
    }

    @Test
    public void testUserParamsNPE() {
        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_USER_PARAMS, null);
        ScriptSender scriptSender = new ScriptSender(scriptConfig);
        AlertResult alertResult;
        alertResult = scriptSender.sendScriptAlert("test user params NPE", "test content");
        Assertions.assertTrue(alertResult.isSuccess());
    }

    @Test
    public void testPathNPE() {
        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_PATH, null);
        ScriptSender scriptSender = new ScriptSender(scriptConfig);
        AlertResult alertResult;
        alertResult = scriptSender.sendScriptAlert("test path NPE", "test content");
        Assertions.assertFalse(alertResult.isSuccess());
    }

    @Test
    public void testPathError() {
        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_PATH, "/usr/sbin/abc");
        ScriptSender scriptSender = new ScriptSender(scriptConfig);
        AlertResult alertResult;
        alertResult = scriptSender.sendScriptAlert("test path NPE", "test content");
        assertFalse(alertResult.isSuccess());
        Assertions.assertTrue(alertResult.getMessage().contains("shell script is invalid, only support .sh file"));
    }

    @Test
    public void testTypeIsError() {
        scriptConfig.put(ScriptParamsConstants.NAME_SCRIPT_TYPE, null);
        ScriptSender scriptSender = new ScriptSender(scriptConfig);
        AlertResult alertResult;
        alertResult = scriptSender.sendScriptAlert("test type is error", "test content");
        assertFalse(alertResult.isSuccess());
    }

}

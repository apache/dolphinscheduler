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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScriptSender
 */
public class ScriptSender {

    private static final Logger logger = LoggerFactory.getLogger(ScriptSender.class);

    private String scriptPath;

    private Integer scriptType;

    private String userParams;

    ScriptSender(Map<String, String> config) {
        scriptPath = config.get(ScriptParamsConstants.NAME_SCRIPT_PATH);
        scriptType = Integer.parseInt(config.get(ScriptParamsConstants.NAME_SCRIPT_TYPE));
        userParams = config.get(ScriptParamsConstants.NAME_SCRIPT_USER_PARAMS);
    }

    AlertResult sendScriptAlert(String msg) {
        AlertResult alertResult = new AlertResult();
        if (ScriptType.of(scriptType).equals(ScriptType.SHELL)) {
            return executeShellScript(msg);
        }
        return alertResult;
    }

    private AlertResult executeShellScript(String msg) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus("false");
        if (Boolean.TRUE.equals(OSUtils.isWindows())) {
            alertResult.setMessage("shell script not support windows os");
            return alertResult;
        }
        String[] cmd = {"/bin/sh", "-c", scriptPath + " " + msg + " " + userParams};
        int exitCode = ProcessUtils.executeScript(cmd);

        if (exitCode == 0) {
            alertResult.setStatus("true");
            alertResult.setMessage("send script alert msg success");
            return alertResult;
        }
        alertResult.setMessage("send script alert msg error,exitCode is " + exitCode);
        logger.info("send script alert msg error,exitCode is {}", exitCode);
        return alertResult;
    }

}

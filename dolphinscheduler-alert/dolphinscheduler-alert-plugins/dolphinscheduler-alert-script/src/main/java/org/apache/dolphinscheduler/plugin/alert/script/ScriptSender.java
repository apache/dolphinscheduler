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

import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.utils.OSUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ScriptSender {

    private static final String ALERT_TITLE_OPTION = " -t ";
    private static final String ALERT_CONTENT_OPTION = " -c ";
    private static final String ALERT_USER_PARAMS_OPTION = " -p ";
    private final String scriptPath;
    private final String scriptType;
    private final String userParams;
    private final String timeoutConfig;

    ScriptSender(Map<String, String> config) {
        scriptPath = StringUtils.isNotBlank(config.get(ScriptParamsConstants.NAME_SCRIPT_PATH))
                ? config.get(ScriptParamsConstants.NAME_SCRIPT_PATH)
                : "";
        scriptType = StringUtils.isNotBlank(config.get(ScriptParamsConstants.NAME_SCRIPT_TYPE))
                ? config.get(ScriptParamsConstants.NAME_SCRIPT_TYPE)
                : "";
        userParams = StringUtils.isNotBlank(config.get(ScriptParamsConstants.NAME_SCRIPT_USER_PARAMS))
                ? config.get(ScriptParamsConstants.NAME_SCRIPT_USER_PARAMS)
                : "";
        timeoutConfig = config.get(ScriptParamsConstants.NAME_SCRIPT_TIMEOUT);
    }

    AlertResult sendScriptAlert(String title, String content) {
        AlertResult alertResult = new AlertResult();
        if (ScriptType.SHELL.getDescp().equals(scriptType)) {
            return executeShellScript(title, content);
        }
        // If it is another type of alarm script can be added here, such as python

        alertResult.setSuccess(false);
        log.error("script type error: {}", scriptType);
        alertResult.setMessage("script type error : " + scriptType);
        return alertResult;
    }

    private AlertResult executeShellScript(String title, String content) {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);
        if (Boolean.TRUE.equals(OSUtils.isWindows())) {
            alertResult.setMessage("shell script not support windows os");
            return alertResult;
        }
        if (!scriptPath.endsWith(".sh")) {
            alertResult.setMessage("shell script is invalid, only support .sh file");
            return alertResult;
        }

        // validate script path in case of injections
        File shellScriptFile = new File(scriptPath);
        // validate existence
        if (!shellScriptFile.exists()) {
            log.error("shell script not exist : {}", scriptPath);
            alertResult.setMessage("shell script not exist : " + scriptPath);
            return alertResult;
        }
        // validate is file
        if (!shellScriptFile.isFile()) {
            log.error("shell script is not a file : {}", scriptPath);
            alertResult.setMessage("shell script is not a file : " + scriptPath);
            return alertResult;
        }

        // avoid command injection (RCE vulnerability)
        if (userParams.contains("'")) {
            log.error("shell script illegal user params : {}", userParams);
            alertResult.setMessage("shell script illegal user params : " + userParams);
            return alertResult;
        }
        if (title.contains("'")) {
            log.error("shell script illegal title : {}", title);
            alertResult.setMessage("shell script illegal title : " + title);
            return alertResult;
        }
        if (content.contains("'")) {
            log.error("shell script illegal content : {}", content);
            alertResult.setMessage("shell script illegal content : " + content);
            return alertResult;
        }

        Long timeout = parseTimeout(alertResult);
        if (timeout == null) {
            return alertResult;
        }

        String[] cmd = {"/bin/sh", "-c", scriptPath + ALERT_TITLE_OPTION + "'" + title + "'" + ALERT_CONTENT_OPTION
                + "'" + content + "'" + ALERT_USER_PARAMS_OPTION + "'" + userParams + "'"};
        ProcessUtils.ProcessExecutionResult executionResult = ProcessUtils.executeScript(timeout, cmd);

        if (executionResult.isTimedOut()) {
            alertResult.setMessage("send script alert msg error, script execution timed out after " + timeout
                    + " seconds");
            log.error("send script alert msg error, script execution timed out after {} seconds", timeout);
            return alertResult;
        }

        Integer exitCode = executionResult.getExitCode();
        if (exitCode != null && exitCode == 0) {
            alertResult.setSuccess(true);
            alertResult.setMessage("send script alert msg success");
            return alertResult;
        }
        if (exitCode == null) {
            alertResult.setMessage("send script alert msg error");
            log.info("send script alert msg error, execute alert script failed");
            return alertResult;
        }
        alertResult.setMessage("send script alert msg error,exitCode is " + exitCode);
        log.info("send script alert msg error,exitCode is {}", exitCode);
        return alertResult;
    }

    private Long parseTimeout(AlertResult alertResult) {
        if (StringUtils.isNotBlank(timeoutConfig)) {
            try {
                long parsedTimeout = Long.parseLong(timeoutConfig.trim());
                if (parsedTimeout <= 0) {
                    log.warn("Invalid script timeout config value: '{}'", timeoutConfig);
                    alertResult.setMessage("script timeout config must be greater than 0");
                    return null;
                }
                return parsedTimeout;
            } catch (NumberFormatException ex) {
                log.warn("Invalid script timeout config value: '{}'", timeoutConfig, ex);
                alertResult.setMessage("script timeout config is invalid, should be a number");
                return null;
            }
        }
        return (long) ScriptParamsConstants.DEFAULT_SCRIPT_TIMEOUT;
    }

}

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

package org.apache.dolphinscheduler.plugin.task.api.am;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResourceManagerType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@Slf4j
@AutoService(ApplicationManager.class)
public class YarnApplicationManager implements ApplicationManager {

    @Override
    public boolean killApplication(ApplicationManagerContext applicationManagerContext) throws TaskException {
        YarnApplicationManagerContext yarnApplicationManagerContext =
                (YarnApplicationManagerContext) applicationManagerContext;
        String executePath = yarnApplicationManagerContext.getExecutePath();
        String tenantCode = yarnApplicationManagerContext.getTenantCode();
        List<String> appIds = yarnApplicationManagerContext.getAppIds();

        try {
            String commandFile = String.format("%s/%s.kill", executePath, String.join(Constants.UNDERLINE, appIds));
            String cmd = getKerberosInitCommand() + "yarn application -kill " + String.join(Constants.SPACE, appIds);
            execYarnKillCommand(tenantCode, commandFile, cmd);
        } catch (Exception e) {
            log.error("Kill yarn application [{}] failed", appIds, e);
            throw new TaskException(e.getMessage());
        }

        return true;
    }

    @Override
    public ResourceManagerType getResourceManagerType() {
        return ResourceManagerType.YARN;
    }

    /**
     * build kill command for yarn application
     *
     * @param tenantCode tenant code
     * @param commandFile command file
     * @param cmd cmd
     */
    private void execYarnKillCommand(String tenantCode, String commandFile,
                                     String cmd) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/sh\n");
        sb.append("BASEDIR=$(cd `dirname $0`; pwd)\n");
        sb.append("cd $BASEDIR\n");

        sb.append("\n\n");
        sb.append(cmd);

        File f = new File(commandFile);

        if (!f.exists()) {
            org.apache.commons.io.FileUtils.writeStringToFile(new File(commandFile), sb.toString(),
                    StandardCharsets.UTF_8);
        }

        String runCmd = String.format("%s %s", Constants.SH, commandFile);
        runCmd = org.apache.dolphinscheduler.common.utils.OSUtils.getSudoCmd(tenantCode, runCmd);
        log.info("kill cmd:{}", runCmd);
        org.apache.dolphinscheduler.common.utils.OSUtils.exeCmd(runCmd);
    }

    /**
     * get kerberos init command
     */
    private String getKerberosInitCommand() {
        log.info("get kerberos init command");
        StringBuilder kerberosCommandBuilder = new StringBuilder();
        boolean hadoopKerberosState =
                PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        if (hadoopKerberosState) {
            kerberosCommandBuilder.append("export KRB5_CONFIG=")
                    .append(PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH))
                    .append("\n\n")
                    .append(String.format("kinit -k -t %s %s || true",
                            PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH),
                            PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME)))
                    .append("\n\n");
            log.info("kerberos init command: {}", kerberosCommandBuilder);
        }
        return kerberosCommandBuilder.toString();
    }
}

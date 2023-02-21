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
import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.KerberosHttpClient;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auto.service.AutoService;

@Slf4j
@AutoService(ApplicationManager.class)
public class YarnApplicationManager implements ApplicationManager {

    private static final String RM_HA_IDS = PropertyUtils.getString(Constants.YARN_RESOURCEMANAGER_HA_RM_IDS);
    private static final String APP_ADDRESS = PropertyUtils.getString(Constants.YARN_APPLICATION_STATUS_ADDRESS);
    private static final String JOB_HISTORY_ADDRESS =
            PropertyUtils.getString(Constants.YARN_JOB_HISTORY_STATUS_ADDRESS);
    private static final int HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_VALUE =
            PropertyUtils.getInt(Constants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT, 8088);

    @Override
    public boolean killApplication(ApplicationManagerContext applicationManagerContext) throws TaskException {
        YarnApplicationManagerContext yarnApplicationManagerContext =
                (YarnApplicationManagerContext) applicationManagerContext;
        String executePath = yarnApplicationManagerContext.getExecutePath();
        String tenantCode = yarnApplicationManagerContext.getTenantCode();
        List<String> appIds = yarnApplicationManagerContext.getAppIds();
        for (String appId : appIds) {
            try {
                TaskExecutionStatus applicationStatus = getApplicationStatus(appId);

                if (!applicationStatus.isFinished()) {
                    String commandFile = String.format("%s/%s.kill", executePath, appId);
                    String cmd = getKerberosInitCommand() + "yarn application -kill " + appId;
                    execYarnKillCommand(tenantCode, appId, commandFile, cmd);
                }
            } catch (Exception e) {
                log.error("Get yarn application app id [{}}] status failed", appId, e);
                throw new TaskException(e.getMessage());
            }
        }
        return true;
    }

    @Override
    public ResourceManagerType getResourceManagerType() {
        return ResourceManagerType.YARN;
    }

    /**
     * get the state of an application
     *
     * @param applicationId application id
     * @return the return may be null or there may be other parse exceptions
     */
    public TaskExecutionStatus getApplicationStatus(String applicationId) throws TaskException {
        if (StringUtils.isEmpty(applicationId)) {
            return null;
        }

        String result;
        String applicationUrl = getApplicationUrl(applicationId);
        log.debug("generate yarn application url, applicationUrl={}", applicationUrl);

        String responseContent = Boolean.TRUE
                .equals(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                        ? KerberosHttpClient.get(applicationUrl)
                        : HttpUtils.get(applicationUrl);
        if (responseContent != null) {
            ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
            if (!jsonObject.has("app")) {
                return TaskExecutionStatus.FAILURE;
            }
            result = jsonObject.path("app").path("finalStatus").asText();

        } else {
            // may be in job history
            String jobHistoryUrl = getJobHistoryUrl(applicationId);
            log.debug("generate yarn job history application url, jobHistoryUrl={}", jobHistoryUrl);
            responseContent = Boolean.TRUE
                    .equals(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                            ? KerberosHttpClient.get(jobHistoryUrl)
                            : HttpUtils.get(jobHistoryUrl);

            if (null != responseContent) {
                ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
                if (!jsonObject.has("job")) {
                    return TaskExecutionStatus.FAILURE;
                }
                result = jsonObject.path("job").path("state").asText();
            } else {
                return TaskExecutionStatus.FAILURE;
            }
        }

        return getExecutionStatus(result);
    }

    /**
     * get application url
     * if rmHaIds contains xx, it signs not use resourcemanager
     * otherwise:
     * if rmHaIds is empty, single resourcemanager enabled
     * if rmHaIds not empty: resourcemanager HA enabled
     *
     * @param applicationId application id
     * @return url of application
     */
    private String getApplicationUrl(String applicationId) throws TaskException {

        String appUrl = StringUtils.isEmpty(RM_HA_IDS) ? APP_ADDRESS : getAppAddress(APP_ADDRESS, RM_HA_IDS);
        if (StringUtils.isBlank(appUrl)) {
            throw new TaskException("yarn application url generation failed");
        }
        log.debug("yarn application url:{}, applicationId:{}", appUrl, applicationId);
        return String.format(appUrl, HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_VALUE, applicationId);
    }

    private String getJobHistoryUrl(String applicationId) {
        // eg:application_1587475402360_712719 -> job_1587475402360_712719
        String jobId = applicationId.replace("application", "job");
        return String.format(JOB_HISTORY_ADDRESS, jobId);
    }

    /**
     * build kill command for yarn application
     *
     * @param tenantCode tenant code
     * @param appId app id
     * @param commandFile command file
     * @param cmd cmd
     */
    private void execYarnKillCommand(String tenantCode, String appId, String commandFile,
                                     String cmd) {
        try {
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
        } catch (Exception e) {
            log.error(String.format("Kill yarn application app id [%s] failed: [%s]", appId, e.getMessage()));
        }
    }

    private TaskExecutionStatus getExecutionStatus(String result) {
        switch (result) {
            case Constants.ACCEPTED:
                return TaskExecutionStatus.SUBMITTED_SUCCESS;
            case Constants.SUCCEEDED:
            case Constants.ENDED:
                return TaskExecutionStatus.SUCCESS;
            case Constants.NEW:
            case Constants.NEW_SAVING:
            case Constants.SUBMITTED:
            case Constants.FAILED:
                return TaskExecutionStatus.FAILURE;
            case Constants.KILLED:
                return TaskExecutionStatus.KILL;
            case Constants.RUNNING:
            default:
                return TaskExecutionStatus.RUNNING_EXECUTION;
        }
    }

    /**
     * getAppAddress
     *
     * @param appAddress app address
     * @param rmHa       resource manager ha
     * @return app address
     */
    private String getAppAddress(String appAddress, String rmHa) {

        String[] split1 = appAddress.split(Constants.DOUBLE_SLASH);

        if (split1.length != 2) {
            return null;
        }

        String start = split1[0] + Constants.DOUBLE_SLASH;
        String[] split2 = split1[1].split(Constants.COLON);

        if (split2.length != 2) {
            return null;
        }

        String end = Constants.COLON + split2[1];

        // get active ResourceManager
        String activeRM = YarnHAAdminUtils.getActiveRMName(start, rmHa);

        if (StringUtils.isEmpty(activeRM)) {
            return null;
        }

        return start + activeRM + end;
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

    /**
     * yarn ha admin utils
     */
    private static final class YarnHAAdminUtils {

        /**
         * get active resourcemanager node
         *
         * @param protocol http protocol
         * @param rmIds    yarn ha ids
         * @return yarn active node
         */
        public static String getActiveRMName(String protocol, String rmIds) {

            String[] rmIdArr = rmIds.split(Constants.COMMA);

            String yarnUrl = protocol + "%s:" + HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_VALUE + "/ws/v1/cluster/info";

            try {

                /**
                 * send http get request to rm
                 */

                for (String rmId : rmIdArr) {
                    String state = getRMState(String.format(yarnUrl, rmId));
                    if (Constants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                        return rmId;
                    }
                }

            } catch (Exception e) {
                log.error("yarn ha application url generation failed, message:{}", e.getMessage());
            }
            return null;
        }

        /**
         * get ResourceManager state
         */
        public static String getRMState(String url) {

            String retStr = Boolean.TRUE
                    .equals(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                            ? KerberosHttpClient.get(url)
                            : HttpUtils.get(url);

            if (StringUtils.isEmpty(retStr)) {
                return null;
            }
            // to json
            ObjectNode jsonObject = JSONUtils.parseObject(retStr);

            // get ResourceManager state
            if (!jsonObject.has("clusterInfo")) {
                return null;
            }
            return jsonObject.get("clusterInfo").path("haState").asText();
        }
    }

}

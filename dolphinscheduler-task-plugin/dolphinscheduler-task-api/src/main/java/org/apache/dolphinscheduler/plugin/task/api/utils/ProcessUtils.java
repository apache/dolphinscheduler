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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.exception.BaseException;
import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.KerberosHttpClient;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

@Slf4j
public final class ProcessUtils {

    private ProcessUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Initialization regularization, solve the problem of pre-compilation performance,
     * avoid the thread safety problem of multi-thread operation
     */
    private static final Pattern MACPATTERN = Pattern.compile("-[+|-]-\\s(\\d+)");

    /**
     * Expression of PID recognition in Windows scene
     */
    private static final Pattern WINDOWSATTERN = Pattern.compile("(\\d+)");

    private static final String RM_HA_IDS = PropertyUtils.getString(Constants.YARN_RESOURCEMANAGER_HA_RM_IDS);
    private static final String APP_ADDRESS = PropertyUtils.getString(Constants.YARN_APPLICATION_STATUS_ADDRESS);
    private static final String JOB_HISTORY_ADDRESS =
            PropertyUtils.getString(Constants.YARN_JOB_HISTORY_STATUS_ADDRESS);
    private static final int HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_VALUE =
            PropertyUtils.getInt(Constants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT, 8088);

    /**
     * kill tasks according to different task types.
     */
    public static boolean kill(@NonNull TaskExecutionContext request) {
        try {
            log.info("Begin kill task instance, processId: {}", request.getProcessId());
            int processId = request.getProcessId();
            if (processId == 0) {
                log.error("Task instance kill failed, processId is not exist");
                return false;
            }

            String cmd = String.format("kill -9 %s", getPidsStr(processId));
            cmd = OSUtils.getSudoCmd(request.getTenantCode(), cmd);
            log.info("process id:{}, cmd:{}", processId, cmd);

            OSUtils.exeCmd(cmd);
            log.info("Success kill task instance, processId: {}", request.getProcessId());
            return true;
        } catch (Exception e) {
            log.error("Kill task instance error, processId: {}", request.getProcessId(), e);
            return false;
        }
    }

    /**
     * get pids str.
     *
     * @param processId process id
     * @return pids pid String
     * @throws Exception exception
     */
    public static String getPidsStr(int processId) throws Exception {
        StringBuilder sb = new StringBuilder();
        Matcher mat = null;
        // pstree pid get sub pids
        if (SystemUtils.IS_OS_MAC) {
            String pids = OSUtils.exeCmd(String.format("%s -sp %d", TaskConstants.PSTREE, processId));
            if (null != pids) {
                mat = MACPATTERN.matcher(pids);
            }
        } else {
            String pids = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
            mat = WINDOWSATTERN.matcher(pids);
        }

        if (null != mat) {
            while (mat.find()) {
                sb.append(mat.group(1)).append(" ");
            }
        }

        return sb.toString().trim();
    }

    /**
     * kill yarn application.
     *
     * @param appIds      app id list
     * @param logger      logger
     * @param tenantCode  tenant code
     * @param executePath execute path
     */
    public static void cancelApplication(List<String> appIds, Logger logger, String tenantCode, String executePath) {
        if (appIds == null || appIds.isEmpty()) {
            return;
        }

        for (String appId : appIds) {
            try {
                TaskExecutionStatus applicationStatus = getApplicationStatus(appId);

                if (!applicationStatus.isFinished()) {
                    String commandFile = String.format("%s/%s.kill", executePath, appId);
                    String cmd = getKerberosInitCommand() + "yarn application -kill " + appId;
                    execYarnKillCommand(logger, tenantCode, appId, commandFile, cmd);
                }
            } catch (Exception e) {
                log.error("Get yarn application app id [{}}] status failed", appId, e);
            }
        }
    }

    /**
     * get the state of an application
     *
     * @param applicationId application id
     * @return the return may be null or there may be other parse exceptions
     */
    public static TaskExecutionStatus getApplicationStatus(String applicationId) throws BaseException {
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
    private static String getApplicationUrl(String applicationId) throws BaseException {

        String appUrl = StringUtils.isEmpty(RM_HA_IDS) ? APP_ADDRESS : getAppAddress(APP_ADDRESS, RM_HA_IDS);
        if (StringUtils.isBlank(appUrl)) {
            throw new BaseException("yarn application url generation failed");
        }
        log.debug("yarn application url:{}, applicationId:{}", appUrl, applicationId);
        return String.format(appUrl, HADOOP_RESOURCE_MANAGER_HTTP_ADDRESS_PORT_VALUE, applicationId);
    }

    private static String getJobHistoryUrl(String applicationId) {
        // eg:application_1587475402360_712719 -> job_1587475402360_712719
        String jobId = applicationId.replace("application", "job");
        return String.format(JOB_HISTORY_ADDRESS, jobId);
    }

    /**
     * build kill command for yarn application
     *
     * @param logger logger
     * @param tenantCode tenant code
     * @param appId app id
     * @param commandFile command file
     * @param cmd cmd
     */
    private static void execYarnKillCommand(Logger logger, String tenantCode, String appId, String commandFile,
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

    private static TaskExecutionStatus getExecutionStatus(String result) {
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
    private static String getAppAddress(String appAddress, String rmHa) {

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
    private static String getKerberosInitCommand() {
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

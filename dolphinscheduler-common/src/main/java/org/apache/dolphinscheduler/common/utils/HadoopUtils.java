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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.exception.BaseException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.client.cli.RMAdminCLI;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * hadoop utils
 * single instance
 */
public class HadoopUtils {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUtils.class);

    public static final String rmHaIds = PropertyUtils.getString(Constants.YARN_RESOURCEMANAGER_HA_RM_IDS);
    public static final String appAddress = PropertyUtils.getString(Constants.YARN_APPLICATION_STATUS_ADDRESS);
    public static final String jobHistoryAddress = PropertyUtils.getString(Constants.YARN_JOB_HISTORY_STATUS_ADDRESS);

    private static final String HADOOP_UTILS_KEY = "HADOOP_UTILS_KEY";

    private static final LoadingCache<String, HadoopUtils> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(PropertyUtils.getInt(Constants.KERBEROS_EXPIRE_TIME, 2), TimeUnit.HOURS)
            .build(new CacheLoader<String, HadoopUtils>() {
                @Override
                public HadoopUtils load(String key) {
                    return new HadoopUtils();
                }
            });

    private static volatile boolean yarnEnabled = false;

    private Configuration configuration;

    public static HadoopUtils getInstance() {

        return cache.getUnchecked(HADOOP_UTILS_KEY);
    }

    /**
     * @return Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * get application url
     *
     * @param applicationId application id
     * @return url of application
     */
    public String getApplicationUrl(String applicationId) throws Exception {
        /**
         * if rmHaIds contains xx, it signs not use resourcemanager
         * otherwise:
         *  if rmHaIds is empty, single resourcemanager enabled
         *  if rmHaIds not empty: resourcemanager HA enabled
         */

        yarnEnabled = true;
        String appUrl = StringUtils.isEmpty(rmHaIds) ? appAddress : getAppAddress(appAddress, rmHaIds);
        if (StringUtils.isBlank(appUrl)) {
            throw new BaseException("yarn application url generation failed");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("yarn application url:{}, applicationId:{}", appUrl, applicationId);
        }
        String activeResourceManagerPort = String.valueOf(PropertyUtils.getInt(Constants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT, 8088));
        return String.format(appUrl, activeResourceManagerPort, applicationId);
    }

    public String getJobHistoryUrl(String applicationId) {
        //eg:application_1587475402360_712719 -> job_1587475402360_712719
        String jobId = applicationId.replace("application", "job");
        return String.format(jobHistoryAddress, jobId);
    }

    /**
     * hadoop resourcemanager enabled or not
     *
     * @return result
     */
    public boolean isYarnEnabled() {
        return yarnEnabled;
    }

    /**
     * get the state of an application
     *
     * @param applicationId application id
     * @return the return may be null or there may be other parse exceptions
     */
    public ExecutionStatus getApplicationStatus(String applicationId) throws Exception {
        if (StringUtils.isEmpty(applicationId)) {
            return null;
        }

        String result = Constants.FAILED;
        String applicationUrl = getApplicationUrl(applicationId);
        if (logger.isDebugEnabled()) {
            logger.debug("generate yarn application url, applicationUrl={}", applicationUrl);
        }

        String responseContent = PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false) ? KerberosHttpClient.get(applicationUrl) : HttpUtils.get(applicationUrl);
        if (responseContent != null) {
            ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
            if (!jsonObject.has("app")) {
                return ExecutionStatus.FAILURE;
            }
            result = jsonObject.path("app").path("finalStatus").asText();

        } else {
            //may be in job history
            String jobHistoryUrl = getJobHistoryUrl(applicationId);
            if (logger.isDebugEnabled()) {
                logger.debug("generate yarn job history application url, jobHistoryUrl={}", jobHistoryUrl);
            }
            responseContent = PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false) ? KerberosHttpClient.get(jobHistoryUrl) : HttpUtils.get(jobHistoryUrl);

            if (null != responseContent) {
                ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
                if (!jsonObject.has("job")) {
                    return ExecutionStatus.FAILURE;
                }
                result = jsonObject.path("job").path("state").asText();
            } else {
                return ExecutionStatus.FAILURE;
            }
        }

        switch (result) {
            case Constants.ACCEPTED:
                return ExecutionStatus.SUBMITTED_SUCCESS;
            case Constants.SUCCEEDED:
                return ExecutionStatus.SUCCESS;
            case Constants.NEW:
            case Constants.NEW_SAVING:
            case Constants.SUBMITTED:
            case Constants.FAILED:
                return ExecutionStatus.FAILURE;
            case Constants.KILLED:
                return ExecutionStatus.KILL;

            case Constants.RUNNING:
            default:
                return ExecutionStatus.RUNNING_EXECUTION;
        }
    }

    /**
     * getAppAddress
     *
     * @param appAddress app address
     * @param rmHa resource manager ha
     * @return app address
     */
    public static String getAppAddress(String appAddress, String rmHa) {

        //get active ResourceManager
        String activeRM = YarnHAAdminUtils.getAcitveRMName(rmHa);

        if (StringUtils.isEmpty(activeRM)) {
            return null;
        }

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

        return start + activeRM + end;
    }

    /**
     * yarn ha admin utils
     */
    private static final class YarnHAAdminUtils extends RMAdminCLI {

        /**
         * get active resourcemanager
         */
        public static String getAcitveRMName(String rmIds) {

            String[] rmIdArr = rmIds.split(Constants.COMMA);

            int activeResourceManagerPort = PropertyUtils.getInt(Constants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT, 8088);

            String yarnUrl = "http://%s:" + activeResourceManagerPort + "/ws/v1/cluster/info";

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
                logger.error("yarn ha application url generation failed, message:{}", e.getMessage());
            }
            return null;
        }

        /**
         * get ResourceManager state
         */
        public static String getRMState(String url) {

            String retStr = PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false) ? KerberosHttpClient.get(url) : HttpUtils.get(url);

            if (StringUtils.isEmpty(retStr)) {
                return null;
            }
            //to json
            ObjectNode jsonObject = JSONUtils.parseObject(retStr);

            //get ResourceManager state
            if (!jsonObject.has("clusterInfo")) {
                return null;
            }
            return jsonObject.get("clusterInfo").path("haState").asText();
        }

    }

}

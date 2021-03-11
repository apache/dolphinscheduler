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
 *//*


package org.apache.dolphinscheduler.plugin.task.api;


import org.apache.dolphinscheduler.spi.task.ExecutionStatus;
import org.apache.dolphinscheduler.spi.task.TaskConstants;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.client.cli.RMAdminCLI;

import java.io.Closeable;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


*/
/**
 * hadoop utils
 * single instance
 *//*

public class HadoopUtils implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUtils.class);

    private FileSystem fs;

    private Configuration configuration;

    private static final String HADOOP_UTILS_KEY = "HADOOP_UTILS_KEY";
    //todo default value


    private static String hdfsUser = TaskProperties.getString(TaskConstants.HDFS_ROOT_USER);
    private static final String resourceUploadPath = TaskProperties.getString(TaskConstants.RESOURCE_UPLOAD_PATH, "/dolphinscheduler");
    private static final String rmHaIds = TaskProperties.getString(TaskConstants.YARN_RESOURCEMANAGER_HA_RM_IDS);
    private static final String appAddress = TaskProperties.getString(TaskConstants.YARN_APPLICATION_STATUS_ADDRESS);
    public static final String jobHistoryAddress = TaskProperties.getString(TaskConstants.YARN_JOB_HISTORY_STATUS_ADDRESS);


    private static final LoadingCache<String, HadoopUtils> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(TaskProperties.getInt(TaskConstants.KERBEROS_EXPIRE_TIME), TimeUnit.HOURS)
            .build(new CacheLoader<String, HadoopUtils>() {
                @Override
                public HadoopUtils load(String key) {
                    return new HadoopUtils();
                }
            });


    private HadoopUtils() {
        init();
        initHdfsPath();
    }

    */
/**
     * init dolphinscheduler root path in hdfs
     *//*


    private void initHdfsPath() {
        Path path = new Path(resourceUploadPath);
        try {
            if (!fs.exists(path)) {
                fs.mkdirs(path);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    */
/**
     * init hadoop configuration
     *//*

    private void init() {
        try {
            configuration = new HdfsConfiguration();

            String resourceStorageType = TaskProperties.getString(TaskConstants.RESOURCE_STORAGE_TYPE);
            ResUploadType resUploadType = ResUploadType.valueOf(resourceStorageType);

            if (resUploadType == ResUploadType.HDFS) {
                if (TaskProperties.getBoolean(TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE,false)){
                    System.setProperty(TaskConstants.JAVA_SECURITY_KRB5_CONF,
                            TaskProperties.getString(TaskConstants.JAVA_SECURITY_KRB5_CONF_PATH));

                    configuration.set(TaskConstants.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
                    hdfsUser = "";
                    UserGroupInformation.setConfiguration(configuration);
                    UserGroupInformation.loginUserFromKeytab(TaskProperties.getString(TaskConstants.LOGIN_USER_KEY_TAB_USERNAME),
                            TaskProperties.getString(TaskConstants.LOGIN_USER_KEY_TAB_PATH));
                }

                String defaultFS = configuration.get(TaskConstants.FS_DEFAULTFS);
                //first get key from core-site.xml hdfs-site.xml ,if null ,then try to get from properties file
                // the default is the local file system
                if (defaultFS.startsWith("file")) {
                    String defaultFSProp = TaskProperties.getString(TaskConstants.FS_DEFAULTFS);
                    if (StringUtils.isNotBlank(defaultFSProp)) {
                        Map<String, String> fsRelatedProps = TaskProperties.getPrefixedProperties("fs.");
                        configuration.set(TaskConstants.FS_DEFAULTFS, defaultFSProp);
                        fsRelatedProps.forEach((key, value) -> configuration.set(key, value));
                    } else {
                        logger.error("property:{} can not to be empty, please set!", TaskConstants.FS_DEFAULTFS);
                        throw new RuntimeException(
                                String.format("property: %s can not to be empty, please set!", TaskConstants.FS_DEFAULTFS)
                        );
                    }
                } else {
                    logger.info("get property:{} -> {}, from core-site.xml hdfs-site.xml ", TaskConstants.FS_DEFAULTFS, defaultFS);
                }

                if (fs == null) {
                    if (StringUtils.isNotEmpty(hdfsUser)) {
                        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
                        ugi.doAs((PrivilegedExceptionAction<Boolean>) () -> {
                            fs = FileSystem.get(configuration);
                            return true;
                        });
                    } else {
                        logger.warn("hdfs.root.user is not set value!");
                        fs = FileSystem.get(configuration);
                    }
                }
            } else if (resUploadType == ResUploadType.S3) {
                System.setProperty(TaskConstants.AWS_S3_V4, Constants.STRING_TRUE);
                configuration.set(TaskConstants.FS_DEFAULTFS, TaskProperties.getString(TaskConstants.FS_DEFAULTFS));
                configuration.set(TaskConstants.FS_S3A_ENDPOINT, TaskProperties.getString(TaskConstants.FS_S3A_ENDPOINT));
                configuration.set(TaskConstants.FS_S3A_ACCESS_KEY, TaskProperties.getString(TaskConstants.FS_S3A_ACCESS_KEY));
                configuration.set(TaskConstants.FS_S3A_SECRET_KEY, TaskProperties.getString(TaskConstants.FS_S3A_SECRET_KEY));
                fs = FileSystem.get(configuration);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    static HadoopUtils getInstance() {

        return cache.getUnchecked(HADOOP_UTILS_KEY);
    }

    */
/**
     * get the state of an application
     *
     * @param applicationId application id
     * @return the return may be null or there may be other parse exceptions
     *//*

    ExecutionStatus getApplicationStatus(String applicationId) throws Exception {
        if (StringUtils.isEmpty(applicationId)) {
            return null;
        }

        String result = TaskConstants.FAILED;
        String applicationUrl = getApplicationUrl(applicationId);
        logger.info("applicationUrl={}", applicationUrl);

        String responseContent;
        // 可能为空
        if (Boolean.parseBoolean(TaskProperties.getString(TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, "false"))) {
            responseContent = KerberosHttpClient.get(applicationUrl);
        } else {
            responseContent = HttpUtils.get(applicationUrl);
        }
        if (responseContent != null) {
            ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
            if (!jsonObject.has("app")) {
                return ExecutionStatus.FAILURE;
            }
            result = jsonObject.path("app").path("finalStatus").asText();

        } else {
            //may be in job history
            String jobHistoryUrl = getJobHistoryUrl(applicationId);
            logger.info("jobHistoryUrl={}", jobHistoryUrl);
            responseContent = HttpUtils.get(jobHistoryUrl);
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
            case TaskConstants.ACCEPTED:
                return ExecutionStatus.SUBMITTED_SUCCESS;
            case TaskConstants.SUCCEEDED:
                return ExecutionStatus.SUCCESS;
            case TaskConstants.NEW:
            case TaskConstants.NEW_SAVING:
            case TaskConstants.SUBMITTED:
            case TaskConstants.FAILED:
                return ExecutionStatus.FAILURE;
            case TaskConstants.KILLED:
                return ExecutionStatus.KILL;

            case TaskConstants.RUNNING:
            default:
                return ExecutionStatus.RUNNING_EXECUTION;
        }
    }


    private String getJobHistoryUrl(String applicationId) {
        //eg:application_1587475402360_712719 -> job_1587475402360_712719
        String jobId = applicationId.replace("application", "job");
        return String.format(Objects.requireNonNull(TaskProperties.getString(TaskConstants.YARN_JOB_HISTORY_STATUS_ADDRESS)), jobId);
    }


    */
/**
     * get application url
     *
     * @param applicationId application id
     * @return url of application
     *//*

    private String getApplicationUrl(String applicationId) throws Exception {
        String appUrl;

        boolean yarnEnabled = false;
        if (StringUtils.isEmpty(rmHaIds)) {
            //single resourcemanager enabled
            appUrl = appAddress;
            yarnEnabled = true;
        } else {
            //resource manager HA enabled
            appUrl = getAppAddress();
            yarnEnabled = true;
            logger.info("application url : {}", appUrl);
        }

        if (StringUtils.isBlank(appUrl)) {
            throw new Exception("application url is blank");
        }
        assert appUrl != null;
        return String.format(appUrl, applicationId);
    }

    */
/**
     * getAppAddress
     *
     * @return app address
     *//*

    private static String getAppAddress() {

        //get active ResourceManager
        String activeRM = YarnHAAdminUtils.getActiveRMName();

        String[] split1 = HadoopUtils.appAddress.split(TaskConstants.DOUBLE_SLASH);

        if (split1.length != 2) {
            return null;
        }

        String start = split1[0] + TaskConstants.DOUBLE_SLASH;
        String[] split2 = split1[1].split(TaskConstants.COLON);

        if (split2.length != 2) {
            return null;
        }

        String end = TaskConstants.COLON + split2[1];

        return start + activeRM + end;
    }

    @Override
    public void close() throws IOException {
        if (fs != null) {
            try {
                fs.close();
            } catch (IOException e) {
                logger.error("Close HadoopUtils instance failed", e);
                throw new IOException("Close HadoopUtils instance failed", e);
            }
        }
    }

    */
/**
     * yarn ha admin utils
     *//*

    private static final class YarnHAAdminUtils extends RMAdminCLI {

        */
/**
         * get active resource manager
         *//*

        static String getActiveRMName() {

            String[] rmIdArr = HadoopUtils.rmHaIds.split(TaskConstants.COMMA);

            int activeResourceManagerPort = TaskProperties.getInt(TaskConstants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT, 8088);

            String yarnUrl = "http://%s:" + activeResourceManagerPort + "/ws/v1/cluster/info";

            try {

                for (String rmId : rmIdArr) {
                    String state = getRMState(String.format(yarnUrl, rmId));
                    if (TaskConstants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                        return rmId;
                    }
                }

            } catch (Exception e) {
                for (int i = 1; i < rmIdArr.length; i++) {
                    String state = getRMState(String.format(yarnUrl, rmIdArr[i]));
                    if (TaskConstants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                        return rmIdArr[i];
                    }
                }
            }
            return null;
        }

        */
/**
         * get ResourceManager state
         *//*

        static String getRMState(String url) {

            String retStr = HttpUtils.get(url);

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
*/

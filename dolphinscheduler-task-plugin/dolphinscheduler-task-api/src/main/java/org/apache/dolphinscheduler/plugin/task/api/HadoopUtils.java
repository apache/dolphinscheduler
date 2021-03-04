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

package org.apache.dolphinscheduler.plugin.task.api;


import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RESOURCE_UPLOAD_PATH;

import org.apache.dolphinscheduler.spi.task.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;


import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.client.cli.RMAdminCLI;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class HadoopUtils implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUtils.class);

    private FileSystem fs;

    //todo default value
    public static final String resourceUploadPath = RESOURCE_UPLOAD_PATH;// "/dolphinscheduler");



    private static final LoadingCache<String, HadoopUtils> cache = CacheBuilder
            .newBuilder()
            // todo maybe is null defult value
            .expireAfterWrite(Long.parseLong(TaskConstants.KERBEROS_EXPIRE_TIME), TimeUnit.HOURS)
            .build(new CacheLoader<String, HadoopUtils>() {
                @Override
                public HadoopUtils load(String key){
                    return new HadoopUtils();
                }
            });



    private HadoopUtils() {
        init();
        initHdfsPath();
    }

    /**
     * init dolphinscheduler root path in hdfs
     */

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

    /**
     * init hadoop configuration
     */
    private void init() {
        try {
            configuration = new HdfsConfiguration();

            String resourceStorageType = TaskConstants.RESOURCE_STORAGE_TYPE;
            ResUploadType resUploadType = ResUploadType.valueOf(resourceStorageType);

            if (resUploadType == ResUploadType.HDFS) {
                if (TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false)) {
                    System.setProperty(Constants.JAVA_SECURITY_KRB5_CONF,
                            PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH));
                    configuration.set(Constants.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
                    hdfsUser = "";
                    UserGroupInformation.setConfiguration(configuration);
                    UserGroupInformation.loginUserFromKeytab(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
                            PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH));
                }

                String defaultFS = configuration.get(Constants.FS_DEFAULTFS);
                //first get key from core-site.xml hdfs-site.xml ,if null ,then try to get from properties file
                // the default is the local file system
                if (defaultFS.startsWith("file")) {
                    String defaultFSProp = PropertyUtils.getString(Constants.FS_DEFAULTFS);
                    if (StringUtils.isNotBlank(defaultFSProp)) {
                        Map<String, String> fsRelatedProps = PropertyUtils.getPrefixedProperties("fs.");
                        configuration.set(Constants.FS_DEFAULTFS, defaultFSProp);
                        fsRelatedProps.forEach((key, value) -> configuration.set(key, value));
                    } else {
                        logger.error("property:{} can not to be empty, please set!", Constants.FS_DEFAULTFS);
                        throw new RuntimeException(
                                String.format("property: %s can not to be empty, please set!", Constants.FS_DEFAULTFS)
                        );
                    }
                } else {
                    logger.info("get property:{} -> {}, from core-site.xml hdfs-site.xml ", Constants.FS_DEFAULTFS, defaultFS);
                }

                if (fs == null) {
                    if (StringUtils.isNotEmpty(hdfsUser)) {
                        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
                        ugi.doAs(new PrivilegedExceptionAction<Boolean>() {
                            @Override
                            public Boolean run() throws Exception {
                                fs = FileSystem.get(configuration);
                                return true;
                            }
                        });
                    } else {
                        logger.warn("hdfs.root.user is not set value!");
                        fs = FileSystem.get(configuration);
                    }
                }
            } else if (resUploadType == ResUploadType.S3) {
                System.setProperty(Constants.AWS_S3_V4, Constants.STRING_TRUE);
                configuration.set(Constants.FS_DEFAULTFS, PropertyUtils.getString(Constants.FS_DEFAULTFS));
                configuration.set(Constants.FS_S3A_ENDPOINT, PropertyUtils.getString(Constants.FS_S3A_ENDPOINT));
                configuration.set(Constants.FS_S3A_ACCESS_KEY, PropertyUtils.getString(Constants.FS_S3A_ACCESS_KEY));
                configuration.set(Constants.FS_S3A_SECRET_KEY, PropertyUtils.getString(Constants.FS_S3A_SECRET_KEY));
                fs = FileSystem.get(configuration);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static HadoopUtils getInstance() {

        return cache.getUnchecked(HADOOP_UTILS_KEY);
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

        String result = TaskConstants.FAILED;
        String applicationUrl = getApplicationUrl(applicationId);
        logger.info("applicationUrl={}", applicationUrl);

        String responseContent;
        // 可能为空
        if (Boolean.parseBoolean(TaskProperties.getProperties(TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, "false"))) {
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
        String appUrl = "";

        if (StringUtils.isEmpty(rmHaIds)) {
            //single resourcemanager enabled
            appUrl = appAddress;
            yarnEnabled = true;
        } else {
            //resourcemanager HA enabled
            appUrl = getAppAddress(appAddress, rmHaIds);
            yarnEnabled = true;
            logger.info("application url : {}", appUrl);
        }

        if (StringUtils.isBlank(appUrl)) {
            throw new Exception("application url is blank");
        }
        return String.format(appUrl, applicationId);
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
}

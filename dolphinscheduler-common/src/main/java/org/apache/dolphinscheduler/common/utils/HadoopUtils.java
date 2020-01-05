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
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.client.cli.RMAdminCLI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * hadoop utils
 * single instance
 */
public class HadoopUtils implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUtils.class);

    private static String hdfsUser = PropertyUtils.getString(Constants.HDFS_ROOT_USER);
    private static volatile HadoopUtils instance = new HadoopUtils();
    private static volatile Configuration configuration;
    private static FileSystem fs;


    private HadoopUtils(){
        if(StringUtils.isEmpty(hdfsUser)){
            hdfsUser = PropertyUtils.getString(Constants.HDFS_ROOT_USER);
        }
        init();
        initHdfsPath();
    }

    public static HadoopUtils getInstance(){
        // if kerberos startup , renew HadoopUtils
        if (CommonUtils.getKerberosStartupState()){
            return new HadoopUtils();
        }
        return instance;
    }

    /**
     * init dolphinscheduler root path in hdfs
     */
    private void initHdfsPath(){
        String hdfsPath = PropertyUtils.getString(Constants.DATA_STORE_2_HDFS_BASEPATH);
        Path path = new Path(hdfsPath);

        try {
            if (!fs.exists(path)) {
                fs.mkdirs(path);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }


    /**
     * init hadoop configuration
     */
    private void init() {
        if (configuration == null) {
            synchronized (HadoopUtils.class) {
                if (configuration == null) {
                    try {
                        configuration = new Configuration();

                        String resUploadStartupType = PropertyUtils.getString(Constants.RES_UPLOAD_STARTUP_TYPE);
                        ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);

                        if (resUploadType == ResUploadType.HDFS){
                            if (PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE)){
                                System.setProperty(Constants.JAVA_SECURITY_KRB5_CONF,
                                        PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH));
                                configuration.set(Constants.HADOOP_SECURITY_AUTHENTICATION,"kerberos");
                                UserGroupInformation.setConfiguration(configuration);
                                UserGroupInformation.loginUserFromKeytab(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
                                        PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH));
                            }

                            String defaultFS = configuration.get(Constants.FS_DEFAULTFS);
                            //first get key from core-site.xml hdfs-site.xml ,if null ,then try to get from properties file
                            // the default is the local file system
                            if(defaultFS.startsWith("file")){
                                String defaultFSProp = PropertyUtils.getString(Constants.FS_DEFAULTFS);
                                if(StringUtils.isNotBlank(defaultFSProp)){
                                    Map<String, String> fsRelatedProps = PropertyUtils.getPrefixedProperties("fs.");
                                    configuration.set(Constants.FS_DEFAULTFS,defaultFSProp);
                                    fsRelatedProps.forEach((key, value) -> configuration.set(key, value));
                                }else{
                                    logger.error("property:{} can not to be empty, please set!", Constants.FS_DEFAULTFS );
                                    throw new RuntimeException(
                                        String.format("property: %s can not to be empty, please set!", Constants.FS_DEFAULTFS)
                                        );
                                }
                            }else{
                                logger.info("get property:{} -> {}, from core-site.xml hdfs-site.xml ", Constants.FS_DEFAULTFS, defaultFS);
                            }

                            if (fs == null) {
                                if(StringUtils.isNotEmpty(hdfsUser)){
                                    //UserGroupInformation ugi = UserGroupInformation.createProxyUser(hdfsUser,UserGroupInformation.getLoginUser());
                                    UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
                                    ugi.doAs(new PrivilegedExceptionAction<Boolean>() {
                                        @Override
                                        public Boolean run() throws Exception {
                                            fs = FileSystem.get(configuration);
                                            return true;
                                        }
                                    });
                                }else{
                                    logger.warn("hdfs.root.user is not set value!");
                                    fs = FileSystem.get(configuration);
                                }
                            }
                        }else if (resUploadType == ResUploadType.S3){
                            configuration.set(Constants.FS_DEFAULTFS, PropertyUtils.getString(Constants.FS_DEFAULTFS));
                            configuration.set(Constants.FS_S3A_ENDPOINT, PropertyUtils.getString(Constants.FS_S3A_ENDPOINT));
                            configuration.set(Constants.FS_S3A_ACCESS_KEY, PropertyUtils.getString(Constants.FS_S3A_ACCESS_KEY));
                            configuration.set(Constants.FS_S3A_SECRET_KEY, PropertyUtils.getString(Constants.FS_S3A_SECRET_KEY));
                            fs = FileSystem.get(configuration);
                        }


                        String rmHaIds = PropertyUtils.getString(Constants.YARN_RESOURCEMANAGER_HA_RM_IDS);
                        String appAddress = PropertyUtils.getString(Constants.YARN_APPLICATION_STATUS_ADDRESS);
                        if (!StringUtils.isEmpty(rmHaIds)) {
                            appAddress = getAppAddress(appAddress, rmHaIds);
                            logger.info("appAddress : {}", appAddress);
                        }
                        configuration.set(Constants.YARN_APPLICATION_STATUS_ADDRESS, appAddress);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                }
            }
        }
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
    public String getApplicationUrl(String applicationId) {
        return String.format(configuration.get(Constants.YARN_APPLICATION_STATUS_ADDRESS), applicationId);
    }

    /**
     * cat file on hdfs
     *
     * @param hdfsFilePath  hdfs file path
     * @return byte[] byte array
     * @throws IOException errors
     */
    public byte[] catFile(String hdfsFilePath) throws IOException {

        if(StringUtils.isBlank(hdfsFilePath)){
            logger.error("hdfs file path:{} is blank",hdfsFilePath);
            return null;
        }

        FSDataInputStream fsDataInputStream = fs.open(new Path(hdfsFilePath));
        return IOUtils.toByteArray(fsDataInputStream);
    }



    /**
     * cat file on hdfs
     *
     * @param hdfsFilePath  hdfs file path
     * @param skipLineNums  skip line numbers
     * @param limit         read how many lines
     * @return content of file
     * @throws IOException errors
     */
    public List<String> catFile(String hdfsFilePath, int skipLineNums, int limit) throws IOException {

        if (StringUtils.isBlank(hdfsFilePath)){
            logger.error("hdfs file path:{} is blank",hdfsFilePath);
            return null;
        }

        try (FSDataInputStream in = fs.open(new Path(hdfsFilePath))){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            Stream<String> stream = br.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }
        
    }

    /**
     * make the given file and all non-existent parents into
     * directories. Has the semantics of Unix 'mkdir -p'.
     * Existence of the directory hierarchy is not an error.
     *
     * @param hdfsPath path to create
     * @return mkdir result
     * @throws IOException errors
     */
    public boolean mkdir(String hdfsPath) throws IOException {
        return fs.mkdirs(new Path(hdfsPath));
    }

    /**
     * copy files between FileSystems
     *
     * @param srcPath      source hdfs path
     * @param dstPath      destination hdfs path
     * @param deleteSource whether to delete the src
     * @param overwrite    whether to overwrite an existing file
     * @return if success or not
     * @throws IOException errors
     */
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        return FileUtil.copy(fs, new Path(srcPath), fs, new Path(dstPath), deleteSource, overwrite, fs.getConf());
    }

    /**
     * the src file is on the local disk.  Add it to FS at
     * the given dst name.

     * @param srcFile       local file
     * @param dstHdfsPath   destination hdfs path
     * @param deleteSource  whether to delete the src
     * @param overwrite     whether to overwrite an existing file
     * @return if success or not
     * @throws IOException errors
     */
    public boolean copyLocalToHdfs(String srcFile, String dstHdfsPath, boolean deleteSource, boolean overwrite) throws IOException {
        Path srcPath = new Path(srcFile);
        Path dstPath= new Path(dstHdfsPath);

        fs.copyFromLocalFile(deleteSource, overwrite, srcPath, dstPath);

        return true;
    }

    /**
     * copy hdfs file to local
     *
     * @param srcHdfsFilePath   source hdfs file path
     * @param dstFile           destination file
     * @param deleteSource      delete source
     * @param overwrite         overwrite
     * @return result of copy hdfs file to local
     * @throws IOException errors
     */
    public boolean copyHdfsToLocal(String srcHdfsFilePath, String dstFile, boolean deleteSource, boolean overwrite) throws IOException {
        Path srcPath = new Path(srcHdfsFilePath);
        File dstPath = new File(dstFile);

        if (dstPath.exists()) {
            if (dstPath.isFile()) {
                if (overwrite) {
                    dstPath.delete();
                }
            } else {
                logger.error("destination file must be a file");
            }
        }

        if(!dstPath.getParentFile().exists()){
            dstPath.getParentFile().mkdirs();
        }

        return FileUtil.copy(fs, srcPath, dstPath, deleteSource, fs.getConf());
    }

    /**
     *
     * delete a file
     *
     * @param hdfsFilePath the path to delete.
     * @param recursive if path is a directory and set to
     * true, the directory is deleted else throws an exception. In
     * case of a file the recursive can be set to either true or false.
     * @return  true if delete is successful else false.
     * @throws IOException errors
     */
    public boolean delete(String hdfsFilePath, boolean recursive) throws IOException {
        return fs.delete(new Path(hdfsFilePath), recursive);
    }

    /**
     * check if exists
     *
     * @param hdfsFilePath source file path
     * @return result of exists or not
     * @throws IOException errors
     */
    public boolean exists(String hdfsFilePath) throws IOException {
        return fs.exists(new Path(hdfsFilePath));
    }

    /**
     * Gets a list of files in the directory
     *
     * @param filePath file path
     * @return {@link FileStatus} file status
     * @throws Exception errors
     */
    public FileStatus[] listFileStatus(String filePath)throws Exception{
        try {
            return fs.listStatus(new Path(filePath));
        } catch (IOException e) {
            logger.error("Get file list exception", e);
            throw new Exception("Get file list exception", e);
        }
    }

    /**
     * Renames Path src to Path dst.  Can take place on local fs
     * or remote DFS.
     * @param src path to be renamed
     * @param dst new path after rename
     * @throws IOException on failure
     * @return true if rename is successful
     */
    public boolean rename(String src, String dst) throws IOException {
        return fs.rename(new Path(src), new Path(dst));
    }


    /**
     * get the state of an application
     *
     * @param applicationId application id
     * @return the return may be null or there may be other parse exceptions
     * @throws JSONException json exception
     */
    public ExecutionStatus getApplicationStatus(String applicationId) throws JSONException {
        if (StringUtils.isEmpty(applicationId)) {
            return null;
        }

        String applicationUrl = getApplicationUrl(applicationId);

        String responseContent = HttpUtils.get(applicationUrl);

        JSONObject jsonObject = JSONObject.parseObject(responseContent);
        String result = jsonObject.getJSONObject("app").getString("finalStatus");

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
                return ExecutionStatus.RUNNING_EXEUTION;
        }
    }

    /**
     *
     * @return data hdfs path
     */
    public static String getHdfsDataBasePath() {
        String basePath = PropertyUtils.getString(Constants.DATA_STORE_2_HDFS_BASEPATH);
        if ("/".equals(basePath)) {
            // if basepath is configured to /,  the generated url may be  //default/resources (with extra leading /)
            return "";
        } else {
            return basePath;
        }
    }

    /**
     * hdfs resource dir
     *
     * @param tenantCode tenant code
     * @return hdfs resource dir
     */
    public static String getHdfsResDir(String tenantCode) {
        return String.format("%s/resources", getHdfsTenantDir(tenantCode));
    }

    /**
     * hdfs user dir
     *
     * @param tenantCode tenant code
     * @param userId user id
     * @return hdfs resource dir
     */
    public static String getHdfsUserDir(String tenantCode,int userId) {
        return String.format("%s/home/%d", getHdfsTenantDir(tenantCode),userId);
    }

    /**
     * hdfs udf dir
     *
     * @param tenantCode tenant code
     * @return get udf dir on hdfs
     */
    public static String getHdfsUdfDir(String tenantCode) {
        return String.format("%s/udfs", getHdfsTenantDir(tenantCode));
    }

    /**
     * get absolute path and name for file on hdfs
     *
     * @param tenantCode tenant code
     * @param filename   file name
     * @return get absolute path and name for file on hdfs
     */
    public static String getHdfsFilename(String tenantCode, String filename) {
        return String.format("%s/%s", getHdfsResDir(tenantCode), filename);
    }

    /**
     * get absolute path and name for udf file on hdfs
     *
     * @param tenantCode tenant code
     * @param filename   file name
     * @return get absolute path and name for udf file on hdfs
     */
    public static String getHdfsUdfFilename(String tenantCode, String filename) {
        return String.format("%s/%s", getHdfsUdfDir(tenantCode), filename);
    }

    /**
     * @param tenantCode tenant code
     * @return file directory of tenants on hdfs
     */
    public static String getHdfsTenantDir(String tenantCode) {
        return String.format("%s/%s", getHdfsDataBasePath(), tenantCode);
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


    /**
     * yarn ha admin utils
     */
    private static final class YarnHAAdminUtils extends RMAdminCLI {

        private static final Logger logger = LoggerFactory.getLogger(YarnHAAdminUtils.class);

        /**
         * get active resourcemanager
         *
         * @param rmIds
         * @return
         */
        public static String getAcitveRMName(String rmIds) {

            String[] rmIdArr = rmIds.split(Constants.COMMA);

            int activeResourceManagerPort = PropertyUtils.getInt(Constants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT, 8088);

            String yarnUrl = "http://%s:" + activeResourceManagerPort + "/ws/v1/cluster/info";

            String state = null;
            try {
                /**
                 * send http get request to rm1
                 */
                state = getRMState(String.format(yarnUrl, rmIdArr[0]));

                if (Constants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                    return rmIdArr[0];
                } else if (Constants.HADOOP_RM_STATE_STANDBY.equals(state)) {
                    state = getRMState(String.format(yarnUrl, rmIdArr[1]));
                    if (Constants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                        return rmIdArr[1];
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                state = getRMState(String.format(yarnUrl, rmIdArr[1]));
                if (Constants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                    return rmIdArr[0];
                }
            }
            return null;
        }


        /**
         * get ResourceManager state
         *
         * @param url
         * @return
         */
        public static String getRMState(String url) {

            String retStr = HttpUtils.get(url);

            if (StringUtils.isEmpty(retStr)) {
                return null;
            }
            //to json
            JSONObject jsonObject = JSON.parseObject(retStr);

            //get ResourceManager state
            String state = jsonObject.getJSONObject("clusterInfo").getString("haState");
            return state;
        }

    }
}

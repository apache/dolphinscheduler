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

package org.apache.dolphinscheduler.plugin.storage.hdfs;

import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_FILE;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_UDF;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.exception.BaseException;
import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.KerberosHttpClient;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Slf4j
public class HdfsStorageOperator implements Closeable, StorageOperate {

    private static HdfsStorageProperties hdfsProperties = new HdfsStorageProperties();
    private static final String HADOOP_UTILS_KEY = "HADOOP_UTILS_KEY";

    private static final LoadingCache<String, HdfsStorageOperator> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(HdfsStorageProperties.getKerberosExpireTime(), TimeUnit.HOURS)
            .build(new CacheLoader<String, HdfsStorageOperator>() {

                @Override
                public HdfsStorageOperator load(String key) {
                    return new HdfsStorageOperator(hdfsProperties);
                }
            });

    private volatile boolean yarnEnabled = false;

    private Configuration configuration;
    private FileSystem fs;

    public HdfsStorageOperator() {
        this(new HdfsStorageProperties());
    }

    public HdfsStorageOperator(HdfsStorageProperties hdfsStorageProperties) {
        // Overwrite config from passing hdfsStorageProperties
        hdfsProperties = hdfsStorageProperties;
        init();
        initHdfsPath();
    }

    public static HdfsStorageOperator getInstance() {
        return cache.getUnchecked(HADOOP_UTILS_KEY);
    }

    /**
     * init dolphinscheduler root path in hdfs
     */

    private void initHdfsPath() {
        Path path = new Path(RESOURCE_UPLOAD_PATH);
        try {
            if (!fs.exists(path)) {
                fs.mkdirs(path);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * init hadoop configuration
     */
    private void init() throws NullPointerException {
        try {
            configuration = new HdfsConfiguration();

            String hdfsUser = hdfsProperties.getUser();
            if (CommonUtils.loadKerberosConf(configuration)) {
                hdfsUser = "";
            }

            String defaultFS = getDefaultFS();
            // first get key from core-site.xml hdfs-site.xml ,if null ,then try to get from properties file
            // the default is the local file system
            if (StringUtils.isNotBlank(defaultFS)) {
                Map<String, String> fsRelatedProps = PropertyUtils.getByPrefix("fs.");
                configuration.set(Constants.HDFS_DEFAULT_FS, defaultFS);
                fsRelatedProps.forEach((key, value) -> configuration.set(key, value));
            } else {
                log.error("property:{} can not to be empty, please set!", Constants.FS_DEFAULT_FS);
                throw new NullPointerException(
                        String.format("property: %s can not to be empty, please set!", Constants.FS_DEFAULT_FS));
            }

            if (!defaultFS.startsWith("file")) {
                log.info("get property:{} -> {}, from core-site.xml hdfs-site.xml ", Constants.FS_DEFAULT_FS,
                        defaultFS);
            }

            if (StringUtils.isNotEmpty(hdfsUser)) {
                UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
                ugi.doAs((PrivilegedExceptionAction<Boolean>) () -> {
                    fs = FileSystem.get(configuration);
                    return true;
                });
            } else {
                log.warn("resource.hdfs.root.user is not set value!");
                fs = FileSystem.get(configuration);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @return Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @return DefaultFS
     */
    public String getDefaultFS() {
        String defaultFS = hdfsProperties.getDefaultFS();
        if (StringUtils.isBlank(defaultFS)) {
            defaultFS = getConfiguration().get(Constants.HDFS_DEFAULT_FS);
        }
        return defaultFS;
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
    public String getApplicationUrl(String applicationId) throws BaseException {

        yarnEnabled = true;
        String appUrl = StringUtils.isEmpty(hdfsProperties.getYarnResourceRmIds())
                ? hdfsProperties.getYarnAppStatusAddress()
                : getAppAddress(hdfsProperties.getYarnAppStatusAddress(), hdfsProperties.getYarnResourceRmIds());
        if (StringUtils.isBlank(appUrl)) {
            throw new BaseException("yarn application url generation failed");
        }
        log.debug("yarn application url:{}, applicationId:{}", appUrl, applicationId);
        return String.format(appUrl, hdfsProperties.getHadoopResourceManagerHttpAddressPort(), applicationId);
    }

    public String getJobHistoryUrl(String applicationId) {
        // eg:application_1587475402360_712719 -> job_1587475402360_712719
        String jobId = applicationId.replace("application", "job");
        return String.format(hdfsProperties.getYarnJobHistoryStatusAddress(), jobId);
    }

    /**
     * cat file on hdfs
     *
     * @param hdfsFilePath hdfs file path
     * @return byte[] byte array
     * @throws IOException errors
     */
    public byte[] catFile(String hdfsFilePath) throws IOException {

        if (StringUtils.isBlank(hdfsFilePath)) {
            log.error("hdfs file path:{} is blank", hdfsFilePath);
            return new byte[0];
        }

        try (FSDataInputStream fsDataInputStream = fs.open(new Path(hdfsFilePath))) {
            return IOUtils.toByteArray(fsDataInputStream);
        }
    }

    /**
     * cat file on hdfs
     *
     * @param hdfsFilePath hdfs file path
     * @param skipLineNums skip line numbers
     * @param limit        read how many lines
     * @return content of file
     * @throws IOException errors
     */
    public List<String> catFile(String hdfsFilePath, int skipLineNums, int limit) throws IOException {

        if (StringUtils.isBlank(hdfsFilePath)) {
            log.error("hdfs file path:{} is blank", hdfsFilePath);
            return Collections.emptyList();
        }

        try (FSDataInputStream in = fs.open(new Path(hdfsFilePath))) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            Stream<String> stream = br.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public List<String> vimFile(String bucketName, String hdfsFilePath, int skipLineNums,
                                int limit) throws IOException {
        return catFile(hdfsFilePath, skipLineNums, limit);
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws IOException {
        getInstance().mkdir(tenantCode, getHdfsResDir(tenantCode));
        getInstance().mkdir(tenantCode, getHdfsUdfDir(tenantCode));
    }

    @Override
    public String getResDir(String tenantCode) {
        return getHdfsResDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return getHdfsUdfDir(tenantCode) + FOLDER_SEPARATOR;
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
    @Override
    public boolean mkdir(String tenantCode, String hdfsPath) throws IOException {
        return fs.mkdirs(new Path(addFolderSeparatorIfNotExisted(hdfsPath)));
    }

    @Override
    public String getResourceFullName(String tenantCode, String fullName) {
        return getHdfsResourceFileName(tenantCode, fullName);
    }

    @Override
    public String getResourceFileName(String tenantCode, String fullName) {
        String resDir = getResDir(tenantCode);
        return fullName.replaceFirst(resDir, "");
    }

    @Override
    public String getFileName(ResourceType resourceType, String tenantCode, String fileName) {
        return getHdfsFileName(resourceType, tenantCode, fileName);
    }

    @Override
    public void download(String tenantCode, String srcHdfsFilePath, String dstFile,
                         boolean overwrite) throws IOException {
        copyHdfsToLocal(srcHdfsFilePath, dstFile, false, overwrite);
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
    @Override
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        return FileUtil.copy(fs, new Path(srcPath), fs, new Path(dstPath), deleteSource, overwrite, fs.getConf());
    }

    /**
     * the src file is on the local disk.  Add it to FS at
     * the given dst name.
     *
     * @param srcFile      local file
     * @param dstHdfsPath  destination hdfs path
     * @param deleteSource whether to delete the src
     * @param overwrite    whether to overwrite an existing file
     * @return if success or not
     * @throws IOException errors
     */
    public boolean copyLocalToHdfs(String srcFile, String dstHdfsPath, boolean deleteSource,
                                   boolean overwrite) throws IOException {
        Path srcPath = new Path(srcFile);
        Path dstPath = new Path(dstHdfsPath);

        fs.copyFromLocalFile(deleteSource, overwrite, srcPath, dstPath);

        return true;
    }

    @Override
    public boolean upload(String buckName, String srcFile, String dstPath, boolean deleteSource,
                          boolean overwrite) throws IOException {
        return copyLocalToHdfs(srcFile, dstPath, deleteSource, overwrite);
    }

    /**
     * copy hdfs file to local
     *
     * @param srcHdfsFilePath source hdfs file path
     *
     * @param dstFile destination file
     *
     * @param deleteSource delete source
     *
     * @param overwrite overwrite
     *
     * @return result of copy hdfs file to local
     *
     * @throws IOException errors
     */
    public boolean copyHdfsToLocal(String srcHdfsFilePath, String dstFile, boolean deleteSource,
                                   boolean overwrite) throws IOException {

        Path srcPath = new Path(srcHdfsFilePath);
        File dstPath = new File(dstFile);

        if (dstPath.exists()) {
            if (dstPath.isFile()) {
                if (overwrite) {
                    Files.delete(dstPath.toPath());
                }
            } else {
                log.error("destination file must be a file");
            }
        }

        if (!dstPath.getParentFile().exists() && !dstPath.getParentFile().mkdirs()) {
            return false;
        }

        return FileUtil.copy(fs, srcPath, dstPath, deleteSource, fs.getConf());
    }

    /**
     * delete a file
     *
     * @param hdfsFilePath the path to delete.
     * @param recursive    if path is a directory and set to
     *                     true, the directory is deleted else throws an exception. In
     *                     case of a file the recursive can be set to either true or false.
     * @return true if delete is successful else false.
     * @throws IOException errors
     */
    @Override
    public boolean delete(String hdfsFilePath, boolean recursive) throws IOException {
        return fs.delete(new Path(hdfsFilePath), recursive);
    }

    /**
     * delete a list of files
     *
     * @param filePath the path to delete, usually it is a directory.
     * @param recursive    if path is a directory and set to
     *                     true, the directory is deleted else throws an exception. In
     *                     case of a file the recursive can be set to either true or false.
     * @return true if delete is successful else false.
     * @throws IOException errors
     */

    @Override
    public boolean delete(String filePath, List<String> childrenPathArray, boolean recursive) throws IOException {
        if (filePath.endsWith("/")) {
            return fs.delete(new Path(filePath), true);
        }
        return fs.delete(new Path(filePath), recursive);
    }

    /**
     * check if exists
     *
     * @param hdfsFilePath source file path
     * @return result of exists or not
     * @throws IOException errors
     */
    @Override
    public boolean exists(String hdfsFilePath) throws IOException {
        return fs.exists(new Path(hdfsFilePath));
    }

    /**
     * Gets a list of files in the directory
     *
     * @param path file fullName path
     * @return {@link FileStatus} file status
     * @throws IOException errors
     */
    @Override
    public List<StorageEntity> listFilesStatus(String path, String defaultPath, String tenantCode,
                                               ResourceType type) throws IOException {
        // TODO: Does listStatus truncate resultList if its size goes above certain threshold (like a 1000 in S3)
        // TODO: add hdfs prefix getFile
        List<StorageEntity> storageEntityList = new ArrayList<>();
        try {
            Path filePath = new Path(path);
            if (!fs.exists(filePath)) {
                return storageEntityList;
            }
            FileStatus[] fileStatuses = fs.listStatus(filePath);

            // transform FileStatusArray into the StorageEntity List
            for (FileStatus fileStatus : fileStatuses) {
                if (fileStatus.isDirectory()) {
                    // the path is a directory
                    String fullName = fileStatus.getPath().toString();
                    fullName = addFolderSeparatorIfNotExisted(fullName);

                    String suffix = StringUtils.difference(path, fullName);
                    String fileName = StringUtils.difference(defaultPath, fullName);

                    StorageEntity entity = new StorageEntity();
                    entity.setAlias(suffix);
                    entity.setFileName(fileName);
                    entity.setFullName(fullName);
                    entity.setDirectory(true);
                    entity.setUserName(tenantCode);
                    entity.setType(type);
                    entity.setSize(fileStatus.getLen());
                    entity.setCreateTime(new Date(fileStatus.getModificationTime()));
                    entity.setUpdateTime(new Date(fileStatus.getModificationTime()));
                    entity.setPfullName(path);

                    storageEntityList.add(entity);
                } else {
                    // the path is a file
                    String fullName = fileStatus.getPath().toString();
                    String[] aliasArr = fullName.split("/");
                    String alias = aliasArr[aliasArr.length - 1];

                    String fileName = StringUtils.difference(defaultPath, fullName);

                    StorageEntity entity = new StorageEntity();
                    entity.setAlias(alias);
                    entity.setFileName(fileName);
                    entity.setFullName(fullName);
                    entity.setDirectory(false);
                    entity.setUserName(tenantCode);
                    entity.setType(type);
                    entity.setSize(fileStatus.getLen());
                    entity.setCreateTime(new Date(fileStatus.getModificationTime()));
                    entity.setUpdateTime(new Date(fileStatus.getModificationTime()));
                    entity.setPfullName(path);

                    storageEntityList.add(entity);
                }
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("The path does not exist.");
        } catch (IOException e) {
            throw new IOException("Get file list exception.", e);
        }

        return storageEntityList;
    }

    @Override
    public StorageEntity getFileStatus(String path, String prefix, String tenantCode,
                                       ResourceType type) throws IOException {
        try {
            FileStatus fileStatus = fs.getFileStatus(new Path(path));
            String alias = "";
            String fileName = "";
            String fullName = fileStatus.getPath().toString();
            if (fileStatus.isDirectory()) {
                fullName = addFolderSeparatorIfNotExisted(fullName);
                alias = findDirAlias(fullName);
                fileName = StringUtils.difference(prefix, fullName);
            } else {
                String[] aliasArr = fileStatus.getPath().toString().split("/");
                alias = aliasArr[aliasArr.length - 1];
                fileName = StringUtils.difference(prefix, fileStatus.getPath().toString());
            }

            StorageEntity entity = new StorageEntity();
            entity.setAlias(alias);
            entity.setFileName(fileName);
            entity.setFullName(fullName);
            entity.setDirectory(fileStatus.isDirectory());
            entity.setUserName(tenantCode);
            entity.setType(type);
            entity.setSize(fileStatus.getLen());
            entity.setCreateTime(new Date(fileStatus.getModificationTime()));
            entity.setUpdateTime(new Date(fileStatus.getModificationTime()));
            entity.setPfullName(path);

            return entity;
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("The path does not exist.");
        } catch (IOException e) {
            throw new IOException("Get file exception.", e);
        }
    }

    /**
     * Renames Path src to Path dst.  Can take place on local fs
     * or remote DFS.
     *
     * @param src path to be renamed
     * @param dst new path after rename
     * @return true if rename is successful
     * @throws IOException on failure
     */
    public boolean rename(String src, String dst) throws IOException {
        return fs.rename(new Path(src), new Path(dst));
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
     * get data hdfs path
     *
     * @return data hdfs path
     */
    public static String getHdfsDataBasePath() {
        String defaultFS = hdfsProperties.getDefaultFS();
        defaultFS = defaultFS.endsWith("/") ? StringUtils.chop(defaultFS) : defaultFS;
        if (FOLDER_SEPARATOR.equals(RESOURCE_UPLOAD_PATH)) {
            return defaultFS + "";
        } else {
            return defaultFS + RESOURCE_UPLOAD_PATH;
        }
    }

    /**
     * hdfs resource dir
     *
     * @param tenantCode   tenant code
     * @param resourceType resource type
     * @return hdfs resource dir
     */
    public static String getHdfsDir(ResourceType resourceType, String tenantCode) {
        switch (resourceType) {
            case UDF:
                return getHdfsUdfDir(tenantCode);
            case FILE:
                return getHdfsResDir(tenantCode);
            case ALL:
                return getHdfsDataBasePath();
            default:
                return EMPTY_STRING;
        }
    }

    @Override
    public String getDir(ResourceType resourceType, String tenantCode) {
        return getHdfsDir(resourceType, tenantCode);
    }

    /**
     * hdfs resource dir
     *
     * @param tenantCode tenant code
     * @return hdfs resource dir
     */
    public static String getHdfsResDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_FILE, getHdfsTenantDir(tenantCode));
    }

    /**
     * hdfs udf dir
     *
     * @param tenantCode tenant code
     * @return get udf dir on hdfs
     */
    public static String getHdfsUdfDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_UDF, getHdfsTenantDir(tenantCode));
    }

    /**
     * get hdfs file name
     *
     * @param resourceType resource type
     * @param tenantCode   tenant code
     * @param fileName     file name
     * @return hdfs file name
     */
    public static String getHdfsFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getHdfsDir(resourceType, tenantCode), fileName);
    }

    /**
     * get absolute path and name for resource file on hdfs
     *
     * @param tenantCode tenant code
     * @param fileName   file name
     * @return get absolute path and name for file on hdfs
     */
    public static String getHdfsResourceFileName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getHdfsResDir(tenantCode), fileName);
    }

    /**
     * get absolute path and name for udf file on hdfs
     *
     * @param tenantCode tenant code
     * @param fileName   file name
     * @return get absolute path and name for udf file on hdfs
     */
    public static String getHdfsUdfFileName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getHdfsUdfDir(tenantCode), fileName);
    }

    /**
     * @param tenantCode tenant code
     * @return file directory of tenants on hdfs
     */
    public static String getHdfsTenantDir(String tenantCode) {
        return String.format(FORMAT_S_S, getHdfsDataBasePath(), tenantCode);
    }

    /**
     * getAppAddress
     *
     * @param appAddress app address
     * @param rmHa       resource manager ha
     * @return app address
     */
    public static String getAppAddress(String appAddress, String rmHa) {

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

    @Override
    public void close() throws IOException {
        if (fs != null) {
            try {
                fs.close();
            } catch (IOException e) {
                log.error("Close HadoopUtils instance failed", e);
                throw new IOException("Close HadoopUtils instance failed", e);
            }
        }
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

            String yarnUrl =
                    protocol + "%s:" + hdfsProperties.getHadoopResourceManagerHttpAddressPort() + "/ws/v1/cluster/info";

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
                    .equals(hdfsProperties.isHadoopSecurityAuthStartupState())
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

    @Override
    public void deleteTenant(String tenantCode) throws Exception {
        String tenantPath = getHdfsDataBasePath() + FOLDER_SEPARATOR + tenantCode;

        if (exists(tenantPath)) {
            delete(tenantPath, true);

        }
    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.HDFS;
    }

    @Override
    public List<StorageEntity> listFilesStatusRecursively(String path, String defaultPath, String tenantCode,
                                                          ResourceType type) {
        List<StorageEntity> storageEntityList = new ArrayList<>();

        LinkedList<StorageEntity> foldersToFetch = new LinkedList<>();

        do {
            String pathToExplore = "";
            if (foldersToFetch.size() == 0) {
                pathToExplore = path;
            } else {
                pathToExplore = foldersToFetch.pop().getFullName();
            }

            try {
                List<StorageEntity> tempList = listFilesStatus(pathToExplore, defaultPath, tenantCode, type);

                for (StorageEntity temp : tempList) {
                    if (temp.isDirectory()) {
                        foldersToFetch.add(temp);
                    }
                }

                storageEntityList.addAll(tempList);
            } catch (FileNotFoundException e) {
                log.error("Resource path: {}", pathToExplore, e);
                // return the resources fetched before error occurs.
                return storageEntityList;
            } catch (IOException e) {
                log.error("Resource path: {}", pathToExplore, e);
                // return the resources fetched before error occurs.
                return storageEntityList;
            }

        } while (foldersToFetch.size() != 0);

        return storageEntityList;

    }

    /**
     * find alias for directories, NOT for files
     * a directory is a path ending with "/"
     */
    private String findDirAlias(String myStr) {
        if (!myStr.endsWith("/")) {
            // Make sure system won't crush down if someone accidentally misuse the function.
            return myStr;
        }
        int lastIndex = myStr.lastIndexOf("/");
        String subbedString = myStr.substring(0, lastIndex);
        int secondLastIndex = subbedString.lastIndexOf("/");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(myStr, secondLastIndex + 1, lastIndex + 1);

        return stringBuilder.toString();
    }

    private String addFolderSeparatorIfNotExisted(String fullName) {
        return fullName.endsWith(FOLDER_SEPARATOR) ? fullName : fullName + FOLDER_SEPARATOR;
    }
}

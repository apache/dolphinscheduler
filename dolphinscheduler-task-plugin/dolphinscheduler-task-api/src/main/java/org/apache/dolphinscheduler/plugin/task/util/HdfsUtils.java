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

package org.apache.dolphinscheduler.plugin.task.util;

import org.apache.dolphinscheduler.spi.enums.ResUploadType;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.apache.dolphinscheduler.spi.task.TaskConstants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * hadoop utils
 * single instance
 */
public class HdfsUtils implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(HdfsUtils.class);

    private static String hdfsUser = PropertyUtils.getString(TaskConstants.HDFS_ROOT_USER);
    public static final String resourceUploadPath = PropertyUtils.getString(TaskConstants.RESOURCE_UPLOAD_PATH, "/dolphinscheduler");

    private static final String HADOOP_UTILS_KEY = "HADOOP_UTILS_KEY";

    private static final LoadingCache<String, HdfsUtils> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(PropertyUtils.getInt(TaskConstants.KERBEROS_EXPIRE_TIME, 2), TimeUnit.HOURS)
            .build(new CacheLoader<String, HdfsUtils>() {
                @Override
                public HdfsUtils load(String key) throws Exception {
                    return new HdfsUtils();
                }
            });

    private static volatile boolean yarnEnabled = false;

    private Configuration configuration;
    private FileSystem fs;

    private HdfsUtils() {
        init();
        initHdfsPath();
    }

    public static HdfsUtils getInstance() {

        return cache.getUnchecked(HADOOP_UTILS_KEY);
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

            String resourceStorageType = PropertyUtils.getUpperCaseString(TaskConstants.RESOURCE_STORAGE_TYPE);
            ResUploadType resUploadType = ResUploadType.valueOf(resourceStorageType);

            if (resUploadType == ResUploadType.HDFS) {
                if (CommonUtils.loadKerberosConf(configuration)) {
                    hdfsUser = "";
                }

                String defaultFS = configuration.get(TaskConstants.FS_DEFAULTFS);
                //first get key from core-site.xml hdfs-site.xml ,if null ,then try to get from properties file
                // the default is the local file system
                if (defaultFS.startsWith("file")) {
                    String defaultFSProp = PropertyUtils.getString(TaskConstants.FS_DEFAULTFS);
                    if (StringUtils.isNotBlank(defaultFSProp)) {
                        Map<String, String> fsRelatedProps = PropertyUtils.getPrefixedProperties("fs.");
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
            } else if (resUploadType == ResUploadType.S3) {
                System.setProperty(TaskConstants.AWS_S3_V4, TaskConstants.STRING_TRUE);
                configuration.set(TaskConstants.FS_DEFAULTFS, PropertyUtils.getString(TaskConstants.FS_DEFAULTFS));
                configuration.set(TaskConstants.FS_S3A_ENDPOINT, PropertyUtils.getString(TaskConstants.FS_S3A_ENDPOINT));
                configuration.set(TaskConstants.FS_S3A_ACCESS_KEY, PropertyUtils.getString(TaskConstants.FS_S3A_ACCESS_KEY));
                configuration.set(TaskConstants.FS_S3A_SECRET_KEY, PropertyUtils.getString(TaskConstants.FS_S3A_SECRET_KEY));
                fs = FileSystem.get(configuration);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @return Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @return Configuration
     */
    public String getDefaultFS() {
        return getConfiguration().get(TaskConstants.FS_DEFAULTFS);
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
            logger.error("hdfs file path:{} is blank", hdfsFilePath);
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
     * @param limit read how many lines
     * @return content of file
     * @throws IOException errors
     */
    public List<String> catFile(String hdfsFilePath, int skipLineNums, int limit) throws IOException {

        if (StringUtils.isBlank(hdfsFilePath)) {
            logger.error("hdfs file path:{} is blank", hdfsFilePath);
            return Collections.emptyList();
        }

        try (FSDataInputStream in = fs.open(new Path(hdfsFilePath))) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
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
     * @param srcPath source hdfs path
     * @param dstPath destination hdfs path
     * @param deleteSource whether to delete the src
     * @param overwrite whether to overwrite an existing file
     * @return if success or not
     * @throws IOException errors
     */
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        return FileUtil.copy(fs, new Path(srcPath), fs, new Path(dstPath), deleteSource, overwrite, fs.getConf());
    }

    /**
     * the src file is on the local disk.  Add it to FS at
     * the given dst name.
     *
     * @param srcFile local file
     * @param dstHdfsPath destination hdfs path
     * @param deleteSource whether to delete the src
     * @param overwrite whether to overwrite an existing file
     * @return if success or not
     * @throws IOException errors
     */
    public boolean copyLocalToHdfs(String srcFile, String dstHdfsPath, boolean deleteSource, boolean overwrite) throws IOException {
        Path srcPath = new Path(srcFile);
        Path dstPath = new Path(dstHdfsPath);

        fs.copyFromLocalFile(deleteSource, overwrite, srcPath, dstPath);

        return true;
    }

    /**
     * copy hdfs file to local
     *
     * @param srcHdfsFilePath source hdfs file path
     * @param dstFile destination file
     * @param deleteSource delete source
     * @param overwrite overwrite
     * @return result of copy hdfs file to local
     * @throws IOException errors
     */
    public boolean copyHdfsToLocal(String srcHdfsFilePath, String dstFile, boolean deleteSource, boolean overwrite) throws IOException {
        Path srcPath = new Path(srcHdfsFilePath);
        File dstPath = new File(dstFile);

        if (dstPath.exists()) {
            if (dstPath.isFile()) {
                if (overwrite) {
                    Files.delete(dstPath.toPath());
                }
            } else {
                logger.error("destination file must be a file");
            }
        }

        if (!dstPath.getParentFile().exists()) {
            dstPath.getParentFile().mkdirs();
        }

        return FileUtil.copy(fs, srcPath, dstPath, deleteSource, fs.getConf());
    }

    /**
     * delete a file
     *
     * @param hdfsFilePath the path to delete.
     * @param recursive if path is a directory and set to
     * true, the directory is deleted else throws an exception. In
     * case of a file the recursive can be set to either true or false.
     * @return true if delete is successful else false.
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
    public FileStatus[] listFileStatus(String filePath) throws Exception {
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
     * get data hdfs path
     *
     * @return data hdfs path
     */
    public static String getHdfsDataBasePath() {
        if ("/".equals(resourceUploadPath)) {
            // if basepath is configured to /,  the generated url may be  //default/resources (with extra leading /)
            return "";
        } else {
            return resourceUploadPath;
        }
    }

    /**
     * hdfs resource dir
     *
     * @param tenantCode tenant code
     * @param resourceType resource type
     * @return hdfs resource dir
     */
    public static String getHdfsDir(ResourceType resourceType, String tenantCode) {
        String hdfsDir = "";
        if (resourceType.equals(ResourceType.FILE)) {
            hdfsDir = getHdfsResDir(tenantCode);
        } else if (resourceType.equals(ResourceType.UDF)) {
            hdfsDir = getHdfsUdfDir(tenantCode);
        }
        return hdfsDir;
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
    public static String getHdfsUserDir(String tenantCode, int userId) {
        return String.format("%s/home/%d", getHdfsTenantDir(tenantCode), userId);
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
     * get hdfs file name
     *
     * @param resourceType resource type
     * @param tenantCode tenant code
     * @param fileName file name
     * @return hdfs file name
     */
    public static String getHdfsFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith("/")) {
            fileName = fileName.replaceFirst("/", "");
        }
        return String.format("%s/%s", getHdfsDir(resourceType, tenantCode), fileName);
    }

    /**
     * get absolute path and name for resource file on hdfs
     *
     * @param tenantCode tenant code
     * @param fileName file name
     * @return get absolute path and name for file on hdfs
     */
    public static String getHdfsResourceFileName(String tenantCode, String fileName) {
        if (fileName.startsWith("/")) {
            fileName = fileName.replaceFirst("/", "");
        }
        return String.format("%s/%s", getHdfsResDir(tenantCode), fileName);
    }

    /**
     * get absolute path and name for udf file on hdfs
     *
     * @param tenantCode tenant code
     * @param fileName file name
     * @return get absolute path and name for udf file on hdfs
     */
    public static String getHdfsUdfFileName(String tenantCode, String fileName) {
        if (fileName.startsWith("/")) {
            fileName = fileName.replaceFirst("/", "");
        }
        return String.format("%s/%s", getHdfsUdfDir(tenantCode), fileName);
    }

    /**
     * @param tenantCode tenant code
     * @return file directory of tenants on hdfs
     */
    public static String getHdfsTenantDir(String tenantCode) {
        return String.format("%s/%s", getHdfsDataBasePath(), tenantCode);
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

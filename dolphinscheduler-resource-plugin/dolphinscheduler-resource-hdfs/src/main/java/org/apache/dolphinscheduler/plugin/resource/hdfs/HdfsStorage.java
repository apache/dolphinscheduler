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

package org.apache.dolphinscheduler.plugin.resource.hdfs;

import static org.apache.dolphinscheduler.plugin.resource.hdfs.HdfsStorageConfiguration.ROOT_USER;

import org.apache.dolphinscheduler.spi.resource.ResourceStorage;
import org.apache.dolphinscheduler.spi.resource.ResourceStorageException;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsStorage implements ResourceStorage {

    private static final Logger logger = LoggerFactory.getLogger(HdfsStorage.class);

    private HdfsConfiguration configuration;
    private FileSystem fs;

    private static final String FS_PREFIX = "fs.";

    @Override
    public void init(Map<String, String> config) {
        if (null == config || config.isEmpty()) {
            logger.error("init hdfs error, config file is null");
            return;
        }
        configuration = new HdfsConfiguration();
        String defaultFS = configuration.get(HdfsStorageConfiguration.FS_DEFAULT_FS.getName());
        if (defaultFS.startsWith("file")) {

            String defaultFSProp = config.get(defaultFS);
            if (StringUtils.isNotBlank(defaultFSProp)) {
                Map<String, String> fsRelatedProps = config.entrySet().stream().filter(data -> data.getKey().startsWith(FS_PREFIX)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                configuration.set(HdfsStorageConfiguration.FS_DEFAULT_FS.getName(), defaultFSProp);
                fsRelatedProps.forEach((key, value) -> configuration.set(key, value));
            }
            String hdfsUser = config.get(ROOT_USER.getName());
            if (StringUtils.isNotEmpty(hdfsUser)) {
                UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
                try {
                    ugi.doAs((PrivilegedExceptionAction<Boolean>) () -> {
                        fs = FileSystem.get(configuration);
                        return true;
                    });
                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ResourceStorageException("hdfs user authed error", e);
                }
            } else {
                logger.warn("hdfs.root.user is not set value!");
                try {
                    fs = FileSystem.get(configuration);
                } catch (IOException e) {
                    throw new ResourceStorageException("init hdfs file system error", e);
                }
            }
        }

        String hdfsUser = ROOT_USER.getParameterValue(config.get(ROOT_USER.getName()));
    }

    @Override
    public byte[] catFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            logger.info("hdfs file path:{} is blank", filePath);
            return new byte[0];
        }

        try (FSDataInputStream fsDataInputStream = fs.open(new Path(filePath))) {
            return IOUtils.toByteArray(fsDataInputStream);
        } catch (IOException e) {
            throw new ResourceStorageException("cat file error,file path is: s%", e, filePath);
        }
    }

    @Override
    public List<String> catFile(String filePath, int skipLineNums, int limit) {
        if (StringUtils.isBlank(filePath)) {
            logger.info("hdfs file path:{} is blank", filePath);
            return Collections.emptyList();
        }

        try (FSDataInputStream in = fs.open(new Path(filePath))) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            Stream<String> stream = br.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new ResourceStorageException("cat file error,file path is: s%", e, filePath);
        }
    }

    private boolean mkdir(String path) {
        try {
            return fs.mkdirs(new Path(path));
        } catch (IOException e) {
            throw new ResourceStorageException("mkdir error,file path is: s%", e, path);
        }
    }

    @Override
    public boolean deleteFile(String filePath, Boolean recursive) {
        try {
            return fs.delete(new Path(filePath), recursive);
        } catch (IOException e) {
            throw new ResourceStorageException("delete file error,file path is :s%", e, filePath);
        }
    }

    @Override
    public boolean rename(String oldPath, String newPath) {
        try {
            return fs.rename(new Path(oldPath), new Path(newPath));
        } catch (IOException e) {
            throw new ResourceStorageException("rename file error,file path is: s%", e, oldPath);
        }
    }

    @Override
    public boolean exists(String filePath) {
        try {
            return fs.exists(new Path(filePath));
        } catch (IOException e) {
            throw new ResourceStorageException("query file exist error,file path is: s%", e, filePath);
        }
    }

    @Override
    public boolean uploadLocalFile(String localFileName, String resourceStorageName, boolean overwrite) {
        Path srcPath = new Path(localFileName);
        Path dstPath = new Path(resourceStorageName);

        try {
            fs.copyFromLocalFile(true, overwrite, srcPath, dstPath);
        } catch (IOException e) {
            throw new ResourceStorageException("hdfs upload file err, file path is : s%", e, localFileName);
        }
        return true;
    }

    @Override
    public boolean downloadFileToLocal(String resourceFilePath, String localFilePath) {
        Path srcPath = new Path(resourceFilePath);
        File dstPath = new File(localFilePath);

        if (dstPath.exists()) {
            if (dstPath.isFile()) {

                try {
                    Files.delete(dstPath.toPath());
                } catch (IOException e) {
                    throw new ResourceStorageException(String.format("copy hdfs file error,delete old target file %s error", dstPath));
                }

            } else {
                logger.error("destination file must be a file");
            }
        }

        if (!dstPath.getParentFile().exists()) {
            dstPath.getParentFile().mkdirs();
        }

        try {
            return FileUtil.copy(fs, srcPath, dstPath, false, fs.getConf());
        } catch (IOException e) {
            throw new ResourceStorageException("copy hdfs file error");
        }
    }

    @Override
    public boolean copyFile(String filePath, String targetFilePath, boolean overwrite,boolean deleteSource) {
        try {
            return FileUtil.copy(fs, new Path(filePath), fs, new Path(targetFilePath), deleteSource, overwrite, fs.getConf());
        } catch (IOException e) {
            throw new ResourceStorageException("copy hdfs file error");
        }
    }

}

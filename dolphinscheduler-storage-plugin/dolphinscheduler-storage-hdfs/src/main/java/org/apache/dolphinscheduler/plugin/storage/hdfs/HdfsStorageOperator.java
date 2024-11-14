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

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.storage.api.AbstractStorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.constants.StorageConstants;

import org.apache.commons.collections4.MapUtils;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HdfsStorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private final HdfsStorageProperties hdfsProperties;

    private Configuration configuration;
    private FileSystem fs;

    public HdfsStorageOperator(HdfsStorageProperties hdfsStorageProperties) {
        super(hdfsStorageProperties.getResourceUploadPath());
        // Overwrite config from passing hdfsStorageProperties
        hdfsProperties = hdfsStorageProperties;
        init();
        initHdfsPath();
    }

    @SneakyThrows
    private void initHdfsPath() {
        Path path = new Path(resourceBaseAbsolutePath);
        if (!fs.exists(path)) {
            if (!fs.mkdirs(path)) {
                log.info("Create hdfs path: {} failed", path);
            } else {
                log.error("Create hdfs path: {} success", path);
            }
        }
    }

    @SneakyThrows
    private void init() {
        configuration = new HdfsConfiguration();

        if (MapUtils.isNotEmpty(hdfsProperties.getConfigurationProperties())) {
            hdfsProperties.getConfigurationProperties().forEach((key, value) -> {
                configuration.set(key, value);
                log.info("Set HDFS prop: {}  -> {}", key, value);
            });
        }

        String defaultFS = hdfsProperties.getDefaultFS();
        if (StringUtils.isNotEmpty(defaultFS)) {
            configuration.set(StorageConstants.HDFS_DEFAULT_FS, hdfsProperties.getDefaultFS());
        }

        if (CommonUtils.getKerberosStartupState()) {
            CommonUtils.loadKerberosConf(configuration);
            fs = FileSystem.get(configuration);
            log.info("Initialize HdfsStorageOperator with kerberos");
            return;
        }
        if (StringUtils.isNotEmpty(hdfsProperties.getUser())) {
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsProperties.getUser());
            ugi.doAs((PrivilegedExceptionAction<Boolean>) () -> {
                fs = FileSystem.get(configuration);
                return true;
            });
            UserGroupInformation.setLoginUser(ugi);
            log.info("Initialize HdfsStorageOperator with remote user: {}", hdfsProperties.getUser());
            return;
        }
        fs = FileSystem.get(configuration);
        log.info("Initialize HdfsStorageOperator with default user");

    }

    @Override
    public String getStorageBaseDirectory() {
        String defaultFS = hdfsProperties.getDefaultFS();
        return FileUtils.concatFilePath(defaultFS, resourceBaseAbsolutePath);
    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String hdfsFilePath, int skipLineNums, int limit) {
        try (
                FSDataInputStream in = fs.open(new Path(hdfsFilePath));
                InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(inputStreamReader)) {
            return br.lines()
                    .skip(skipLineNums)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @SneakyThrows
    public void createStorageDir(String directoryAbsolutePath) {
        Path path = new Path(directoryAbsolutePath);
        if (fs.exists(path)) {
            throw new FileAlreadyExistsException("Directory already exists: " + directoryAbsolutePath);
        }
        fs.mkdirs(new Path(directoryAbsolutePath));
    }

    @SneakyThrows
    @Override
    public void download(String srcHdfsFilePath, String dstFile, boolean overwrite) {
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
            throw new IOException("Failed to create parent directory for destination file");
        }

        FileUtil.copy(fs, srcPath, dstPath, false, fs.getConf());
    }

    @SneakyThrows
    @Override
    public void copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) {
        FileUtil.copy(fs, new Path(srcPath), fs, new Path(dstPath), deleteSource, overwrite, fs.getConf());
    }

    @SneakyThrows
    @Override
    public void upload(String srcAbsoluteFilePath,
                       String dstAbsoluteFilePath,
                       boolean deleteSource,
                       boolean overwrite) {
        Path srcPath = new Path(srcAbsoluteFilePath);
        Path dstPath = new Path(dstAbsoluteFilePath);
        fs.copyFromLocalFile(deleteSource, overwrite, srcPath, dstPath);
    }

    @SneakyThrows
    @Override
    public void delete(String resourceAbsolutePath, boolean recursive) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        fs.delete(new Path(resourceAbsolutePath), recursive);
    }

    @SneakyThrows
    @Override
    public boolean exists(String resourceAbsolutePath) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        return fs.exists(new Path(resourceAbsolutePath));
    }

    @SneakyThrows
    @Override
    public List<StorageEntity> listStorageEntity(String resourceAbsolutePath) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        Path path = new Path(resourceAbsolutePath);
        if (!fs.exists(path)) {
            return Collections.emptyList();
        }
        return Arrays.stream(fs.listStatus(new Path(resourceAbsolutePath)))
                .map(this::transformFileStatusToResourceMetadata)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        List<StorageEntity> result = new ArrayList<>();

        LinkedList<String> foldersToFetch = new LinkedList<>();
        foldersToFetch.addLast(resourceAbsolutePath);

        while (!foldersToFetch.isEmpty()) {
            String absolutePath = foldersToFetch.pollFirst();
            Path path = new Path(absolutePath);
            if (!fs.exists(path)) {
                continue;
            }
            FileStatus[] fileStatuses = fs.listStatus(path);
            for (FileStatus fileStatus : fileStatuses) {
                if (fileStatus.isDirectory()) {
                    foldersToFetch.addLast(fileStatus.getPath().toString());
                }
                result.add(transformFileStatusToResourceMetadata(fileStatus));
            }
        }
        return result;
    }

    @SneakyThrows
    @Override
    public StorageEntity getStorageEntity(String resourceAbsolutePath) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        FileStatus fileStatus = fs.getFileStatus(new Path(resourceAbsolutePath));
        return transformFileStatusToResourceMetadata(fileStatus);
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

    private StorageEntity transformFileStatusToResourceMetadata(FileStatus fileStatus) {
        Path fileStatusPath = fileStatus.getPath();
        String fileAbsolutePath = fileStatusPath.toString();
        ResourceMetadata resourceMetaData = getResourceMetaData(fileAbsolutePath);

        return StorageEntity.builder()
                .fileName(fileStatusPath.getName())
                .fullName(fileAbsolutePath)
                .pfullName(resourceMetaData.getResourceParentAbsolutePath())
                .type(resourceMetaData.getResourceType())
                .isDirectory(fileStatus.isDirectory())
                .size(fileStatus.getLen())
                .createTime(new Date(fileStatus.getModificationTime()))
                .updateTime(new Date(fileStatus.getModificationTime()))
                .build();
    }

}

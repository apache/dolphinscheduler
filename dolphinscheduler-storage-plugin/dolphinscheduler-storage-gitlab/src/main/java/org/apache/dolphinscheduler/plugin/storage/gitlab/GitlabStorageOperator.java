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

package org.apache.dolphinscheduler.plugin.storage.gitlab;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.storage.api.AbstractStorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.storage.gitlab.api.RepositoryFileTreeApi;
import org.apache.dolphinscheduler.plugin.storage.gitlab.dto.GitlabFileNode;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.collections4.CollectionUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.RepositoryFile;

@Slf4j
public class GitlabStorageOperator extends AbstractStorageOperator implements Closeable, StorageOperator {

    private final String BRANCH;
    private final int PROJECT_ID;
    private final RepositoryFileTreeApi repositoryFileTreeApi;
    private final static String UNSUPPORTED_OPERATION_MSG =
            "The operation is not supported when resource file was managed by Gitlab";

    public GitlabStorageOperator(String resourceBasePath, GitLabApi gitLabApi, int projectId, String branch) {
        super(resourceBasePath);
        if (log.isInfoEnabled()) {
            gitLabApi.enableRequestResponseLogging(Level.INFO);
        }
        this.repositoryFileTreeApi = new RepositoryFileTreeApi(gitLabApi);
        this.BRANCH = branch;
        this.PROJECT_ID = projectId;

    }

    @Override
    public String getStorageBaseDirectory() {
        // gitlab storage base dir is empty.
        return resourceBaseAbsolutePath;
    }

    @Override
    public String getStorageBaseDirectory(String tenantCode, ResourceType resourceType) {
        // GitLab does not support tenant-based isolation of resource files
        return getStorageBaseDirectory();
    }

    @SneakyThrows
    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        return repositoryFileTreeApi.getFileTreeRecursive(PROJECT_ID, BRANCH)
                .stream()
                .map(transformGitlabTreeNodeToStorageEntity())
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        // ignore
    }

    @Override
    public void createStorageDir(String directoryAbsolutePath) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MSG);
    }

    @Override
    public boolean exists(String resourceAbsolutePath) {
        // GitLab REST API v4 没有一个“直接判断路径是文件还是目录”的接口，得靠两个 api 的「404 语义 + 类型字段」来区分
        try {
            // 如果查询文件信息接口没有抛异常，说明这是一个文件，且存在
            repositoryFileTreeApi.getFileInfo(PROJECT_ID, BRANCH, resourceAbsolutePath);
            return true;
        } catch (GitLabApiException e) {
            // 出现异常，说明不是文件，此时再查看是否文件夹
            List<StorageEntity> storageEntities = this.listStorageEntity(resourceAbsolutePath);
            return CollectionUtils.isNotEmpty(storageEntities);
        }
    }

    @Override
    public void delete(String resourceAbsolutePath, boolean recursive) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MSG);
    }

    @Override
    public void copy(String srcAbsolutePath, String dstAbsolutePath, boolean deleteSource, boolean overwrite) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MSG);
    }

    @Override
    public void upload(String srcLocalFileAbsolutePath, String dstAbsolutePath, boolean deleteSource,
                       boolean overwrite) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MSG);
    }

    @SneakyThrows
    @Override
    public void download(String srcFileAbsolutePath, String dstAbsoluteFile, boolean overwrite) {
        File file = new File(dstAbsoluteFile);
        if (file.exists() && !overwrite) {
            throw new RuntimeException("the destination file " + dstAbsoluteFile + " already exists");
        }
        try (InputStream in = repositoryFileTreeApi.getRawFile(PROJECT_ID, BRANCH, srcFileAbsolutePath)) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String fileAbsolutePath, int skipLineNums, int limit) {
        try (
                InputStream inputStream = repositoryFileTreeApi.getRawFile(PROJECT_ID, BRANCH, fileAbsolutePath);
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return bufferedReader.lines()
                    .skip(skipLineNums)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    @SneakyThrows
    @Override
    public List<StorageEntity> listStorageEntity(String resourceAbsolutePath) {
        return repositoryFileTreeApi.getFileTree(PROJECT_ID, resourceAbsolutePath, BRANCH)
                .stream()
                .map(transformGitlabTreeNodeToStorageEntity())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public StorageEntity getStorageEntity(String resourceAbsolutePath) {
        RepositoryFile fileInfo = repositoryFileTreeApi.getFileInfo(PROJECT_ID, resourceAbsolutePath, BRANCH);
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setFileName(fileInfo.getFileName());
        storageEntity.setSize(fileInfo.getSize());
        storageEntity.setType(ResourceType.FILE);
        storageEntity.setFullName(fileInfo.getFilePath());
        storageEntity.setPfullName(FileUtils.getParentPath(fileInfo.getFilePath()).toString());
        return storageEntity;
    }

    /**
     * transform GitlabFileNode to StorageEntity
     */
    private Function<GitlabFileNode, StorageEntity> transformGitlabTreeNodeToStorageEntity() {
        return gitlabFileNode -> {
            StorageEntity storageEntity = new StorageEntity();
            storageEntity.setFileName(gitlabFileNode.getName());
            storageEntity.setFullName(gitlabFileNode.getPath());
            storageEntity.setPfullName(FileUtils.getParentPath(gitlabFileNode.getPath()).toString());
            storageEntity.setType(ResourceType.FILE);
            // tree is directory, blob is file
            storageEntity.setDirectory(gitlabFileNode.isDirectory());
            return storageEntity;
        };
    }

}

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

package org.apache.dolphinscheduler.plugin.storage.git;

import org.apache.dolphinscheduler.plugin.storage.api.AbstractStorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Git-backed implementation of the DolphinScheduler {@link AbstractStorageOperator}.
 * Uses the Git provider REST API (GitHub/GitLab/Gitea) for all file operations.
 * <p>
 * This maps the DS virtual filesystem layout ({@code baseDir/tenantCode/resources/fileName})
 * onto the repository as a simple directory structure under the configured
 * {@code resourceUploadPath}.
 */
@Slf4j
public class GitStorageOperator extends AbstractStorageOperator {

    private final GitStorageProperties properties;
    private final GitApiClient apiClient;

    /**
     * Constructs a Git-backed storage operator.
     *
     * @param properties configuration for Git storage
     */
    public GitStorageOperator(GitStorageProperties properties) {
        super(resolveResourceBaseAbsolutePath(properties));
        validateProperties(properties);
        this.properties = properties;
        this.apiClient = GitApiClientFactory.create(properties);
        log.info("Initialized Git storage operator for provider={}, repo={}, branch={}",
                properties.getProvider(), properties.getRepository(), properties.getBranch());
    }

    // ---------------------------------------------------------------------
    // Construction helpers
    // ---------------------------------------------------------------------

    private static String resolveResourceBaseAbsolutePath(GitStorageProperties properties) {
        String uploadPath = properties.getResourceUploadPath();
        if (StringUtils.isBlank(uploadPath)) {
            uploadPath = "resources";
        }
        // Prefix with a git:// scheme so the path convention is distinguishable
        return "git://" + properties.getRepository() + "/" + uploadPath;
    }

    private static void validateProperties(GitStorageProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("GitStorageProperties must not be null");
        }
        if (StringUtils.isBlank(properties.getRepository())) {
            throw new IllegalArgumentException("Git repository must be configured (resource.git.repository)");
        }
        if (StringUtils.isBlank(properties.getAuthToken())) {
            throw new IllegalArgumentException("Git auth token must be configured (resource.git.auth.token)");
        }
        if (StringUtils.isBlank(properties.getProvider())) {
            properties.setProvider("github");
        }
        if (StringUtils.isBlank(properties.getBranch())) {
            properties.setBranch("main");
        }
    }

    // ---------------------------------------------------------------------
    // Path mapping helpers
    // ---------------------------------------------------------------------

    /**
     * Converts a DS absolute path (e.g. "git://owner/repo/resources/default/resources/test.sh")
     * to a repository-relative path (e.g. "resources/default/resources/test.sh").
     */
    private String toRepoRelativePath(String dsAbsolutePath) {
        exceptionIfPathEmpty(dsAbsolutePath);
        exceptionIfPathNotUnderStorageBaseDir(dsAbsolutePath);

        String baseDir = getStorageBaseDirectory();
        String relative = dsAbsolutePath.substring(baseDir.length());
        if (relative.startsWith(File.separator)) {
            relative = relative.substring(1);
        } else if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        // Prepend the resource upload path if not already present in the relative path
        String uploadPath = properties.getResourceUploadPath();
        if (StringUtils.isNotEmpty(uploadPath) && !relative.startsWith(uploadPath)) {
            // Ensure leading slash separator
            relative = uploadPath + "/" + relative;
        } else if (StringUtils.isEmpty(relative)) {
            relative = uploadPath;
        } else {
            relative = uploadPath + "/" + relative;
        }
        return stripLeadingSlash(relative);
    }

    /**
     * Converts a repo-relative path back to a DS absolute path.
     */
    private String toDsAbsolutePath(String repoRelativePath) {
        if (StringUtils.isEmpty(repoRelativePath)) {
            return getStorageBaseDirectory();
        }
        String uploadPath = properties.getResourceUploadPath();
        String suffix;
        if (repoRelativePath.startsWith(uploadPath + "/")) {
            suffix = repoRelativePath.substring(uploadPath.length() + 1);
        } else {
            suffix = repoRelativePath;
        }
        if (suffix.isEmpty()) {
            return getStorageBaseDirectory();
        }
        // Normalize separators
        suffix = suffix.replace("/", File.separator);
        return getStorageBaseDirectory() + File.separator + suffix;
    }

    private String stripLeadingSlash(String path) {
        if (path != null && (path.startsWith("/") || path.startsWith(File.separator))) {
            return path.substring(1);
        }
        return path;
    }

    // ---------------------------------------------------------------------
    // StorageOperator interface implementation
    // ---------------------------------------------------------------------

    @SneakyThrows
    @Override
    public void createStorageDir(String directoryAbsolutePath) {
        exceptionIfPathEmpty(directoryAbsolutePath);
        log.debug("createStorageDir {}", directoryAbsolutePath);

        if (exists(directoryAbsolutePath)) {
            throw new FileAlreadyExistsException(
                    "The directory " + directoryAbsolutePath + " already exists in Git storage");
        }

        // Git doesn't have real directories – create a placeholder ".keep" file if the dir is empty.
        // First ensure all parent directories exist by creating empty placeholder files along the way.
        String repoPath = toRepoRelativePath(directoryAbsolutePath);

        // Check that parent exists
        File asFile = new File(directoryAbsolutePath);
        if (asFile.getParentFile() != null && !getStorageBaseDirectory().equals(asFile.getParent())) {
            String parentDsPath = asFile.getParent();
            if (!exists(parentDsPath)) {
                createStorageDir(parentDsPath);
            }
        }

        // Create a placeholder file so the directory exists
        String keepPath = repoPath.endsWith("/") ? repoPath + ".keep" : repoPath + "/.keep";
        try {
            apiClient.upsertFile(keepPath, "", null,
                    properties.getCommitMessage() + " - create directory " + repoPath);
        } catch (GitApiException e) {
            // If the placeholder already exists (race), ignore
            if (e.getStatusCode() != GitStorageConstants.HTTP_CONFLICT) {
                throw e;
            }
        }
    }

    @Override
    public boolean exists(String resourceAbsolutePath) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        log.debug("exists {}", resourceAbsolutePath);
        String repoPath = toRepoRelativePath(resourceAbsolutePath);
        return apiClient.pathExists(repoPath);
    }

    @Override
    public void delete(String resourceAbsolutePath, boolean recursive) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        log.debug("delete {} recursive={}", resourceAbsolutePath, recursive);

        if (!exists(resourceAbsolutePath)) {
            log.debug("Path does not exist, skipping delete: {}", resourceAbsolutePath);
            return;
        }

        String repoPath = toRepoRelativePath(resourceAbsolutePath);
        ResourceMetadata meta = getResourceMetaData(resourceAbsolutePath);

        if (!meta.isDirectory()) {
            // Single file delete
            deleteSingleFile(repoPath, resourceAbsolutePath);
            return;
        }

        if (recursive) {
            // Delete all children first
            List<StorageEntity> children = listStorageEntity(resourceAbsolutePath);
            for (StorageEntity child : children) {
                delete(child.getFullName(), true);
            }
        }

        // Delete remaining files at this level (placeholders etc.)
        List<GitFileEntry> entries = apiClient.listContents(repoPath);
        for (GitFileEntry entry : entries) {
            if (entry.isDirectory()) {
                if (recursive) {
                    delete(toDsAbsolutePath(entry.getPath()), true);
                }
            } else {
                apiClient.deleteFile(entry.getPath(), entry.getSha(),
                        properties.getCommitMessage() + " - delete " + entry.getPath());
            }
        }
    }

    @SneakyThrows
    private void deleteSingleFile(String repoPath, String dsAbsolutePath) {
        Optional<GitFileEntry> entry = apiClient.getFile(repoPath);
        if (entry.isPresent()) {
            apiClient.deleteFile(repoPath, entry.get().getSha(),
                    properties.getCommitMessage() + " - delete " + repoPath);
        }
    }

    @Override
    public void copy(String srcAbsolutePath, String dstAbsolutePath, boolean deleteSource, boolean overwrite) {
        exceptionIfPathEmpty(srcAbsolutePath);
        exceptionIfPathEmpty(dstAbsolutePath);
        log.debug("copy {} -> {} deleteSource={} overwrite={}", srcAbsolutePath, dstAbsolutePath,
                deleteSource, overwrite);

        ResourceMetadata srcMeta = getResourceMetaData(srcAbsolutePath);

        if (srcMeta.isDirectory()) {
            copyDirectory(srcAbsolutePath, dstAbsolutePath, deleteSource, overwrite);
        } else {
            copyFile(srcAbsolutePath, dstAbsolutePath, deleteSource, overwrite);
        }
    }

    @SneakyThrows
    private void copyFile(String srcDsPath, String dstDsPath, boolean deleteSource, boolean overwrite) {
        String srcRepoPath = toRepoRelativePath(srcDsPath);
        String dstRepoPath = toRepoRelativePath(dstDsPath);

        Optional<GitFileEntry> dstEntry = apiClient.getFile(dstRepoPath);
        if (dstEntry.isPresent() && !overwrite) {
            throw new FileAlreadyExistsException(
                    "Destination file " + dstDsPath + " already exists and overwrite is not allowed");
        }

        // Read source content, write to destination
        String content = apiClient.getFileContent(srcRepoPath);

        // Ensure destination repo-relative parent directory exists
        ensureParentDirExists(dstDsPath);

        String existingSha = dstEntry.isPresent() ? dstEntry.get().getSha() : null;
        apiClient.upsertFile(dstRepoPath, content, existingSha,
                properties.getCommitMessage() + " - copy " + srcRepoPath + " -> " + dstRepoPath);

        if (deleteSource) {
            apiClient.deleteFile(srcRepoPath,
                    apiClient.getFile(srcRepoPath).map(GitFileEntry::getSha).orElse(null),
                    properties.getCommitMessage() + " - delete source " + srcRepoPath);
        }
    }

    private void copyDirectory(String srcDsPath, String dstDsPath, boolean deleteSource, boolean overwrite) {
        List<StorageEntity> children = listStorageEntity(srcDsPath);
        for (StorageEntity child : children) {
            String childDst = dstDsPath + File.separator + child.getFileName();
            if (child.isDirectory()) {
                copyDirectory(child.getFullName(), childDst, deleteSource, overwrite);
            } else {
                copyFile(child.getFullName(), childDst, deleteSource, overwrite);
            }
        }
        if (deleteSource) {
            delete(srcDsPath, true);
        }
    }

    private void ensureParentDirExists(String dsPath) {
        File file = new File(dsPath);
        File parent = file.getParentFile();
        if (parent == null) {
            return;
        }
        String parentDsPath = parent.getPath();
        if (!exists(parentDsPath)) {
            createStorageDir(parentDsPath);
        }
    }

    @SneakyThrows
    @Override
    public void upload(String srcLocalFileAbsolutePath, String dstAbsolutePath, boolean deleteSource,
                       boolean overwrite) {
        exceptionIfPathEmpty(srcLocalFileAbsolutePath);
        exceptionIfPathEmpty(dstAbsolutePath);
        log.debug("upload local={} -> git={} deleteSource={} overwrite={}", srcLocalFileAbsolutePath,
                dstAbsolutePath, deleteSource, overwrite);

        String dstRepoPath = toRepoRelativePath(dstAbsolutePath);

        // Check overwrite
        Optional<GitFileEntry> existing = apiClient.getFile(dstRepoPath);
        if (existing.isPresent() && !overwrite) {
            throw new FileAlreadyExistsException(
                    "Destination file " + dstAbsolutePath + " already exists and overwrite is not allowed");
        }

        // Ensure parent dir exists
        ensureParentDirExists(dstAbsolutePath);

        // Read local file content
        byte[] fileBytes = Files.readAllBytes(Paths.get(srcLocalFileAbsolutePath));
        String base64Content = Base64.getEncoder().encodeToString(fileBytes);

        String existingSha = existing.isPresent() ? existing.get().getSha() : null;
        apiClient.upsertFileBase64(dstRepoPath, base64Content, existingSha,
                properties.getCommitMessage() + " - upload " + dstRepoPath);

        if (deleteSource) {
            Files.delete(Paths.get(srcLocalFileAbsolutePath));
        }
    }

    @SneakyThrows
    @Override
    public void download(String srcFileAbsolutePath, String dstAbsoluteFile, boolean overwrite) {
        exceptionIfPathEmpty(srcFileAbsolutePath);
        exceptionIfPathEmpty(dstAbsoluteFile);
        log.debug("download git={} -> local={} overwrite={}", srcFileAbsolutePath, dstAbsoluteFile, overwrite);

        File dstFile = new File(dstAbsoluteFile);
        if (dstFile.exists() && !overwrite) {
            throw new FileAlreadyExistsException(
                    "Destination file " + dstAbsoluteFile + " already exists and overwrite is not allowed");
        }

        // Ensure parent directory exists
        File parentDir = dstFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            Files.createDirectories(parentDir.toPath());
        }

        String srcRepoPath = toRepoRelativePath(srcFileAbsolutePath);
        Optional<String> base64Opt = apiClient.getFileContentBase64(srcRepoPath);
        if (!base64Opt.isPresent()) {
            throw new IOException("File not found in Git storage: " + srcFileAbsolutePath);
        }

        byte[] decoded = Base64.getDecoder().decode(base64Opt.get());
        Files.write(dstFile.toPath(), decoded);
    }

    @SneakyThrows
    @Override
    public List<String> fetchFileContent(String fileAbsolutePath, int skipLineNums, int limit) {
        exceptionIfPathEmpty(fileAbsolutePath);
        log.debug("fetchFileContent {} skip={} limit={}", fileAbsolutePath, skipLineNums, limit);

        String repoPath = toRepoRelativePath(fileAbsolutePath);
        String content = apiClient.getFileContent(repoPath);

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new java.io.StringBufferInputStream(content), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(skipLineNums)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<StorageEntity> listStorageEntity(String resourceAbsolutePath) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        log.debug("listStorageEntity {}", resourceAbsolutePath);

        String repoPath = toRepoRelativePath(resourceAbsolutePath);

        if (exists(resourceAbsolutePath)) {
            ResourceMetadata meta = getResourceMetaData(resourceAbsolutePath);
            if (!meta.isDirectory()) {
                // It's a single file – return it as one entity
                GitFileEntry entry = apiClient.getFile(repoPath).orElse(null);
                if (entry == null) {
                    return Collections.emptyList();
                }
                return Collections.singletonList(convertToStorageEntity(entry));
            }
        }

        List<GitFileEntry> entries = apiClient.listContents(repoPath);
        List<StorageEntity> result = new ArrayList<>();
        for (GitFileEntry entry : entries) {
            // Skip placeholder files
            if (".keep".equals(entry.getName()) || ".gitkeep".equals(entry.getName())) {
                continue;
            }
            result.add(convertToStorageEntity(entry));
        }
        return result;
    }

    @Override
    public List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath) {
        return listStorageEntityRecursively(resourceAbsolutePath)
                .stream()
                .filter(e -> !e.isDirectory())
                .collect(Collectors.toList());
    }

    private List<StorageEntity> listStorageEntityRecursively(String resourceAbsolutePath) {
        Set<String> visited = new HashSet<>();
        List<StorageEntity> result = new ArrayList<>();
        LinkedList<String> queue = new LinkedList<>();
        queue.add(resourceAbsolutePath);

        while (!queue.isEmpty()) {
            String current = queue.pop();
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            List<StorageEntity> children = listStorageEntity(current);
            for (StorageEntity entity : children) {
                if (entity.isDirectory()) {
                    queue.add(entity.getFullName());
                }
            }
            result.addAll(children);
        }
        return result;
    }

    @Override
    public StorageEntity getStorageEntity(String resourceAbsolutePath) {
        exceptionIfPathEmpty(resourceAbsolutePath);
        log.debug("getStorageEntity {}", resourceAbsolutePath);

        String repoPath = toRepoRelativePath(resourceAbsolutePath);
        Optional<GitFileEntry> entry = apiClient.getFile(repoPath);
        if (!entry.isPresent()) {
            return null;
        }
        return convertToStorageEntity(entry.get());
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    private StorageEntity convertToStorageEntity(GitFileEntry entry) {
        String dsPath = toDsAbsolutePath(entry.getPath());
        ResourceMetadata meta = getResourceMetaData(dsPath);

        StorageEntity entity = new StorageEntity();
        entity.setFileName(entry.getName());
        entity.setFullName(dsPath);
        entity.setPfullName(new File(dsPath).getParent());
        entity.setDirectory(meta.isDirectory() || entry.isDirectory());
        entity.setType(meta.getResourceType());
        entity.setSize(entry.getSize());
        // Git API does not always return precise timestamps for contents listing;
        // we leave createTime/updateTime as null.
        entity.setCreateTime(null);
        entity.setUpdateTime(null);
        entity.setRelativePath(meta.getResourceRelativePath());
        return entity;
    }

    /**
     * Allows subclasses/tests to access the underlying API client.
     */
    GitApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Allows subclasses/tests to access the properties.
     */
    GitStorageProperties getProperties() {
        return properties;
    }
}

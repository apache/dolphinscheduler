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

package org.apache.dolphinscheduler.plugin.storage.s3;

import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_FILE;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_TYPE_UDF;

import org.apache.dolphinscheduler.authentication.aws.AmazonS3ClientFactory;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

@Slf4j
@Data
public class S3StorageOperator implements Closeable, StorageOperate {

    private String bucketName;

    private AmazonS3 s3Client;

    public S3StorageOperator() {
    }

    public void init() {
        bucketName = readBucketName();
        s3Client = buildS3Client();
        checkBucketNameExists(bucketName);
    }

    protected AmazonS3 buildS3Client() {
        return AmazonS3ClientFactory.createAmazonS3Client(PropertyUtils.getByPrefix("aws.s3.", ""));
    }

    protected String readBucketName() {
        return PropertyUtils.getString(Constants.AWS_S3_BUCKET_NAME);
    }

    @Override
    public void close() throws IOException {
        s3Client.shutdown();
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws Exception {
        mkdir(tenantCode, getS3ResDir(tenantCode));
        mkdir(tenantCode, getS3UdfDir(tenantCode));
    }

    @Override
    public String getResDir(String tenantCode) {
        return getS3ResDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return getS3UdfDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public boolean mkdir(String tenantCode, String path) throws IOException {
        String objectName = path + FOLDER_SEPARATOR;
        if (!s3Client.doesObjectExist(bucketName, objectName)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, emptyContent, metadata);
            s3Client.putObject(putObjectRequest);
        }
        return true;
    }

    @Override
    public String getResourceFullName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getS3ResDir(tenantCode), fileName);
    }

    @Override
    public String getFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return getDir(resourceType, tenantCode) + fileName;
    }

    @Override
    public void download(String srcFilePath, String dstFilePath,
                         boolean overwrite) throws IOException {
        File dstFile = new File(dstFilePath);
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            FileUtils.createDirectoryWith755(dstFile.getParentFile().toPath());
        }
        S3Object o = s3Client.getObject(bucketName, srcFilePath);
        try (
                S3ObjectInputStream s3is = o.getObjectContent();
                FileOutputStream fos = new FileOutputStream(dstFilePath)) {
            byte[] readBuf = new byte[1024];
            int readLen;
            while ((readLen = s3is.read(readBuf)) > 0) {
                fos.write(readBuf, 0, readLen);
            }
        } catch (AmazonServiceException e) {
            throw new IOException(e.getMessage());
        } catch (FileNotFoundException e) {
            log.error("the destination file {} not found", dstFilePath);
            throw e;
        }
    }

    @Override
    public boolean exists(String fullName) throws IOException {
        return s3Client.doesObjectExist(bucketName, fullName);
    }

    @Override
    public boolean delete(String fullName, boolean recursive) throws IOException {
        try {
            s3Client.deleteObject(bucketName, fullName);
            return true;
        } catch (AmazonServiceException e) {
            log.error("delete the object error,the resource path is {}", fullName);
            return false;
        }
    }

    @Override
    public boolean delete(String fullName, List<String> childrenPathList, boolean recursive) throws IOException {
        // append the resource fullName to the list for deletion.
        childrenPathList.add(fullName);

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                .withKeys(childrenPathList.stream().toArray(String[]::new));
        try {
            s3Client.deleteObjects(deleteObjectsRequest);
        } catch (AmazonServiceException e) {
            log.error("delete objects error", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        s3Client.copyObject(bucketName, srcPath, bucketName, dstPath);
        if (deleteSource) {
            s3Client.deleteObject(bucketName, srcPath);
        }
        return true;
    }

    @Override
    public String getDir(ResourceType resourceType, String tenantCode) {
        switch (resourceType) {
            case UDF:
                return getUdfDir(tenantCode);
            case FILE:
                return getResDir(tenantCode);
            case ALL:
                return getS3DataBasePath();
            default:
                return "";
        }

    }

    @Override
    public boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource,
                          boolean overwrite) throws IOException {
        try {
            s3Client.putObject(bucketName, dstPath, new File(srcFile));

            if (deleteSource) {
                Files.delete(Paths.get(srcFile));
            }
            return true;
        } catch (AmazonServiceException e) {
            log.error("upload failed,the bucketName is {},the filePath is {}", bucketName, dstPath);
            return false;
        }
    }

    @Override
    public List<String> vimFile(String tenantCode, String filePath, int skipLineNums, int limit) throws IOException {
        if (StringUtils.isBlank(filePath)) {
            log.error("file path:{} is blank", filePath);
            return Collections.emptyList();
        }
        S3Object s3Object = s3Client.getObject(bucketName, filePath);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
            Stream<String> stream = bufferedReader.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public void deleteTenant(String tenantCode) throws Exception {
        deleteTenantCode(tenantCode);
    }

    /**
     * S3 resource dir
     *
     * @param tenantCode tenant code
     * @return S3 resource dir
     */
    public String getS3ResDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_FILE, getS3TenantDir(tenantCode));
    }

    /**
     * S3 udf dir
     *
     * @param tenantCode tenant code
     * @return get udf dir on S3
     */
    public String getS3UdfDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_UDF, getS3TenantDir(tenantCode));
    }

    /**
     * @param tenantCode tenant code
     * @return file directory of tenants on S3
     */
    public String getS3TenantDir(String tenantCode) {
        return String.format(FORMAT_S_S, getS3DataBasePath(), tenantCode);
    }

    /**
     * get data S3 path
     *
     * @return data S3 path
     */
    public String getS3DataBasePath() {
        if (FOLDER_SEPARATOR.equals(RESOURCE_UPLOAD_PATH)) {
            return "";
        } else {
            return RESOURCE_UPLOAD_PATH.replaceFirst(FOLDER_SEPARATOR, "");
        }
    }

    protected void deleteTenantCode(String tenantCode) {
        deleteDir(getResDir(tenantCode));
        deleteDir(getUdfDir(tenantCode));
    }

    /**
     * xxx   untest
     * upload local directory to S3
     *
     * @param tenantCode
     * @param keyPrefix  the name of directory
     * @param strPath
     */
    private void uploadDirectory(String tenantCode, String keyPrefix, String strPath) {
        s3Client.putObject(bucketName, tenantCode + FOLDER_SEPARATOR + keyPrefix, new File(strPath));
    }

    /**
     * xxx untest
     * download S3 Directory to local
     *
     * @param tenantCode
     * @param keyPrefix  the name of directory
     * @param srcPath
     */
    private void downloadDirectory(String tenantCode, String keyPrefix, String srcPath) {
        TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        try {
            MultipleFileDownload download =
                    tm.downloadDirectory(bucketName, tenantCode + FOLDER_SEPARATOR + keyPrefix, new File(srcPath));
            download.waitForCompletion();
        } catch (AmazonS3Exception | InterruptedException e) {
            log.error("download the directory failed with the bucketName is {} and the keyPrefix is {}", bucketName,
                    tenantCode + FOLDER_SEPARATOR + keyPrefix);
            Thread.currentThread().interrupt();
        } finally {
            tm.shutdownNow();
        }
    }

    public void checkBucketNameExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("resource.aws.s3.bucket.name is blank");
        }

        boolean existsBucket = s3Client.doesBucketExistV2(bucketName);
        if (!existsBucket) {
            throw new IllegalArgumentException(
                    "bucketName: " + bucketName + " is not exists, you need to create them by yourself");
        }

        log.info("bucketName: {} has been found, the current regionName is {}", bucketName,
                s3Client.getRegionName());
    }

    /**
     * only delete the object of directory ,it`s better to delete the files in it -r
     */
    protected void deleteDir(String directoryName) {
        if (s3Client.doesObjectExist(bucketName, directoryName)) {
            s3Client.deleteObject(bucketName, directoryName);
        }
    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.S3;
    }

    @Override
    public List<StorageEntity> listFilesStatusRecursively(String path, String defaultPath, String tenantCode,
                                                          ResourceType type) {
        List<StorageEntity> storageEntityList = new ArrayList<>();
        LinkedList<StorageEntity> foldersToFetch = new LinkedList<>();

        StorageEntity initialEntity = null;
        try {
            initialEntity = getFileStatus(path, defaultPath, tenantCode, type);
        } catch (Exception e) {
            log.error("error while listing files status recursively, path: {}", path, e);
            return storageEntityList;
        }
        foldersToFetch.add(initialEntity);

        while (!foldersToFetch.isEmpty()) {
            String pathToExplore = foldersToFetch.pop().getFullName();
            try {
                List<StorageEntity> tempList = listFilesStatus(pathToExplore, defaultPath, tenantCode, type);
                for (StorageEntity temp : tempList) {
                    if (temp.isDirectory()) {
                        foldersToFetch.add(temp);
                    }
                }
                storageEntityList.addAll(tempList);
            } catch (Exception e) {
                log.error("error while listing files status recursively, path: {}", pathToExplore, e);
            }
        }

        return storageEntityList;

    }

    @Override
    public List<StorageEntity> listFilesStatus(String path, String defaultPath, String tenantCode,
                                               ResourceType type) throws AmazonServiceException {
        List<StorageEntity> storageEntityList = new ArrayList<>();

        // TODO: optimize pagination
        ListObjectsV2Request request = new ListObjectsV2Request();
        request.setBucketName(bucketName);
        request.setPrefix(path);
        request.setDelimiter("/");

        ListObjectsV2Result v2Result;
        do {
            try {
                v2Result = s3Client.listObjectsV2(request);
            } catch (AmazonServiceException e) {
                throw new AmazonServiceException("Get S3 file list exception, error type:" + e.getErrorType(), e);
            }

            List<S3ObjectSummary> summaries = v2Result.getObjectSummaries();

            for (S3ObjectSummary summary : summaries) {
                if (!summary.getKey().endsWith("/")) {
                    // the path is a file
                    String[] aliasArr = summary.getKey().split("/");
                    String alias = aliasArr[aliasArr.length - 1];
                    String fileName = StringUtils.difference(defaultPath, summary.getKey());

                    StorageEntity entity = new StorageEntity();
                    entity.setAlias(alias);
                    entity.setFileName(fileName);
                    entity.setFullName(summary.getKey());
                    entity.setDirectory(false);
                    entity.setUserName(tenantCode);
                    entity.setType(type);
                    entity.setSize(summary.getSize());
                    entity.setCreateTime(summary.getLastModified());
                    entity.setUpdateTime(summary.getLastModified());
                    entity.setPfullName(path);

                    storageEntityList.add(entity);
                }
            }

            for (String commonPrefix : v2Result.getCommonPrefixes()) {
                // the paths in commonPrefix are directories
                String suffix = StringUtils.difference(path, commonPrefix);
                String fileName = StringUtils.difference(defaultPath, commonPrefix);

                StorageEntity entity = new StorageEntity();
                entity.setAlias(suffix);
                entity.setFileName(fileName);
                entity.setFullName(commonPrefix);
                entity.setDirectory(true);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(0);
                entity.setCreateTime(null);
                entity.setUpdateTime(null);
                entity.setPfullName(path);

                storageEntityList.add(entity);
            }

            request.setContinuationToken(v2Result.getContinuationToken());

        } while (v2Result.isTruncated());

        return storageEntityList;
    }

    @Override
    public StorageEntity getFileStatus(String path, String defaultPath, String tenantCode,
                                       ResourceType type) throws AmazonServiceException, FileNotFoundException {
        // Notice: we do not use getObject here because intermediate directories
        // may not exist in S3, which can cause getObject to throw exception.
        // Since we still want to access it on frontend, this is a workaround using listObjects.

        ListObjectsV2Request request = new ListObjectsV2Request();
        request.setBucketName(bucketName);
        request.setPrefix(path);
        request.setDelimiter("/");

        ListObjectsV2Result v2Result;
        try {
            v2Result = s3Client.listObjectsV2(request);
        } catch (AmazonServiceException e) {
            throw new AmazonServiceException("Get S3 file list exception, error type:" + e.getErrorType(), e);
        }

        List<S3ObjectSummary> summaries = v2Result.getObjectSummaries();

        if (path.endsWith("/")) {
            // the path is a directory that may or may not exist in S3
            String alias = findDirAlias(path);
            String fileName = StringUtils.difference(defaultPath, path);

            StorageEntity entity = new StorageEntity();
            entity.setAlias(alias);
            entity.setFileName(fileName);
            entity.setFullName(path);
            entity.setDirectory(true);
            entity.setUserName(tenantCode);
            entity.setType(type);
            entity.setSize(0);

            return entity;

        } else {
            // the path is a file
            if (summaries.size() > 0) {
                S3ObjectSummary summary = summaries.get(0);
                String[] aliasArr = summary.getKey().split("/");
                String alias = aliasArr[aliasArr.length - 1];
                String fileName = StringUtils.difference(defaultPath, summary.getKey());

                StorageEntity entity = new StorageEntity();
                entity.setAlias(alias);
                entity.setFileName(fileName);
                entity.setFullName(summary.getKey());
                entity.setDirectory(false);
                entity.setUserName(tenantCode);
                entity.setType(type);
                entity.setSize(summary.getSize());
                entity.setCreateTime(summary.getLastModified());
                entity.setUpdateTime(summary.getLastModified());

                return entity;
            }
        }

        throw new FileNotFoundException("Object is not found in S3 Bucket: " + bucketName);
    }

    /**
     * find alias for directories, NOT for files
     * a directory is a path ending with "/"
     */
    private String findDirAlias(String myStr) {
        if (!myStr.endsWith(FOLDER_SEPARATOR)) {
            // Make sure system won't crush down if someone accidentally misuse the function.
            return myStr;
        }

        Path path = Paths.get(myStr);
        return path.getName(path.getNameCount() - 1) + FOLDER_SEPARATOR;
    }
}

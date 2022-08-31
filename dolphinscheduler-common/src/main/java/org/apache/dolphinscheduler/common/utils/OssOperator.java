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

import static org.apache.dolphinscheduler.common.Constants.ALIBABA_CLOUD_OSS_END_POINT;
import static org.apache.dolphinscheduler.common.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.Constants.RESOURCE_STORAGE_TYPE;
import static org.apache.dolphinscheduler.common.Constants.RESOURCE_TYPE_FILE;
import static org.apache.dolphinscheduler.common.Constants.RESOURCE_TYPE_UDF;
import static org.apache.dolphinscheduler.common.Constants.STORAGE_OSS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;

public class OssOperator implements Closeable, StorageOperate {

    private static final Logger logger = LoggerFactory.getLogger(OssOperator.class);

    public static final String ACCESS_KEY_ID = PropertyUtils.getString(TaskConstants.ALIBABA_CLOUD_ACCESS_KEY_ID);

    public static final String ACCESS_KEY_SECRET =
            PropertyUtils.getString(TaskConstants.ALIBABA_CLOUD_ACCESS_KEY_SECRET);

    public static final String REGION = PropertyUtils.getString(TaskConstants.ALIBABA_CLOUD_REGION);

    public static final String BUCKET_NAME = PropertyUtils.getString(Constants.ALIBABA_CLOUD_OSS_BUCKET_NAME);

    private OSS ossClient = null;

    private OssOperator() {
        if (PropertyUtils.getString(RESOURCE_STORAGE_TYPE).equals(STORAGE_OSS)) {

            if (!StringUtils.isEmpty(PropertyUtils.getString(ALIBABA_CLOUD_OSS_END_POINT))) {
                ossClient = new OSSClientBuilder().build(PropertyUtils.getString(ALIBABA_CLOUD_OSS_END_POINT),
                        ACCESS_KEY_ID, ACCESS_KEY_SECRET);
            } else {
                ossClient = new OSSClientBuilder().build(PropertyUtils.getString(ALIBABA_CLOUD_OSS_END_POINT),
                        ACCESS_KEY_ID, ACCESS_KEY_SECRET);
            }
            checkBucketNameExists(BUCKET_NAME);
        }
    }

    /**
     * S3Utils single
     */
    private enum OssOperatorSingleton {

        INSTANCE;

        private final OssOperator instance;

        OssOperatorSingleton() {
            instance = new OssOperator();
        }

        private OssOperator getInstance() {
            return instance;
        }
    }

    public static OssOperator getInstance() {
        return OssOperatorSingleton.INSTANCE.getInstance();
    }

    @Override
    public void close() throws IOException {
        ossClient.shutdown();
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws Exception {
        getInstance().mkdir(tenantCode, getOssResDir(tenantCode));
        getInstance().mkdir(tenantCode, getOssUdfDir(tenantCode));
    }

    @Override
    public String getResDir(String tenantCode) {
        return getOssResDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return getOssUdfDir(tenantCode) + FOLDER_SEPARATOR;
    }

    @Override
    public boolean mkdir(String tenantCode, String path) throws IOException {
        String objectName = path + FOLDER_SEPARATOR;
        if (!ossClient.doesObjectExist(BUCKET_NAME, objectName)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, emptyContent, metadata);
            ossClient.putObject(putObjectRequest);
        }
        return true;
    }

    @Override
    public String getResourceFileName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, getOssResDir(tenantCode), fileName);
    }

    @Override
    public String getFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return getDir(resourceType, tenantCode) + fileName;
    }

    @Override
    public void download(String tenantCode, String srcFilePath, String dstFilePath, boolean deleteSource,
                         boolean overwrite) throws IOException {
        File dstFile = new File(dstFilePath);
        if (dstFile.isDirectory()) {
            Files.delete(dstFile.toPath());
        } else {
            Files.createDirectories(dstFile.getParentFile().toPath());
        }
        OSSObject ossObject = ossClient.getObject(BUCKET_NAME, srcFilePath);
        try (
                InputStream ossInputStream = ossObject.getObjectContent();
                FileOutputStream fos = new FileOutputStream(dstFilePath)) {
            byte[] readBuf = new byte[1024];
            int readLen;
            while ((readLen = ossInputStream.read(readBuf)) > 0) {
                fos.write(readBuf, 0, readLen);
            }
        } catch (OSSException e) {
            throw new IOException(e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("the destination file {} not found", dstFilePath);
            throw e;
        }
    }

    @Override
    public boolean exists(String tenantCode, String fileName) throws IOException {
        return ossClient.doesObjectExist(BUCKET_NAME, fileName);
    }

    @Override
    public boolean delete(String tenantCode, String filePath, boolean recursive) throws IOException {
        try {
            ossClient.deleteObject(BUCKET_NAME, filePath);
            return true;
        } catch (OSSException e) {
            logger.error("delete the object error,the resource path is {}", filePath);
            return false;
        }
    }

    @Override
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        ossClient.copyObject(BUCKET_NAME, srcPath, BUCKET_NAME, dstPath);
        ossClient.deleteObject(BUCKET_NAME, srcPath);
        return true;
    }

    @Override
    public String getDir(ResourceType resourceType, String tenantCode) {
        switch (resourceType) {
            case UDF:
                return getUdfDir(tenantCode);
            case FILE:
                return getResDir(tenantCode);
            default:
                return "";
        }

    }

    @Override
    public boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource,
                          boolean overwrite) throws IOException {
        try {
            ossClient.putObject(BUCKET_NAME, dstPath, new File(srcFile));
            return true;
        } catch (OSSException e) {
            logger.error("upload failed,the bucketName is {},the filePath is {}", BUCKET_NAME, dstPath);
            return false;
        }
    }

    @Override
    public List<String> vimFile(String tenantCode, String filePath, int skipLineNums, int limit) throws IOException {
        if (StringUtils.isBlank(filePath)) {
            logger.error("file path:{} is blank", filePath);
            return Collections.emptyList();
        }
        OSSObject s3Object = ossClient.getObject(BUCKET_NAME, filePath);
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
    public static String getOssResDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_FILE, getOssTenantDir(tenantCode));
    }

    /**
     * S3 udf dir
     *
     * @param tenantCode tenant code
     * @return get udf dir on S3
     */
    public static String getOssUdfDir(String tenantCode) {
        return String.format("%s/" + RESOURCE_TYPE_UDF, getOssTenantDir(tenantCode));
    }

    /**
     * @param tenantCode tenant code
     * @return file directory of tenants on S3
     */
    public static String getOssTenantDir(String tenantCode) {
        return String.format(FORMAT_S_S, getOssDataBasePath(), tenantCode);
    }

    /**
     * get data S3 path
     *
     * @return data S3 path
     */
    public static String getOssDataBasePath() {
        if (FOLDER_SEPARATOR.equals(RESOURCE_UPLOAD_PATH)) {
            return "";
        } else {
            return RESOURCE_UPLOAD_PATH.replaceFirst(FOLDER_SEPARATOR, "");
        }
    }

    private void deleteTenantCode(String tenantCode) {
        deleteDirectory(getResDir(tenantCode));
        deleteDirectory(getUdfDir(tenantCode));
    }

    public void checkBucketNameExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("resource.aws.s3.bucket.name is blank");
        }

        Bucket existsBucket = ossClient.listBuckets()
                .stream()
                .filter(
                        bucket -> bucket.getName().equals(bucketName))
                .findFirst()
                .orElseThrow(() -> {
                    return new IllegalArgumentException(
                            "bucketName: " + bucketName + " is not exists, you need to create them by yourself");
                });

        logger.info("bucketName: {} has been found, the current regionName is {}", existsBucket.getName(), REGION);
    }

    private void deleteDirectory(String directoryName) {
        if (ossClient.doesObjectExist(BUCKET_NAME, directoryName)) {
            ossClient.deleteObject(BUCKET_NAME, directoryName);
        }
    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.OSS;
    }
}

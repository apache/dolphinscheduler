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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.jets3t.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.dolphinscheduler.common.Constants.*;

@Component
public class S3Utils implements Closeable, StorageOperate {

    private static final Logger logger = LoggerFactory.getLogger(S3Utils.class);

    public static final String ACCESS_KEY_ID = PropertyUtils.getString(Constants.AWS_ACCESS_KEY_ID);

    public static final String SECRET_KEY_ID = PropertyUtils.getString(Constants.AWS_SECRET_ACCESS_KEY);

    public static final String REGION = PropertyUtils.getString(Constants.AWS_REGION);


    private AmazonS3 s3Client = null;

    private S3Utils() {
        if (PropertyUtils.getString(RESOURCE_STORAGE_TYPE).equals(STORAGE_S3)) {

            if (!StringUtils.isEmpty(PropertyUtils.getString(AWS_END_POINT))) {
                s3Client = AmazonS3ClientBuilder
                        .standard()
                        .withPathStyleAccessEnabled(true)
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(PropertyUtils.getString(AWS_END_POINT), Regions.fromName(REGION).getName()))
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY_ID)))
                        .build();
            } else {
                s3Client = AmazonS3ClientBuilder
                        .standard()
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY_ID)))
                        .withRegion(Regions.fromName(REGION))
                        .build();
            }
            checkBucketNameIfNotPresent(BUCKET_NAME);
        }
    }

    /**
     * S3Utils single
     */
    private enum S3Singleton {
        INSTANCE;

        private final S3Utils instance;

        S3Singleton() {
            instance = new S3Utils();
        }

        private S3Utils getInstance() {
            return instance;
        }
    }

    public static S3Utils getInstance() {
        return S3Singleton.INSTANCE.getInstance();
    }

    @Override
    public void close() throws IOException {
        s3Client.shutdown();
    }

    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws ServiceException {
        createFolder(tenantCode+ FOLDER_SEPARATOR +RESOURCE_TYPE_UDF);
        createFolder(tenantCode+ FOLDER_SEPARATOR +RESOURCE_TYPE_FILE);
    }

    @Override
    public String getResDir(String tenantCode) {
        return tenantCode+ FOLDER_SEPARATOR +RESOURCE_TYPE_FILE+FOLDER_SEPARATOR;
    }

    @Override
    public String getUdfDir(String tenantCode) {
        return tenantCode+ FOLDER_SEPARATOR +RESOURCE_TYPE_UDF+FOLDER_SEPARATOR;
    }

    @Override
    public boolean mkdir(String tenantCode, String path) throws IOException {
         createFolder(path);
         return true;
    }

    @Override
    public String getResourceFileName(String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return String.format(FORMAT_S_S, tenantCode+FOLDER_SEPARATOR+RESOURCE_TYPE_FILE, fileName);
    }
    @Override
    public String getFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.replaceFirst(FOLDER_SEPARATOR, "");
        }
        return getDir(resourceType, tenantCode)+fileName;
    }

    @Override
    public void download(String tenantCode, String srcFilePath, String dstFile, boolean deleteSource, boolean overwrite) throws IOException {
        S3Object o = s3Client.getObject(BUCKET_NAME, srcFilePath);
        try (S3ObjectInputStream s3is = o.getObjectContent();
             FileOutputStream fos = new FileOutputStream(new File(dstFile))) {
            byte[] readBuf = new byte[1024];
            int readLen = 0;
            while ((readLen = s3is.read(readBuf)) > 0) {
                fos.write(readBuf, 0, readLen);
            }
        } catch (AmazonServiceException e) {
            logger.error("the resource can`t be downloaded,the bucket is {},and the src is {}", tenantCode, srcFilePath);
            throw new IOException(e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("the file isn`t exists");
            throw new IOException("the file isn`t exists");
        }
    }

    @Override
    public boolean exists(String tenantCode, String fileName) throws IOException {
        return s3Client.doesObjectExist(BUCKET_NAME, fileName);
    }

    @Override
    public boolean delete(String tenantCode, String filePath, boolean recursive) throws IOException {
        try {
            s3Client.deleteObject(BUCKET_NAME, filePath);
            return true;
        } catch (AmazonServiceException e) {
            logger.error("delete the object error,the resource path is {}", filePath);
            return false;
        }
    }

    @Override
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        s3Client.copyObject(BUCKET_NAME, srcPath, BUCKET_NAME, dstPath);
        s3Client.deleteObject(BUCKET_NAME, srcPath);
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
                return tenantCode+ FOLDER_SEPARATOR ;
        }

    }

    @Override
    public boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        try {
            s3Client.putObject(BUCKET_NAME, dstPath, new File(srcFile));
            return true;
        } catch (AmazonServiceException e) {
            logger.error("upload failed,the bucketName is {},the dstPath is {}", BUCKET_NAME, tenantCode+ FOLDER_SEPARATOR +dstPath);
            return false;
        }
    }


    @Override
    public List<String> vimFile(String tenantCode,String filePath, int skipLineNums, int limit) throws IOException {
        if (StringUtils.isBlank(filePath)) {
            logger.error("file path:{} is blank", filePath);
            return Collections.emptyList();
        }
            S3Object s3Object=s3Client.getObject(BUCKET_NAME,filePath);
            try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))){
                Stream<String> stream = bufferedReader.lines().skip(skipLineNums).limit(limit);
                return stream.collect(Collectors.toList());
            }
    }

    private void
    createFolder( String folderName) {
        if (!s3Client.doesObjectExist(BUCKET_NAME, folderName + FOLDER_SEPARATOR)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, folderName + FOLDER_SEPARATOR, emptyContent, metadata);
            s3Client.putObject(putObjectRequest);
        }
    }

    @Override
    public void deleteTenant(String tenantCode) throws Exception {
        deleteTenantCode(tenantCode);
    }

    private void deleteTenantCode(String tenantCode) {
        deleteDirectory(getResDir(tenantCode));
        deleteDirectory(getUdfDir(tenantCode));
    }

    /**
     * xxx   untest
     * upload local directory to S3
     * @param tenantCode
     * @param keyPrefix the name of directory
     * @param strPath
     */
    private void uploadDirectory(String tenantCode, String keyPrefix, String strPath) {
        s3Client.putObject(BUCKET_NAME, tenantCode+ FOLDER_SEPARATOR +keyPrefix, new File(strPath));
    }


    /**
     * xxx untest
     * download S3 Directory to local
     * @param tenantCode
     * @param keyPrefix the name of directory
     * @param srcPath
     */
    private void downloadDirectory(String  tenantCode, String keyPrefix, String srcPath){
        TransferManager  tm= TransferManagerBuilder.standard().withS3Client(s3Client).build();
        try{
            MultipleFileDownload download = tm.downloadDirectory(BUCKET_NAME, tenantCode + FOLDER_SEPARATOR + keyPrefix, new File(srcPath));
            download.waitForCompletion();
        } catch (AmazonS3Exception | InterruptedException e) {
            logger.error("download the directory failed with the bucketName is {} and the keyPrefix is {}", BUCKET_NAME, tenantCode + FOLDER_SEPARATOR + keyPrefix);
            Thread.currentThread().interrupt();
        } finally {
            tm.shutdownNow();
        }
    }

    public void checkBucketNameIfNotPresent(String bucketName) {
        if (!s3Client.doesBucketExistV2(bucketName)) {
            logger.info("the current regionName is {}", s3Client.getRegionName());
            s3Client.createBucket(bucketName);
        }
    }

    /*
    only delete the object of directory ,it`s better to delete the files in it -r
     */
    private void deleteDirectory(String directoryName) {
        if (s3Client.doesObjectExist(BUCKET_NAME, directoryName)) {
            s3Client.deleteObject(BUCKET_NAME, directoryName);
        }
    }

    @Override
    public ResUploadType returnStorageType() {
        return ResUploadType.S3;
    }
}
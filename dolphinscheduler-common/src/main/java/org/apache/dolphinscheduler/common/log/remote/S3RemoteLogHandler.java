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

package org.apache.dolphinscheduler.common.log.remote;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

@Slf4j
public class S3RemoteLogHandler implements RemoteLogHandler, Closeable {

    private String accessKeyId;

    private String accessKeySecret;

    private String region;

    private String bucketName;

    private String endPoint;

    private AmazonS3 s3Client;

    private static S3RemoteLogHandler instance;

    private S3RemoteLogHandler() {

    }

    public static synchronized S3RemoteLogHandler getInstance() {
        if (instance == null) {
            instance = new S3RemoteLogHandler();
            instance.init();
        }

        return instance;
    }

    public void init() {
        accessKeyId = readAccessKeyID();
        accessKeySecret = readAccessKeySecret();
        region = readRegion();
        bucketName = readBucketName();
        endPoint = readEndPoint();
        s3Client = buildS3Client();
        checkBucketNameExists(bucketName);
    }

    protected AmazonS3 buildS3Client() {
        if (StringUtils.isNotEmpty(endPoint)) {
            return AmazonS3ClientBuilder
                    .standard()
                    .withPathStyleAccessEnabled(true)
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                            endPoint, Regions.fromName(region).getName()))
                    .withCredentials(
                            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, accessKeySecret)))
                    .build();
        } else {
            return AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(
                            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, accessKeySecret)))
                    .withRegion(Regions.fromName(region))
                    .build();
        }
    }

    @Override
    public void close() throws IOException {
        s3Client.shutdown();
    }

    @Override
    public void sendRemoteLog(String logPath) {
        String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);

        try {
            log.info("send remote log {} to S3 {}", logPath, objectName);
            s3Client.putObject(bucketName, objectName, new File(logPath));
        } catch (Exception e) {
            log.error("error while sending remote log {} to S3 {}", logPath, objectName, e);
        }
    }

    @Override
    public void getRemoteLog(String logPath) {
        String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);

        try {
            log.info("get remote log on S3 {} to {}", objectName, logPath);
            S3Object o = s3Client.getObject(bucketName, objectName);
            try (
                    S3ObjectInputStream s3is = o.getObjectContent();
                    FileOutputStream fos = new FileOutputStream(logPath)) {
                byte[] readBuf = new byte[1024];
                int readLen = 0;
                while ((readLen = s3is.read(readBuf)) > 0) {
                    fos.write(readBuf, 0, readLen);
                }
            }
        } catch (Exception e) {
            log.error("error while getting remote log on S3 {} to {}", objectName, logPath, e);
        }
    }

    protected String readAccessKeyID() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_S3_ACCESS_KEY_ID);
    }

    protected String readAccessKeySecret() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_S3_ACCESS_KEY_SECRET);
    }

    protected String readRegion() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_S3_REGION);
    }

    protected String readBucketName() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_S3_BUCKET_NAME);
    }

    protected String readEndPoint() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_S3_ENDPOINT);
    }

    public void checkBucketNameExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("remote.logging.s3.bucket.name is blank");
        }

        boolean existsBucket = s3Client.doesBucketExistV2(bucketName);
        if (!existsBucket) {
            throw new IllegalArgumentException(
                    "bucketName: " + bucketName + " is not exists, you need to create them by yourself");
        }

        log.info("bucketName: {} has been found, the current regionName is {}", bucketName,
                s3Client.getRegionName());
    }
}

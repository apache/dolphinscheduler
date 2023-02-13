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
import org.apache.dolphinscheduler.common.factory.OssClientFactory;
import org.apache.dolphinscheduler.common.model.OssConnection;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.PutObjectRequest;

@Slf4j
public class OssRemoteLogHandler implements RemoteLogHandler, Closeable {

    private static final int OBJECT_NAME_COUNT = 2;

    private OSS ossClient;

    private String bucketName;

    public OssRemoteLogHandler() {
    }

    public void init() {
        String accessKeyId = readOssAccessKeyId();
        String accessKeySecret = readOssAccessKeySecret();
        String endpoint = readOssEndpoint();
        ossClient = OssClientFactory.buildOssClient(new OssConnection(accessKeyId, accessKeySecret, endpoint));

        bucketName = readOssBucketName();
        checkBucketNameExists(bucketName);
    }

    @Override
    public void sendRemoteLog(String logPath) {
        String objectName = getObjectNameFromLogPath(logPath);

        try {
            log.info("send remote log {} to OSS {}", logPath, objectName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(logPath));
            ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            log.error("error while sending remote log {} to OSS {}", logPath, objectName, e);
        }
    }

    @Override
    public void getRemoteLog(String logPath) {
        String objectName = getObjectNameFromLogPath(logPath);

        try {
            log.info("get remote log on OSS {} to {}", objectName, logPath);
            ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(logPath));
        } catch (Exception e) {
            log.error("error while getting remote log on OSS {} to {}", objectName, logPath, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    private String getObjectNameFromLogPath(String logPath) {
        Path path = Paths.get(logPath);
        int nameCount = path.getNameCount();

        if (nameCount < OBJECT_NAME_COUNT) {
            return Paths.get(readOssBaseDir(), logPath).toString();
        } else {
            return Paths.get(readOssBaseDir(), path.subpath(nameCount - OBJECT_NAME_COUNT, nameCount).toString())
                    .toString();
        }
    }

    private void checkBucketNameExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException(Constants.REMOTE_LOGGING_OSS_BUCKET_NAME + " is empty");
        }

        Bucket existsBucket = ossClient.listBuckets()
                .stream()
                .filter(
                        bucket -> bucket.getName().equals(bucketName))
                .findFirst()
                .orElseThrow(() -> {
                    return new IllegalArgumentException(
                            "bucketName: " + bucketName + " does not exist, you need to create them by yourself");
                });

        log.info("bucketName: {} has been found", existsBucket.getName());
    }

    private String readOssAccessKeyId() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_OSS_ACCESS_KEY_ID);
    }

    private String readOssAccessKeySecret() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_OSS_ACCESS_KEY_SECRET);
    }

    private String readOssEndpoint() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_OSS_ENDPOINT);
    }

    private String readOssBucketName() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_OSS_BUCKET_NAME);
    }

    private String readOssBaseDir() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_BASE_DIR);
    }
}

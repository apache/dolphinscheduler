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
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;

@Slf4j
public class CosRemoteLogHandler implements RemoteLogHandler, Closeable {

    private final COSClient cosClient;

    private final String bucketName;

    private static CosRemoteLogHandler instance;

    private CosRemoteLogHandler() {
        String secretId = PropertyUtils.getString(Constants.REMOTE_LOGGING_COS_ACCESS_KEY_ID);
        String secretKey = PropertyUtils.getString(Constants.REMOTE_LOGGING_COS_ACCESS_KEY_SECRET);
        COSCredentials cosCredentials = new BasicCOSCredentials(secretId, secretKey);
        String regionName = PropertyUtils.getString(Constants.REMOTE_LOGGING_COS_REGION);
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        this.cosClient = new COSClient(cosCredentials, clientConfig);
        this.bucketName = PropertyUtils.getString(Constants.REMOTE_LOGGING_COS_BUCKET_NAME);
        checkBucketNameExists(this.bucketName);
    }

    public static synchronized CosRemoteLogHandler getInstance() {
        if (instance == null) {
            instance = new CosRemoteLogHandler();
        }
        return instance;
    }

    @Override
    public void sendRemoteLog(String logPath) {
        String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);

        try {
            log.info("send remote log from {} to tencent cos {}", logPath, objectName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(logPath));
            cosClient.putObject(putObjectRequest);
        } catch (Exception e) {
            log.error("error while sending remote log from {} to tencent cos {}, reason:", logPath, objectName, e);
        }
    }

    @Override
    public void getRemoteLog(String logPath) {
        String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);

        try {
            log.info("get remote log from tencent cos {} to {}", objectName, logPath);
            cosClient.getObject(new GetObjectRequest(bucketName, objectName), new File(logPath));
        } catch (Exception e) {
            log.error("error while sending remote log from {} to tencent cos {}, reason:", objectName, logPath, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }

    private void checkBucketNameExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException(Constants.REMOTE_LOGGING_COS_BUCKET_NAME + " is empty");
        }
        boolean existsBucket = cosClient.doesBucketExist(bucketName);
        if (!existsBucket) {
            throw new IllegalArgumentException(
                    "bucketName: " + bucketName + " does not exists, you should create it first");
        }

        log.debug("tencent cos bucket {} has been found for remote logging", bucketName);
    }
}

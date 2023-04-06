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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Slf4j
public class GcsRemoteLogHandler implements RemoteLogHandler, Closeable {

    private Storage gcsStorage;

    private String bucketName;

    private String credential;

    private static GcsRemoteLogHandler instance;

    private GcsRemoteLogHandler() {

    }

    public static synchronized GcsRemoteLogHandler getInstance() {
        if (instance == null) {
            instance = new GcsRemoteLogHandler();
            instance.init();
        }

        return instance;
    }

    public void init() {
        try {
            credential = readCredentials();
            bucketName = readBucketName();
            gcsStorage = buildGcsStorage(credential);

            checkBucketNameExists(bucketName);
        } catch (IOException e) {
            log.error("GCS Remote Log Handler init failed", e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (gcsStorage != null) {
                gcsStorage.close();
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void sendRemoteLog(String logPath) {
        String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);

        try {
            log.info("send remote log {} to GCS {}", logPath, objectName);

            BlobInfo blobInfo = BlobInfo.newBuilder(
                    BlobId.of(bucketName, objectName)).build();

            gcsStorage.create(blobInfo, Files.readAllBytes(Paths.get(logPath)));
        } catch (Exception e) {
            log.error("error while sending remote log {} to GCS {}", logPath, objectName, e);
        }
    }

    @Override
    public void getRemoteLog(String logPath) {
        String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);

        try {
            log.info("get remote log on GCS {} to {}", objectName, logPath);

            Blob blob = gcsStorage.get(BlobId.of(bucketName, objectName));
            blob.downloadTo(Paths.get(logPath));
        } catch (Exception e) {
            log.error("error while getting remote log on GCS {} to {}", objectName, logPath, e);
        }
    }

    protected Storage buildGcsStorage(String credential) throws IOException {
        return StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                        Files.newInputStream(Paths.get(credential))))
                .build()
                .getService();
    }

    protected String readCredentials() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_GCS_CREDENTIAL);
    }

    protected String readBucketName() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_GCS_BUCKET_NAME);
    }

    public void checkBucketNameExists(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException(Constants.REMOTE_LOGGING_GCS_BUCKET_NAME + " is blank");
        }

        boolean exist = false;
        for (Bucket bucket : gcsStorage.list().iterateAll()) {
            if (bucketName.equals(bucket.getName())) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            log.error(
                    "bucketName: {} does not exist, you need to create them by yourself", bucketName);
        } else {
            log.info("bucketName: {} has been found", bucketName);
        }
    }
}

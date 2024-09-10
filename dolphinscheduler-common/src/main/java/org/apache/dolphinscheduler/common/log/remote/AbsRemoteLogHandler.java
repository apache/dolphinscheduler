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
import java.io.FileOutputStream;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.common.StorageSharedKeyCredential;

@Slf4j
public class AbsRemoteLogHandler implements RemoteLogHandler, Closeable {

    private String accountName;

    private String accountKey;

    private String containerName;

    private BlobContainerClient blobContainerClient;

    private static AbsRemoteLogHandler instance;

    private AbsRemoteLogHandler() {
        accountName = readAccountName();
        accountKey = readAccountKey();
        containerName = readContainerName();
        blobContainerClient = buildBlobContainerClient();
    }

    public static synchronized AbsRemoteLogHandler getInstance() {
        if (instance == null) {
            instance = new AbsRemoteLogHandler();
        }

        return instance;
    }

    protected BlobContainerClient buildBlobContainerClient() {

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net/", accountName))
                .credential(new StorageSharedKeyCredential(accountName, accountKey))
                .buildClient();

        if (StringUtils.isBlank(containerName)) {
            throw new IllegalArgumentException("remote.logging.abs.container.name is blank");
        }

        try {
            this.blobContainerClient = serviceClient.getBlobContainerClient(containerName);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "containerName: " + containerName + " is not exists, you need to create them by yourself");
        }

        log.info("containerName: {} has been found.", containerName);

        return blobContainerClient;
    }

    @Override
    public void close() throws IOException {
        // no need to close blobContainerClient
    }

    @Override
    public void sendRemoteLog(String logPath) {
        String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);

        try {
            log.info("send remote log {} to Azure Blob {}", logPath, objectName);
            blobContainerClient.getBlobClient(objectName).uploadFromFile(logPath);
        } catch (Exception e) {
            log.error("error while sending remote log {} to Azure Blob {}", logPath, objectName, e);
        }
    }

    @Override
    public void getRemoteLog(String logPath) {
        String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);

        try {
            log.info("get remote log on Azure Blob {} to {}", objectName, logPath);

            try (
                    BlobInputStream bis = blobContainerClient.getBlobClient(objectName).openInputStream();
                    FileOutputStream fos = new FileOutputStream(logPath)) {
                byte[] readBuf = new byte[1024];
                int readLen = 0;
                while ((readLen = bis.read(readBuf)) > 0) {
                    fos.write(readBuf, 0, readLen);
                }
            }
        } catch (Exception e) {
            log.error("error while getting remote log on Azure Blob {} to {}", objectName, logPath, e);
        }
    }

    protected String readAccountName() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_ACCOUNT_NAME);
    }

    protected String readAccountKey() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_ACCOUNT_KEY);
    }

    protected String readContainerName() {
        return PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_CONTAINER_NAME);
    }
}

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AbsRemoteLogHandlerTest {

    @Mock
    BlobServiceClient blobServiceClient;

    @Mock
    BlobContainerClient blobContainerClient;

    @Mock
    BlobClient blobClient;

    @Test
    public void testAbsRemoteLogHandlerContainerNameBlack() {
        try (
                MockedStatic<PropertyUtils> propertyUtilsMockedStatic = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<LogUtils> remoteLogUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_ACCOUNT_NAME))
                    .thenReturn("account_name");
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_ACCOUNT_KEY))
                    .thenReturn("account_key");
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_CONTAINER_NAME))
                    .thenReturn("");
            remoteLogUtilsMockedStatic.when(LogUtils::getLocalLogBaseDir).thenReturn("logs");

            IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                AbsRemoteLogHandler.getInstance();
            });
            Assertions.assertEquals("remote.logging.abs.container.name is blank", thrown.getMessage());
        }
    }

    @Test
    public void testAbsRemoteLogHandlerContainerNotExists() {
        try (
                MockedStatic<PropertyUtils> propertyUtilsMockedStatic = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<LogUtils> remoteLogUtilsMockedStatic = Mockito.mockStatic(LogUtils.class);
                MockedConstruction<BlobServiceClientBuilder> k8sClientWrapperMockedConstruction =
                        Mockito.mockConstruction(BlobServiceClientBuilder.class, (mock, context) -> {
                            when(mock.endpoint(any(String.class))).thenReturn(mock);
                            when(mock.credential(any(StorageSharedKeyCredential.class))).thenReturn(mock);
                            when(mock.buildClient())
                                    .thenReturn(blobServiceClient);
                        })) {
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_ACCOUNT_NAME))
                    .thenReturn("account_name");
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_ACCOUNT_KEY))
                    .thenReturn("account_key");
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_CONTAINER_NAME))
                    .thenReturn("container_name");
            remoteLogUtilsMockedStatic.when(LogUtils::getLocalLogBaseDir).thenReturn("logs");

            when(blobServiceClient.getBlobContainerClient(any(String.class))).thenThrow(
                    new NullPointerException("container not exists"));
            IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                AbsRemoteLogHandler.getInstance();
            });
            Assertions.assertEquals("containerName: container_name is not exists, you need to create them by yourself",
                    thrown.getMessage());
        }
    }

    @Test
    public void testAbsRemoteLogHandler() {

        try (
                MockedStatic<PropertyUtils> propertyUtilsMockedStatic = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<LogUtils> remoteLogUtilsMockedStatic = Mockito.mockStatic(LogUtils.class);
                MockedConstruction<BlobServiceClientBuilder> blobServiceClientBuilderMockedConstruction =
                        Mockito.mockConstruction(BlobServiceClientBuilder.class, (mock, context) -> {
                            when(mock.endpoint(any(String.class))).thenReturn(mock);
                            when(mock.credential(any(StorageSharedKeyCredential.class))).thenReturn(mock);
                            when(mock.buildClient())
                                    .thenReturn(blobServiceClient);
                        });
                MockedStatic<RemoteLogUtils> remoteLogUtilsMockedStatic1 = Mockito.mockStatic(RemoteLogUtils.class)) {
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_ACCOUNT_NAME))
                    .thenReturn("account_name");
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_ACCOUNT_KEY))
                    .thenReturn("account_key");
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_ABS_CONTAINER_NAME))
                    .thenReturn("container_name");
            remoteLogUtilsMockedStatic.when(LogUtils::getLocalLogBaseDir).thenReturn("logs");
            String logPath = "logpath";
            String objectName = "objectname";
            remoteLogUtilsMockedStatic1.when(() -> RemoteLogUtils.getObjectNameFromLogPath(logPath))
                    .thenReturn(objectName);

            when(blobServiceClient.getBlobContainerClient(any(String.class))).thenReturn(blobContainerClient);
            when(blobContainerClient.getBlobClient(objectName)).thenReturn(blobClient);

            AbsRemoteLogHandler absRemoteLogHandler = AbsRemoteLogHandler.getInstance();
            Assertions.assertNotNull(absRemoteLogHandler);

            absRemoteLogHandler.sendRemoteLog(logPath);
            Mockito.verify(blobClient, times(1)).uploadFromFile(logPath);
        }
    }
}

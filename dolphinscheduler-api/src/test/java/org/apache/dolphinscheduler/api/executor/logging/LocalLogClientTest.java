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

package org.apache.dolphinscheduler.api.executor.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.SpringServerMethodInvokerDiscovery;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryRequest;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LocalLogClientTest {

    @InjectMocks
    private LocalLogClient localLogClient;

    private SpringServerMethodInvokerDiscovery springServerMethodInvokerDiscovery;

    private int nettyServerPort = 18080;

    @BeforeEach
    public void setUp() {
        try (ServerSocket s = new ServerSocket(0)) {
            nettyServerPort = s.getLocalPort();
        } catch (IOException e) {
            return;
        }

        springServerMethodInvokerDiscovery = new SpringServerMethodInvokerDiscovery(
                NettyServerConfig.builder().serverName("TestLogServer").listenPort(nettyServerPort).build());
        springServerMethodInvokerDiscovery.start();
        springServerMethodInvokerDiscovery.registerServerMethodInvokerProvider(new ILogService() {

            @Override
            public TaskInstanceLogFileDownloadResponse getTaskInstanceWholeLogFileBytes(TaskInstanceLogFileDownloadRequest taskInstanceLogFileDownloadRequest) {
                if (taskInstanceLogFileDownloadRequest.getTaskInstanceId() == 1) {
                    return new TaskInstanceLogFileDownloadResponse(new byte[0], LogResponseStatus.SUCCESS, "");
                } else if (taskInstanceLogFileDownloadRequest.getTaskInstanceId() == 10) {
                    return new TaskInstanceLogFileDownloadResponse("log content".getBytes(), LogResponseStatus.SUCCESS,
                            "");
                }

                throw new ServiceException("download error");
            }

            @Override
            public TaskInstanceLogPageQueryResponse pageQueryTaskInstanceLog(TaskInstanceLogPageQueryRequest taskInstanceLogPageQueryRequest) {
                if (taskInstanceLogPageQueryRequest.getTaskInstanceId() != null) {
                    if (taskInstanceLogPageQueryRequest.getTaskInstanceId() == 100) {
                        throw new ServiceException("query log error");
                    } else if (taskInstanceLogPageQueryRequest.getTaskInstanceId() == 10) {
                        return new TaskInstanceLogPageQueryResponse("Partial log content", LogResponseStatus.SUCCESS,
                                "");
                    }
                }

                return new TaskInstanceLogPageQueryResponse();
            }

            @Override
            public void removeTaskInstanceLog(String taskInstanceLogAbsolutePath) {

            }
        });
        springServerMethodInvokerDiscovery.start();
    }

    @AfterEach
    public void tearDown() {
        if (springServerMethodInvokerDiscovery != null) {
            springServerMethodInvokerDiscovery.close();
        }
    }

    @Test
    public void testGetWholeLogSuccess() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setId(1);
        taskInstance.setLogPath("/path/to/log");

        TaskInstanceLogFileDownloadResponse actualResponse = localLogClient.getWholeLog(taskInstance);

        assertNotNull(actualResponse);
        assertArrayEquals("".getBytes(), actualResponse.getLogBytes());
    }

    @Test
    public void testGetPartLogSuccess() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(10);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath("/path/to/log");

        TaskInstanceLogPageQueryResponse actualResponse = localLogClient.getPartLog(taskInstance, 0, 10);

        assertNotNull(actualResponse);
        assertEquals("Partial log content", actualResponse.getLogContent());
    }
}

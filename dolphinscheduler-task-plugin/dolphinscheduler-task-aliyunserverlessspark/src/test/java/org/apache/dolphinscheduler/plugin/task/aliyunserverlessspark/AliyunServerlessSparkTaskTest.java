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
package org.apache.dolphinscheduler.plugin.task.aliyunserverlessspark;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.aliyun.emr_serverless_spark20230808.Client;
import com.aliyun.emr_serverless_spark20230808.models.StartJobRunResponse;
import com.aliyun.emr_serverless_spark20230808.models.Tag;
import com.aliyun.tea.TeaException;

public class AliyunServerlessSparkTaskTest {
    @Mock
    private TaskExecutionContext mockTaskExecutionContext;

    @Mock
    private Client mockAliyunServerlessSparkClient;

    @InjectMocks
    private AliyunServerlessSparkTask aliyunServerlessSparkTask;

    @BeforeEach
    public void before() {

    }

    public void testInit() {

    }
}

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

package org.apache.dolphinscheduler.alert.service;

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.alert.config.AlertConfig;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertSenderThreadPoolFactoryTest {

    private final AlertConfig alertConfig = new AlertConfig();

    private final AlertSenderThreadPoolFactory alertSenderThreadPoolFactory =
            new AlertSenderThreadPoolFactory(alertConfig);

    @Test
    void getThreadPool() {
        ThreadPoolExecutor threadPool = alertSenderThreadPoolFactory.getThreadPool();
        assertThat(threadPool.getCorePoolSize()).isEqualTo(alertConfig.getSenderParallelism());
        assertThat(threadPool.getMaximumPoolSize()).isEqualTo(alertConfig.getSenderParallelism());
    }
}

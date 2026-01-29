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

package org.apache.dolphinscheduler.tools.datasource.mysql.v8;

import org.apache.dolphinscheduler.tools.datasource.DolphinSchedulerManager;
import org.apache.dolphinscheduler.tools.datasource.jupiter.DolphinSchedulerDatabaseContainer;
import org.apache.dolphinscheduler.tools.datasource.mysql.DolphinSchedulerMysqlProfile;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DolphinSchedulerMysqlProfile
@DolphinSchedulerDatabaseContainer(imageName = "mysql:8.0")
class InitializeWithMysqlIT {

    @Autowired
    private DolphinSchedulerManager dolphinSchedulerManager;

    @Test
    @DisplayName("Test Initialize DolphinScheduler database in MySQL")
    void testInitializeWithMysqlProfile() {
        Assertions.assertDoesNotThrow(() -> dolphinSchedulerManager.initDolphinScheduler());
        // todo: Assert table count

    }

}

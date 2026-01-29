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

package org.apache.dolphinscheduler.tools.datasource.postgresql.v11;

import org.apache.dolphinscheduler.tools.datasource.DolphinSchedulerManager;
import org.apache.dolphinscheduler.tools.datasource.jupiter.DolphinSchedulerDatabaseContainer;
import org.apache.dolphinscheduler.tools.datasource.postgresql.DolphinSchedulerPostgresqlProfile;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DolphinSchedulerPostgresqlProfile
@DolphinSchedulerDatabaseContainer(imageName = "postgres:11.1")
class InitializeWithPostgresqlIT {

    @Autowired
    private DolphinSchedulerManager dolphinSchedulerManager;

    @Test
    @DisplayName("Test initDolphinScheduler database in PostgreSQL")
    void testInitializeWithPostgreSQLProfile() {
        Assertions.assertDoesNotThrow(() -> {
            dolphinSchedulerManager.initDolphinScheduler();
        });
        // todo: Assert table count
    }
}

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

package org.apache.dolphinscheduler.tools.datasource.mysql.v5;

import org.apache.dolphinscheduler.common.sql.SqlScriptRunner;
import org.apache.dolphinscheduler.tools.datasource.DolphinSchedulerManager;
import org.apache.dolphinscheduler.tools.datasource.jupiter.DolphinSchedulerDatabaseContainer;
import org.apache.dolphinscheduler.tools.datasource.mysql.DolphinSchedulerMysqlProfile;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@EnabledOnOs(OS.LINUX)
@DolphinSchedulerMysqlProfile
@DolphinSchedulerDatabaseContainer(imageName = "mysql:5.7")
class UpgradeWithMysqlIT {

    @Autowired
    private DolphinSchedulerManager dolphinSchedulerManager;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Test Upgrade DolphinScheduler database in MySQL")
    void testUpgradeWithMysqlProfile() throws SQLException, IOException {

        // initialize the 3.0.0 schema
        SqlScriptRunner sqlScriptRunner = new SqlScriptRunner(dataSource, "3.0.0_schema/mysql_3.0.0.sql");
        sqlScriptRunner.execute();
        log.info("Initialize the 3.0.0 schema successfully.");

        Assertions.assertDoesNotThrow(() -> dolphinSchedulerManager.upgradeDolphinScheduler());
        // todo: Assert table count

    }

}

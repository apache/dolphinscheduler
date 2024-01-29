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

package org.apache.dolphinscheduler.tools.datasource;

import org.apache.dolphinscheduler.dao.plugin.api.dialect.DatabaseDialect;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerUpgrader;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerVersion;
import org.apache.dolphinscheduler.tools.datasource.upgrader.UpgradeDao;
import org.apache.dolphinscheduler.tools.datasource.utils.SchemaUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DolphinSchedulerManager {

    @Autowired
    private UpgradeDao upgradeDao;

    @Autowired
    private DatabaseDialect databaseDialect;

    private Map<DolphinSchedulerVersion, DolphinSchedulerUpgrader> upgraderMap = new HashMap<>();

    public DolphinSchedulerManager(List<DolphinSchedulerUpgrader> dolphinSchedulerUpgraders) throws Exception {
        if (CollectionUtils.isNotEmpty(dolphinSchedulerUpgraders)) {
            upgraderMap = dolphinSchedulerUpgraders.stream()
                    .collect(Collectors.toMap(DolphinSchedulerUpgrader::getCurrentVersion, Function.identity()));
        }
    }

    public void initDolphinScheduler() {
        this.initDolphinSchedulerSchema();
    }

    /**
     * whether schema is initialized
     * @return true if schema is initialized
     */
    public boolean schemaIsInitialized() {
        // Determines whether the dolphinscheduler table structure has been init
        if (databaseDialect.tableExists("t_escheduler_version")
                || databaseDialect.tableExists("t_ds_version")
                || databaseDialect.tableExists("t_escheduler_queue")) {
            log.info("The database has been initialized. Skip the initialization step");
            return true;
        }
        return false;
    }

    public void initDolphinSchedulerSchema() {
        log.info("Start initializing the DolphinScheduler manager table structure");
        upgradeDao.initSchema();
    }

    public void upgradeDolphinScheduler() throws IOException {
        // Gets a list of all upgrades
        List<String> schemaList = SchemaUtils.getAllSchemaList();
        if (schemaList == null || schemaList.size() == 0) {
            log.info("There is no schema to upgrade!");
        } else {
            String version;
            // Gets the version of the current system
            if (databaseDialect.tableExists("t_escheduler_version")) {
                version = upgradeDao.getCurrentVersion("t_escheduler_version");
            } else if (databaseDialect.tableExists("t_ds_version")) {
                version = upgradeDao.getCurrentVersion("t_ds_version");
            } else if (databaseDialect.columnExists("t_escheduler_queue", "create_time")) {
                version = "1.0.1";
            } else if (databaseDialect.tableExists("t_escheduler_queue")) {
                version = "1.0.0";
            } else {
                log.error("Unable to determine current software version, so cannot upgrade");
                throw new RuntimeException("Unable to determine current software version, so cannot upgrade");
            }
            // The target version of the upgrade
            String schemaVersion = "";
            String currentVersion = version;
            for (String schemaDir : schemaList) {
                schemaVersion = schemaDir.split("_")[0];
                if (SchemaUtils.isAGreatVersion(schemaVersion, version)) {
                    log.info("upgrade DolphinScheduler metadata version from {} to {}", version, schemaVersion);
                    log.info("Begin upgrading DolphinScheduler's table structure");
                    upgradeDao.upgradeDolphinScheduler(schemaDir);
                    DolphinSchedulerVersion.getVersion(schemaVersion).ifPresent(v -> upgraderMap.get(v).doUpgrade());
                    version = schemaVersion;
                }
            }
            // todo: do we need to do this in all version > 2.0.6?
            if (SchemaUtils.isAGreatVersion("2.0.6", currentVersion)
                    && SchemaUtils.isAGreatVersion(SchemaUtils.getSoftVersion(), currentVersion)) {
                upgradeDao.upgradeDolphinSchedulerResourceFileSize();
            }
        }

        // Assign the value of the version field in the version table to the version of the product
        upgradeDao.updateVersion(SchemaUtils.getSoftVersion());
    }
}

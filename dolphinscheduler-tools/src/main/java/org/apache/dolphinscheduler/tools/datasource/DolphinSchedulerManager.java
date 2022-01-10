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

import org.apache.dolphinscheduler.dao.upgrade.SchemaUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.tools.datasource.dao.UpgradeDao;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DolphinSchedulerManager {
    private static final Logger logger = LoggerFactory.getLogger(DolphinSchedulerManager.class);

    private final UpgradeDao upgradeDao;

    public DolphinSchedulerManager(DataSource dataSource, List<UpgradeDao> daos) throws Exception {
        final DbType type = getCurrentDbType(dataSource);
        upgradeDao = daos.stream()
                         .filter(it -> it.getDbType() == type)
                         .findFirst()
                         .orElseThrow(() -> new RuntimeException(
                             "Cannot find UpgradeDao implementation for db type: " + type
                         ));
    }

    private DbType getCurrentDbType(DataSource dataSource) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String name = conn.getMetaData().getDatabaseProductName().toUpperCase();
            return DbType.valueOf(name);
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
        if (upgradeDao.isExistsTable("t_escheduler_version")
            || upgradeDao.isExistsTable("t_ds_version")
            || upgradeDao.isExistsTable("t_escheduler_queue")) {
            logger.info("The database has been initialized. Skip the initialization step");
            return true;
        }
        return false;
    }

    public void initDolphinSchedulerSchema() {
        logger.info("Start initializing the DolphinScheduler manager table structure");
        upgradeDao.initSchema();
    }
    public void upgradeDolphinScheduler() throws IOException {
        // Gets a list of all upgrades
        List<String> schemaList = SchemaUtils.getAllSchemaList();
        if (schemaList == null || schemaList.size() == 0) {
            logger.info("There is no schema to upgrade!");
        } else {
            String version;
            // Gets the version of the current system
            if (upgradeDao.isExistsTable("t_escheduler_version")) {
                version = upgradeDao.getCurrentVersion("t_escheduler_version");
            } else if (upgradeDao.isExistsTable("t_ds_version")) {
                version = upgradeDao.getCurrentVersion("t_ds_version");
            } else if (upgradeDao.isExistsColumn("t_escheduler_queue", "create_time")) {
                version = "1.0.1";
            } else if (upgradeDao.isExistsTable("t_escheduler_queue")) {
                version = "1.0.0";
            } else {
                logger.error("Unable to determine current software version, so cannot upgrade");
                throw new RuntimeException("Unable to determine current software version, so cannot upgrade");
            }
            // The target version of the upgrade
            String schemaVersion = "";
            for (String schemaDir : schemaList) {
                schemaVersion = schemaDir.split("_")[0];
                if (SchemaUtils.isAGreatVersion(schemaVersion, version)) {
                    logger.info("upgrade DolphinScheduler metadata version from {} to {}", version, schemaVersion);
                    logger.info("Begin upgrading DolphinScheduler's table structure");
                    upgradeDao.upgradeDolphinScheduler(schemaDir);
                    if ("1.3.0".equals(schemaVersion)) {
                        upgradeDao.upgradeDolphinSchedulerWorkerGroup();
                    } else if ("1.3.2".equals(schemaVersion)) {
                        upgradeDao.upgradeDolphinSchedulerResourceList();
                    } else if ("2.0.0".equals(schemaVersion)) {
                        upgradeDao.upgradeDolphinSchedulerTo200(schemaDir);
                    }
                    version = schemaVersion;
                }
            }
        }

        // Assign the value of the version field in the version table to the version of the product
        upgradeDao.updateVersion(SchemaUtils.getSoftVersion());
    }
}

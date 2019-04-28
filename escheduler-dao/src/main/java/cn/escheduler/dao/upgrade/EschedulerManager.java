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
package cn.escheduler.dao.upgrade;

import cn.escheduler.common.utils.SchemaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * upgrade manager
 */
public class EschedulerManager {
    private static final Logger logger = LoggerFactory.getLogger(EschedulerManager.class);
    UpgradeDao upgradeDao = UpgradeDao.getInstance();

    public void initEscheduler() {
        // Determines whether the escheduler table structure has been init
        if(upgradeDao.isExistsTable("t_escheduler_version") || upgradeDao.isExistsTable("t_escheduler_queue")) {
            logger.info("The database has been initialized. Skip the initialization step");
            return;
        }
        this.initEschedulerSchema();
    }

    public void initEschedulerSchema() {

        logger.info("Start initializing the escheduler manager mysql table structure");
        upgradeDao.initEschedulerSchema();
    }


    /**
     * upgrade escheduler
     */
    public void upgradeEscheduler() throws Exception{

        // Gets a list of all upgrades
        List<String> schemaList = SchemaUtils.getAllSchemaList();
        if(schemaList == null || schemaList.size() == 0) {
            logger.info("There is no schema to upgrade!");
        }else {

            String version = "";
            // Gets the version of the current system
            if (upgradeDao.isExistsTable("t_escheduler_version")) {
                version = upgradeDao.getCurrentVersion();
            }else if(upgradeDao.isExistsColumn("t_escheduler_queue","create_time")){
                version = "1.0.1";
            }else if(upgradeDao.isExistsTable("t_escheduler_queue")){
                version = "1.0.0";
            }else{
                logger.error("Unable to determine current software version, so cannot upgrade");
                throw new RuntimeException("Unable to determine current software version, so cannot upgrade");
            }
            // The target version of the upgrade
            String schemaVersion = "";
            for(String schemaDir : schemaList) {


                schemaVersion = schemaDir.split("_")[0];
                if(SchemaUtils.isAGreatVersion(schemaVersion , version)) {

                    logger.info("upgrade escheduler metadata version from " + version + " to " + schemaVersion);


                    logger.info("Begin upgrading escheduler's mysql table structure");
                    upgradeDao.upgradeEscheduler(schemaDir);
                    if(SchemaUtils.isAGreatVersion(version,"1.0.1")){
                        version = upgradeDao.getCurrentVersion();
                    }else {
                        version = schemaVersion;
                    }
                }

            }
        }

        // Assign the value of the version field in the version table to the version of the product
        upgradeDao.updateVersion(SchemaUtils.getSoftVersion());
    }
}

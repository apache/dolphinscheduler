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

package org.apache.dolphinscheduler.plugin.storage.hdfs;

import static org.apache.dolphinscheduler.common.constants.Constants.FS_DEFAULT_FS;
import static org.apache.dolphinscheduler.common.constants.Constants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT;
import static org.apache.dolphinscheduler.common.constants.Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE;
import static org.apache.dolphinscheduler.common.constants.Constants.HDFS_ROOT_USER;
import static org.apache.dolphinscheduler.common.constants.Constants.KERBEROS_EXPIRE_TIME;
import static org.apache.dolphinscheduler.common.constants.Constants.YARN_APPLICATION_STATUS_ADDRESS;
import static org.apache.dolphinscheduler.common.constants.Constants.YARN_JOB_HISTORY_STATUS_ADDRESS;
import static org.apache.dolphinscheduler.common.constants.Constants.YARN_RESOURCEMANAGER_HA_RM_IDS;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import lombok.Data;

import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class HdfsStorageProperties {

    /**
     * HDFS storage user
     */
    private String user = PropertyUtils.getString(HDFS_ROOT_USER);

    /**
     * HDFS default fs
     */
    private String defaultFS = PropertyUtils.getString(FS_DEFAULT_FS);

    /**
     * YARN resource manager HA RM ids
     */
    private String yarnResourceRmIds = PropertyUtils.getString(YARN_RESOURCEMANAGER_HA_RM_IDS);

    /**
     * YARN application status address
     */
    private String yarnAppStatusAddress = PropertyUtils.getString(YARN_APPLICATION_STATUS_ADDRESS);

    /**
     * YARN job history status address
     */
    private String yarnJobHistoryStatusAddress = PropertyUtils.getString(YARN_JOB_HISTORY_STATUS_ADDRESS);

    /**
     * Hadoop resouece manager http address port
     */
    private String hadoopResourceManagerHttpAddressPort =
            PropertyUtils.getString(HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT);

    /**
     * Hadoop security authentication startup state
     */
    private boolean hadoopSecurityAuthStartupState =
            PropertyUtils.getBoolean(HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);

    /**
     * Kerberos expire time
     */
    public static int getKerberosExpireTime() {
        return PropertyUtils.getInt(KERBEROS_EXPIRE_TIME, 2);
    }
}

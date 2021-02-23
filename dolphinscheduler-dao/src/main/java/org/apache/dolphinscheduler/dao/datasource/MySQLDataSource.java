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

package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * data source of mySQL
 */
public class MySQLDataSource extends BaseDataSource {

    private final Logger logger = LoggerFactory.getLogger(MySQLDataSource.class);

    private final String sensitiveParam = "autoDeserialize=true";

    private static final String ALLOW_LOAD_LOCAL_IN_FILE_NAME = "allowLoadLocalInfile";

    private static final String AUTO_DESERIALIZE = "autoDeserialize";

    private static final String ALLOW_LOCAL_IN_FILE_NAME = "allowLocalInfile";

    private static final String ALLOW_URL_IN_LOCAL_IN_FILE_NAME = "allowUrlInLocalInfile";

    private static boolean checkKeyIsLegitimate(String key) {
        return !key.contains(ALLOW_LOAD_LOCAL_IN_FILE_NAME) && !key.contains(AUTO_DESERIALIZE) && !key.contains(ALLOW_LOCAL_IN_FILE_NAME) && !key.contains(ALLOW_URL_IN_LOCAL_IN_FILE_NAME);
    }

    private final char symbol = '&';

    /**
     * gets the JDBC url for the data source connection
     *
     * @return jdbc url
     */
    @Override
    public String driverClassSelector() {
        return Constants.COM_MYSQL_JDBC_DRIVER;
    }

    /**
     * @return db type
     */
    @Override
    public DbType dbTypeSelector() {
        return DbType.MYSQL;
    }

    public static Map<String, String> buildOtherParams(String other) {
        Map<String, String> newParamMap = new HashMap<>();
        newParamMap.put(ALLOW_LOAD_LOCAL_IN_FILE_NAME, Boolean.FALSE.toString());
        newParamMap.put(AUTO_DESERIALIZE, Boolean.FALSE.toString());
        newParamMap.put(ALLOW_LOCAL_IN_FILE_NAME, Boolean.FALSE.toString());
        newParamMap.put(ALLOW_URL_IN_LOCAL_IN_FILE_NAME, Boolean.FALSE.toString());

        if (StringUtils.isBlank(other)) {
            return newParamMap;
        }
        Map<String, String> paramMap = JSONUtils.toMap(other);
        if (MapUtils.isEmpty(paramMap)) {
            return newParamMap;
        }

        paramMap.forEach((k, v) -> {
            if (!checkKeyIsLegitimate(k)) {
                return;
            }
            newParamMap.put(k, v);

        });
        return newParamMap;
    }

    @Override
    public String getUser() {
        if (user.contains(sensitiveParam)) {
            logger.warn("sensitive param : {} in username field is filtered", sensitiveParam);
            user = user.replace(sensitiveParam, "");
        }
        logger.debug("username : {}", user);
        return user;
    }

    @Override
    public String getPassword() {
        // password need decode
        password = super.getPassword();
        if (password.contains(sensitiveParam)) {
            logger.warn("sensitive param : {} in password field is filtered", sensitiveParam);
            password = password.replace(sensitiveParam, "");
        }
        return password;
    }
}

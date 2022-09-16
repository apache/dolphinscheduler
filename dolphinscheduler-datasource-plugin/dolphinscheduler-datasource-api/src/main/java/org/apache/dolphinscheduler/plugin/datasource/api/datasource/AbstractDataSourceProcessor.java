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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

public abstract class AbstractDataSourceProcessor implements DataSourceProcessor {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.\\,]+$");

    private static final Pattern IPV6_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.\\:\\[\\]\\,]+$");

    private static final Pattern DATABASE_PATTER = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.]+$");

    private static final Pattern PARAMS_PATTER = Pattern.compile("^[a-zA-Z0-9\\-\\_\\/\\@\\.]+$");

    private static final Set<String> POSSIBLE_MALICIOUS_KEYS = Sets.newHashSet("allowLoadLocalInfile");

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        checkHost(baseDataSourceParamDTO.getHost());
        checkDatabasePatter(baseDataSourceParamDTO.getDatabase());
        checkOther(baseDataSourceParamDTO.getOther());
    }

    /**
     * Check the host is valid
     *
     * @param host datasource host
     */
    protected void checkHost(String host) {
        if (!IPV4_PATTERN.matcher(host).matches() || !IPV6_PATTERN.matcher(host).matches()) {
            throw new IllegalArgumentException("datasource host illegal");
        }
    }

    /**
     * check database name is valid
     *
     * @param database database name
     */
    protected void checkDatabasePatter(String database) {
        if (!DATABASE_PATTER.matcher(database).matches()) {
            throw new IllegalArgumentException("database name illegal");
        }
    }

    /**
     * check other is valid
     *
     * @param other other
     */
    protected void checkOther(Map<String, String> other) {
        if (MapUtils.isEmpty(other)) {
            return;
        }
        if (!Sets.intersection(other.keySet(), POSSIBLE_MALICIOUS_KEYS).isEmpty()) {
            throw new IllegalArgumentException("Other params include possible malicious keys.");
        }
        boolean paramsCheck = other.entrySet().stream().allMatch(p -> PARAMS_PATTER.matcher(p.getValue()).matches());
        if (!paramsCheck) {
            throw new IllegalArgumentException("datasource other params illegal");
        }
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        BaseConnectionParam baseConnectionParam = (BaseConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}@{2}@{3}", dbType.getDescp(), baseConnectionParam.getUser(),
                PasswordUtils.encodePassword(baseConnectionParam.getPassword()), baseConnectionParam.getJdbcUrl());
    }
}

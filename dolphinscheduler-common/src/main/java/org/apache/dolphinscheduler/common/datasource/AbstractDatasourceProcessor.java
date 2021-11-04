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

package org.apache.dolphinscheduler.common.datasource;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class AbstractDatasourceProcessor implements DatasourceProcessor {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.]+$");

    private static final Pattern IPV6_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.\\:\\[\\]]+$");

    private static final Pattern DATABASE_PATTER = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.]+$");

    private static final Pattern PARAMS_PATTER = Pattern.compile("^[a-zA-Z0-9\\-\\_\\/]+$");

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        checkHost(baseDataSourceParamDTO.getHost());
        checkDatasourcePatter(baseDataSourceParamDTO.getDatabase());
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
    protected void checkDatasourcePatter(String database) {
        if (!DATABASE_PATTER.matcher(database).matches()) {
            throw new IllegalArgumentException("datasource name illegal");
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
        boolean paramsCheck = other.entrySet().stream().allMatch(p -> PARAMS_PATTER.matcher(p.getValue()).matches());
        if (!paramsCheck) {
            throw new IllegalArgumentException("datasource other params illegal");
        }
    }

    /**
     * transform other
     *
     * @param type type
     * @param otherMap otherMap
     */
    protected String transformOther(DbType type, Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }

        List<String> list = new ArrayList<>();
        otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
        return String.join(getDbSeparator(type), list);
    }

    /**
     * parse Other
     *
     * @param type type
     * @param other other
     */
    protected Map<String, String> parseOther(DbType type, String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        String[] configs = other.split(getDbSeparator(type));
        for (String config : configs) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }

    /**
     * get db separator
     * @param type type
     */
    private String getDbSeparator(DbType type) {
        String separator = "";
        if (Constants.MYSQL.equals(type.name())
                || Constants.POSTGRESQL.equals(type.name())
                || Constants.CLICKHOUSE.equals(type.name())
                || Constants.ORACLE.equals(type.name())
                || Constants.PRESTO.equals(type.name())) {
            separator = "&";
        } else if (Constants.HIVE.equals(type.name())
                || Constants.SPARK.equals(type.name())
                || Constants.DB2.equals(type.name())
                || Constants.SQLSERVER.equals(type.name())) {
            separator = ";";
        }
        return separator;
    }
}

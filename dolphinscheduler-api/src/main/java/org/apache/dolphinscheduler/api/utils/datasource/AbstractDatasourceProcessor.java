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

package org.apache.dolphinscheduler.api.utils.datasource;

import org.apache.dolphinscheduler.api.dto.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.dao.datasource.MySQLDataSource;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections4.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class AbstractDatasourceProcessor<T extends BaseDataSourceParamDTO> implements DatasourceProcessor<T> {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.]+$");

    private static final Pattern IPV6_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.\\:\\[\\]]+$");

    private static final Pattern DATABASE_PATTER = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.]+$");

    private static final Pattern PARAMS_PATTER = Pattern.compile("^[a-zA-Z0-9]+$");

    protected static final String NAME = "name";
    protected static final String NOTE = "note";
    protected static final String TYPE = "type";
    protected static final String HOST = "host";
    protected static final String PORT = "port";
    protected static final String PRINCIPAL = "principal";
    protected static final String DATABASE = "database";
    protected static final String USER_NAME = "userName";
    protected static final String OTHER = "other";

    @Override
    public void checkDatasourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        checkHost(baseDataSourceParamDTO.getHost());
        checkDatasourcePatter(baseDataSourceParamDTO.getDatabase());
        checkOther(baseDataSourceParamDTO.getOther());
    }

    protected Map<String, Object> buildCommonParamMap(String address, String jdbcUrl, BaseDataSourceParamDTO dataSourceParam) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put(Constants.ADDRESS, address);
        parameterMap.put(Constants.DATABASE, dataSourceParam.getDatabase());
        parameterMap.put(Constants.JDBC_URL, jdbcUrl);
        parameterMap.put(Constants.USER, dataSourceParam.getUserName());
        parameterMap.put(Constants.PASSWORD, CommonUtils.encodePassword(dataSourceParam.getPassword()));

        return parameterMap;
    }

    /**
     * Check the host is valid
     *
     * @param host datasource host
     * @throws ServiceException if datasource is invalid
     */
    protected void checkHost(String host) {
        if (!IPV4_PATTERN.matcher(host).matches() || !IPV6_PATTERN.matcher(host).matches()) {
            throw new ServiceException(Status.DATASOURCE_HOST_ILLEGAL);
        }
    }

    /**
     * check database name is valid
     *
     * @param database database name
     * @throws ServiceException if database name is invalid
     */
    protected void checkDatasourcePatter(String database) {
        if (!DATABASE_PATTER.matcher(database).matches()) {
            throw new ServiceException(Status.DATASOURCE_NAME_ILLEGAL);
        }
    }

    /**
     * check other is valid
     *
     * @param other other
     * @throws ServiceException if other is not empty and invalid
     */
    protected void checkOther(String other) {
        if (StringUtils.isBlank(other)) {
            return;
        }
        Map<String, String> map = JSONUtils.toMap(other);
        if (MapUtils.isEmpty(map)) {
            return;
        }
        boolean paramsCheck = map.entrySet().stream().allMatch(p -> PARAMS_PATTER.matcher(p.getValue()).matches());
        if (!paramsCheck) {
            throw new ServiceException(Status.DATASOURCE_OTHER_PARAMS_ILLEGAL);
        }
    }

    /**
     * transform other to otherStr
     *
     * @param other other
     * @param dbType datasource type
     * @param separator separator
     * @return otherStr
     */
    protected String transformOther(String other, DbType dbType, String separator) {
        //todo: wrapper other
        Map<String, String> map;
        if (DbType.MYSQL.equals(dbType)) {
            map = MySQLDataSource.buildOtherParams(other);
        } else {
            map = JSONUtils.toMap(other);
        }
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        StringBuilder otherSb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            otherSb.append(String.format("%s=%s%s", entry.getKey(), entry.getValue(), separator));
        }
        if (DbType.DB2.equals(dbType)) {
            otherSb.deleteCharAt(otherSb.length() - 1);
        }
        return otherSb.toString();
    }

}

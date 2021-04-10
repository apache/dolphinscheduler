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
import org.apache.dolphinscheduler.common.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceParamUtil {

    private DatasourceParamUtil() {
    }

    private static final Logger logger = LoggerFactory.getLogger(DatasourceParamUtil.class);
    private static final Map<Integer, DatasourceProcessor> checkProcessorMap = new HashMap<>();

    static {
        checkProcessorMap.put(DbType.MYSQL.getCode(), new MysqlDatasourceProcessor());
        checkProcessorMap.put(DbType.POSTGRESQL.getCode(), new PostgreSqlDatasourceProcessor());
        checkProcessorMap.put(DbType.HIVE.getCode(), new HiveDatasourceProcessor());
        checkProcessorMap.put(DbType.SPARK.getCode(), new SparkDatasourceProcessor());
        checkProcessorMap.put(DbType.CLICKHOUSE.getCode(), new ClickHouseDatasourceProcessor());
        checkProcessorMap.put(DbType.ORACLE.getCode(), new OracleDatasourceProcessor());
        checkProcessorMap.put(DbType.SQLSERVER.getCode(), new SqlServerDatasourceProcessor());
        checkProcessorMap.put(DbType.DB2.getCode(), new Db2DatasourceProcessor());
        checkProcessorMap.put(DbType.PRESTO.getCode(), new PrestoDatasourceProcessor());
    }

    /**
     * check datasource param
     *
     * @param baseDataSourceParamDTO datasource param
     * @throws ServiceException if param invalid
     */
    public static void checkDatasourceParam(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        Integer datasourceType = baseDataSourceParamDTO.getType().getCode();
        if (!checkProcessorMap.containsKey(datasourceType)) {
            throw new ServiceException(Status.DATASOURCE_DB_TYPE_ILLEGAL);
        }
        checkProcessorMap.get(datasourceType).checkDatasourceParam(baseDataSourceParamDTO);
    }

    /**
     * build connection url
     * @param baseDataSourceParamDTO datasourceParam
     */
    public static String buildConnectionParams(BaseDataSourceParamDTO baseDataSourceParamDTO) {
        Integer datasourceType = baseDataSourceParamDTO.getType().getCode();
        if (!checkProcessorMap.containsKey(datasourceType)) {
            throw new ServiceException(Status.DATASOURCE_DB_TYPE_ILLEGAL);
        }
        String connectionParams = checkProcessorMap.get(datasourceType).buildConnectionParams(baseDataSourceParamDTO);
        if (logger.isDebugEnabled()) {
            logger.info("parameters map:{}", connectionParams);
        }
        return connectionParams;
    }

}

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

package org.apache.dolphinscheduler.plugin.datasource.sqlserver;

import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.api.exception.DataSourceException;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class SQLServerDataSourceClient extends CommonDataSourceClient {

    public SQLServerDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    @Override
    protected String getTableListSql(String dbName, String schemaName, String tablePattern) {
        StringBuilder sql = new StringBuilder("select top 1000  concat(s.name, '.', a.name) AS TABLE_NAME\n"
            + " from sys.SysObjects a\n"
            + "    left join sys.extended_properties g\n"
            + "        on (a.id = g.major_id AND g.minor_id = 0)\n"
            + "    left join sys.schemas s\n"
            + "        on s.schema_id = a.uid\n"
            + " where a.type in ('u','v')");
        if (StringUtils.isNotBlank(tablePattern)) {
            sql.append("and a.name like '%")
                .append(tablePattern.trim())
                .append("%' \n")
                .append("Order By a.name");
        }
        return sql.toString();
    }

    @Override
    public List<Map<String, Object>> getTableStruct(String dbName, String schemaName, String tableName) {
        if (StringUtils.isBlank(tableName)) {
            throw new DataSourceException("TABLE_NAME_IS_BLANK");
        }
        String sql = String.format(" SELECT \n" +
            "        col.name AS col_name ,\n" +
            "        t.name AS data_type ,\n" +
            "        CONVERT(nvarchar(50),ISNULL(ep.[value], '')) AS comment \n" +
            " FROM   dbo.syscolumns col\n" +
            "        LEFT  JOIN dbo.systypes t ON col.xtype = t.xusertype\n" +
            "        INNER JOIN dbo.sysobjects obj ON col.id = obj.id\n" +
            "                                         AND obj.xtype in ('u','v')\n" +
            "                                         AND obj.status >= 0\n" +
            "        LEFT  JOIN sys.extended_properties ep ON col.id = ep.major_id\n" +
            "                                                 AND col.colid = ep.minor_id\n" +
            "                                                 AND ep.name = 'MS_Description'\n" +
            "        LEFT  JOIN sys.extended_properties epTwo ON obj.id = epTwo.major_id\n" +
            "                                                    AND epTwo.minor_id = 0\n" +
            "                                                    AND epTwo.name = 'MS_Description'\n" +
            " WHERE  obj.name = '%s' \n" +
            " ORDER BY obj.name ,col.colorder ", tableName);

        return super.executeSql(dbName, schemaName, Boolean.FALSE, sql).getMiddle();
    }

    @Override
    protected String switchEnvironment(String dbName, String schemaName) {
        return String.format("use %s",dbName.trim());
    }
}

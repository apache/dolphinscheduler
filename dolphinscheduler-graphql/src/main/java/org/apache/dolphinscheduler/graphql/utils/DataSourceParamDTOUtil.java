package org.apache.dolphinscheduler.graphql.utils;

import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.clickhouse.ClickHouseDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.db2.Db2DatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.hive.HiveDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.mysql.MysqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.oracle.OracleDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.postgresql.PostgreSqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.presto.PrestoDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.spark.SparkDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.sqlserver.SqlServerDatasourceParamDTO;
import org.apache.dolphinscheduler.common.enums.DbType;

public class DataSourceParamDTOUtil {
    public static BaseDataSourceParamDTO getDataSourceParamDTO(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return new MysqlDatasourceParamDTO();
            case POSTGRESQL:
                return new PostgreSqlDatasourceParamDTO();
            case HIVE:
                return new HiveDataSourceParamDTO();
            case SPARK:
                return new SparkDatasourceParamDTO();
            case CLICKHOUSE:
                return new ClickHouseDatasourceParamDTO();
            case ORACLE:
                return new OracleDatasourceParamDTO();
            case SQLSERVER:
                return new SqlServerDatasourceParamDTO();
            case DB2:
                return new Db2DatasourceParamDTO();
            case PRESTO:
                return new PrestoDatasourceParamDTO();
            default:
                throw new IllegalArgumentException("datasource type illegal:" + dbType);
        }
    }
}

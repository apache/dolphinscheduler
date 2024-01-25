package org.apache.dolphinscheduler.plugin.datasource.s3;

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.datasource.s3.param.S3ConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.s3.param.S3DataSourceParamDTO;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@AutoService(DataSourceProcessor.class)
public class S3DataSourceProcessor extends AbstractDataSourceProcessor {
    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
	  return JSONUtils.parseObject(paramJson, S3DataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
	  S3DataSourceParamDTO s3Param = (S3DataSourceParamDTO) createConnectionParams(connectionJson);

	  S3ConnectionParam s3ConnectionParam = new S3ConnectionParam();
	  s3ConnectionParam.setDatabase(s3Param.getDatabase());
	  s3ConnectionParam.setAddress(s3Param.getHost());
	  s3ConnectionParam.setJdbcUrl(s3Param.getHost());
	  s3ConnectionParam.setUser(s3Param.getUserName());
	  s3ConnectionParam.setPassword(PasswordUtils.decodePassword(s3Param.getPassword()));

	  return s3Param;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
	  S3DataSourceParamDTO s3Param = (S3DataSourceParamDTO) datasourceParam;

	  S3ConnectionParam s3ConnectionParam = new S3ConnectionParam();
	  s3ConnectionParam.setDatabase(s3Param.getDatabase());
	  s3ConnectionParam.setAddress(s3Param.getHost());
	  s3ConnectionParam.setJdbcUrl(s3Param.getHost());
	  s3ConnectionParam.setUser(s3Param.getUserName());
	  s3ConnectionParam.setPassword(PasswordUtils.decodePassword(s3Param.getPassword()));

	  return s3ConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
	  return JSONUtils.parseObject(connectionJson, S3ConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
	  return "";
    }

    @Override
    public String getValidationQuery() {
	  return "";
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
	  S3ConnectionParam s3ConnectionParam = (S3ConnectionParam) connectionParam;
	  String jdbcUrl = s3ConnectionParam.getJdbcUrl();
	  return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException, IOException {
	  S3ConnectionParam s3ConnectionParam = (S3ConnectionParam) connectionParam;
	  Class.forName(getDatasourceDriver());
	  return DriverManager.getConnection(getJdbcUrl(s3ConnectionParam), s3ConnectionParam.getUser(), PasswordUtils.decodePassword(s3ConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
	  return DbType.S3;
    }

    @Override
    public DataSourceProcessor create() {
	  return new S3DataSourceProcessor();
    }
}

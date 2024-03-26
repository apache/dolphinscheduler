package org.apache.dolphinscheduler.plugin.datasource.dolphindb.param;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.AbstractDataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

@AutoService(DataSourceProcessor.class)
public class DolphinDBDataSourceProcessor extends AbstractDataSourceProcessor {

    private static final Set<String> POSSIBLE_MALICIOUS_KEYS = Sets.newHashSet("allowLoadLocalInfile");

    private static final Pattern PARAMS_PATTER = Pattern.compile("^[a-zA-Z0-9\\-\\_\\/\\@\\.\\:\\,\\ ]+$");

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return JSONUtils.parseObject(paramJson, DolphinDBDataSourceParamDTO.class);
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        DolphinDBConnectionParam connectionParams = (DolphinDBConnectionParam) createConnectionParams(connectionJson);
        DolphinDBDataSourceParamDTO dolphinDBDataSourceParamDTO = new DolphinDBDataSourceParamDTO();

        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        dolphinDBDataSourceParamDTO.setDatabase(connectionParams.getDatabase());
        dolphinDBDataSourceParamDTO.setUserName(connectionParams.getUser());
        dolphinDBDataSourceParamDTO.setPassword(connectionParams.getPassword());
        dolphinDBDataSourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        dolphinDBDataSourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);
        dolphinDBDataSourceParamDTO.setOther(connectionParams.getOther());

        return dolphinDBDataSourceParamDTO;
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        DolphinDBDataSourceParamDTO dolphinDBParam = (DolphinDBDataSourceParamDTO) datasourceParam;
        String address = String.format("%s%s:%s", DataSourceConstants.JDBC_DOLPHINDB, dolphinDBParam.getHost(),
                dolphinDBParam.getPort());
        String jdbcUrl = address;
        if (!StringUtils.isEmpty(datasourceParam.getDatabase())) {
            jdbcUrl = jdbcUrl + "/" + datasourceParam.getDatabase();
        }

        DolphinDBConnectionParam dolphinDBConnectionParam = new DolphinDBConnectionParam();
        dolphinDBConnectionParam.setUser(dolphinDBParam.getUserName());
        dolphinDBConnectionParam.setPassword(PasswordUtils.encodePassword(dolphinDBParam.getPassword()));
        dolphinDBConnectionParam.setOther(dolphinDBParam.getOther());
        dolphinDBConnectionParam.setAddress(address);
        dolphinDBConnectionParam.setJdbcUrl(jdbcUrl);
        dolphinDBConnectionParam.setDatabase(dolphinDBConnectionParam.getDatabase());
        dolphinDBConnectionParam.setDriverClassName(getDatasourceDriver());
        dolphinDBConnectionParam.setValidationQuery(getValidationQuery());
        return dolphinDBConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return JSONUtils.parseObject(connectionJson, DolphinDBConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {
        return DataSourceConstants.COM_DOLPHINDB_JDBC_DRIVER;
    }

    @Override
    public String getValidationQuery() {
        return DataSourceConstants.DOLPHINDB_VALIDATION_QUERY;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        DolphinDBConnectionParam dolphinDBConnectionParam = (DolphinDBConnectionParam) connectionParam;
        String jdbcUrl = dolphinDBConnectionParam.getJdbcUrl();
        if (!MapUtils.isEmpty(dolphinDBConnectionParam.getOther())) {
            return String.format("%s?%s", jdbcUrl, transformOther(dolphinDBConnectionParam.getOther()));
        }
        return jdbcUrl;
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException, IOException {
        DolphinDBConnectionParam dolphinDBConnectionParam = (DolphinDBConnectionParam) connectionParam;
        Class.forName(getDatasourceDriver());
        if (!StringUtils.isEmpty(dolphinDBConnectionParam.getUser())
                && !StringUtils.isEmpty(dolphinDBConnectionParam.getPassword()))
            return DriverManager.getConnection(getJdbcUrl(connectionParam), dolphinDBConnectionParam.getUser(),
                    PasswordUtils.decodePassword(dolphinDBConnectionParam.getPassword()));
        else
            return DriverManager.getConnection(getJdbcUrl(connectionParam));
    }

    @Override
    public DbType getDbType() {
        return DbType.DOLPHINDB;
    }

    @Override
    public DataSourceProcessor create() {
        return new DolphinDBDataSourceProcessor();
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        List<String> list = new ArrayList<>();
        otherMap.forEach((key, value) -> list.add(String.format("%s=%s", key, value)));
        return String.join("&", list);
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        String[] configs = other.split("&");
        for (String config : configs) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }

    @Override
    protected void checkDatabasePatter(String database) {
        if (database == null || database.isEmpty()) {
            return;
        }
        super.checkDatabasePatter(database);
    }

    @Override
    public String getDatasourceUniqueId(ConnectionParam connectionParam, DbType dbType) {
        DolphinDBConnectionParam baseConnectionParam = (DolphinDBConnectionParam) connectionParam;
        return MessageFormat.format("{0}@{1}@{2}@{3}@{4}", dbType.getDescp(), baseConnectionParam.getUser(),
                PasswordUtils.encodePassword(baseConnectionParam.getPassword()), baseConnectionParam.getJdbcUrl(),
                baseConnectionParam.getOther());
    }

    @Override
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
}

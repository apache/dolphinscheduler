package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EQUAL_SIGN;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DecoratorDataSourceProcessor extends AbstractDataSourceProcessor {

    private final DataSourceProcessor dataSourceProcessor;

    /**
     * Mysql other param
     */
    private static final String OTHER_PARAMS_REGEX = "([a-zA-Z0-9]+=\\S+)(?:&[a-zA-Z0-9]+=\\S+)*";

    public DecoratorDataSourceProcessor(DataSourceProcessor dataSourceProcessor) {
        this.dataSourceProcessor = dataSourceProcessor;
    }

    @Override
    public BaseDataSourceParamDTO castDatasourceParamDTO(String paramJson) {
        return dataSourceProcessor.castDatasourceParamDTO(fixOtherParam(paramJson));
    }

    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        return dataSourceProcessor.createDatasourceParamDTO(fixOtherParam(connectionJson));
    }

    @Override
    public ConnectionParam createConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        return dataSourceProcessor.createConnectionParams(datasourceParam);
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {
        return dataSourceProcessor.createConnectionParams(fixOtherParam(connectionJson));
    }

    @Override
    public String getDatasourceDriver() {
        return dataSourceProcessor.getDatasourceDriver();
    }

    @Override
    public String getValidationQuery() {
        return dataSourceProcessor.getValidationQuery();
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        return dataSourceProcessor.getJdbcUrl(connectionParam);
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException, IOException {
        return dataSourceProcessor.getConnection(connectionParam);
    }

    @Override
    public DbType getDbType() {
        return dataSourceProcessor.getDbType();
    }

    @Override
    public DataSourceProcessor create() {
        return dataSourceProcessor;
    }

    public static DataSourceProcessor wrap(DataSourceProcessor dataSourceProcessor) {
        return new DecoratorDataSourceProcessor(dataSourceProcessor);
    }

    private String fixOtherParam(String connectionJson) {
        String other = JSONUtils.getNodeString(connectionJson, Constants.OTHER);
        if (Pattern.compile(OTHER_PARAMS_REGEX)
                .matcher(other)
                .matches()) {
            ObjectNode jn = JSONUtils.parseObject(connectionJson);
            HashMap<String, Object> otherMap = Arrays.stream(other.split("&"))
                    .filter(kvStr -> StringUtils.isNotEmpty(kvStr) && kvStr.contains(EQUAL_SIGN))
                    .map(kvStr -> kvStr.split(EQUAL_SIGN))
                    .collect(HashMap::new, (map, kv) -> map.put(kv[0], kv[1]), HashMap::putAll);
            jn.set(Constants.OTHER, JSONUtils.toJsonNode(otherMap));
            return JSONUtils.toJsonString(jn);
        }
        return connectionJson;
    }
}

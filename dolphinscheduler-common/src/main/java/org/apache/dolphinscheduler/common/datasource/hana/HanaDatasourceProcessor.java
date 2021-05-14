package org.apache.dolphinscheduler.common.datasource.hana;


import org.apache.commons.collections4.MapUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.AbstractDatasourceProcessor;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HanaDatasourceProcessor extends AbstractDatasourceProcessor {
    @Override
    public BaseDataSourceParamDTO createDatasourceParamDTO(String connectionJson) {
        HanaConnectionParam connectionParams = (HanaConnectionParam) createConnectionParams(connectionJson);
        String[] hostSeperator = connectionParams.getAddress().split(Constants.DOUBLE_SLASH);
        String[] hostPortArray = hostSeperator[hostSeperator.length - 1].split(Constants.COMMA);

        HanaDatasourceParamDTO hanaDatasourceParamDTO = new HanaDatasourceParamDTO();
        hanaDatasourceParamDTO.setDatabase(connectionParams.getDatabase());
        hanaDatasourceParamDTO.setUserName(connectionParams.getUser());
        hanaDatasourceParamDTO.setOther(parseOther(connectionParams.getOther()));
        hanaDatasourceParamDTO.setPort(Integer.parseInt(hostPortArray[0].split(Constants.COLON)[1]));
        hanaDatasourceParamDTO.setHost(hostPortArray[0].split(Constants.COLON)[0]);

        return null;
    }

    @Override
    public BaseConnectionParam createConnectionParams(BaseDataSourceParamDTO dataSourceParam) {
        HanaDatasourceParamDTO hanaDatasourceParam = (HanaDatasourceParamDTO) dataSourceParam;
        String address = String.format("%s%s:%s", Constants.JDBC_HANA, hanaDatasourceParam.getHost(), hanaDatasourceParam.getPort());
        String jdbcUrl = String.format("%s/%s", address, hanaDatasourceParam.getDatabase());

        HanaConnectionParam hanaConnectionParam = new HanaConnectionParam();
        hanaConnectionParam.setJdbcUrl(jdbcUrl);
        hanaConnectionParam.setDatabase(hanaDatasourceParam.getDatabase());
        hanaConnectionParam.setAddress(address);
        hanaConnectionParam.setUser(hanaDatasourceParam.getUserName());
        hanaConnectionParam.setPassword(CommonUtils.encodePassword(hanaDatasourceParam.getPassword()));
        hanaConnectionParam.setOther(transformOther(hanaDatasourceParam.getOther()));
        return hanaConnectionParam;
    }

    @Override
    public ConnectionParam createConnectionParams(String connectionJson) {

        return JSONUtils.parseObject(connectionJson,HanaConnectionParam.class);
    }

    @Override
    public String getDatasourceDriver() {

        return Constants.COM_HANA_JDBC_DRIVER;
    }

    @Override
    public String getJdbcUrl(ConnectionParam connectionParam) {
        HanaConnectionParam hanaConnectionParam = (HanaConnectionParam) connectionParam;
        if (StringUtils.isNotEmpty(hanaConnectionParam.getOther())) {
            return String.format("%s;%s", hanaConnectionParam.getJdbcUrl(), hanaConnectionParam.getOther());
        }
        return hanaConnectionParam.getJdbcUrl();
    }

    @Override
    public Connection getConnection(ConnectionParam connectionParam) throws ClassNotFoundException, SQLException, IOException {
        HanaConnectionParam hanaConnectionParam = (HanaConnectionParam) connectionParam;

        Class.forName(getDatasourceDriver());
        return DriverManager.getConnection(getJdbcUrl(connectionParam), hanaConnectionParam.getUser(),
                CommonUtils.decodePassword(hanaConnectionParam.getPassword()));
    }

    @Override
    public DbType getDbType() {
        return DbType.HANA;
    }

    private Map<String, String> parseOther(String other) {
        if (StringUtils.isEmpty(other)) {
            return null;
        }
        Map<String, String> otherMap = new LinkedHashMap<>();
        for (String config : other.split("&")) {
            otherMap.put(config.split("=")[0], config.split("=")[1]);
        }
        return otherMap;
    }

    private String transformOther(Map<String, String> otherMap) {
        if (MapUtils.isEmpty(otherMap)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        otherMap.forEach((key, value) -> stringBuilder.append(String.format("%s=%s;", key, value)));
        return stringBuilder.toString();
    }
}

package org.apache.dolphinscheduler.plugin.task.datax.content.reader;

import static org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils.decodePassword;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.datax.DataxParameters;
import org.apache.dolphinscheduler.plugin.task.datax.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.datax.DataxUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BuildDataxJobContentJsonDefaultReader extends AbstractBuildDataxJobContentJsonReader {

    @Override
    public void init(DataxTaskExecutionContext dataxTaskExecutionContext, DataxParameters dataXParameters) {
        this.dataxTaskExecutionContext = dataxTaskExecutionContext;
        this.dataXParameters = dataXParameters;
    }

    @Override
    public ObjectNode reader() {
        BaseConnectionParam dataSourceCfg = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                dataxTaskExecutionContext.getSourcetype(),
                dataxTaskExecutionContext.getSourceConnectionParams());

        List<ObjectNode> readerConnArr = new ArrayList<>();
        ObjectNode readerConn = JSONUtils.createObjectNode();

        ArrayNode sqlArr = readerConn.putArray("querySql");
        for (String sql : new String[]{dataXParameters.getSql()}) {
            sqlArr.add(sql);
        }

        ArrayNode urlArr = readerConn.putArray("jdbcUrl");
        urlArr.add(DataSourceUtils.getJdbcUrl(DbType.valueOf(dataXParameters.getDsType()), dataSourceCfg));

        readerConnArr.add(readerConn);

        ObjectNode readerParam = JSONUtils.createObjectNode();
        readerParam.put("username", dataSourceCfg.getUser());
        readerParam.put("password", decodePassword(dataSourceCfg.getPassword()));
        readerParam.putArray("connection").addAll(readerConnArr);

        ObjectNode reader = JSONUtils.createObjectNode();
        reader.put("name", DataxUtils.getReaderPluginName(dataxTaskExecutionContext.getSourcetype()));
        reader.set("parameter", readerParam);

        return reader;
    }
}

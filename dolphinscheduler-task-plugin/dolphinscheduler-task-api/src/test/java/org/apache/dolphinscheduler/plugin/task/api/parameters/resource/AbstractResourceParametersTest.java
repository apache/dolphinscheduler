package org.apache.dolphinscheduler.plugin.task.api.parameters.resource;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.junit.Assert;
import org.junit.Test;

public class AbstractResourceParametersTest {

    @Test
    public void testDataSource() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        String taskParam = "{\"localParams\":[],\"resourceList\":[],\"type\":\"MYSQL\",\"datasource\":\"1\",\"sql\":\"select now();\",\"sqlType\":\"0\",\"preStatements\":[],\"postStatements\":[],\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}";

        ResourceParametersHelper resourceParametersHelper = JSONUtils.parseObject(taskParam, SqlParameters.class).getResources();

        resourceParametersHelper.getResourceMap().forEach((type, map) -> {
            map.forEach((code, parameters) -> {
                DataSourceParameters dataSourceParameters = new DataSourceParameters();
                dataSourceParameters.setType(DbType.MYSQL);
                dataSourceParameters.setConnectionParams("127.0.0.1:3306");
                map.put(code, dataSourceParameters);
            });
        });

        taskExecutionContext.setResourceParametersHelper(resourceParametersHelper);

        String json = JSONUtils.toJsonString(taskExecutionContext);

        taskExecutionContext = JSONUtils.parseObject(json, TaskExecutionContext.class);

        Assert.assertNotNull(taskExecutionContext);
    }
}



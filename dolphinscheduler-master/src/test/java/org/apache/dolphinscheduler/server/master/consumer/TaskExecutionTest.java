package org.apache.dolphinscheduler.server.master.consumer;

import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.AbstractResourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.UdfFuncParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.sql.SqlTaskChannel;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.List;
import java.util.Map;

public class TaskExecutionTest {

    public static void main(String[] args) {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        String taskParam = "{\"localParams\":[],\"resourceList\":[],\"type\":\"MYSQL\",\"datasource\":\"1\",\"sql\":\"select now();\",\"sqlType\":\"0\",\"preStatements\":[],\"postStatements\":[],\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}";

        SqlTaskChannel sqlTaskChannel = new SqlTaskChannel();
        ResourceParametersHelper resourceParametersHelper = sqlTaskChannel.getResources(taskParam);

        resourceParametersHelper.getResourceMap().forEach((type, map) -> {
            map.forEach((code, parameters) -> {
                DataSourceParameters dataSourceParameters = new DataSourceParameters();
                dataSourceParameters.setType(DbType.MYSQL);
                dataSourceParameters.setConnectionParams("127.0.0.1:3306");
                map.put(code, dataSourceParameters);
            });
        });

        taskExecutionContext.setResourceParametersHelper(resourceParametersHelper);

        taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildResourceParametersInfo(resourceParametersHelper)
                .create();

        String json = JSONUtils.toJsonString(taskExecutionContext);

        taskExecutionContext = JSONUtils.parseObject(json, TaskExecutionContext.class);
        System.out.println(JSONUtils.toJsonString(taskExecutionContext));
    }
}

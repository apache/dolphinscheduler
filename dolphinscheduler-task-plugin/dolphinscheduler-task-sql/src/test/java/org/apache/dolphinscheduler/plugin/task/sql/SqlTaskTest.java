package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;


public class SqlTaskTest {
    private static final Logger logger = LoggerFactory.getLogger(SqlTaskTest.class);

    private SqlTask sqlTask;

    private TaskExecutionContext taskExecutionContext;

    @Before
    public void before() throws Exception {
        String taskParams = "{\"type\": \"MYSQL\", \"datasource\": 1, \"sql\": \"select name from dolphinscheduler.userinfo where in (${name})\", \"sqlType\":0 ,\"sendMail\": null, \"displayRows\":\"10\", \"limit\": 0, \"segmentSeparator\":\"\",\"udfs\":\"null\", \"connParams\":\"null\",\"groupId\":\"0\",\"title\":\"null\",\"preStatements\":[], \"postStatements\":[]}";
        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskLogName()).thenReturn("SqlTaskLogger");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(taskParams);
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getVarPool())
                .thenReturn("[{\"direct\":\"IN\",\"prop\":\"name\",\"type\":\"LIST\",\"value\":\"[\\\"lisi\\\", \\\"zhangsan\\\"]\"}]");
        ResourceParametersHelper resourceParametersHelper = JSONUtils.parseObject(taskParams, SqlParameters.class).getResources();
        resourceParametersHelper.getResourceMap().forEach((type, map) -> map.forEach((code, parameters) -> {
            DataSourceParameters dataSourceParameters = new DataSourceParameters();
            dataSourceParameters.setType(DbType.MYSQL);
            dataSourceParameters.setConnectionParams("{\"jdbcUrl\": \"jdbc:mysql://127.0.0.1:3306/dolphinscheduler\", \"user\":\"root\", \"password\":\"123456\",\"driverClassName\": \"com.mysql.cj.jdbc.Driver\", \"other\":\"useUnicode=true&characterEncoding=UTF-8&useSSL=false\"}");
            map.put(code, dataSourceParameters);
        }));

        Mockito.when(taskExecutionContext.getResourceParametersHelper()).thenReturn(resourceParametersHelper);
        sqlTask = new SqlTask(taskExecutionContext);
        sqlTask.init();
        this.sqlTask.getParameters().setVarPool(taskExecutionContext.getVarPool());

    }

    @Test
    public void testHandle() throws Exception{
        sqlTask.handle();
        Assert.assertEquals("SqlTask execute be success", ExecutionStatus.SUCCESS, sqlTask.getExitStatus());
    }
}
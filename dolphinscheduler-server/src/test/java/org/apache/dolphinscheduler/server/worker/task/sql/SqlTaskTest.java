package org.apache.dolphinscheduler.server.worker.task.sql;

import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.common.task.sql.SqlBinds;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author simfo
 * @date 2020/5/26 14:17
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SqlTaskTest {

    private ApplicationContext applicationContext;

    @Before
    public void before() throws Exception{

        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(AlertDao.class)).thenReturn(getAlertDao());

    }

    @Test
    public void test() throws  Exception{
        String contextStr = "{\"cmdTypeIfComplement\":0,\"dataxTaskExecutionContext\":{\"dataSourceId\":0,\"dataTargetId\":0,\"sourcetype\":0,\"targetType\":0},\"definedParams\":{},\"envFile\":\"C:\\\\Users\\\\.bash_profile\",\"executePath\":\"/tmp/dolphinscheduler/exec/process/1/42/223/444\",\"executorId\":1,\"host\":\"172.16.207.146:1234\",\"logPath\":\"F:\\\\opt\\\\soft\\\\ds\\\\20200518\\\\incubator-dolphinscheduler/logs/42/223/444.log\",\"procedureTaskExecutionContext\":{},\"processDefineId\":42,\"processId\":0,\"processInstanceId\":223,\"projectId\":1,\"queue\":\"default\",\"resources\":[],\"sqlTaskExecutionContext\":{\"connectionParams\":\"{\\\"address\\\":\\\"jdbc:mysql://localhost:3306\\\",\\\"database\\\":\\\"test\\\",\\\"jdbcUrl\\\":\\\"jdbc:mysql://localhost:3306/test\\\",\\\"user\\\":\\\"root\\\",\\\"password\\\":\\\"123456\\\",\\\"other\\\":\\\"autoReconnect=true\\\"}\",\"warningGroupId\":0},\"sqoopTaskExecutionContext\":{\"dataSourceId\":0,\"dataTargetId\":0,\"sourcetype\":0,\"targetType\":0},\"startTime\":1590482755000,\"taskAppId\":\"42_223_444\",\"taskInstanceId\":444,\"taskJson\":\"{\\\"conditionResult\\\":\\\"{\\\\\\\"successNode\\\\\\\":[\\\\\\\"\\\\\\\"],\\\\\\\"failedNode\\\\\\\":[\\\\\\\"\\\\\\\"]}\\\",\\\"conditionsTask\\\":false,\\\"depList\\\":[],\\\"dependence\\\":\\\"{}\\\",\\\"forbidden\\\":false,\\\"id\\\":\\\"tasks-16936\\\",\\\"maxRetryTimes\\\":0,\\\"name\\\":\\\"sql1\\\",\\\"params\\\":\\\"{\\\\\\\"postStatements\\\\\\\":[],\\\\\\\"connParams\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"receiversCc\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"udfs\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"type\\\\\\\":\\\\\\\"MYSQL\\\\\\\",\\\\\\\"title\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"sql\\\\\\\":\\\\\\\"insert into test1 (id,company,date,c1,c2,c3) values (8,${aaa},2020,42,43,54);\\\\\\\\ninsert into test1 (id,company,date,c1,c2,c3) values (18,${aaa},2020,42,43,54);\\\\\\\\ninsert into test1 (id,company,date,c1,c2,c3) values (28,${aaa},2020,42,43,54)\\\\\\\\n\\\\\\\",\\\\\\\"preStatements\\\\\\\":[\\\\\\\"delete  from test1 where id=8\\\\\\\"],\\\\\\\"sqlType\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"receivers\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"datasource\\\\\\\":2,\\\\\\\"showType\\\\\\\":\\\\\\\"TABLE\\\\\\\",\\\\\\\"localParams\\\\\\\":[{\\\\\\\"prop\\\\\\\":\\\\\\\"aaa\\\\\\\",\\\\\\\"direct\\\\\\\":\\\\\\\"IN\\\\\\\",\\\\\\\"type\\\\\\\":\\\\\\\"VARCHAR\\\\\\\",\\\\\\\"value\\\\\\\":\\\\\\\"1\\\\\\\"}]}\\\",\\\"preTasks\\\":\\\"[]\\\",\\\"retryInterval\\\":1,\\\"runFlag\\\":\\\"NORMAL\\\",\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"taskTimeoutParameter\\\":{\\\"enable\\\":false,\\\"interval\\\":0},\\\"timeout\\\":\\\"{\\\\\\\"enable\\\\\\\":false,\\\\\\\"strategy\\\\\\\":\\\\\\\"\\\\\\\"}\\\",\\\"type\\\":\\\"SQL\\\",\\\"workerGroup\\\":\\\"default\\\"}\",\"taskName\":\"sql1\",\"taskParams\":\"{\\\"postStatements\\\":[],\\\"connParams\\\":\\\"\\\",\\\"receiversCc\\\":\\\"\\\",\\\"udfs\\\":\\\"\\\",\\\"type\\\":\\\"MYSQL\\\",\\\"title\\\":\\\"\\\",\\\"sql\\\":\\\"insert into test1 (id,company,date,c1,c2,c3) values (8,${aaa},2020,42,43,54);\\\\ninsert into test1 (id,company,date,c1,c2,c3) values (18,${aaa},2020,42,43,54);\\\\ninsert into test1 (id,company,date,c1,c2,c3) values (28,${aaa},2020,42,43,54)\\\\n\\\",\\\"preStatements\\\":[\\\"delete  from test1 where id=8\\\"],\\\"sqlType\\\":\\\"1\\\",\\\"receivers\\\":\\\"\\\",\\\"datasource\\\":2,\\\"showType\\\":\\\"TABLE\\\",\\\"localParams\\\":[{\\\"prop\\\":\\\"aaa\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"1\\\"}]}\",\"taskTimeout\":2147483647,\"taskTimeoutStrategy\":0,\"taskType\":\"SQL\",\"tenantCode\":\"test\",\"workerGroup\":\"default\"}";
        TaskExecutionContext taskExecutionContext = JSONUtils.parseObject(contextStr,TaskExecutionContext.class);
        // custom logger
        Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskExecutionContext.getProcessDefineId(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId()));
        System.out.println(taskExecutionContext.getTaskParams());

        SqlTask sqlTask = new SqlTask(taskExecutionContext,taskLogger);
        String sql = "insert into test1 (id,company,date,c1,c2,c3) values (18,${aaa},2020,42,43,54)";
        SqlBinds sqlBinds =sqlTask.getSqlAndSqlParamsMap(sql);
        System.out.println(sqlBinds.getSql());

    }

    public AlertDao getAlertDao(){
        AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        Alert alert = new Alert();
        alert.setTitle("Mysql Exception");
        alert.setShowType(ShowType.TEXT);
        alert.setContent("[\"alarm time：2018-02-05\", \"service name：MYSQL_ALTER\", \"alarm name：MYSQL_ALTER_DUMP\", " +
                "\"get the alarm exception.！，interface error，exception information：timed out\", \"request address：http://blog.csdn.net/dreamInTheWorld/article/details/78539286\"]");
        alert.setAlertType(AlertType.EMAIL);
        alert.setAlertGroupId(1);
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
//        alertDao.addAlert(alert);

        return alertDao;
    }
}

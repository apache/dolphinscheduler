package org.apache.dolphinscheduler.plugin.task.spark;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    JSONUtils.class
})
@PowerMockIgnore({"javax.*"})

public class SparkTaskTest {

    @Test
    public void testBuildCommandWithSparkSql() throws Exception {
        String parameters = buildSparkParametersWithSparkSql();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        when(taskExecutionContext.getTaskAppId()).thenReturn("5536");

        SparkTask sparkTask = spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assert.assertEquals(sparkTask.buildCommand(),
            "${SPARK_HOME2}/bin/spark-sql " +
                "--master yarn " +
                "--deploy-mode client " +
                "--driver-cores 1 " +
                "--driver-memory 512M " +
                "--num-executors 2 " +
                "--executor-cores 2 " +
                "--executor-memory 1G " +
                "--name sparksql " +
                "-f /tmp/5536_node.sql");
    }

    private String buildSparkParametersWithSparkSql() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setRawScript("selcet 11111;");
        sparkParameters.setProgramType(ProgramType.SQL);
        sparkParameters.setMainClass(StringUtils.EMPTY);
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("sparksql");
        sparkParameters.setOthers(StringUtils.EMPTY);
        sparkParameters.setSparkVersion("SPARK2");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        return JSONUtils.toJsonString(sparkParameters);
    }

}

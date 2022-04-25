package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    JSONUtils.class
})
@PowerMockIgnore({"javax.*"})
public class FlinkTaskTest {

    @Test
    public void testBuildCommand() {
        String parameters = buildFlinkParameters();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        when(taskExecutionContext.getQueue()).thenReturn("default");
        FlinkTask flinkTask = spy(new FlinkTask(taskExecutionContext));
        flinkTask.init();
        Assert.assertEquals(flinkTask.buildCommand(),
           "flink run " +
                  "-m yarn-cluster " +
                  "-ys 1 " +
                  "-ynm TopSpeedWindowing " +
                  "-yjm 1G " +
                  "-ytm 1G " +
                  "-yqu default " +
                  "-p 2 -sae " +
                  "-c org.apache.flink.streaming.examples.windowing.TopSpeedWindowing " +
                  "TopSpeedWindowing.jar");
    }

    @Test
    public void testBuildCommandWithFlinkSql() {
        String parameters = buildFlinkParametersWithFlinkSql();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        when(taskExecutionContext.getTaskAppId()).thenReturn("4483");
        FlinkTask flinkTask = spy(new FlinkTask(taskExecutionContext));
        flinkTask.init();
        Assert.assertEquals(flinkTask.buildCommand(), "sql-client.sh -f /tmp/4483_node.sql");
    }

    private String buildFlinkParameters() {
        ResourceInfo resource = new ResourceInfo();
        resource.setId(2);
        resource.setResourceName("/TopSpeedWindowing.jar");
        resource.setRes("TopSpeedWindowing.jar");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setLocalParams(Collections.emptyList());
        parameters.setResourceList(Collections.emptyList());
        parameters.setProgramType(ProgramType.JAVA);
        parameters.setMainClass("org.apache.flink.streaming.examples.windowing.TopSpeedWindowing");
        parameters.setMainJar(resource);
        parameters.setDeployMode("cluster");
        parameters.setAppName("TopSpeedWindowing");
        parameters.setFlinkVersion(">=1.10");
        parameters.setJobManagerMemory("1G");
        parameters.setTaskManagerMemory("1G");
        parameters.setSlot(1);
        parameters.setTaskManager(2);
        parameters.setParallelism(2);
        return JSONUtils.toJsonString(parameters);
    }

    private String buildFlinkParametersWithFlinkSql() {
        FlinkParameters parameters = new FlinkParameters();
        parameters.setLocalParams(Collections.emptyList());
        parameters.setRawScript("selcet 11111;");
        parameters.setProgramType(ProgramType.SQL);
        parameters.setMainClass(StringUtils.EMPTY);
        parameters.setDeployMode("cluster");
        parameters.setAppName("FlinkSQL");
        parameters.setOthers(StringUtils.EMPTY);
        parameters.setJobManagerMemory("1G");
        parameters.setTaskManagerMemory("1G");
        parameters.setParallelism(1);
        parameters.setFlinkVersion(">=1.10");
        return JSONUtils.toJsonString(parameters);
    }
}

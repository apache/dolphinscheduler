package org.apache.dolphinler.plugin.task.mlflow;

import java.io.IOException;
import java.util.*;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowConstants;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowParameters;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import static org.apache.dolphinscheduler.spi.utils.Constants.COMMON_PROPERTIES_PATH;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
        JSONUtils.class,
        PropertyUtils.class,
})
@PowerMockIgnore({"javax.*"})
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.spi.utils.PropertyUtils")
public class MlflowTaskTest {
    private static final Logger logger = LoggerFactory.getLogger(MlflowTask.class);

    private MlflowTask mlflowTask;

    private TaskExecutionContext taskExecutionContext;


    @Before
    public void before() throws Exception {

        MlflowParameters mlflowParameters = createParameters();
        String parameters = JSONUtils.toJsonString(mlflowParameters);
        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        PowerMockito.mockStatic(PropertyUtils.class);
        TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getTaskLogName()).thenReturn("MLflowTest");
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/dolphinscheduler_test");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dolphinscheduler_test/log");
        Mockito.when(taskExecutionContext.getEnvironmentConfig()).thenReturn("export PATH=$HOME/anaconda3/bin:$PATH");

        String userName = System.getenv().get("USER");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn(userName);

        mlflowTask = new MlflowTask(taskExecutionContext);
        this.mlflowTask.init();
        this.mlflowTask.getParameters().setVarPool(taskExecutionContext.getVarPool());
    }

    @Test
    public void testInit()
            throws Exception {
        try {
            mlflowTask.init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testGetParamsMap() {
        MlflowParameters mlflowParameters = createParameters();
        HashMap<String, String> paramsMap = mlflowParameters.getParamsMap();
    }

    @Test
    public void testLoadRunScript() throws IOException {
        String scriptPath = MlflowTask.class.getClassLoader().getResource(MlflowConstants.RUN_PROJECT_SCRIPT).getPath();
        String script = MlflowTask.loadRunScript(scriptPath);
        logger.info(script);

    }

    @Test
    public void testbuildCommand() throws Exception {
        String command = mlflowTask.buildCommand();

    }

// Must have conda env

//    @Test
//    public void testHandle() throws Exception {
//        mlflowTask.handle();
//    }

    private MlflowParameters createParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setAlgorithm("xgboost");
        mlflowParameters.setParams("");
        mlflowParameters.setSearchParams("");
        mlflowParameters.setDataPaths("xxx");
        mlflowParameters.setExperimentNames("DsTaskTest");
        mlflowParameters.setModelNames("DsTaskTest");
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        return mlflowParameters;
    }

}

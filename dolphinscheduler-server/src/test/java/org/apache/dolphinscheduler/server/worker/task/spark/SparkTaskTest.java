/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.worker.task.spark;

import org.apache.dolphinscheduler.common.enums.SparkVersion;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.utils.SparkArgsUtils;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OSUtils.class)
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
public class SparkTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(SparkTaskTest.class);

    /**
     * spark1 command .
     */
    private static final String SPARK1_COMMAND = "${SPARK_HOME1}/bin/spark-submit";

    /**
     * spark2 command .
     */
    private static final String SPARK2_COMMAND = "${SPARK_HOME2}/bin/spark-submit";

    @Test
    public void testSparkTaskInit() {

        TaskProps taskProps = new TaskProps();

        String spark1Params = "{"
                + "\"mainArgs\":\"\", "
                + "\"driverMemory\":\"1G\", "
                + "\"executorMemory\":\"2G\", "
                + "\"programType\":\"SCALA\", "
                + "\"mainClass\":\"basicetl.GlobalUserCar\", "
                + "\"driverCores\":\"2\", "
                + "\"deployMode\":\"cluster\", "
                + "\"executorCores\":2, "
                + "\"mainJar\":{\"res\":\"test-1.0-SNAPSHOT.jar\"}, "
                + "\"sparkVersion\":\"SPARK1\", "
                + "\"numExecutors\":\"10\", "
                + "\"localParams\":[], "
                + "\"others\":\"\", "
                + "\"resourceList\":[]"
                + "}";

        String spark2Params = "{"
                + "\"mainArgs\":\"\", "
                + "\"driverMemory\":\"1G\", "
                + "\"executorMemory\":\"2G\", "
                + "\"programType\":\"SCALA\", "
                + "\"mainClass\":\"basicetl.GlobalUserCar\", "
                + "\"driverCores\":\"2\", "
                + "\"deployMode\":\"cluster\", "
                + "\"executorCores\":2, "
                + "\"mainJar\":{\"res\":\"test-1.0-SNAPSHOT.jar\"}, "
                + "\"sparkVersion\":\"SPARK2\", "
                + "\"numExecutors\":\"10\", "
                + "\"localParams\":[], "
                + "\"others\":\"\", "
                + "\"resourceList\":[]"
                + "}";

        taskProps.setTaskParams(spark2Params);

        logger.info("spark task params {}", taskProps.getTaskParams());

        SparkParameters sparkParameters = JSONUtils.parseObject(taskProps.getTaskParams(), SparkParameters.class);

        assert sparkParameters != null;
        if (!sparkParameters.checkParameters()) {
            throw new RuntimeException("spark task params is not valid");
        }
        sparkParameters.setQueue(taskProps.getQueue());

        if (StringUtils.isNotEmpty(sparkParameters.getMainArgs())) {
            String args = sparkParameters.getMainArgs();

            /**
             *  combining local and global parameters
             */
            Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
                    taskProps.getDefinedParams(),
                    sparkParameters.getLocalParametersMap(),
                    taskProps.getCmdTypeIfComplement(),
                    taskProps.getScheduleTime());
            if (paramsMap != null) {
                args = ParameterUtils.convertParameterPlaceholders(args, ParamUtils.convert(paramsMap));
            }
            sparkParameters.setMainArgs(args);
        }

        List<String> args = new ArrayList<>();

        //spark version
        String sparkCommand = SPARK2_COMMAND;

        if (SparkVersion.SPARK1.name().equals(sparkParameters.getSparkVersion())) {
            sparkCommand = SPARK1_COMMAND;
        }

        args.add(sparkCommand);

        // other parameters
        args.addAll(SparkArgsUtils.buildArgs(sparkParameters));

        String sparkArgs = String.join(" ", args);

        logger.info("spark task command : {}", sparkArgs);

        Assert.assertEquals(SPARK2_COMMAND, sparkArgs.split(" ")[0]);

    }

    @Test
    public void testSparkTaskInitWithQueue() {
        String param = new StringBuilder().append("{")
                .append("\"mainClass\":\"com.test.main\",")
                .append("\"mainJar\":{\"id\":1},")
                .append("\"deployMode\":\"cluster\",")
                .append("\"resourceList\":[],")
                .append("\"localParams\":[],")
                .append("\"driverCores\":1,")
                .append("\"driverMemory\":\"512M\",")
                .append("\"numExecutors\":2,")
                .append("\"executorMemory\":\"2G\",")
                .append("\"executorCores\":2,")
                .append("\"mainArgs\":\"\",")
                .append("\"others\":\"\",")
                .append("\"programType\":\"SCALA\",")
                .append("\"sparkVersion\":\"SPARK2\",")
                .append("\"queue\":\"queueB\"")
                .append("}").toString();
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

        PowerMockito.mockStatic(OSUtils.class);
        ProcessService processService = PowerMockito.mock(ProcessService.class);

        Resource resource = PowerMockito.mock(Resource.class);
        Mockito.when(resource.getFullName()).thenReturn("/");
        Mockito.when(processService.getResourceById(1)).thenReturn(resource);

        ApplicationContext applicationContext = PowerMockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        PowerMockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskProps props = new TaskProps();
        props.setExecutePath("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstanceId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams(param);

        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("1");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");
        Mockito.when(taskExecutionContext.getQueue()).thenReturn("testQueue");
        SparkTask sparkTask = new SparkTask(taskExecutionContext, logger);

        sparkTask.init();
        SparkParameters sparkParameters = (SparkParameters) sparkTask.getParameters();
        Assert.assertEquals("queueB", sparkParameters.getQueue());

    }

    @Test
    public void testSparkTaskInitWithNoQueue() {
        String param = new StringBuilder().append("{")
                .append("\"mainClass\":\"com.test.main\",")
                .append("\"mainJar\":{\"id\":1},")
                .append("\"deployMode\":\"cluster\",")
                .append("\"resourceList\":[],")
                .append("\"localParams\":[],")
                .append("\"driverCores\":1,")
                .append("\"driverMemory\":\"512M\",")
                .append("\"numExecutors\":2,")
                .append("\"executorMemory\":\"2G\",")
                .append("\"executorCores\":2,")
                .append("\"mainArgs\":\"\",")
                .append("\"others\":\"\",")
                .append("\"programType\":\"SCALA\",")
                .append("\"sparkVersion\":\"SPARK2\"")
                .append("}").toString();
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

        PowerMockito.mockStatic(OSUtils.class);
        ProcessService processService = PowerMockito.mock(ProcessService.class);
        Resource resource = PowerMockito.mock(Resource.class);
        Mockito.when(resource.getFullName()).thenReturn("/");
        Mockito.when(processService.getResourceById(1)).thenReturn(resource);

        ApplicationContext applicationContext = PowerMockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        PowerMockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskProps props = new TaskProps();
        props.setExecutePath("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstanceId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams(param);

        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("1");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");
        Mockito.when(taskExecutionContext.getQueue()).thenReturn("testQueue");
        SparkTask sparkTask = new SparkTask(taskExecutionContext, logger);

        sparkTask.init();
        SparkParameters sparkParameters = (SparkParameters) sparkTask.getParameters();
        Assert.assertEquals("testQueue", sparkParameters.getQueue());
    }
}

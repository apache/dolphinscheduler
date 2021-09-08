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

package org.apache.dolphinscheduler.plugin.task.tis;

import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;
import static com.github.dreamhead.moco.Runner.running;

import org.apache.dolphinscheduler.spi.task.ExecutionStatus;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dreamhead.moco.HttpServer;

public class TISTaskTest {
    private static final Logger logger = LoggerFactory.getLogger(TISTaskTest.class);
    private TISTask tisTask;

    private TaskRequest taskExecutionContext;

    @Before
    public void before() throws Exception {
    /*
        TaskProps props = new TaskProps();
        props.setExecutePath("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstanceId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams("{\"targetJobName\":\"mysql_elastic\"}");

        taskExecutionContext = Mockito.mock(TaskRequest.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");
        //        Mockito.when(taskExecutionContext.getVarPool())
        //                .thenReturn("[{\"direct\":\"IN\",\"prop\":\"" + TISTask.KEY_POOL_VAR_TIS_HOST + "\",\"type\":\"VARCHAR\",\"value\":\"127.0.0.1:8080\"}]");
        Map<String, String> gloabParams = Collections.singletonMap(TISTask.KEY_POOL_VAR_TIS_HOST, "127.0.0.1:8080");
        Mockito.when(taskExecutionContext.getDefinedParams()).thenReturn(gloabParams);

        tisTask = PowerMockito.spy(new TISTask(taskExecutionContext));
        tisTask.init();*/

    }

    /**
     * Method: DataxTask()
     */
    @Test
    public void testDataxTask() {
       /*     throws Exception {
        TaskProps props = new TaskProps();
        props.setExecutePath("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstanceId(1);
        props.setTenantCode("1");
        Assert.assertNotNull(new TISTask(null, logger));*/
    }

    @Test
    public void testInit()
            throws Exception {
        try {
            tisTask.init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testHandle()
            throws Exception {
        HttpServer server = jsonHttpServer(8080, pathResource("org/apache/dolphinscheduler/plugin/task/tis/TISTaskTest.json"));

        running(server, () -> {
            tisTask.handle();

            Assert.assertEquals("TIS execute be success", ExecutionStatus.SUCCESS, tisTask.getExitStatus());
        });
    }

    private String loadResContent(String resName) {
        try (InputStream i = this.getClass().getResourceAsStream(resName)) {
            Objects.requireNonNull(i, "resource " + resName + " relevant stream content can not be null");
            String content = IOUtils.toString(i, StandardCharsets.UTF_8);

            return content;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCancelApplication()
            throws Exception {
        try {
            tisTask.cancelApplication(true);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}

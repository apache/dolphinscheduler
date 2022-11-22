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

package org.apache.dolphinscheduler.plugin.task.pigeon;

import static com.github.dreamhead.moco.Moco.file;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;
import static com.github.dreamhead.moco.Runner.running;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dreamhead.moco.HttpServer;

public class PigeonTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(PigeonTaskTest.class);
    private PigeonTask pigeonTask;

    private TaskExecutionContext taskExecutionContext;

    @BeforeEach
    public void before() throws Exception {

        String taskParams = "{\"targetJobName\":\"mysql_elastic\"}";

        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskLogName()).thenReturn("pigeonlogger");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(taskParams);
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(System.currentTimeMillis());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");
        // Mockito.when(taskExecutionContext.getVarPool())
        // .thenReturn("[{\"direct\":\"IN\",\"prop\":\"" + TISTask.KEY_POOL_VAR_TIS_HOST +
        // "\",\"type\":\"VARCHAR\",\"value\":\"127.0.0.1:8080\"}]");
        Map<String, String> gloabParams =
                Collections.singletonMap(PigeonTask.KEY_POOL_VAR_PIGEON_HOST, "127.0.0.1:8080");
        Mockito.when(taskExecutionContext.getDefinedParams()).thenReturn(gloabParams);

        pigeonTask = new PigeonTask(taskExecutionContext);
        pigeonTask.init();

    }

    @Test
    public void testGetTISConfigParams() {
        PigeonConfig cfg = PigeonConfig.getInstance();
        String tisHost = "127.0.0.1:8080";
        Assertions.assertEquals("http://127.0.0.1:8080/tjs/coredefine/coredefine.ajax", cfg.getJobTriggerUrl(tisHost));
        String jobName = "mysql_elastic";
        int taskId = 123;
        Assertions.assertEquals(
                "ws://" + tisHost + "/tjs/download/logfeedback?logtype=full&collection=mysql_elastic&taskid=" + taskId,
                cfg.getJobLogsFetchUrl(tisHost, jobName, taskId));

        Assertions.assertEquals("action=datax_action&emethod=trigger_fullbuild_task", cfg.getJobTriggerPostBody());

        Assertions.assertEquals(
                "http://127.0.0.1:8080/tjs/config/config.ajax?action=collection_action&emethod=get_task_status",
                cfg.getJobStatusUrl(tisHost));

        Assertions.assertEquals("{\n taskid: " + taskId + "\n, log: false }", cfg.getJobStatusPostBody(taskId));

        Assertions.assertEquals("action=core_action&event_submit_do_cancel_task=y&taskid=" + taskId,
                cfg.getJobCancelPostBody(taskId));
    }

    @Test
    public void testInit() throws Exception {
        try {
            pigeonTask.init();
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void testHandle() throws Exception {
        HttpServer server = jsonHttpServer(8080,
                file("src/test/resources/org/apache/dolphinscheduler/plugin/task/pigeon/PigeonTaskTest.json"));

        running(server, () -> {
            pigeonTask.handle(null);

            Assertions.assertEquals(TaskExecutionStatus.SUCCESS, pigeonTask.getExitStatus());
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

    // @Test
    // public void testCancelApplication()
    // throws Exception {
    // try {
    // tisTask.cancelApplication(true);
    // } catch (Exception e) {
    // Assertions.fail(e.getMessage());
    // }
    // }

}

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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.stream.StreamTask;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlinkStreamTask extends FlinkTask implements StreamTask {

    /**
     * flink parameters
     */
    private FlinkStreamParameters flinkParameters;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    public FlinkStreamTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {

        flinkParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), FlinkStreamParameters.class);
        log.info("Initialize Flink task params {}", JSONUtils.toPrettyJsonString(flinkParameters));

        if (flinkParameters == null || !flinkParameters.checkParameters()) {
            throw new RuntimeException("flink task params is not valid");
        }

        FileUtils.generateScriptFile(taskExecutionContext, flinkParameters);
    }

    /**
     * create command
     *
     * @return command
     */
    @Override
    protected String getScript() {
        // flink run/run-application [OPTIONS] <jar-file> <arguments>
        List<String> args = FlinkArgsUtils.buildRunCommandLine(taskExecutionContext, flinkParameters);
        return args.stream().collect(Collectors.joining(" "));
    }

    @Override
    protected Map<String, String> getProperties() {
        return taskExecutionContext.getDefinedParams();
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {
        List<String> appIds = getApplicationIds();
        if (CollectionUtils.isEmpty(appIds)) {
            log.error("can not get appId, taskInstanceId:{}", taskExecutionContext.getTaskInstanceId());
            return;
        }
        taskExecutionContext.setAppIds(String.join(TaskConstants.COMMA, appIds));
        List<String> args = FlinkArgsUtils.buildCancelCommandLine(taskExecutionContext);

        log.info("cancel application args:{}", args);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(args);
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new TaskException("cancel application error", e);
        }
    }

    @Override
    public void savePoint() throws Exception {
        List<String> appIds = getApplicationIds();
        if (CollectionUtils.isEmpty(appIds)) {
            log.warn("can not get appId, taskInstanceId:{}", taskExecutionContext.getTaskInstanceId());
            return;
        }

        taskExecutionContext.setAppIds(String.join(TaskConstants.COMMA, appIds));
        List<String> args = FlinkArgsUtils.buildSavePointCommandLine(taskExecutionContext);
        log.info("savepoint args:{}", args);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(args);
        processBuilder.start();
    }
}

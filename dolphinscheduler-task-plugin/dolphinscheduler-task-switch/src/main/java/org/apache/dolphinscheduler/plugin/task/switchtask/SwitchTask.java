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

package org.apache.dolphinscheduler.plugin.task.switchtask;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

/**
 * switch task
 */
public class SwitchTask extends AbstractTaskExecutor {

    /**
     * switch parameters
     */
    private SwitchParameters switchParameters;

    /**
     * switch command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public SwitchTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void init() {
        logger.info("switch task params {}", taskExecutionContext.getTaskParams());

        switchParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SwitchParameters.class);

        if (!switchParameters.checkParameters()) {
            throw new RuntimeException("switch task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    @Override
    public AbstractParameters getParameters() {
        return switchParameters;
    }
}

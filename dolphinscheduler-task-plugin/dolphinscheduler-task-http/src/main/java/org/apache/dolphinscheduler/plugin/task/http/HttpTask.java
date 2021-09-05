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

package org.apache.dolphinscheduler.plugin.task.http;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

public class HttpTask extends AbstractTaskExecutor {

    /**
     * taskExecutionContext
     */
    private TaskRequest taskExecutionContext;

    private HttpParameters httpParameters;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public HttpTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void init() {
        logger.info("http task params {}", taskExecutionContext.getTaskParams());
        this.httpParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), HttpParameters.class);

        if (!httpParameters.checkParameters()) {
            throw new RuntimeException("http task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {

    }

    @Override
    public AbstractParameters getParameters() {
        return null;
    }
}

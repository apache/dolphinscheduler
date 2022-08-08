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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.seatunnel.flink.SeatunnelFlinkTask;
import org.apache.dolphinscheduler.plugin.task.seatunnel.spark.SeatunnelSparkTask;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

public class SeatunnelTaskChannel implements TaskChannel {

    @Override
    public void cancelApplication(boolean status) {

    }

    @Override
    public SeatunnelTask createTask(TaskExecutionContext taskRequest) {
        SeatunnelParameters seatunnelParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), SeatunnelParameters.class);
        if (EngineEnum.FLINK == seatunnelParameters.getEngine()) {
            return new SeatunnelFlinkTask(taskRequest);
        } else if (EngineEnum.SPARK == seatunnelParameters.getEngine()) {
            return new SeatunnelSparkTask(taskRequest);
        }
        throw new IllegalArgumentException("Unsupported engine type:" + seatunnelParameters.getEngine());
    }

    @Override
    public AbstractParameters parseParameters(ParametersNode parametersNode) {
        return JSONUtils.parseObject(parametersNode.getTaskParams(), SeatunnelParameters.class);
    }

    @Override
    public ResourceParametersHelper getResources(String parameters) {
        return null;
    }

}

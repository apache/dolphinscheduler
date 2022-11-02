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

package org.apache.dolphinscheduler.plugin.task.seatunnel.spark;

import static org.apache.dolphinscheduler.plugin.task.seatunnel.Constants.DEPLOY_MODE_OPTIONS;
import static org.apache.dolphinscheduler.plugin.task.seatunnel.Constants.MASTER_OPTIONS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.seatunnel.DeployModeEnum;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelTask;
import org.apache.dolphinscheduler.plugin.task.seatunnel.spark.SeatunnelSparkParameters.MasterTypeEnum;

import java.util.List;

public class SeatunnelSparkTask extends SeatunnelTask {

    private SeatunnelSparkParameters seatunnelParameters;
    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public SeatunnelSparkTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void init() {
        seatunnelParameters =
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SeatunnelSparkParameters.class);
        setSeatunnelParameters(seatunnelParameters);
        super.init();
    }

    @Override
    public List<String> buildOptions() throws Exception {
        List<String> args = super.buildOptions();
        args.add(DEPLOY_MODE_OPTIONS);
        args.add(seatunnelParameters.getDeployMode().getCommand());

        MasterTypeEnum master = DeployModeEnum.local == seatunnelParameters.getDeployMode() ? MasterTypeEnum.LOCAL
                : seatunnelParameters.getMaster();

        args.add(MASTER_OPTIONS);
        args.add(master.getCommand());
        if (MasterTypeEnum.SPARK.equals(master) || MasterTypeEnum.MESOS.equals(master)) {
            args.add(seatunnelParameters.getMasterUrl());
        }

        return args;
    }
}

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

import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.plugin.task.seatunnel.DeployModeEnum;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelParameters;

import java.util.Objects;

public class SeatunnelSparkParameters extends SeatunnelParameters {

    private DeployModeEnum deployMode;
    private MasterTypeEnum master;
    private String masterUrl;
    private String queue;

    @Override
    public boolean checkParameters() {
        return super.checkParameters()
                && Objects.nonNull(deployMode)
                && (DeployModeEnum.local != deployMode && Objects.nonNull(master))
                && (DeployModeEnum.local != deployMode && (MasterTypeEnum.SPARK == master || MasterTypeEnum.MESOS == master) && StringUtils.isNotBlank(masterUrl));
    }

    public static enum MasterTypeEnum {
        YARN("yarn"),
        LOCAL("local"),
        SPARK("spark://"),
        MESOS("mesos://");

        private String command;

        MasterTypeEnum(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }

    public DeployModeEnum getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(DeployModeEnum deployMode) {
        this.deployMode = deployMode;
    }

    public MasterTypeEnum getMaster() {
        return master;
    }

    public void setMaster(MasterTypeEnum master) {
        this.master = master;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }
}

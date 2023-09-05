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

import org.apache.dolphinscheduler.plugin.task.seatunnel.DeployModeEnum;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SeatunnelSparkParameters extends SeatunnelParameters {

    private DeployModeEnum deployMode;
    private MasterTypeEnum master;
    private String masterUrl;

    @Override
    public boolean checkParameters() {
        boolean result = super.checkParameters() && Objects.nonNull(deployMode);
        if (result && DeployModeEnum.local != deployMode) {
            result = Objects.nonNull(master);
            if (result && (MasterTypeEnum.SPARK == master || MasterTypeEnum.MESOS == master)) {
                result = StringUtils.isNotBlank(masterUrl);
            }
        }
        return result;
    }

    @Getter
    public enum MasterTypeEnum {

        YARN("yarn"),
        LOCAL("local"),
        SPARK("spark://"),
        MESOS("mesos://");

        private final String command;

        MasterTypeEnum(String command) {
            this.command = command;
        }
    }
}

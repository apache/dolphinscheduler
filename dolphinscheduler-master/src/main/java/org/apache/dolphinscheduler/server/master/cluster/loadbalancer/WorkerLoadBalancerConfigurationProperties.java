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

package org.apache.dolphinscheduler.server.master.cluster.loadbalancer;

import lombok.Data;

import org.springframework.validation.Errors;

@Data
public class WorkerLoadBalancerConfigurationProperties {

    private WorkerLoadBalancerType type = WorkerLoadBalancerType.ROUND_ROBIN;

    private DynamicWeightConfigProperties dynamicWeightConfigProperties = new DynamicWeightConfigProperties();

    public void validate(Errors errors) {
        dynamicWeightConfigProperties.validated(errors);
    }

    @Data
    public static class DynamicWeightConfigProperties {

        private int cpuUsageWeight = 30;

        private int memoryUsageWeight = 30;

        private int taskThreadPoolUsageWeight = 40;

        public void validated(Errors errors) {
            if (cpuUsageWeight < 0) {
                errors.rejectValue("cpuUsageWeight", "cpuUsageWeight", "cpuUsageWeight must >= 0");
            }
            if (memoryUsageWeight < 0) {
                errors.rejectValue("memoryUsageWeight", "memoryUsageWeight", "memoryUsageWeight must >= 0");
            }
            if (taskThreadPoolUsageWeight < 0) {
                errors.rejectValue("threadUsageWeight", "threadUsageWeight", "threadUsageWeight must >= 0");
            }
            if (cpuUsageWeight + memoryUsageWeight + taskThreadPoolUsageWeight != 100) {
                errors.rejectValue("cpuUsageWeight", "cpuUsageWeight",
                        "cpuUsageWeight + memoryUsageWeight + threadUsageWeight must be 100");
            }
        }

    }
}

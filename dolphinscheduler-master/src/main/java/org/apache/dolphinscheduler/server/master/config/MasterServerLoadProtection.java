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

package org.apache.dolphinscheduler.server.master.config;

import org.apache.dolphinscheduler.meter.metrics.BaseServerLoadProtection;
import org.apache.dolphinscheduler.meter.metrics.SystemMetrics;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterServerLoadProtection extends BaseServerLoadProtection {

    private final IWorkflowRepository workflowRepository;

    private final MasterServerLoadProtectionConfig masterServerLoadProtectionConfig;

    public MasterServerLoadProtection(IWorkflowRepository workflowRepository,
                                      MasterConfig masterConfig) {
        super(masterConfig.getServerLoadProtection());
        this.masterServerLoadProtectionConfig = masterConfig.getServerLoadProtection();
        this.workflowRepository = workflowRepository;
    }

    @Override
    public boolean isOverload(SystemMetrics systemMetrics) {
        if (!masterServerLoadProtectionConfig.isEnabled()) {
            return false;
        }

        if (super.isOverload(systemMetrics)) {
            return true;
        }

        // Check workflow instance count
        int currentWorkflowInstanceCount = workflowRepository.getAll().size();
        if (currentWorkflowInstanceCount >= masterServerLoadProtectionConfig.getMaxConcurrentWorkflowInstances()) {
            log.info(
                    "OverLoad: the workflow instance count: {} exceeds the maxConcurrentWorkflowInstances {}",
                    currentWorkflowInstanceCount, masterServerLoadProtectionConfig.getMaxConcurrentWorkflowInstances());
            return true;
        }
        return false;
    }
}

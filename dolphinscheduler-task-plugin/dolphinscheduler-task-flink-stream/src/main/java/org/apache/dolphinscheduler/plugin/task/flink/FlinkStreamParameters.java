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

import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.flink.enums.FlinkStreamDeployMode;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * flink stream parameters
 */
@Getter
@Setter
public class FlinkStreamParameters extends AbstractParameters {

    /**
     * major jar
     */
    private ResourceInfo mainJar;

    /**
     * deploy mode  yarn-per-job yarn-application
     */
    private FlinkStreamDeployMode deployMode;

    /**
     * arguments
     */
    private String mainArgs;

    /**
     * slot count
     */
    private int slot;

    /**
     * parallelism
     */
    private int parallelism;

    /**
     * yarn application name
     */
    private String appName;

    /**
     * taskManager count
     */
    private int taskManager;

    /**
     * job manager memory
     */
    private String jobManagerMemory;

    /**
     * task manager memory
     */
    private String taskManagerMemory;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList = new ArrayList<>();

    /**
     * The YARN queue to submit to
     */
    private String queue;

    /**
     * other arguments
     */
    private String others;

    /**
     * program type
     * 0 JAVA,1 SCALA,2 PYTHON,3 SQL
     */
    private ProgramType programType;

    /**
     * flink sql initialization file
     */
    private String initScript;

    /**
     * flink sql script file
     */
    private String rawScript;

    @Override
    public boolean checkParameters() {
        return programType != null &&  mainJar != null;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        if (mainJar != null && !resourceList.contains(mainJar)) {
            resourceList.add(mainJar);
        }
        return resourceList;
    }
}

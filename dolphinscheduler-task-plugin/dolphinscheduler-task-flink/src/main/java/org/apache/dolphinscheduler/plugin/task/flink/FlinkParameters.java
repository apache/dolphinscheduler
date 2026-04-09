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

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class FlinkParameters extends AbstractParameters {

    /**
     * major jar
     */
    private ResourceInfo mainJar;

    /**
     * major class
     */
    private String mainClass;

    /**
     * deploy mode  yarn-cluster yarn-local yarn-application
     */
    private FlinkDeployMode deployMode;

    /**
     * arguments
     */
    private String mainArgs;

    /**
     * slot count
     */
    private int slot;

    private int parallelism;

    /**
     * yarn application name
     */
    private String appName;

    /**
     * taskManager count
     */
    private int taskManager;

    private String jobManagerMemory;

    private String taskManagerMemory;

    private List<ResourceInfo> resourceList = new ArrayList<>();

    /**
     * The YARN queue to submit to
     */
    private String yarnQueue;

    /**
     * other arguments
     */
    private String others;

    private String flinkVersion;

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
        /**
         * When saving a task, the parameter cannot be empty. There are two judgments:
         * (1) When ProgramType is SQL, rawScript cannot be empty.
         * (2) When ProgramType is Java/Scala/Python, mainJar cannot be empty.
         */
        return programType != null && (rawScript != null || mainJar != null);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        if (mainJar != null && !resourceList.contains(mainJar)) {
            resourceList.add(mainJar);
        }
        return resourceList;
    }
}

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

package org.apache.dolphinscheduler.plugin.task.spark;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SparkParameters extends AbstractParameters {

    /**
     * main jar
     */
    private ResourceInfo mainJar;

    /**
     * main class
     */
    private String mainClass;

    /**
     * deploy mode  local / cluster / client
     */
    private String deployMode;

    /**
     * arguments
     */
    private String mainArgs;

    /**
     * driver-cores Number of cores used by the driver, only in cluster mode
     */
    private int driverCores;

    /**
     * driver-memory Memory for driver
     */

    private String driverMemory;

    /**
     * num-executors Number of executors to launch
     */
    private int numExecutors;

    /**
     * executor-cores Number of cores per executor
     */
    private int executorCores;

    /**
     * Memory per executor
     */
    private String executorMemory;

    /**
     * app name
     */
    private String appName;

    /**
     * The YARN queue to submit to
     */
    private String yarnQueue;

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
     * spark sql script
     */
    private String rawScript;

    /**
     * kubernetes cluster namespace
     */
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    private List<ResourceInfo> resourceList = new ArrayList<>();

    private String sqlExecutionType;

    @Override
    public boolean checkParameters() {
        /**
         * When saving a task, the parameters cannot be empty and mainJar or rawScript cannot be empty:
         * (1) When ProgramType is SQL, rawScript cannot be empty.
         * (2) When ProgramType is Java/Scala/Python, mainJar cannot be empty.
         */
        return programType != null && (mainJar != null || rawScript != null);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        if (mainJar != null && !resourceList.contains(mainJar)) {
            resourceList.add(mainJar);
        }
        return resourceList;
    }

}

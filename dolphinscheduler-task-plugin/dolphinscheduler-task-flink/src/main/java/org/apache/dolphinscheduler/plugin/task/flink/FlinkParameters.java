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

/**
 * flink parameters
 */
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
    private String yarnQueue;

    /**
     * other arguments
     */
    private String others;

    /**
     * flink version
     */
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

    public ResourceInfo getMainJar() {
        return mainJar;
    }

    public void setMainJar(ResourceInfo mainJar) {
        this.mainJar = mainJar;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public FlinkDeployMode getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(FlinkDeployMode deployMode) {
        this.deployMode = deployMode;
    }

    public String getMainArgs() {
        return mainArgs;
    }

    public void setMainArgs(String mainArgs) {
        this.mainArgs = mainArgs;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(int taskManager) {
        this.taskManager = taskManager;
    }

    public String getJobManagerMemory() {
        return jobManagerMemory;
    }

    public void setJobManagerMemory(String jobManagerMemory) {
        this.jobManagerMemory = jobManagerMemory;
    }

    public String getTaskManagerMemory() {
        return taskManagerMemory;
    }

    public void setTaskManagerMemory(String taskManagerMemory) {
        this.taskManagerMemory = taskManagerMemory;
    }

    public String getYarnQueue() {
        return yarnQueue;
    }

    public void setYarnQueue(String yarnQueue) {
        this.yarnQueue = yarnQueue;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public ProgramType getProgramType() {
        return programType;
    }

    public void setProgramType(ProgramType programType) {
        this.programType = programType;
    }

    public String getFlinkVersion() {
        return flinkVersion;
    }

    public void setFlinkVersion(String flinkVersion) {
        this.flinkVersion = flinkVersion;
    }

    public String getInitScript() {
        return initScript;
    }

    public void setInitScript(String initScript) {
        this.initScript = initScript;
    }

    public String getRawScript() {
        return rawScript;
    }

    public void setRawScript(String rawScript) {
        this.rawScript = rawScript;
    }

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

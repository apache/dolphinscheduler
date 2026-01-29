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

package org.apache.dolphinscheduler.plugin.task.mr;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * mapreduce parameters
 */
public class MapReduceParameters extends AbstractParameters {

    /**
     * major jar
     */
    private ResourceInfo mainJar;

    /**
     * major class
     */
    private String mainClass;

    /**
     * arguments
     */
    private String mainArgs;

    /**
     * other arguments
     */
    private String others;

    /**
     * app name
     */
    private String appName;

    /**
     * The YARN queue to submit to
     */
    private String yarnQueue;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList = new ArrayList<>();

    /**
     * program type
     * 0 JAVA,1 SCALA,2 PYTHON
     */
    private ProgramType programType;

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMainArgs() {
        return mainArgs;
    }

    public void setMainArgs(String mainArgs) {
        this.mainArgs = mainArgs;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getYarnQueue() {
        return yarnQueue;
    }

    public void setYarnQueue(String yarnQueue) {
        this.yarnQueue = yarnQueue;
    }

    public List<ResourceInfo> getResourceList() {
        return this.resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    public void setMainJar(ResourceInfo mainJar) {
        this.mainJar = mainJar;
    }

    public ResourceInfo getMainJar() {
        return mainJar;
    }

    public ProgramType getProgramType() {
        return programType;
    }

    public void setProgramType(ProgramType programType) {
        this.programType = programType;
    }

    @Override
    public boolean checkParameters() {
        return this.mainJar != null && this.programType != null;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        if (mainJar != null && !resourceList.contains(mainJar)) {
            resourceList.add(mainJar);
        }

        return resourceList;
    }

    @Override
    public String toString() {
        return "mainJar= " + mainJar
                + "mainClass=" + mainClass
                + "mainArgs=" + mainArgs
                + "yarnQueue=" + yarnQueue
                + "other mainArgs=" + others;
    }
}

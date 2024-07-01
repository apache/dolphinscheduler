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

package org.apache.dolphinscheduler.plugin.task.java;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.Data;

@Data
public class JavaParameters extends AbstractParameters {

    /**
     * origin java script
     */
    private String rawScript;

    /**
     * run in jar file
     */
    private ResourceInfo mainJar;

    /**
     * Marks the current task running mode
     */
    private String runType;

    /**
     * main method args
     **/
    private String mainArgs;

    /**
     * java virtual machine args
     **/
    private String jvmArgs;

    /**
     * module path or class path flag
     **/
    private boolean isModulePath;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    /**
     * Check that the parameters are valid
     *
     * @returnboolean
     */
    @Override
    public boolean checkParameters() {
        return runType != null && (StringUtils.isNotBlank(rawScript) || mainJar != null);
    }

    /**
     * Gets a list of known resource files
     *
     * @return List<ResourceInfo>
     **/
    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return this.resourceList;
    }
}

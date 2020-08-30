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
package org.apache.dolphinscheduler.common.task.sqoop;

import org.apache.dolphinscheduler.common.enums.SqoopJobType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * sqoop parameters
 */
public class SqoopParameters  extends AbstractParameters {

    /**
     * sqoop job type:
     * CUSTOM - custom sqoop job
     * TEMPLATE - sqoop template job
     */
    private String jobType;

    /**
     * customJob eq 1, use customShell
     */
    private String customShell;

    /**
     * sqoop job name - map-reduce job name
     */
    private String jobName;

    /**
     * model type
     */
    private String modelType;
    /**
     * concurrency
     */
    private int concurrency;
    /**
     * source type
     */
    private String sourceType;
    /**
     * target type
     */
    private String targetType;
    /**
     * source params
     */
    private String sourceParams;
    /**
     * target params
     */
    private String targetParams;

    /**
     * hadoop custom param for sqoop job
     */
    private List<Property> hadoopCustomParams;

    /**
     * sqoop advanced param
     */
    private List<Property> sqoopAdvancedParams;

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getSourceParams() {
        return sourceParams;
    }

    public void setSourceParams(String sourceParams) {
        this.sourceParams = sourceParams;
    }

    public String getTargetParams() {
        return targetParams;
    }

    public void setTargetParams(String targetParams) {
        this.targetParams = targetParams;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getCustomShell() {
        return customShell;
    }

    public void setCustomShell(String customShell) {
        this.customShell = customShell;
    }

    public List<Property> getHadoopCustomParams() {
        return hadoopCustomParams;
    }

    public void setHadoopCustomParams(List<Property> hadoopCustomParams) {
        this.hadoopCustomParams = hadoopCustomParams;
    }

    public List<Property> getSqoopAdvancedParams() {
        return sqoopAdvancedParams;
    }

    public void setSqoopAdvancedParams(List<Property> sqoopAdvancedParams) {
        this.sqoopAdvancedParams = sqoopAdvancedParams;
    }

    @Override
    public boolean checkParameters() {

        boolean sqoopParamsCheck = false;

        if (StringUtils.isEmpty(jobType)) {
            return sqoopParamsCheck;
        }

        if (SqoopJobType.TEMPLATE.getDescp().equals(jobType)) {
            sqoopParamsCheck = StringUtils.isEmpty(customShell) &&
                    StringUtils.isNotEmpty(modelType) &&
                    StringUtils.isNotEmpty(jobName) &&
                    concurrency != 0 &&
                    StringUtils.isNotEmpty(sourceType) &&
                    StringUtils.isNotEmpty(targetType) &&
                    StringUtils.isNotEmpty(sourceParams) &&
                    StringUtils.isNotEmpty(targetParams);
        } else if (SqoopJobType.CUSTOM.getDescp().equals(jobType)) {
            sqoopParamsCheck = StringUtils.isNotEmpty(customShell) &&
                    StringUtils.isEmpty(jobName);
        }

        return sqoopParamsCheck;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }
}

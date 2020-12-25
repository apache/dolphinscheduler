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

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(modelType)&&
                concurrency != 0 &&
                StringUtils.isNotEmpty(sourceType)&&
                StringUtils.isNotEmpty(targetType)&&
                StringUtils.isNotEmpty(sourceParams)&&
                StringUtils.isNotEmpty(targetParams);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
       return new ArrayList<>();
    }
}

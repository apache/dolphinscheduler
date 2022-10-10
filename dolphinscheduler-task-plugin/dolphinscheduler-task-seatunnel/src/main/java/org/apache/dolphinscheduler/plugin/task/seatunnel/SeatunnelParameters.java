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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.flink.SeatunnelFlinkParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.spark.SeatunnelSparkParameters;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "engine")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SeatunnelFlinkParameters.class, name = "FLINK"),
    @JsonSubTypes.Type(value = SeatunnelSparkParameters.class, name = "SPARK")
})
public class SeatunnelParameters extends AbstractParameters {

    private EngineEnum engine;

    private Boolean useCustom;

    private String rawScript;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    public EngineEnum getEngine() {
        return engine;
    }

    public void setEngine(EngineEnum engine) {
        this.engine = engine;
    }

    public Boolean getUseCustom() {
        return useCustom;
    }

    public void setUseCustom(Boolean useCustom) {
        this.useCustom = useCustom;
    }

    public String getRawScript() {
        return rawScript;
    }

    public void setRawScript(String rawScript) {
        this.rawScript = rawScript;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    @Override
    public boolean checkParameters() {
        return Objects.nonNull(engine)
                && ((BooleanUtils.isTrue(useCustom) && StringUtils.isNotBlank(rawScript))
                || (BooleanUtils.isFalse(useCustom) && CollectionUtils.isNotEmpty(resourceList) && resourceList.size() == 1));
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return resourceList;
    }
}

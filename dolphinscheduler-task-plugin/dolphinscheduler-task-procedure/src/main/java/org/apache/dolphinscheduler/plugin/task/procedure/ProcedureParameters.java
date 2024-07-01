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

package org.apache.dolphinscheduler.plugin.task.procedure;

import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * procedure parameter
 */
public class ProcedureParameters extends AbstractParameters {

    /**
     * data source type，eg  MYSQL, POSTGRES, HIVE ...
     */
    private String type;

    /**
     * data source id
     */
    private int datasource;

    private Map<String, Property> outProperty;

    /**
     * procedure name
     */
    private String method;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDatasource() {
        return datasource;
    }

    public void setDatasource(int datasource) {
        this.datasource = datasource;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean checkParameters() {
        return datasource != 0 && StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(method);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ProcessdureParam{"
                + "type='" + type + '\''
                + ", datasource=" + datasource
                + ", method='" + method + '\''
                + '}';
    }

    public void dealOutParam4Procedure(Object result, String pop) {
        Map<String, Property> properties = getOutProperty();
        if (this.outProperty == null) {
            return;
        }
        properties.get(pop).setValue(String.valueOf(result));
        varPool.add(properties.get(pop));
    }

    public Map<String, Property> getOutProperty() {
        if (this.outProperty != null) {
            return this.outProperty;
        }
        if (CollectionUtils.isEmpty(localParams)) {
            return null;
        }
        List<Property> outPropertyList = getOutProperty(localParams);
        Map<String, Property> outProperty = new HashMap<>();
        for (Property info : outPropertyList) {
            outProperty.put(info.getProp(), info);
        }
        this.outProperty = outProperty;
        return this.outProperty;
    }

    public void setOutProperty(Map<String, Property> outProperty) {
        this.outProperty = outProperty;
    }

    @Override
    public ResourceParametersHelper getResources() {
        ResourceParametersHelper resources = super.getResources();
        resources.put(ResourceType.DATASOURCE, datasource);
        return resources;
    }

    public ProcedureTaskExecutionContext generateExtendedContext(ResourceParametersHelper parametersHelper) {
        DataSourceParameters dataSourceParameters =
                (DataSourceParameters) parametersHelper.getResourceParameters(ResourceType.DATASOURCE, datasource);
        ProcedureTaskExecutionContext procedureTaskExecutionContext = new ProcedureTaskExecutionContext();
        procedureTaskExecutionContext.setConnectionParams(
                Objects.nonNull(dataSourceParameters) ? dataSourceParameters.getConnectionParams() : null);
        return procedureTaskExecutionContext;
    }
}

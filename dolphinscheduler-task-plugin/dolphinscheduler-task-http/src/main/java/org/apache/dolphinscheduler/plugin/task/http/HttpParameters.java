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

package org.apache.dolphinscheduler.plugin.task.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http parameter
 */
public class HttpParameters extends AbstractParameters {
    /**
     * url
     */
    private String url;

    /**
     * httpMethod
     */
    private HttpMethod httpMethod;

    /**
     *  http params
     */
    private List<HttpProperty> httpParams;

    /**
     * httpCheckCondition
     */
    private HttpCheckCondition httpCheckCondition = HttpCheckCondition.STATUS_CODE_DEFAULT;

    /**
     * condition
     */
    private String condition;

    /**
     * Connect Timeout
     * Unit: ms
     */
    private int connectTimeout;

    /**
     * Socket Timeout
     * Unit: ms
     */
    private int socketTimeout;

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(url);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<HttpProperty> getHttpParams() {
        return httpParams;
    }

    public void setHttpParams(List<HttpProperty> httpParams) {
        this.httpParams = httpParams;
    }

    public HttpCheckCondition getHttpCheckCondition() {
        return httpCheckCondition;
    }

    public void setHttpCheckCondition(HttpCheckCondition httpCheckCondition) {
        this.httpCheckCondition = httpCheckCondition;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    @Override
    public void dealOutParam(String result) {
        if (CollectionUtils.isEmpty(localParams)) {
            return;
        }
        List<Property> outProperty = getOutProperty(localParams);
        if (CollectionUtils.isEmpty(outProperty)) {
            return;
        }
        if (StringUtils.isEmpty(result)) {
            varPool.addAll(outProperty);
            return;
        }
        List<Map<String, String>> httpMapByString = getHttpMapByString(result);
        if (httpMapByString == null || httpMapByString.size() == 0) {
            return;
        }

        for (Property info : outProperty) {
            for (int i = 0; i < httpMapByString.size() ; i++) {
                Map<String, String> stringStringMap=httpMapByString.get(i);
                info.setValue(stringStringMap.get(info.getProp()));

                // If varPool contains some key objects delete and add again
                List<Property> tmpList = new ArrayList<>(varPool);
                for (Property p : varPool) {
                    if (p.getProp().equals(info.getProp())) {
                        tmpList.remove(p);
                    }
                }
                varPool = tmpList;
                varPool.add(info);
            }
        }
    }

    protected String setBodyReturn(String updateResult, List<Property> properties) {
        String result = null;
        List<Map<String, String>> updateRL = new ArrayList<>();
        Map<String, String> updateRM = new HashMap<>();
        for (Property info : properties) {
            if (Direct.OUT == info.getDirect()) {
                updateRM.put(info.getProp(), updateResult);
                updateRL.add(updateRM);
                result = JSONUtils.toJsonString(updateRL);
            }
        }
        return result;
    }

    /**
     * Convert the body result returned from HTTP to a map
     * @param result
     * @return
     */
    public List<Map<String, String>> getHttpMapByString(String result) {
        List<Map<String, String>> allParams = new ArrayList<>();
        ArrayNode paramsByJson = JSONUtils.parseArray(result);
        for (JsonNode jsonNode : paramsByJson) {
            Map<String, String> param = JSONUtils.toMap(jsonNode.toString());
            allParams.add(param);
        }
        return allParams;
    }
}

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
package org.apache.dolphinscheduler.common.task.http;

import org.apache.dolphinscheduler.common.enums.HttpCheckCondition;
import org.apache.dolphinscheduler.common.enums.HttpMethod;
import org.apache.dolphinscheduler.common.process.HttpProperty;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

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



    @Override
    public boolean checkParameters() {
        return  StringUtils.isNotEmpty(url);
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
}

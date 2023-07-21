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

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * http parameter
 */
@Data
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
}

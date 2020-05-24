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

package org.apache.dolphinscheduler.spi.params;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * definition the plugin params
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextParam.class, name = "_type"),
        @JsonSubTypes.Type(value = RadioParam.class, name = "_type"),
})
public abstract class AbsPluginParams {

    /**
     * param name
     */
    private String name;

    /**
     * show in web ui English Name
     */
    private String showNameEn;

    /**
     * show in web ui Chinese Name
     */
    private String showNameCh;

    public AbsPluginParams(String name, String showNameEn, String showNameCh) {
        this.name = name;
        this.showNameCh = showNameCh;
        this.showNameEn = showNameEn;
    }
}



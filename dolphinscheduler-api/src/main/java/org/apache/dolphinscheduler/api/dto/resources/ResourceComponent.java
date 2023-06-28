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

package org.apache.dolphinscheduler.api.dto.resources;

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * resource component
 */
@Data
@NoArgsConstructor
@JsonPropertyOrder({"id", "pid", "name", "fullName", "description", "isDirctory", "children", "type"})
public abstract class ResourceComponent {

    public ResourceComponent(int id, String pid, String name, String fullName, String description, boolean isDirctory) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.fullName = fullName;
        this.isDirctory = isDirctory;
        int directoryFlag = isDirctory ? 1 : 0;
        this.idValue = String.format("%s_%s", id, directoryFlag);
    }

    /**
     * id
     */
    protected int id;
    /**
     * parent id
     */
    protected String pid;
    /**
     * name
     */
    protected String name;
    /**
     * current directory
     */
    protected String currentDir;
    /**
     * full name
     */
    protected String fullName;
    /**
     * description
     */
    protected String description;
    /**
     * is directory
     */
    protected boolean isDirctory;
    /**
     * id value
     */
    protected String idValue;
    /**
     * resoruce type
     */
    protected ResourceType type;
    /**
     * children
     */
    protected List<ResourceComponent> children = new ArrayList<>();

    /**
     * add resource component
     * @param resourceComponent resource component
     */
    public void add(ResourceComponent resourceComponent) {
        children.add(resourceComponent);
    }

    public void setIdValue(int id, boolean isDirctory) {
        int directoryFlag = isDirctory ? 1 : 0;
        this.idValue = String.format("%s_%s", id, directoryFlag);
    }
}

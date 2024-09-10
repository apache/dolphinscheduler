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

/**
 * resource component
 */
@Data
@NoArgsConstructor
public abstract class ResourceComponent {

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
     * resoruce type
     */
    protected ResourceType type;
    /**
     * children
     */
    protected List<ResourceComponent> children = new ArrayList<>();

    /**
     * add resource component
     *
     * @param resourceComponent resource component
     */
    public void add(ResourceComponent resourceComponent) {
        children.add(resourceComponent);
    }

}

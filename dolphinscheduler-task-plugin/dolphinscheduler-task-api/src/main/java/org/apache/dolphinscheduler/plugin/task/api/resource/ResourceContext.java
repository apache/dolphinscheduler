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

package org.apache.dolphinscheduler.plugin.task.api.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
public class ResourceContext {

    /**
     * ResourceAbsolutePathInStorage -> ResourceItem
     */
    private final Map<String, ResourceItem> resourceItemMap;

    public ResourceContext() {
        this.resourceItemMap = new HashMap<>();
    }

    public void addResourceItem(ResourceItem resourceItem) {
        checkNotNull(resourceItem);
        resourceItemMap.put(resourceItem.getResourceAbsolutePathInStorage(), resourceItem);
    }

    public ResourceItem getResourceItem(String resourceAbsolutePathInStorage) {
        ResourceItem resourceItem = resourceItemMap.get(resourceAbsolutePathInStorage);
        if (resourceItem == null) {
            throw new IllegalArgumentException("Cannot find the resourceItem: " + resourceAbsolutePathInStorage);
        }
        return resourceItem;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResourceItem {

        private String resourceAbsolutePathInStorage;
        private String resourceRelativePath;
        private String resourceAbsolutePathInLocal;
    }

}

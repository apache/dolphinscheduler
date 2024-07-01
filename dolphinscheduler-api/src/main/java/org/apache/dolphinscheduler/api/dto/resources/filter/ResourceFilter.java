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
package org.apache.dolphinscheduler.api.dto.resources.filter;

import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * resource filter
 */
public class ResourceFilter implements IFilter {

    /**
     * resource suffix
     */
    private String suffix;
    /**
     * resource list
     */
    private List<StorageEntity> resourceList;

    /**
     * parent list
     */
    // Set<Resource> parentList = new HashSet<>();

    /**
     * constructor
     * @param suffix        resource suffix
     * @param resourceList  resource list
     */
    public ResourceFilter(String suffix, List<StorageEntity> resourceList) {
        this.suffix = suffix;
        this.resourceList = resourceList;
    }

    /**
     * file filter
     * @return file filtered by suffix
     */
    public Set<StorageEntity> fileFilter() {
        return resourceList.stream().filter(t -> t.getFullName().endsWith(suffix)).collect(Collectors.toSet());
    }

    /**
     * list all parent dir
     * @return parent resource dir set
     */
    Set<StorageEntity> listAllParent() {
        Set<StorageEntity> parentList = new HashSet<>();
        Set<StorageEntity> filterFileList = fileFilter();
        for (StorageEntity file : filterFileList) {
            String fullName = file.getFullName();
            for (StorageEntity resource : resourceList) {
                if (fullName.startsWith(resource.getFullName())) {
                    parentList.add(resource);
                }
            }
        }

        return parentList;
    }

    @Override
    public List<StorageEntity> filter() {
        return new ArrayList<>(listAllParent());
    }
}

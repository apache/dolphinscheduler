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

package org.apache.dolphinscheduler.api.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.dto.resources.visitor.ResourceTreeVisitor;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * resource utils
 */
public final class ResourceUtils {

    private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

    private ResourceUtils() {
        throw new IllegalStateException("ResourceUtils class");
    }

    /**
     * copy resource files
     *
     * @param resourceList resource list
     * @param srcBasePath src base path
     * @param dstBasePath dst base path
     */
    public static void copyResourceFiles(List<Resource> resourceList, String srcBasePath, String dstBasePath) throws IOException {
        ResourceTreeVisitor resourceTreeVisitor = new ResourceTreeVisitor(resourceList);
        ResourceComponent resourceComponent = resourceTreeVisitor.visit();
        copyResourceFiles(resourceComponent, srcBasePath, dstBasePath);
    }

    /**
     * copy resource files
     *
     * @param resourceComponent resource component
     * @param srcBasePath src base path
     * @param dstBasePath dst base path
     * @throws IOException io exception
     */
    private static void copyResourceFiles(ResourceComponent resourceComponent, String srcBasePath, String dstBasePath) throws IOException {
        List<ResourceComponent> components = resourceComponent.getChildren();

        if (CollectionUtils.isNotEmpty(components)) {
            for (ResourceComponent component : components) {
                // verify whether exist
                if (!HadoopUtils.getInstance().exists(String.format("%s/%s", srcBasePath, component.getFullName()))) {
                    logger.error("resource file: {} not exist,copy error", component.getFullName());
                    throw new ServiceException(Status.RESOURCE_NOT_EXIST);
                }

                if (!component.isDirctory()) {
                    // copy it to dst
                    HadoopUtils.getInstance().copy(String.format("%s/%s", srcBasePath, component.getFullName()), String.format("%s/%s", dstBasePath, component.getFullName()), false, true);
                    continue;
                }

                if (CollectionUtils.isEmpty(component.getChildren())) {
                    // if not exist,need create it
                    if (!HadoopUtils.getInstance().exists(String.format("%s/%s", dstBasePath, component.getFullName()))) {
                        HadoopUtils.getInstance().mkdir(String.format("%s/%s", dstBasePath, component.getFullName()));
                    }
                } else {
                    copyResourceFiles(component, srcBasePath, dstBasePath);
                }
            }
        }
    }

}

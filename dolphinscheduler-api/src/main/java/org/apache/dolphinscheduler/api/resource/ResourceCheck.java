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

package org.apache.dolphinscheduler.api.resource;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;

import com.google.common.base.Strings;

@Getter
@Setter
public class ResourceCheck {

    /**
     * logger
     */
    private Logger logger;
    /**
     * Resource Type
     */
    private ResourceType resourceType;

    /**
     * Process Service
     */
    private ProcessService processService;

    /**
     * need check resourceIds
     */
    private String resourceIds;

    /**
     * task name
     */
    private String taskName;

    /**
     * resource exist check
     * @param resourceType resource type
     * @param processService process service
     * @param resourceIds resource ids string with , combine
     * @param taskName task name
     * @param logger logger
     */
    public ResourceCheck(ResourceType resourceType, ProcessService processService, String resourceIds, String taskName,
                         Logger logger) {
        this.resourceType = resourceType;
        this.processService = processService;
        this.resourceIds = resourceIds;
        this.taskName = taskName;
        this.logger = logger;
    }

    /**
     * check all resources exist,
     * if contains removed resource throws ServiceException
     */
    public void checkAllExist() throws ServiceException {
        switch (resourceType) {
            case FILE:
                if (Strings.isNullOrEmpty(this.resourceIds)) {
                    logger.error("The given task definition has null resources str, taskName: {}", this.taskName);
                    return;
                }

                Integer[] resourceIdArray =
                        Arrays.stream(this.resourceIds.split(",")).map(Integer::parseInt).toArray(Integer[]::new);

                if (resourceIdArray.length > 0) {
                    List<Resource> list = processService.listResourceByIds(resourceIdArray);
                    if (CollectionUtils.isEmpty(list) || list.size() != resourceIdArray.length) {
                        logger.error(
                                "The given task definition has deleted resources, taskName: {}, resourceIds: {}",
                                this.taskName, this.resourceIds);
                        throw new ServiceException(
                                Status.TASK_RESOURCE_NOT_EXIST, this.taskName);
                    }
                }
                break;
            default:
                logger.error("Error resourceType: {}", this.resourceType);
                throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, this.resourceType);
        }
    }
}

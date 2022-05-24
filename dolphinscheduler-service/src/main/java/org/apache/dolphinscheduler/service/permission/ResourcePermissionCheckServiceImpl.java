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

package org.apache.dolphinscheduler.service.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toSet;


/**
 * resource permission check
 */
@Component
@Service
public class ResourcePermissionCheckServiceImpl implements ResourcePermissionCheckService<Object>, ApplicationContextAware {

    @Autowired
    private ProcessService processService;

    protected static final Map<AuthorizationType, ResourceAcquisitionAndPermissionCheck<?>> RESOURCE_LIST_MAP = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        for (ResourceAcquisitionAndPermissionCheck<?> authorizedResourceList : applicationContext.getBeansOfType(ResourceAcquisitionAndPermissionCheck.class).values()) {
            List<AuthorizationType> authorizationTypes = authorizedResourceList.authorizationTypes();
            authorizationTypes.forEach(auth -> RESOURCE_LIST_MAP.put(auth, authorizedResourceList));
        }
    }

    @Override
    public boolean resourcePermissionCheck(AuthorizationType authorizationType, Object[] needChecks, int userId, Logger logger) {
        if (Objects.nonNull(needChecks) && needChecks.length > 0){
            Set<Object> originResSet = new HashSet<>(Arrays.asList(needChecks));
            Set<Object> ownResSets = RESOURCE_LIST_MAP.get(authorizationType).listAuthorizedResource(userId, logger);
            originResSet.removeAll(ownResSets);
            return originResSet.isEmpty();
        }
        return true;
    }

    @Override
    public boolean operationPermissionCheck(AuthorizationType authorizationType, int userId, String sourceUrl, Logger logger) {
        return RESOURCE_LIST_MAP.get(authorizationType).permissionCheck(userId, sourceUrl, logger);
    }

    @Override
    public <T> Set<T> userOwnedResourceIdsAcquisition(AuthorizationType authorizationType, int userId, Logger logger) {
        User user = processService.getUserById(userId);
        if (user == null){
            logger.error("user id {} doesn't exist", userId);
            return null;
        }
        return RESOURCE_LIST_MAP.get(authorizationType).listAuthorizedResource(user.getUserType().equals(UserType.ADMIN_USER) ? 0 : userId, logger);
    }

    @Component
    public static class ProjectsResourceList implements ResourceAcquisitionAndPermissionCheck<Integer> {

        private final ProjectMapper projectMapper;

        public ProjectsResourceList(ProjectMapper projectMapper) {
            this.projectMapper = projectMapper;
        }

        @Override
        public List<AuthorizationType> authorizationTypes() {
            return Collections.singletonList(AuthorizationType.PROJECTS);
        }

        @Override
        public boolean permissionCheck(int userId, String url, Logger logger) {
            return true;
        }


        @Override
        public Set<Integer> listAuthorizedResource(int userId, Logger logger) {
            return projectMapper.listAuthorizedProjects(userId, null).stream().map(Project::getId).collect(toSet());
        }
    }

    interface ResourceAcquisitionAndPermissionCheck<T> {

        /**
         * authorization types
         * @return
         */
        List<AuthorizationType> authorizationTypes();

        /**
         * get all resources under the user (no admin)
         * @param userId
         * @param <T>
         * @return
         */
        <T> Set<T> listAuthorizedResource(int userId, Logger logger);

        /**
         * permission check
         * @param userId
         * @return
         */
        boolean permissionCheck(int userId, String url, Logger logger);

    }
}

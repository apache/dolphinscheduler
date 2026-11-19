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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.AlertGroupServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.DataSourceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.EnvironmentServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.K8SNamespaceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.QueueServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TenantServiceImpl;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.repository.AlertGroupDao;
import org.apache.dolphinscheduler.dao.repository.DataSourceDao;
import org.apache.dolphinscheduler.dao.repository.K8sNamespaceDao;
import org.apache.dolphinscheduler.dao.repository.QueueDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GlobalResourceVerificationTest {

    @InjectMocks
    private DataSourceServiceImpl dataSourceService;
    @InjectMocks
    private TenantServiceImpl tenantService;
    @InjectMocks
    private QueueServiceImpl queueService;
    @InjectMocks
    private EnvironmentServiceImpl environmentService;
    @InjectMocks
    private K8SNamespaceServiceImpl namespaceService;
    @InjectMocks
    private AlertGroupServiceImpl alertGroupService;
    @Mock
    private DataSourceDao dataSourceDao;
    @Mock
    private TenantDao tenantDao;
    @Mock
    private QueueDao queueDao;
    @Mock
    private EnvironmentMapper environmentMapper;
    @Mock
    private K8sNamespaceDao namespaceDao;
    @Mock
    private AlertGroupDao alertGroupDao;

    @ParameterizedTest
    @ValueSource(strings = {"datasource", "tenant", "queue", "environment", "namespace", "alert"})
    void ordinaryUsersCannotProbeGlobalResourceNames(String resource) {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        ServiceException error = Assertions.assertThrows(ServiceException.class, () -> {
            switch (resource) {
                case "datasource":
                    dataSourceService.verifyDataSourceName(user, "private");
                    break;
                case "tenant":
                    tenantService.verifyTenantCode(user, "private");
                    break;
                case "queue":
                    queueService.verifyQueue(user, "private", "private");
                    break;
                case "environment":
                    environmentService.verifyEnvironment(user, "private");
                    break;
                case "namespace":
                    namespaceService.verifyNamespaceK8s(user, "private", 1L);
                    break;
                case "alert":
                    alertGroupService.existGroupName(user, "private");
                    break;
                default:
                    Assertions.fail(resource);
            }
        });
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), error.getCode());
        Mockito.verifyNoInteractions(dataSourceDao, tenantDao, queueDao, environmentMapper, namespaceDao,
                alertGroupDao);
    }
}

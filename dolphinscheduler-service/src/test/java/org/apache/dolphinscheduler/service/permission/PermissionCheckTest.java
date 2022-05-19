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
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static org.powermock.api.mockito.PowerMockito.when;

public class PermissionCheckTest {

    private ProcessService processService;
    private static final Logger logger = LoggerFactory.getLogger(PermissionCheckTest.class);

    @Before
    public void before() throws Exception {
        processService = PowerMockito.mock(ProcessService.class);
    }

    @Test(expected = ServiceException.class)
    public void testCheckPermissionExceptionNullUser() throws ServiceException {
        Integer[] arr = {1,2};
        PermissionCheck<Integer> permissionCheck = new PermissionCheck(AuthorizationType.RESOURCE_FILE_ID, processService, arr, 1, logger);
        when(processService.getUserById(1)).thenReturn(null);
        permissionCheck.checkPermission();
    }
}

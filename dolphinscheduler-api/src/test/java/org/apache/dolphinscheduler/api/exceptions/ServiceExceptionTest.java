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
package org.apache.dolphinscheduler.api.exceptions;

import org.apache.dolphinscheduler.api.enums.Status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceExceptionTest {

    @Test
    public void getCodeTest() {
        ServiceException serviceException = new ServiceException();
        Assertions.assertEquals(Status.INTERNAL_SERVER_ERROR_ARGS.getCode(), serviceException.getCode());

        serviceException = new ServiceException(Status.ALERT_GROUP_EXIST);
        Assertions.assertEquals(Status.ALERT_GROUP_EXIST.getCode(), serviceException.getCode());

        serviceException = new ServiceException(10012, "alarm group already exists");
        Assertions.assertEquals(10012, serviceException.getCode());
    }
    @Test
    public void getMessageTest() {
        ServiceException serviceException = new ServiceException();
        Assertions.assertEquals(Status.INTERNAL_SERVER_ERROR_ARGS.getMsg(), serviceException.getMessage());

        serviceException = new ServiceException(Status.ALERT_GROUP_EXIST);
        Assertions.assertNotNull(serviceException.getMessage());

        serviceException = new ServiceException(10012, "alarm group already exists");
        Assertions.assertNotNull(serviceException.getMessage());
    }
}

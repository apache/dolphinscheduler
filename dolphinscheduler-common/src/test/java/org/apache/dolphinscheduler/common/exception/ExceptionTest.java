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

package org.apache.dolphinscheduler.common.exception;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionTest {

    @Test
    public void testException(){
        final String message = "Test";
        RuntimeException time = new RuntimeException(message);

        Assertions.assertNull(new BaseException().getMessage());
        Assertions.assertNotNull(new BaseException(message).getMessage());
        Assertions.assertNotNull(new BaseException(message, time).getMessage());
        Assertions.assertNotNull(new BaseException(time).getCause());
        Assertions.assertNotNull(new BaseException(message, time, false, false).getMessage());

        Assertions.assertNull(new StorageOperateNoConfiguredException().getMessage());
        Assertions.assertNotNull(new StorageOperateNoConfiguredException(message).getMessage());
        Assertions.assertNotNull(new StorageOperateNoConfiguredException(message, time).getMessage());
        Assertions.assertNotNull(new StorageOperateNoConfiguredException(time).getCause());
        Assertions.assertNotNull(new StorageOperateNoConfiguredException(message, time, false, false).getMessage());
    }
}

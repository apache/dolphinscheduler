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


import org.junit.Test;
import org.mockito.junit.MockitoJUnit;

public class StorageOperateNoConfiguredExceptionTest {

    @Test
    public void test(){
        String message = "Test";
        RuntimeException time = new RuntimeException(message);
        MockitoJUnit.testRule(new StorageOperateNoConfiguredException());
        MockitoJUnit.testRule(new StorageOperateNoConfiguredException(message));
        MockitoJUnit.testRule(new StorageOperateNoConfiguredException(message, time));
        MockitoJUnit.testRule(new StorageOperateNoConfiguredException(time));
        MockitoJUnit.testRule(new StorageOperateNoConfiguredException(message, time, false, false));
    }
}

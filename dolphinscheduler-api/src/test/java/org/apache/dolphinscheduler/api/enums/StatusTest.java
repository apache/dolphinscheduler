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
package org.apache.dolphinscheduler.api.enums;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

public class StatusTest {

    @Test
    public void testGetCode() {
        Assertions.assertEquals(0, Status.SUCCESS.getCode());
        Assertions.assertNotEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), 0);
    }

    @Test
    public void testGetMsg() {
        LocaleContextHolder.setLocale(Locale.US);
        Assertions.assertEquals("success", Status.SUCCESS.getMsg());

        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
        Assertions.assertEquals("成功", Status.SUCCESS.getMsg());
    }

    @Test
    public void testGetStatusByCode() {
        // FAILURE
        Optional<Status> optional = Status.findStatusBy(1);
        Assertions.assertFalse(optional.isPresent());

        // SUCCESS
        optional = Status.findStatusBy(10018);
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, optional.get());
    }
}

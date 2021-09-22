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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.Assert.*;

public class StatusTest {

    @Test
    public void testGetCode() {
        assertEquals(0, Status.SUCCESS.getCode());
        assertNotEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), 0);
    }

    @Test
    public void testGetMsg() {
        LocaleContextHolder.setLocale(Locale.US);
        Assert.assertEquals("success", Status.SUCCESS.getMsg());

        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
        Assert.assertEquals("成功", Status.SUCCESS.getMsg());
    }

}

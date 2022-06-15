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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;

import java.util.Map;

import org.junit.Test;

public class CheckUtilsTest {

    /**
     * check username
     */
    @Test
    public void testCheckUserName() {
        assertTrue(CheckUtils.checkUserName("test01"));
        assertFalse(CheckUtils.checkUserName(null));
        assertFalse(CheckUtils.checkUserName("test01@abc"));
    }

    /**
     * check email
     */
    @Test
    public void testCheckEmail() {
        assertTrue(CheckUtils.checkEmail("test01@gmail.com"));
        assertFalse(CheckUtils.checkEmail("test01@gmail"));
        assertFalse(CheckUtils.checkEmail("test01@gmail."));
        assertTrue(CheckUtils.checkEmail("test01@gmail.edu.cn"));
    }

    /**
     * check desc
     */
    @Test
    public void testCheckDesc() {
        Map<String, Object> objectMap = CheckUtils.checkDesc("I am desc");
        Status status = (Status) objectMap.get(Constants.STATUS);
        assertEquals(status.getCode(), Status.SUCCESS.getCode());
    }

    @Test
    public void testCheckOtherParams() {
        assertFalse(CheckUtils.checkOtherParams(null));
        assertFalse(CheckUtils.checkOtherParams(""));
        assertTrue(CheckUtils.checkOtherParams("xxx"));
        assertFalse(CheckUtils.checkOtherParams("{}"));
        assertFalse(CheckUtils.checkOtherParams("{\"key1\":111}"));
    }

    /**
     * check passwd
     */
    @Test
    public void testCheckPassword() {
        assertFalse(CheckUtils.checkPassword(null));
        assertFalse(CheckUtils.checkPassword("a"));
        assertFalse(CheckUtils.checkPassword("1234567890abcderfasdf2"));
        assertTrue(CheckUtils.checkPassword("123456"));
        assertFalse(CheckUtils.checkPasswordLength("1"));
        assertTrue(CheckUtils.checkPasswordLength("dolphinscheduler123"));
        assertFalse(CheckUtils.checkPasswordLength("dolphinscheduler123456"));
    }

    /**
     * check phone
     */
    @Test
    public void testCheckPhone() {
        // phone can be null
        assertTrue(CheckUtils.checkPhone(null));
        assertFalse(CheckUtils.checkPhone("14567134578654"));
        assertTrue(CheckUtils.checkPhone("17362537263"));
    }

}

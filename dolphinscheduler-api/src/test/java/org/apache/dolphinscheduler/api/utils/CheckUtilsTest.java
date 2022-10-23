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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.constants.Constants;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckUtilsTest {

    /**
     * check username
     */
    @Test
    public void testCheckUserName() {
        Assertions.assertTrue(CheckUtils.checkUserName("test01"));
        Assertions.assertFalse(CheckUtils.checkUserName(null));
        Assertions.assertFalse(CheckUtils.checkUserName("test01@abc"));
    }

    /**
     * check email
     */
    @Test
    public void testCheckEmail() {
        Assertions.assertTrue(CheckUtils.checkEmail("test01@gmail.com"));
        Assertions.assertFalse(CheckUtils.checkEmail("test01@gmail"));
        Assertions.assertFalse(CheckUtils.checkEmail("test01@gmail."));
        Assertions.assertTrue(CheckUtils.checkEmail("test01@gmail.edu.cn"));
    }

    /**
     * check desc
     */
    @Test
    public void testCheckDesc() {
        Map<String, Object> objectMap = CheckUtils.checkDesc("I am desc");
        Status status = (Status) objectMap.get(Constants.STATUS);
        Assertions.assertEquals(status.getCode(), Status.SUCCESS.getCode());
    }

    @Test
    public void testCheckOtherParams() {
        Assertions.assertFalse(CheckUtils.checkOtherParams(null));
        Assertions.assertFalse(CheckUtils.checkOtherParams(""));
        Assertions.assertTrue(CheckUtils.checkOtherParams("xxx"));
        Assertions.assertFalse(CheckUtils.checkOtherParams("{}"));
        Assertions.assertFalse(CheckUtils.checkOtherParams("{\"key1\":111}"));
    }

    /**
     * check passwd
     */
    @Test
    public void testCheckPassword() {
        Assertions.assertFalse(CheckUtils.checkPassword(null));
        Assertions.assertFalse(CheckUtils.checkPassword("a"));
        Assertions.assertFalse(CheckUtils.checkPassword("1234567890abcderfasdf2"));
        Assertions.assertTrue(CheckUtils.checkPassword("123456"));
        Assertions.assertFalse(CheckUtils.checkPasswordLength("1"));
        Assertions.assertTrue(CheckUtils.checkPasswordLength("dolphinscheduler123"));
        Assertions.assertFalse(CheckUtils.checkPasswordLength("dolphinscheduler123456"));
    }

    /**
     * check phone
     */
    @Test
    public void testCheckPhone() {
        // phone can be null
        Assertions.assertTrue(CheckUtils.checkPhone(null));
        Assertions.assertFalse(CheckUtils.checkPhone("14567134578654"));
        Assertions.assertTrue(CheckUtils.checkPhone("17362537263"));
    }

}

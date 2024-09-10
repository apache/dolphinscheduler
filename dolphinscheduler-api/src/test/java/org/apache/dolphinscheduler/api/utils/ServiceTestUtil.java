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

import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ServiceTestUtil {

    public static String randomStringWithLengthN(int n) {
        byte[] bitArray = new byte[n];
        new Random().nextBytes(bitArray);
        return new String(bitArray, StandardCharsets.UTF_8);
    }

    private static User getUser(Integer userId, String userName, UserType userType) {
        User user = new User();
        user.setUserType(userType);
        user.setId(userId);
        user.setUserName(userName);
        return user;
    }

    public static User getAdminUser() {
        return getUser(1, "admin", UserType.ADMIN_USER);
    }
    public static User getGeneralUser() {
        return getUser(10, "user", UserType.GENERAL_USER);
    }
}

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

package org.apache.dolphinscheduler.api.it.core;

import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.springframework.stereotype.Component;

@Component
public class TestSessionHelper {

    private final UserMapper userMapper;
    private final SessionService sessionService;

    public TestSessionHelper(UserMapper userMapper, SessionService sessionService) {
        this.userMapper = userMapper;
        this.sessionService = sessionService;
    }

    /**
     * Find user by user_name, create session, and return the sessionId string.
     */
    public String loginAs(String userName) {
        User user = userMapper.queryByUserNameAccurately(userName);
        if (user == null) {
            throw new IllegalStateException(
                    "Test user not found in DB (did you load the right fixture?): " + userName);
        }
        Session session = sessionService.createSessionIfAbsent(user);
        return session.getId();
    }

    /**
     * Directly return the logged-in user, convenient for test cases to get user.getId().
     */
    public User user(String userName) {
        return userMapper.queryByUserNameAccurately(userName);
    }
}

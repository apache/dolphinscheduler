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

package org.apache.dolphinscheduler.graphql.datafetcher.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.SessionMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.graphql.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
public class UserArgumentService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SessionMapper sessionMapper;

    public Result getUserFromArgument(LinkedHashMap<String, String> loginUserMap) {
        int id = Integer.parseInt(loginUserMap.get("id"));
        String sessionId = loginUserMap.get("sessionId");

        Result result = new Result();

        if (sessionId == null) {
            ResultUtil.putStatus(result, Status.USER_LOGIN_FAILURE, id);
            return result;
        }

        User user = userMapper.selectById(id);

        if (user == null) {
            ResultUtil.putStatus(result, Status.USER_NOT_EXIST, id);
            return result;
        }

        List<Session> sessions = sessionMapper.queryByUserId(id);

        Session loginUserSession = sessions.stream().filter(session -> session.getId().equals(sessionId))
                .findFirst().orElse(null);

        if (loginUserSession == null) {
            ResultUtil.putStatus(result, Status.USER_LOGIN_FAILURE, id);
            return result;
        }

        result.setData(user);
        ResultUtil.putStatus(result, Status.SUCCESS);
        return result;
    }



}

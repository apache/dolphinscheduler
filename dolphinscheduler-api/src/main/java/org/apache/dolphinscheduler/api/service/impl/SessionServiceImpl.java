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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.SessionDao;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * session service implement
 */
@Service
@Slf4j
public class SessionServiceImpl extends BaseServiceImpl implements SessionService {

    @Autowired
    private SessionDao sessionDao;

    @Override
    public Session getSession(String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            return null;
        }
        return sessionDao.queryById(sessionId);
    }

    @Override
    @Transactional
    public Session createSessionIfAbsent(User user) {
        Session session;
        List<Session> sessionList = sessionDao.queryByUserId(user.getId());
        // todo: this can be remove after the old session data is cleared
        if (CollectionUtils.isNotEmpty(sessionList)) {
            // is session list greater 1 ， delete other ，get one
            if (sessionList.size() > 1) {
                for (int i = 1; i < sessionList.size(); i++) {
                    sessionDao.deleteById(sessionList.get(i).getId());
                }
            }
            session = sessionList.get(0);
            if (isSessionExpire(session)) {
                session.setLastLoginTime(new Date());
                sessionDao.updateById(session);
                return session;
            } else {
                sessionDao.deleteById(session.getId());
            }
        }

        // assign new session
        Session newSession = Session.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .lastLoginTime(new Date())
                .build();
        sessionDao.insert(newSession);
        return newSession;
    }

    @Override
    public void expireSession(Integer userId) {
        sessionDao.deleteByUserId(userId);
    }

    @Override
    public boolean isSessionExpire(Session session) {
        return System.currentTimeMillis() - session.getLastLoginTime().getTime() <= Constants.SESSION_TIME_OUT * 1000;
    }

}

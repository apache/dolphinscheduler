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
package cn.escheduler.api.service;


import cn.escheduler.api.controller.BaseController;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.dao.mapper.SessionMapper;
import cn.escheduler.dao.model.Session;
import cn.escheduler.dao.model.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * session service
 */
@Service
public class SessionService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

  @Autowired
  private SessionMapper sessionMapper;

  /**
   * get user session from request
   *
   * @param request
   * @return
   */
  public Session getSession(HttpServletRequest request)  {
    String sessionId = request.getHeader(Constants.SESSION_ID);

    if(StringUtils.isBlank(sessionId)) {
      Cookie cookie = getCookie(request, Constants.SESSION_ID);

      if (cookie != null) {
        sessionId = cookie.getValue();
      }
    }

    if(StringUtils.isBlank(sessionId)) {
      return null;
    }

    String ip = BaseController.getClientIpAddress(request);
    logger.debug("get session: {}, ip: {}", sessionId, ip);

    return sessionMapper.queryBySessionId(sessionId);
  }

  /**
   * create session
   *
   * @param user
   * @param ip
   * @return
   */
  public String createSession(User user, String ip) {
    Session session = null;

    // logined
    List<Session> sessionList = sessionMapper.queryByUserId(user.getId());

    Date now = new Date();

    /**
     * if you have logged in and are still valid, return directly
     */
    if (CollectionUtils.isNotEmpty(sessionList)) {
      // is session list greater 1 ， delete other ，get one
      if (sessionList.size() > 1){
        for (int i=1 ; i < sessionList.size();i++){
          sessionMapper.deleteById(sessionList.get(i).getId());
        }
      }
      session = sessionList.get(0);
      if (now.getTime() - session.getLastLoginTime().getTime() <= Constants.SESSION_TIME_OUT * 1000) {
        /**
         * updateProcessInstance the latest login time
         */
        sessionMapper.update(session.getId(), now);

        return session.getId();

      } else {
        /**
         * session expired, then delete this session first
         */
        sessionMapper.deleteById(session.getId());
      }
    }

    // assign new session
    session = new Session();

    session.setId(UUID.randomUUID().toString());
    session.setIp(ip);
    session.setUserId(user.getId());
    session.setLastLoginTime(now);

    sessionMapper.insert(session);

    return session.getId();
  }

  /**
   * sign out
   * remove ip restrictions
   *
   * @param ip   no use
   * @param loginUser
   */
  public void signOut(String ip, User loginUser) {
    /**
     * query session by user id and ip
     */
    List<Session> sessionList = sessionMapper.queryByUserId(loginUser.getId());

    for (Session session : sessionList){
      //delete session
      sessionMapper.deleteById(session.getId());
    }
  }
}

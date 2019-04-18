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
package cn.escheduler.api.interceptor;

import cn.escheduler.api.service.SessionService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.dao.mapper.UserMapper;
import cn.escheduler.dao.model.Session;
import cn.escheduler.dao.model.User;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * login interceptor, must login first
 */
public class LoginHandlerInterceptor implements HandlerInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(LoginHandlerInterceptor.class);

  @Autowired
  private SessionService sessionService;

  @Autowired
  private UserMapper userMapper;

  /**
   * Intercept the execution of a handler. Called after HandlerMapping determined
   * an appropriate handler object, but before HandlerAdapter invokes the handler.
   * <p>DispatcherServlet processes a handler in an execution chain, consisting
   * of any number of interceptors, with the handler itself at the end.
   * With this method, each interceptor can decide to abort the execution chain,
   * typically sending a HTTP error or writing a custom response.
   * <p><strong>Note:</strong> special considerations apply for asynchronous
   * request processing. For more details see
   * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
   * @param request current HTTP request
   * @param response current HTTP response
   * @param handler chosen handler to execute, for type and/or instance evaluation
   * @return {@code true} if the execution chain should proceed with the
   * next interceptor or the handler itself. Else, DispatcherServlet assumes
   * that this interceptor has already dealt with the response itself.
   * @throws Exception in case of errors
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

    // get token
    String token = request.getHeader("token");
    User user = null;
    if (StringUtils.isEmpty(token)){
      Session session = sessionService.getSession(request);

      if (session == null) {
        response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        logger.info("session info is null ");
        return false;
      }

      //get user object from session
      user = userMapper.queryById(session.getUserId());
    }else {
       user = userMapper.queryUserByToken(token);
    }

    // if user is null
    if (user == null) {
      response.setStatus(HttpStatus.SC_UNAUTHORIZED);
      logger.info("user does not exist");
      return false;
    }
    request.setAttribute(Constants.SESSION_USER, user);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

  }

}

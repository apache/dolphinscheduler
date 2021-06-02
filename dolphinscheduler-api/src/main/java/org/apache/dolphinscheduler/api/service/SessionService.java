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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * session service
 */
public interface SessionService {

  /**
   * get user session from request
   *
   * @param request request
   * @return session
   */
  Session getSession(HttpServletRequest request);

  /**
   * create session
   *
   * @param user user
   * @param ip ip
   * @return session string
   */
  String createSession(User user, String ip);

  /**
   * sign out
   * remove ip restrictions
   *
   * @param ip   no use
   * @param loginUser login user
   */
  void signOut(String ip, User loginUser);
}

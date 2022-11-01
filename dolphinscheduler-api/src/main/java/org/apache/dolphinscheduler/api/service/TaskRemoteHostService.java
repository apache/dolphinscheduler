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

import org.apache.dolphinscheduler.api.dto.TaskRemoteHostDTO;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.TaskRemoteHostVO;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

/**
 * Task Remote Host Service
 */
public interface TaskRemoteHostService {

    /**
     * Create a new Task Remote Host
     * @param loginUser login user
     * @param taskRemoteHostDTO task remote host DTO
     * @return insert result
     */
    int createTaskRemoteHost(User loginUser, TaskRemoteHostDTO taskRemoteHostDTO);

    /**
     * Update task remote host
     * @param code task remote host code
     * @param loginUser login user
     * @param taskRemoteHostDTO task remote host DTO
     * @return result
     */
    int updateTaskRemoteHost(long code, User loginUser, TaskRemoteHostDTO taskRemoteHostDTO);

    /**
     * Delete task remote host
     * @param code task remote host code
     * @param loginUser login user
     * @return result
     */
    int deleteByCode(long code, User loginUser);

    /**
     * Query all task remote hosts
     * @param loginUser login user
     * @return list of task remote hosts
     */
    List<TaskRemoteHostVO> queryAllTaskRemoteHosts(User loginUser);

    /**
     * Query task remote host pages
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return result
     */
    PageInfo<TaskRemoteHostVO> queryTaskRemoteHostListPaging(User loginUser, String searchVal, Integer pageNo,
                                                             Integer pageSize);

    /**
     * Test connect task remote host
     * @param taskRemoteHostDTO task remote host DTO
     * @return result
     */
    boolean testConnect(TaskRemoteHostDTO taskRemoteHostDTO);

    /**
     * Verify task remote host
     * @param taskRemoteHostName task remote host name
     * @return result
     */
    boolean verifyTaskRemoteHost(String taskRemoteHostName);
}

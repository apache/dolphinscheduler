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

package org.apache.dolphinscheduler.api.v3.service;

import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

/**
 * project service
 **/
public interface ProjectV3Service {

    /**
     * query project details by code
     *
     * @param projectCode project code
     * @return project detail information
     */
    Project queryProjectForUpdate(User user, long projectCode);

    /**
     * admin can view all projects
     *
     * @param user       User
     * @param offset     offset
     * @param maxResults max result
     * @return project list which the login user have permission to see
     */
    List<Project> listAuthorizedProject(User user, int offset, int maxResults, String searchVal);

    Project createProject(User user, String name, String description);

    /**
     * delete project by code
     *
     * @param project Project
     * @return delete result code
     */
    void deleteProject(Project project);

    Project updateProject(Project project, String name, String description);
}

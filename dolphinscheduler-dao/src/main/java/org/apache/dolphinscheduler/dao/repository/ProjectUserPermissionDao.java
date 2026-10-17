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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.dao.entity.ProjectUserPermission;

import java.util.Collection;
import java.util.List;

public interface ProjectUserPermissionDao extends IDao<ProjectUserPermission> {

    List<ProjectUserPermission> queryByProjectsAndUser(Collection<Long> projectCodes, int userId);

    boolean hasPermission(long projectCode, int userId, ProjectPermission permission);

    List<ProjectUserPermission> queryByProjectAndUser(long projectCode, int userId);

    void revokeAllPermissions(long projectCode, int userId);

    void revokePermission(long projectCode, int userId, ProjectPermission permission);
}

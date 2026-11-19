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

import org.apache.dolphinscheduler.api.vo.UserSimpleInfoVO;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Set;

public interface ProjectAuthorizationService {

    List<UserSimpleInfoVO> queryAssignableMembers(User loginUser, long projectCode);

    Set<ProjectPermission> queryMemberPermissions(User loginUser, long projectCode, int userId);

    void replaceMemberPermissions(User loginUser, long projectCode, int userId, Set<ProjectPermission> permissions);

    Project requirePermission(User user, long projectCode, ProjectPermission permission);

    void requireResourceUse(User user, long projectCode, ProjectPermission permission,
                            ProjectResourceType resourceType, String resourceKey);
}

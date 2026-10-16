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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.dao.entity.ProjectUserPermission;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserPermissionMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.ProjectUserPermissionDao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Repository
public class ProjectUserPermissionDaoImpl extends BaseDao<ProjectUserPermission, ProjectUserPermissionMapper>
        implements
            ProjectUserPermissionDao {

    public ProjectUserPermissionDaoImpl(@NonNull ProjectUserPermissionMapper mapper) {
        super(mapper);
    }

    @Override
    public List<ProjectUserPermission> queryByProjectsAndUser(Collection<Long> projectCodes, int userId) {
        if (projectCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return mybatisMapper.selectList(new LambdaQueryWrapper<ProjectUserPermission>()
                .in(ProjectUserPermission::getProjectCode, projectCodes)
                .eq(ProjectUserPermission::getUserId, userId));
    }

    @Override
    public boolean hasPermission(long projectCode, int userId, @NonNull ProjectPermission permission) {
        return mybatisMapper.selectCount(new LambdaQueryWrapper<ProjectUserPermission>()
                .eq(ProjectUserPermission::getProjectCode, projectCode)
                .eq(ProjectUserPermission::getUserId, userId)
                .eq(ProjectUserPermission::getPermission, permission)) > 0;
    }

    @Override
    public List<ProjectUserPermission> queryByProjectAndUser(long projectCode, int userId) {
        return mybatisMapper.selectList(new LambdaQueryWrapper<ProjectUserPermission>()
                .eq(ProjectUserPermission::getProjectCode, projectCode)
                .eq(ProjectUserPermission::getUserId, userId));
    }

    @Override
    public void revokeAllPermissions(long projectCode, int userId) {
        mybatisMapper.delete(new LambdaQueryWrapper<ProjectUserPermission>()
                .eq(ProjectUserPermission::getProjectCode, projectCode)
                .eq(ProjectUserPermission::getUserId, userId));
    }

    @Override
    public void revokePermission(long projectCode, int userId, @NonNull ProjectPermission permission) {
        mybatisMapper.delete(new LambdaQueryWrapper<ProjectUserPermission>()
                .eq(ProjectUserPermission::getProjectCode, projectCode)
                .eq(ProjectUserPermission::getUserId, userId)
                .eq(ProjectUserPermission::getPermission, permission));
    }
}

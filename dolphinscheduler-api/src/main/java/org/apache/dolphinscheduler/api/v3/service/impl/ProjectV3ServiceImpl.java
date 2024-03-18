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

package org.apache.dolphinscheduler.api.v3.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.v3.service.ProjectV3Service;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.v3.ProjectV3Mapper;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

/**
 * project service impl
 **/
@Service
@Slf4j
public class ProjectV3ServiceImpl extends BaseServiceImpl implements ProjectV3Service {

    @Autowired
    private ProjectV3Mapper projectMapper;

    /**
     * query project details by code
     *
     * @param projectCode project code
     * @return project detail information
     */
    @Override
    @PostAuthorize("hasAuthority('ADMIN_USER') or returnObject.perm == 7 or returnObject.userId == authentication.getUserId()")
    public Project queryProjectForUpdate(User user, long projectCode) {
        Project project = projectMapper.queryProjectByCodeForUpdate(user.getId(), projectCode);

        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }

        return project;
    }

    /**
     * admin can view all projects
     *
     * @param user       User
     * @param searchVal  search value
     * @param maxResults max result
     * @return project list which the login user have permission to see
     */
    @Override
    @PostFilter("hasAuthority('ADMIN_USER') or filterObject.perm > 0 or filterObject.userId == authentication.getUserId()")
    public List<Project> listAuthorizedProject(User user, int offset, int maxResults, String searchVal) {
        return projectMapper.listProjects(
                user.getId(),
                offset,
                maxResults,
                searchVal);
    }

    @Override
    public Project createProject(User user, String projectName, String desc) {
        Project existProject = projectMapper.queryProjectByName(projectName);

        if (existProject != null) {
            throw new ServiceException(Status.PROJECT_ALREADY_EXISTS, projectName);
        }

        long projectCode;

        try {
            projectCode = CodeGenerateUtils.getInstance().genCode();
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            throw new ServiceException(Status.PROJECT_PARAMETER_CODE_EMPTY);
        }

        Date now = new Date();

        Project project = Project
                .builder()
                .name(projectName)
                .code(projectCode)
                .description(desc)
                .userId(user.getId())
                .userName(user.getUserName())
                .createTime(now)
                .updateTime(now)
                .build();

        if (projectMapper.insert(project) <= 0) {
            throw new ServiceException(Status.CREATE_PROJECT_ERROR);
        }

        return project;
    }

    @Override
    public void deleteProject(Project project) {
        if (projectMapper.deleteById(project.getId()) <= 0) {
            throw new ServiceException(Status.DELETE_PROJECT_ERROR);
        }
    }

    @Override
    public Project updateProject(Project project, String name, String description) {
        if (name != null) {
            Project existingProject = projectMapper.queryProjectByName(name);

            if (existingProject != null && !existingProject.getId().equals(project.getId())) {
                throw new ServiceException(Status.PROJECT_ALREADY_EXISTS, name);
            }

            project.setName(name);
        }

        if (description != null) {
            project.setDescription(description);
        }

        project.setUpdateTime(new Date());

        if (projectMapper.updateById(project) <= 0) {
            throw new ServiceException(Status.UPDATE_PROJECT_ERROR);
        }

        return project;
    }
}

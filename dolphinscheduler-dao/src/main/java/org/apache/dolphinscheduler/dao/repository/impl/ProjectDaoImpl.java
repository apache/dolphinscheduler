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

import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;

import java.util.Collection;
import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;

@Repository
public class ProjectDaoImpl extends BaseDao<Project, ProjectMapper> implements ProjectDao {

    public ProjectDaoImpl(@NonNull ProjectMapper projectMapper) {
        super(projectMapper);
    }

    @Override
    public List<Project> queryByCodes(Collection<Long> projectCodes) {
        return mybatisMapper.queryByCodes(projectCodes);
    }

    @Override
    public Project queryByCode(Long projectCode) {
        return mybatisMapper.queryByCode(projectCode);
    }

    @Override
    public Project queryDetailById(int projectId) {
        return mybatisMapper.queryDetailById(projectId);
    }

    @Override
    public Project queryByName(String projectName) {
        return mybatisMapper.queryByName(projectName);
    }

    @Override
    public IPage<Project> queryProjectListPaging(IPage<Project> page,
                                                 List<Integer> projectsIds,
                                                 String searchName) {
        return mybatisMapper.queryProjectListPaging(page, projectsIds, searchName);
    }

    @Override
    public List<Project> queryProjectCreatedByUser(int userId) {
        return mybatisMapper.queryProjectCreatedByUser(userId);
    }

    @Override
    public List<Project> queryAuthedProjectListByUserId(int userId) {
        return mybatisMapper.queryAuthedProjectListByUserId(userId);
    }

    @Override
    public List<Project> queryProjectCreatedAndAuthorizedByUserId(int userId) {
        return mybatisMapper.queryProjectCreatedAndAuthorizedByUserId(userId);
    }

    @Override
    public ProjectUser queryProjectWithUserByWorkflowInstanceId(int workflowInstanceId) {
        return mybatisMapper.queryProjectWithUserByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public List<Project> queryAllProject(int userId) {
        return mybatisMapper.queryAllProject(userId);
    }

    @Override
    public List<Project> listAuthorizedProjects(int userId, List<Integer> projectsIds) {
        return mybatisMapper.listAuthorizedProjects(userId, projectsIds);
    }

    @Override
    public List<Project> queryAllProjectForDependent() {
        return mybatisMapper.queryAllProjectForDependent();
    }

    @Override
    public Project queryProjectByTaskInstanceId(int taskInstanceId) {
        return mybatisMapper.queryProjectByTaskInstanceId(taskInstanceId);
    }
}

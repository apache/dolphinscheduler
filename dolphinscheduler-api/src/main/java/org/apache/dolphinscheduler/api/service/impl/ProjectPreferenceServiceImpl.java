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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectParameterService;
import org.apache.dolphinscheduler.api.service.ProjectPreferenceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectParameter;
import org.apache.dolphinscheduler.dao.entity.ProjectPreference;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectParameterMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectPreferenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ProjectPreferenceServiceImpl extends BaseServiceImpl implements
    ProjectPreferenceService {

    @Autowired
    private ProjectPreferenceMapper projectPreferenceMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public Result updateProjectPreference(User loginUser, long projectCode, String preferences) {
        Result result = new Result();

        // check if the user has the writing permission for project
        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }

        ProjectPreference projectPreference = projectPreferenceMapper
            .selectOne(new QueryWrapper<ProjectPreference>().lambda().eq(ProjectPreference::getProjectCode, projectCode));

        Date now = new Date();
        if (Objects.isNull(projectPreference)) {
            projectPreference = new ProjectPreference();
            projectPreference.setProjectCode(projectCode);
            projectPreference.setPreferences(preferences);
            projectPreference.setUserId(loginUser.getId());
            projectPreference.setCode(CodeGenerateUtils.getInstance().genCode());
            projectPreference.setCreateTime(now);
            projectPreference.setUpdateTime(now);
            if (projectPreferenceMapper.insert(projectPreference) > 0) {
                log.info("Project preference is created and id is :{}", projectPreference.getId());
                result.setData(projectPreference);
                putMsg(result, Status.SUCCESS);
            } else {
                log.error("Project preference create error, projectCode:{}.", projectPreference.getProjectCode());
                putMsg(result, Status.CREATE_PROJECT_PREFERENCE_ERROR);
            }
        } else {
            projectPreference.setPreferences(preferences);
            projectPreference.setUserId(loginUser.getId());
            projectPreference.setUpdateTime(now);

            if (projectPreferenceMapper.updateById(projectPreference) > 0) {
                log.info("Project preference is updated and id is :{}", projectPreference.getId());
                result.setData(projectPreference);
                putMsg(result, Status.SUCCESS);
            } else {
                log.error("Project preference update error, projectCode:{}.", projectPreference.getProjectCode());
                putMsg(result, Status.CREATE_PROJECT_PREFERENCE_ERROR);
            }
        }
        return result;
    }


    @Override
    public Result queryProjectPreferenceByProjectCode(User loginUser, long projectCode) {
        Result result = new Result();

        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, PROJECT);
        if (!hasProjectAndPerm) {
            return result;
        }

        ProjectPreference projectPreference = projectPreferenceMapper
            .selectOne(new QueryWrapper<ProjectPreference>().lambda().eq(ProjectPreference::getProjectCode, projectCode));

        if (Objects.nonNull(projectPreference)) {
            result.setData(projectPreference);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.QUERY_PROJECT_PREFERENCE_ERROR);
        }
        return result;
    }
}

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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectParameterService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectParameter;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectParameterMapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

@Service
@Slf4j
public class ProjectParameterServiceImpl extends BaseServiceImpl implements ProjectParameterService {

    @Autowired
    private ProjectParameterMapper projectParameterMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    @Transactional
    public Result createProjectParameter(User loginUser, long projectCode, String projectParameterName,
                                         String projectParameterValue, String projectParameterDataType) {
        Result result = new Result();

        // check if user have write perm for project
        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }

        // check if project parameter name exists
        ProjectParameter projectParameter = projectParameterMapper.selectOne(new QueryWrapper<ProjectParameter>()
                .lambda()
                .eq(ProjectParameter::getProjectCode, projectCode)
                .eq(ProjectParameter::getParamName, projectParameterName));

        if (projectParameter != null) {
            log.warn("ProjectParameter {} already exists.", projectParameter.getParamName());
            putMsg(result, Status.PROJECT_PARAMETER_ALREADY_EXISTS, projectParameter.getParamName());
            return result;
        }

        Date now = new Date();

        try {
            projectParameter = ProjectParameter
                    .builder()
                    .paramName(projectParameterName)
                    .paramValue(projectParameterValue)
                    .paramDataType(projectParameterDataType)
                    .code(CodeGenerateUtils.genCode())
                    .projectCode(projectCode)
                    .userId(loginUser.getId())
                    .createTime(now)
                    .updateTime(now)
                    .build();
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            log.error("Generate project parameter code error.", e);
            putMsg(result, Status.CREATE_PROJECT_PARAMETER_ERROR);
            return result;
        }

        if (projectParameterMapper.insert(projectParameter) > 0) {
            log.info("Project parameter is created and id is :{}", projectParameter.getId());
            result.setData(projectParameter);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project parameter create error, projectName:{}.", projectParameter.getParamName());
            putMsg(result, Status.CREATE_PROJECT_PARAMETER_ERROR);
        }
        return result;
    }

    @Override
    public Result updateProjectParameter(User loginUser, long projectCode, long code, String projectParameterName,
                                         String projectParameterValue, String projectParameterDataType) {
        Result result = new Result();

        // check if user have write perm for project
        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }

        ProjectParameter projectParameter = projectParameterMapper.queryByCode(code);
        // check project parameter exists
        if (projectParameter == null || projectCode != projectParameter.getProjectCode()) {
            log.error("Project parameter does not exist, code:{}.", code);
            putMsg(result, Status.PROJECT_PARAMETER_NOT_EXISTS, String.valueOf(code));
            return result;
        }

        // check if project parameter name exists
        ProjectParameter tempProjectParameter = projectParameterMapper.selectOne(new QueryWrapper<ProjectParameter>()
                .lambda()
                .eq(ProjectParameter::getProjectCode, projectCode)
                .eq(ProjectParameter::getParamName, projectParameterName)
                .ne(ProjectParameter::getCode, code));

        if (tempProjectParameter != null) {
            log.error("Project parameter name {} already exists", projectParameterName);
            putMsg(result, Status.PROJECT_PARAMETER_ALREADY_EXISTS, projectParameterName);
            return result;
        }

        projectParameter.setParamName(projectParameterName);
        projectParameter.setParamValue(projectParameterValue);
        projectParameter.setParamDataType(projectParameterDataType);
        projectParameter.setUpdateTime(new Date());
        projectParameter.setOperator(loginUser.getId());

        if (projectParameterMapper.updateById(projectParameter) > 0) {
            log.info("Project parameter is updated and id is :{}", projectParameter.getId());
            result.setData(projectParameter);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project parameter update error, {}.", projectParameterName);
            putMsg(result, Status.UPDATE_PROJECT_PARAMETER_ERROR);
        }
        return result;
    }

    @Override
    public Result deleteProjectParametersByCode(User loginUser, long projectCode, long code) {
        Result result = new Result();

        // check if user have write perm for project
        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndWritePerm = projectService.hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }

        ProjectParameter projectParameter = projectParameterMapper.queryByCode(code);
        // check project parameter exists
        if (projectParameter == null || projectCode != projectParameter.getProjectCode()) {
            log.error("Project parameter does not exist, code:{}.", code);
            putMsg(result, Status.PROJECT_PARAMETER_NOT_EXISTS, String.valueOf(code));
            return result;
        }

        // TODO: check project parameter is used by workflow

        if (projectParameterMapper.deleteById(projectParameter.getId()) > 0) {
            log.info("Project parameter is deleted and id is :{}.", projectParameter.getId());
            result.setData(Boolean.TRUE);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project parameter delete error, {}.", projectParameter.getParamName());
            putMsg(result, Status.DELETE_PROJECT_PARAMETER_ERROR);
        }
        return result;
    }

    @Override
    public Result batchDeleteProjectParametersByCodes(User loginUser, long projectCode, String codes) {
        Result result = new Result();

        if (StringUtils.isEmpty(codes)) {
            log.error("Project parameter codes is empty, projectCode is {}.", projectCode);
            putMsg(result, Status.PROJECT_PARAMETER_CODE_EMPTY);
            return result;
        }

        Set<Long> requestCodeSet = Lists.newArrayList(codes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        List<ProjectParameter> projectParameterList = projectParameterMapper.queryByCodes(requestCodeSet);
        Set<Long> actualCodeSet =
                projectParameterList.stream().map(ProjectParameter::getCode).collect(Collectors.toSet());
        // requestCodeSet - actualCodeSet
        Set<Long> diffCode =
                requestCodeSet.stream().filter(code -> !actualCodeSet.contains(code)).collect(Collectors.toSet());

        String diffCodeString = diffCode.stream().map(String::valueOf).collect(Collectors.joining(Constants.COMMA));
        if (CollectionUtils.isNotEmpty(diffCode)) {
            log.error("Project parameter does not exist, codes:{}.", diffCodeString);
            throw new ServiceException(Status.PROJECT_PARAMETER_NOT_EXISTS, diffCodeString);
        }

        for (ProjectParameter projectParameter : projectParameterList) {
            this.deleteProjectParametersByCode(loginUser, projectCode, projectParameter.getCode());
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result queryProjectParameterListPaging(User loginUser, long projectCode, Integer pageSize, Integer pageNo,
                                                  String searchVal, String projectParameterDataType) {
        Result result = new Result();

        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, PROJECT);
        if (!hasProjectAndPerm) {
            return result;
        }

        PageInfo<ProjectParameter> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<ProjectParameter> page = new Page<>(pageNo, pageSize);

        IPage<ProjectParameter> iPage =
                projectParameterMapper.queryProjectParameterListPaging(page, projectCode, null, searchVal,
                        projectParameterDataType);

        List<ProjectParameter> projectParameterList = iPage.getRecords();

        pageInfo.setTotal((int) iPage.getTotal());
        pageInfo.setTotalList(projectParameterList);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result queryProjectParameterByCode(User loginUser, long projectCode, long code) {
        Result result = new Result();

        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, PROJECT);
        if (!hasProjectAndPerm) {
            return result;
        }

        ProjectParameter projectParameter = projectParameterMapper.queryByCode(code);
        if (projectParameter == null || projectCode != projectParameter.getProjectCode()) {
            log.error("Project parameter does not exist, code:{}.", code);
            putMsg(result, Status.PROJECT_PARAMETER_NOT_EXISTS, String.valueOf(code));
            return result;
        }

        result.setData(projectParameter);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}

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

import org.apache.dolphinscheduler.api.dto.EnvironmentDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.EnvironmentService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentWorkerGroupRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * task definition service impl
 */
@Service
public class EnvironmentServiceImpl extends BaseServiceImpl implements EnvironmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentServiceImpl.class);

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Autowired
    private EnvironmentWorkerGroupRelationMapper relationMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    /**
     * create environment
     *
     * @param loginUser login user
     * @param name environment name
     * @param config environment config
     * @param desc environment desc
     * @param workerGroups worker groups
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> createEnvironment(User loginUser, String name, String config, String desc, String workerGroups) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        Map<String, Object> checkResult = checkParams(name,config,workerGroups);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Environment environment = environmentMapper.queryByEnvironmentName(name);
        if (environment != null) {
            putMsg(result, Status.ENVIRONMENT_NAME_EXISTS, name);
            return result;
        }

        Environment env = new Environment();
        env.setName(name);
        env.setConfig(config);
        env.setDescription(desc);
        env.setOperator(loginUser.getId());
        env.setCreateTime(new Date());
        env.setUpdateTime(new Date());
        long code = 0L;
        try {
            code = CodeGenerateUtils.getInstance().genCode();
            env.setCode(code);
        } catch (CodeGenerateException e) {
            logger.error("Environment code get error, ", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating environment code");
            return result;
        }

        if (environmentMapper.insert(env) > 0) {
            if (!StringUtils.isEmpty(workerGroups)) {
                List<String> workerGroupList = JSONUtils.parseObject(workerGroups, new TypeReference<List<String>>(){});
                if (CollectionUtils.isNotEmpty(workerGroupList)) {
                    workerGroupList.stream().forEach(workerGroup -> {
                        if (!StringUtils.isEmpty(workerGroup)) {
                            EnvironmentWorkerGroupRelation relation = new EnvironmentWorkerGroupRelation();
                            relation.setEnvironmentCode(env.getCode());
                            relation.setWorkerGroup(workerGroup);
                            relation.setOperator(loginUser.getId());
                            relation.setCreateTime(new Date());
                            relation.setUpdateTime(new Date());
                            relationMapper.insert(relation);
                        }
                    });
                }
            }
            result.put(Constants.DATA_LIST, env.getCode());
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_ENVIRONMENT_ERROR);
        }
        return result;
    }

    /**
     * query environment paging
     *
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return environment list page
     */
    @Override
    public Result queryEnvironmentListPaging(Integer pageNo, Integer pageSize, String searchVal) {
        Result result = new Result();

        Page<Environment> page = new Page<>(pageNo, pageSize);

        IPage<Environment> environmentIPage = environmentMapper.queryEnvironmentListPaging(page, searchVal);

        PageInfo<EnvironmentDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) environmentIPage.getTotal());

        if (CollectionUtils.isNotEmpty(environmentIPage.getRecords())) {
            Map<Long, List<String>> relationMap = relationMapper.selectList(null).stream()
                    .collect(Collectors.groupingBy(EnvironmentWorkerGroupRelation::getEnvironmentCode,Collectors.mapping(EnvironmentWorkerGroupRelation::getWorkerGroup,Collectors.toList())));

            List<EnvironmentDto> dtoList = environmentIPage.getRecords().stream().map(environment -> {
                EnvironmentDto dto = new EnvironmentDto();
                BeanUtils.copyProperties(environment,dto);
                List<String> workerGroups = relationMap.getOrDefault(environment.getCode(),new ArrayList<String>());
                dto.setWorkerGroups(workerGroups);
                return dto;
            }).collect(Collectors.toList());

            pageInfo.setTotalList(dtoList);
        } else {
            pageInfo.setTotalList(new ArrayList<>());
        }

        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all environment
     *
     * @return all environment list
     */
    @Override
    public Map<String, Object> queryAllEnvironmentList() {
        Map<String,Object> result = new HashMap<>();
        List<Environment> environmentList = environmentMapper.queryAllEnvironmentList();

        if (CollectionUtils.isNotEmpty(environmentList)) {
            Map<Long, List<String>> relationMap = relationMapper.selectList(null).stream()
                    .collect(Collectors.groupingBy(EnvironmentWorkerGroupRelation::getEnvironmentCode,Collectors.mapping(EnvironmentWorkerGroupRelation::getWorkerGroup,Collectors.toList())));

            List<EnvironmentDto> dtoList = environmentList.stream().map(environment -> {
                EnvironmentDto dto = new EnvironmentDto();
                BeanUtils.copyProperties(environment,dto);
                List<String> workerGroups = relationMap.getOrDefault(environment.getCode(),new ArrayList<String>());
                dto.setWorkerGroups(workerGroups);
                return dto;
            }).collect(Collectors.toList());
            result.put(Constants.DATA_LIST,dtoList);
        } else {
            result.put(Constants.DATA_LIST, new ArrayList<>());
        }

        putMsg(result,Status.SUCCESS);
        return result;
    }

    /**
     * query environment
     *
     * @param code environment code
     */
    @Override
    public Map<String, Object> queryEnvironmentByCode(Long code) {
        Map<String, Object> result = new HashMap<>();

        Environment env = environmentMapper.queryByEnvironmentCode(code);

        if (env == null) {
            putMsg(result, Status.QUERY_ENVIRONMENT_BY_CODE_ERROR, code);
        } else {
            List<String> workerGroups = relationMapper.queryByEnvironmentCode(env.getCode()).stream()
                    .map(item -> item.getWorkerGroup())
                    .collect(Collectors.toList());

            EnvironmentDto dto = new EnvironmentDto();
            BeanUtils.copyProperties(env,dto);
            dto.setWorkerGroups(workerGroups);
            result.put(Constants.DATA_LIST, dto);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * query environment
     *
     * @param name environment name
     */
    @Override
    public Map<String, Object> queryEnvironmentByName(String name) {
        Map<String, Object> result = new HashMap<>();

        Environment env = environmentMapper.queryByEnvironmentName(name);
        if (env == null) {
            putMsg(result, Status.QUERY_ENVIRONMENT_BY_NAME_ERROR, name);
        } else {
            List<String> workerGroups = relationMapper.queryByEnvironmentCode(env.getCode()).stream()
                    .map(item -> item.getWorkerGroup())
                    .collect(Collectors.toList());

            EnvironmentDto dto = new EnvironmentDto();
            BeanUtils.copyProperties(env,dto);
            dto.setWorkerGroups(workerGroups);
            result.put(Constants.DATA_LIST, dto);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * delete environment
     *
     * @param loginUser login user
     * @param code environment code
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> deleteEnvironmentByCode(User loginUser, Long code) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        Integer relatedTaskNumber = taskDefinitionMapper
                .selectCount(new QueryWrapper<TaskDefinition>().lambda().eq(TaskDefinition::getEnvironmentCode,code));

        if (relatedTaskNumber > 0) {
            putMsg(result, Status.DELETE_ENVIRONMENT_RELATED_TASK_EXISTS);
            return result;
        }

        int delete = environmentMapper.deleteByCode(code);
        if (delete > 0) {
            relationMapper.delete(new QueryWrapper<EnvironmentWorkerGroupRelation>()
                    .lambda()
                    .eq(EnvironmentWorkerGroupRelation::getEnvironmentCode,code));
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_ENVIRONMENT_ERROR);
        }
        return result;
    }

    /**
     * update environment
     *
     * @param loginUser login user
     * @param code environment code
     * @param name environment name
     * @param config environment config
     * @param desc environment desc
     * @param workerGroups worker groups
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> updateEnvironmentByCode(User loginUser, Long code, String name, String config, String desc, String workerGroups) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        Map<String, Object> checkResult = checkParams(name,config,workerGroups);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Environment environment = environmentMapper.queryByEnvironmentName(name);
        if (environment != null && !environment.getCode().equals(code)) {
            putMsg(result, Status.ENVIRONMENT_NAME_EXISTS, name);
            return result;
        }

        Set<String> workerGroupSet;
        if (!StringUtils.isEmpty(workerGroups)) {
            workerGroupSet = JSONUtils.parseObject(workerGroups, new TypeReference<Set<String>>() {});
        } else {
            workerGroupSet = new TreeSet<>();
        }

        Set<String> existWorkerGroupSet = relationMapper
                .queryByEnvironmentCode(code)
                .stream()
                .map(item -> item.getWorkerGroup())
                .collect(Collectors.toSet());

        Set<String> deleteWorkerGroupSet = SetUtils.difference(existWorkerGroupSet,workerGroupSet).toSet();
        Set<String> addWorkerGroupSet = SetUtils.difference(workerGroupSet,existWorkerGroupSet).toSet();

        // verify whether the relation of this environment and worker groups can be adjusted
        checkResult = checkUsedEnvironmentWorkerGroupRelation(deleteWorkerGroupSet, name, code);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Environment env = new Environment();
        env.setCode(code);
        env.setName(name);
        env.setConfig(config);
        env.setDescription(desc);
        env.setOperator(loginUser.getId());
        env.setUpdateTime(new Date());

        int update = environmentMapper.update(env, new UpdateWrapper<Environment>().lambda().eq(Environment::getCode, code));
        if (update > 0) {
            deleteWorkerGroupSet.stream().forEach(key -> {
                if (StringUtils.isNotEmpty(key)) {
                    relationMapper.delete(new QueryWrapper<EnvironmentWorkerGroupRelation>()
                            .lambda()
                            .eq(EnvironmentWorkerGroupRelation::getEnvironmentCode, code)
                            .eq(EnvironmentWorkerGroupRelation::getWorkerGroup, key));
                }
            });
            addWorkerGroupSet.stream().forEach(key -> {
                if (StringUtils.isNotEmpty(key)) {
                    EnvironmentWorkerGroupRelation relation = new EnvironmentWorkerGroupRelation();
                    relation.setEnvironmentCode(code);
                    relation.setWorkerGroup(key);
                    relation.setUpdateTime(new Date());
                    relation.setCreateTime(new Date());
                    relation.setOperator(loginUser.getId());
                    relationMapper.insert(relation);
                }
            });
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.UPDATE_ENVIRONMENT_ERROR, name);
        }
        return result;
    }



    /**
     * verify environment name
     *
     * @param environmentName environment name
     * @return true if the environment name not exists, otherwise return false
     */
    @Override
    public Map<String, Object> verifyEnvironment(String environmentName) {
        Map<String, Object> result = new HashMap<>();

        if (StringUtils.isEmpty(environmentName)) {
            putMsg(result, Status.ENVIRONMENT_NAME_IS_NULL);
            return result;
        }

        Environment environment = environmentMapper.queryByEnvironmentName(environmentName);
        if (environment != null) {
            putMsg(result, Status.ENVIRONMENT_NAME_EXISTS, environmentName);
            return result;
        }

        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

    private Map<String, Object> checkUsedEnvironmentWorkerGroupRelation(Set<String> deleteKeySet,String environmentName, Long environmentCode) {
        Map<String, Object> result = new HashMap<>();
        for (String workerGroup : deleteKeySet) {
            List<TaskDefinition> taskDefinitionList = taskDefinitionMapper
                    .selectList(new QueryWrapper<TaskDefinition>().lambda()
                            .eq(TaskDefinition::getEnvironmentCode,environmentCode)
                            .eq(TaskDefinition::getWorkerGroup,workerGroup));

            if (Objects.nonNull(taskDefinitionList) && taskDefinitionList.size() != 0) {
                Set<String> collect = taskDefinitionList.stream().map(TaskDefinition::getName).collect(Collectors.toSet());
                putMsg(result, Status.UPDATE_ENVIRONMENT_WORKER_GROUP_RELATION_ERROR,workerGroup,environmentName, collect);
                return result;
            }
        }
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

    public Map<String, Object> checkParams(String name, String config, String workerGroups) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(name)) {
            putMsg(result, Status.ENVIRONMENT_NAME_IS_NULL);
            return result;
        }
        if (StringUtils.isEmpty(config)) {
            putMsg(result, Status.ENVIRONMENT_CONFIG_IS_NULL);
            return result;
        }
        if (!StringUtils.isEmpty(workerGroups)) {
            List<String> workerGroupList = JSONUtils.parseObject(workerGroups, new TypeReference<List<String>>(){});
            if (Objects.isNull(workerGroupList)) {
                putMsg(result, Status.ENVIRONMENT_WORKER_GROUPS_IS_INVALID);
                return result;
            }
        }
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

}


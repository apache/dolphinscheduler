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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.EnvironmentService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils.SnowFlakeException;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


/**
 * task definition service impl
 */
@Service
public class EnvironmentServiceImpl extends BaseServiceImpl implements EnvironmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentServiceImpl.class);

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    /**
     * create environment
     *
     * @param loginUser login user
     * @param name environment name
     * @param config environment config
     * @param desc environment desc
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> createEnvironment(User loginUser, String name, String config, String desc) {
        Map<String, Object> result = new HashMap<>();

        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        Map<String, Object> checkResult = checkParams(name,config);
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
        long code = 0L;
        try {
            code = SnowFlakeUtils.getInstance().nextId();
            env.setCode(code);
        } catch (SnowFlakeException e) {
            logger.error("Environment code get error, ", e);
        }
        if (code == 0L) {
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating environment code");
            return result;
        }

        if (environmentMapper.insert(env) > 0) {
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
        PageInfo<Environment> pageInfo = new PageInfo<>(pageNo, pageSize);

        Page<Environment> page = new Page<>(pageNo, pageSize);

        IPage<Environment> environmentIPage = environmentMapper.queryEnvironmentListPaging(page, searchVal);

        List<Environment> environmentList = environmentIPage.getRecords();
        pageInfo.setTotal((int) environmentIPage.getTotal());
        pageInfo.setTotalList(environmentList);
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
        Map<String,Object> result =new HashMap<>();
        List<Environment> environmentList = environmentMapper.queryAllEnvironmentList();
        result.put(Constants.DATA_LIST,environmentList);
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
            result.put(Constants.DATA_LIST, env);
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
            result.put(Constants.DATA_LIST, env);
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
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> updateEnvironmentByCode(User loginUser, Long code, String name, String config, String desc) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        Map<String, Object> checkResult = checkParams(name,config);
        if (checkResult.get(Constants.STATUS) != Status.SUCCESS) {
            return checkResult;
        }

        Environment environment = environmentMapper.queryByEnvironmentName(name);
        if (environment != null && !environment.getCode().equals(code)) {
            putMsg(result, Status.ENVIRONMENT_NAME_EXISTS, name);
            return result;
        }

        Environment env = new Environment();
        env.setCode(code);
        env.setName(name);
        env.setConfig(config);
        env.setDescription(desc);

        int update = environmentMapper.update(env, new UpdateWrapper<Environment>().lambda().eq(Environment::getCode,code));
        if (update > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.UPDATE_ENVIRONMENT_ERROR);
        }
        return result;
    }

    public Map<String, Object> checkParams(String name, String config) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(name)) {
            putMsg(result, Status.ENVIRONMENT_NAME_IS_NULL);
            return result;
        }
        if (StringUtils.isEmpty(config)) {
            putMsg(result, Status.ENVIRONMENT_CONFIG_IS_NULL);
            return result;
        }
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }

}


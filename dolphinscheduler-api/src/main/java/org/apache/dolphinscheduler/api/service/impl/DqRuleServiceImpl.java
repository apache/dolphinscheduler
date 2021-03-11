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

import static org.apache.dolphinscheduler.common.Constants.DATA_LIST;

import org.apache.dolphinscheduler.api.dto.RuleDefinition;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.service.DqRuleService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.dq.OptionSourceType;
import org.apache.dolphinscheduler.common.enums.dq.PropsType;
import org.apache.dolphinscheduler.common.enums.dq.RuleType;
import org.apache.dolphinscheduler.common.form.CascaderParamsOptions;
import org.apache.dolphinscheduler.common.form.ParamsOptions;
import org.apache.dolphinscheduler.common.form.PluginParams;
import org.apache.dolphinscheduler.common.form.Validate;
import org.apache.dolphinscheduler.common.form.props.InputParamsProps;
import org.apache.dolphinscheduler.common.form.type.CascaderParam;
import org.apache.dolphinscheduler.common.form.type.InputParam;
import org.apache.dolphinscheduler.common.form.type.RadioParam;
import org.apache.dolphinscheduler.common.form.type.SelectParam;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleExecuteSqlMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleInputEntryMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleMapper;
import org.apache.dolphinscheduler.dao.utils.DqRuleUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DqRuleServiceImpl
 */
@Service
public class DqRuleServiceImpl extends BaseService implements DqRuleService {

    @Autowired
    private DqRuleMapper dqRuleMapper;

    @Autowired
    private DqRuleInputEntryMapper dqRuleInputEntryMapper;

    @Autowired
    private DqRuleExecuteSqlMapper dqRuleExecuteSqlMapper;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Override
    public  Map<String, Object> getRuleFormCreateJsonById(int id) {

        Map<String, Object> result = new HashMap<>(5);

        DqRule dqRule = dqRuleMapper.selectById(id);
        if (dqRule == null) {
            return null;
        }

        List<DqRuleInputEntry> ruleInputEntryList = dqRuleInputEntryMapper.getRuleInputEntryList(id);

        if (ruleInputEntryList == null || ruleInputEntryList.isEmpty()) {
            putMsg(result, Status.EDIT_RESOURCE_FILE_ON_LINE_ERROR);
        } else {
            result.put(Constants.DATA_LIST, getRuleFormCreateJson(ruleInputEntryList));
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    @Override
    public  Map<String, Object> createRule(User loginUser,
                                           String name,
                                           int type,
                                           String ruleJson) {
        Map<String, Object> result = new HashMap<>(5);

        DqRule entity = new DqRule();
        entity.setName(name);
        entity.setType(RuleType.of(type));
        entity.setUserId(loginUser.getId());
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        int insert = dqRuleMapper.insert(entity);

        if (insert > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_ACCESS_TOKEN_ERROR);
        }

        return result;
    }

    @Override
    public  Map<String, Object> updateRule(User loginUser,
                                           int ruleId,
                                           String name,
                                           int type,
                                           String ruleJson) {
        Map<String, Object> result = new HashMap<>(5);

        //判断是否有权限进行更新，没有的话直接返回权限不足

        DqRule entity = dqRuleMapper.selectById(ruleId);
        if (entity == null) {
            //直接返回报错信息
            return null;
        }

        entity.setName(name);
        entity.setType(RuleType.of(type));
        entity.setUserId(loginUser.getId());
        entity.setUpdateTime(new Date());

        int update = dqRuleMapper.updateById(entity);

        if (update > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_ACCESS_TOKEN_ERROR);
        }

        return result;
    }

    @Override
    public  Map<String, Object> deleteById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>(5);
        int delete = dqRuleMapper.deleteById(id);

        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_ACCESS_TOKEN_ERROR);
        }

        return result;
    }

    @Override
    public Map<String, Object> queryDqsRuleListPage(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Page<DqRule> page = new Page<>(pageNo, pageSize);
        IPage<DqRule> rulePage =
                dqRuleMapper.selectPage(page,new QueryWrapper<DqRule>().like(StringUtils.isNotEmpty(searchVal),"name",searchVal));

        PageInfo<DqRule> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) rulePage.getTotal());
        pageInfo.setLists(rulePage.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    @Override
    public Map<String, Object> queryAllRuleList() {
        Map<String, Object> result = new HashMap<>();

        List<DqRule> ruleList =
                dqRuleMapper.selectList(new QueryWrapper<DqRule>());

        result.put(Constants.DATA_LIST, ruleList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    @Override
    public Map<String, Object> queryRuleListPaging(User loginUser,
                                                   String searchVal,
                                                   Integer ruleType,
                                                   String startTime,
                                                   String endTime,
                                                   Integer pageNo,
                                                   Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        Date start = null;
        Date end = null;
        try {
            if (StringUtils.isNotEmpty(startTime)) {
                start = DateUtils.getScheduleDate(startTime);
            }
            if (StringUtils.isNotEmpty(endTime)) {
                end = DateUtils.getScheduleDate(endTime);
            }
        } catch (Exception e) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "startTime,endTime");
            return result;
        }

        Page<DqRule> page = new Page<>(pageNo, pageSize);
        PageInfo<DqRule> pageInfo = new PageInfo<>(pageNo, pageSize);

        if (ruleType == null) {
            ruleType = -1;
        }

        IPage<DqRule> dqRulePage =
                dqRuleMapper.queryRuleListPaging(
                        page,
                        searchVal,
                        loginUser.getId(),
                        ruleType,
                        start,
                        end);

        List<DqRule> dataList = dqRulePage.getRecords();
        dataList.forEach(new Consumer<DqRule>() {
            @Override
            public void accept(DqRule dqRule) {
                List<DqRuleInputEntry> ruleInputEntryList =
                        DqRuleUtils.transformInputEntry(dqRuleInputEntryMapper.getRuleInputEntryList(dqRule.getId()));
                List<DqRuleExecuteSql> ruleExecuteSqlList = dqRuleExecuteSqlMapper.getExecuteSqlList(dqRule.getId());

                RuleDefinition ruleDefinition = new RuleDefinition(ruleInputEntryList,ruleExecuteSqlList);
                dqRule.setRuleJson(JSONUtils.toJsonString(ruleDefinition));
            }
        });

        pageInfo.setTotalCount((int) dqRulePage.getTotal());
        pageInfo.setLists(dataList);
        result.put(DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private String getRuleFormCreateJson(List<DqRuleInputEntry> ruleInputEntryList) {
        List<PluginParams> params = new ArrayList<>();

        for (DqRuleInputEntry inputEntry : ruleInputEntryList) {
            if (inputEntry.getShow()) {
                switch (inputEntry.getType()) {
                    case INPUT:
                        InputParam inputParam = InputParam
                                        .newBuilder(inputEntry.getField(),inputEntry.getTitle())
                                        .addValidate(Validate.newBuilder()
                                                             .setRequired(true)
                                                             .build())
                                        .setProps(new InputParamsProps().setDisabled(!inputEntry.getCanEdit()))
                                        .setValue(inputEntry.getValue())
                                        .setPlaceholder(inputEntry.getPlaceholder())
                                        .setSize("small")
                                        .build();
                        params.add(inputParam);
                        break;
                    case SELECT:
                        List<ParamsOptions> options = null;

                        if (OptionSourceType.DEFAULT == inputEntry.getOptionSourceType()) {
                            String optionStr = inputEntry.getOptions();
                            if (StringUtils.isNotEmpty(optionStr)) {
                                options = JSONUtils.toList(optionStr, ParamsOptions.class);
                            }
                        }

                        SelectParam selectParam = SelectParam
                                .newBuilder(inputEntry.getField(),inputEntry.getTitle())
                                .setParamsOptionsList(options)
                                .setValue(inputEntry.getValue())
                                .setSize("small")
                                .build();
                        params.add(selectParam);
                        break;
                    case RADIO:
                        List<ParamsOptions> radioOptions = null;

                        if (OptionSourceType.DEFAULT == inputEntry.getOptionSourceType()) {
                            String optionStr = inputEntry.getOptions();
                            if (StringUtils.isNotEmpty(optionStr)) {
                                radioOptions = JSONUtils.toList(optionStr, ParamsOptions.class);
                            }
                        }

                        RadioParam radioParam = RadioParam
                                .newBuilder(inputEntry.getField(),inputEntry.getTitle())
                                .setParamsOptionsList(radioOptions)
                                .setValue(inputEntry.getValue())
                                .setSize("small")
                                .build();
                        params.add(radioParam);
                        break;
                    case SWITCH:
                        break;
                    case CASCADER:
                        List<CascaderParamsOptions> cascaderOptions = null;

                        if (OptionSourceType.DEFAULT == inputEntry.getOptionSourceType()) {
                            String optionStr = inputEntry.getOptions();
                            if (StringUtils.isNotEmpty(optionStr)) {
                                cascaderOptions = JSONUtils.toList(optionStr, CascaderParamsOptions.class);
                            }
                        } else if (OptionSourceType.DATASOURCE == inputEntry.getOptionSourceType()) {
                            cascaderOptions = new ArrayList<>();
                            for (DbType dbtype: DbType.values()) {
                                CascaderParamsOptions cascaderParamsOptions =
                                        new CascaderParamsOptions(dbtype.getDescp(),dbtype.getCode(),false);
                                List<CascaderParamsOptions> children = null;
                                List<DataSource> dataSourceList = dataSourceMapper.listAllDataSourceByType(dbtype.getCode());
                                if (CollectionUtils.isNotEmpty(dataSourceList)) {
                                    children = new ArrayList<>();
                                    for (DataSource dataSource: dataSourceList) {
                                        CascaderParamsOptions childrenOption =
                                                new CascaderParamsOptions(dataSource.getName(),dataSource.getId(),false);
                                        children.add(childrenOption);
                                    }
                                    cascaderParamsOptions.setChildren(children);
                                    cascaderOptions.add(cascaderParamsOptions);
                                }

                            }
                        }

                        CascaderParam cascaderParam = CascaderParam
                                .newBuilder(inputEntry.getField(),inputEntry.getTitle())
                                .setParamsOptionsList(cascaderOptions)
                                .setValue(Integer.valueOf(inputEntry.getValue()))
                                .setSize("small")
                                .build();
                        params.add(cascaderParam);
                        break;

                    case TEXTAREA:
                        InputParam textareaParam = InputParam
                                .newBuilder(inputEntry.getField(),inputEntry.getTitle())
                                .addValidate(Validate.newBuilder()
                                        .setRequired(true)
                                        .build())
                                .setProps(new InputParamsProps().setDisabled(!inputEntry.getCanEdit()))
                                .setValue(inputEntry.getValue())
                                .setSize("small")
                                .setType(PropsType.TEXTAREA)
                                .setRows(1)
                                .setPlaceholder(inputEntry.getPlaceholder())
                                .build();
                        params.add(textareaParam);
                        break;
                    default:
                        break;
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }
    
}

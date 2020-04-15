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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.GlobalVariable;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.GlobalVariableMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;

/**
 * Variable service
 */
@Service
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class GlobalVariableService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(GlobalVariableService.class);

  @Autowired
  private GlobalVariableMapper schedulerVariableMapper;

  @Autowired
  private UserMapper userMapper;


  /**
   *
   * @param loginUser
   * @param name
   * @param key
   * @param value
   * @return
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String,Object> createVariable(User loginUser,
                         int projectId,
                         String name,
                         String key,
                         String value)  {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);

    if (checkVariableExists(projectId,key)){
      putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, name);
      return result;
    }

    GlobalVariable variable = new GlobalVariable();
    Date now = new Date();
    variable.setProjectId(projectId);
    variable.setName(name);
    variable.setKeyData(key);
    variable.setValueData(value);
    variable.setFlag(Flag.NO);
    variable.setUserId(loginUser.getId());
    variable.setCreateTime(now);
    variable.setUpdateTime(now);

    // save
    schedulerVariableMapper.insert(variable);

    putMsg(result, Status.SUCCESS);
    result.put(Constants.MSG, Status.SUCCESS.getMsg());

    return result;
}


  /**
   * query Variable list paging
   * @param loginUser
   * @param searchVal
   * @param projectId
   * @param pageNo
   * @param pageSize
   * @return
   */
  public Map<String,Object> queryVariableList(User loginUser, String searchVal, int projectId, Integer pageNo, Integer pageSize) {

    Map<String, Object> result = new HashMap<>(5);
    if (checkAdmin(loginUser, result)) {
      return result;
    }

    Page<GlobalVariable> page = new Page(pageNo, pageSize);
    IPage<GlobalVariable> variableIPage = schedulerVariableMapper.queryVariablePaging(page ,projectId, searchVal);
    PageInfo<GlobalVariable> pageInfo = new PageInfo<>(pageNo, pageSize);
    pageInfo.setTotalCount((int)variableIPage.getTotal());
    pageInfo.setLists(variableIPage.getRecords());
    result.put(Constants.DATA_LIST, pageInfo);

    putMsg(result, Status.SUCCESS);

    return result;
  }

  /**
   *
   * @param loginUser
   * @param id
   * @param name
   * @param key
   * @param value
   * @return
   */
  public Map<String, Object>  updateVariable(User loginUser,
          int id,
          String name,
          String key,
          String value)  {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);


    GlobalVariable variable = schedulerVariableMapper.selectById(id);

    if (variable == null){
      putMsg(result, Status.VARIABLE_NOT_EXIST);
      return result;
    }

    // update GlobalVariable Instance
    Date now = new Date();

    if (StringUtils.isNotEmpty(name)){
      variable.setName(name);
    }
    if (StringUtils.isNotEmpty(key)){
      variable.setKeyData(key);
    }
    if (StringUtils.isNotEmpty(value)){
      variable.setValueData(value);
    }

    variable.setUserId(loginUser.getId());
    variable.setUpdateTime(now);

    schedulerVariableMapper.updateById(variable);


    putMsg(result, Status.SUCCESS);
    result.put(Constants.MSG, Status.SUCCESS.getMsg());

    return result;

  }

  /**
   * delete GlobalVariable
   *
   * @param loginUser login user
   * @param id GlobalVariable id
   * @return delete result code
   * @throws Exception exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> deleteVariableById(User loginUser, int id) throws Exception {
    Map<String, Object> result = new HashMap<>(5);


    GlobalVariable variable = schedulerVariableMapper.selectById(id);
    if (variable == null){
      putMsg(result, Status.VARIABLE_NOT_EXIST);
      return result;
    }

    schedulerVariableMapper.deleteById(id);

    putMsg(result, Status.SUCCESS);
    return result;
  }


  /**
   *
   * @param loginUser
   * @param projectId
   * @param id
   * @return
   * @throws Exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> selectById(User loginUser, int projectId, int id) throws Exception {
    Map<String, Object> result = new HashMap<>(5);

    GlobalVariable variable = schedulerVariableMapper.selectById(projectId,id);

    if (variable == null){
      putMsg(result, Status.VARIABLE_NOT_EXIST);
      return result;
    }

    putMsg(result, Status.SUCCESS);
    return result;
  }



  /**
   * query GlobalVariable list
   *
   * @param loginUser login user
   * @return GlobalVariable list
   */
  public Map<String, Object> queryGlobalVariableList(User loginUser,  int projectId, String key , String name) {
    Map<String, Object> result = new HashMap<>(5);

    List<GlobalVariable> resourceList = schedulerVariableMapper.queryList(projectId, key ,name);
    result.put(Constants.DATA_LIST, resourceList);
    putMsg(result, Status.SUCCESS);
    
    return result;
  }

  /**
   *
   * @param loginUser
   * @return
   */
  public Map<String, Object> queryVariableList(User loginUser , int projectId, String key ) {
    Map<String, Object> result = new HashMap<>(5);

    List<GlobalVariable> resourceList = schedulerVariableMapper.queryByVariableKey(projectId, key);
    if (CollectionUtils.isNotEmpty(resourceList)) {
      result.put(Constants.DATA_LIST, resourceList);
      putMsg(result, Status.SUCCESS);
    } else {
      putMsg(result, Status.VARIABLE_NOT_EXIST);
    }

    return result;
  }

  /**
   * verify GlobalVariable key
   *
   * @param    key
   *
   * @return true if GlobalVariable name can user, otherwise return false
   */
  public Result verifyVariableKey(int projectId ,String key) {
    Result result=new Result();
    if (checkVariableExists(projectId,key)) {
      logger.error("Variable {} has exist, can't create again.", key);
      putMsg(result, Status.VARIABLE_KEY_EXIST);
    }else{
      putMsg(result, Status.SUCCESS);
    }
    return result;
  }


  /**
   * check GlobalVariable exists
   * @param   projectId
   * @param   key
   * @return ture if the GlobalVariable name exists, otherwise return false
   */
  private boolean checkVariableExists(int projectId , String key) {
      List<GlobalVariable> Variables = schedulerVariableMapper.queryByVariableKey(projectId,key);
      return CollectionUtils.isNotEmpty(Variables);
  }


}

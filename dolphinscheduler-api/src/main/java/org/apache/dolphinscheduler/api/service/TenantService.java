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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * tenant service
 */
@Service
public class TenantService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

  @Autowired
  private TenantMapper tenantMapper;

  @Autowired
  private ProcessInstanceMapper processInstanceMapper;

  @Autowired
  private ProcessDefinitionMapper processDefinitionMapper;

  @Autowired
  private UserMapper userMapper;



  /**
   * create tenant
   *
   *
   * @param loginUser login user
   * @param tenantCode tenant code
   * @param tenantName tenant name
   * @param queueId queue id
   * @param desc description
   * @return create result code
   * @throws Exception exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String,Object> createTenant(User loginUser,
                         String tenantCode,
                         String tenantName,
                         int queueId,
                         String desc) throws Exception {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);
    if (checkAdmin(loginUser, result)) {
      return result;
    }

    if (checkTenantExists(tenantCode)){
      putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, tenantCode);
      return result;
    }


    Tenant tenant = new Tenant();
    Date now = new Date();

    if (!tenantCode.matches("^[0-9a-zA-Z_.-]{1,}$") || tenantCode.startsWith("-") || tenantCode.startsWith(".")){
      putMsg(result, Status.VERIFY_TENANT_CODE_ERROR);
      return result;
    }
    tenant.setTenantCode(tenantCode);
    tenant.setTenantName(tenantName);
    tenant.setQueueId(queueId);
    tenant.setDescription(desc);
    tenant.setCreateTime(now);
    tenant.setUpdateTime(now);

    // save
    tenantMapper.insert(tenant);

    // if hdfs startup
    if (PropertyUtils.getResUploadStartupState()){
        createTenantDirIfNotExists(tenantCode);
    }

    putMsg(result, Status.SUCCESS);

    return result;
}



  /**
   * query tenant list paging
   *
   * @param loginUser login user
   * @param searchVal search value
   * @param pageNo page number
   * @param pageSize page size
   * @return tenant list page
   */
  public Map<String,Object> queryTenantList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

    Map<String, Object> result = new HashMap<>(5);
    if (checkAdmin(loginUser, result)) {
      return result;
    }

    Page<Tenant> page = new Page(pageNo, pageSize);
    IPage<Tenant> tenantIPage = tenantMapper.queryTenantPaging(page, searchVal);
    PageInfo<Tenant> pageInfo = new PageInfo<>(pageNo, pageSize);
    pageInfo.setTotalCount((int)tenantIPage.getTotal());
    pageInfo.setLists(tenantIPage.getRecords());
    result.put(Constants.DATA_LIST, pageInfo);

    putMsg(result, Status.SUCCESS);

    return result;
  }

  /**
   * updateProcessInstance tenant
   *
   * @param loginUser login user
   * @param id tennat id
   * @param tenantCode tennat code
   * @param tenantName tennat name
   * @param queueId queue id
   * @param desc description
   * @return update result code
   * @throws Exception exception
   */
  public Map<String, Object>  updateTenant(User loginUser,int id,String tenantCode, String tenantName, int queueId, String desc) throws Exception {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);

    if (checkAdmin(loginUser, result)) {
      return result;
    }

    Tenant tenant = tenantMapper.queryById(id);

    if (tenant == null){
      putMsg(result, Status.TENANT_NOT_EXIST);
      return result;
    }

    // updateProcessInstance tenant
    /**
     * if the tenant code is modified, the original resource needs to be copied to the new tenant.
     */
    if (!tenant.getTenantCode().equals(tenantCode)){
      if (checkTenantExists(tenantCode)){
        // if hdfs startup
        if (PropertyUtils.getResUploadStartupState()){
          String resourcePath = HadoopUtils.getHdfsDataBasePath() + "/" + tenantCode + "/resources";
          String udfsPath = HadoopUtils.getHdfsUdfDir(tenantCode);
          //init hdfs resource
          HadoopUtils.getInstance().mkdir(resourcePath);
          HadoopUtils.getInstance().mkdir(udfsPath);
        }
      }else {
        putMsg(result, Status.TENANT_CODE_HAS_ALREADY_EXISTS);
        return result;
      }
    }

    Date now = new Date();

    if (StringUtils.isNotEmpty(tenantCode)){
      tenant.setTenantCode(tenantCode);
    }

    if (StringUtils.isNotEmpty(tenantName)){
      tenant.setTenantName(tenantName);
    }

    if (queueId != 0){
      tenant.setQueueId(queueId);
    }
    tenant.setDescription(desc);
    tenant.setUpdateTime(now);
    tenantMapper.updateById(tenant);

    result.put(Constants.STATUS, Status.SUCCESS);
    result.put(Constants.MSG, Status.SUCCESS.getMsg());
    return result;
  }

  /**
   * delete tenant 
   *
   * @param loginUser login user
   * @param id tenant id
   * @return delete result code
   * @throws Exception exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> deleteTenantById(User loginUser, int id) throws Exception {
    Map<String, Object> result = new HashMap<>(5);

    if (checkAdmin(loginUser, result)) {
      return result;
    }

    Tenant tenant = tenantMapper.queryById(id);
    if (tenant == null){
      putMsg(result, Status.TENANT_NOT_EXIST);
      return result;
    }

    List<ProcessInstance> processInstances = getProcessInstancesByTenant(tenant);
    if(CollectionUtils.isNotEmpty(processInstances)){
      putMsg(result, Status.DELETE_TENANT_BY_ID_FAIL, processInstances.size());
      return result;
    }

    List<ProcessDefinition> processDefinitions = processDefinitionMapper.queryDefinitionListByTenant(tenant.getId());
    if(CollectionUtils.isNotEmpty(processDefinitions)){
      putMsg(result, Status.DELETE_TENANT_BY_ID_FAIL_DEFINES, processDefinitions.size());
      return result;
    }

    List<User> userList = userMapper.queryUserListByTenant(tenant.getId());
    if(CollectionUtils.isNotEmpty(userList)){
      putMsg(result, Status.DELETE_TENANT_BY_ID_FAIL_USERS, userList.size());
      return result;
    }

    // if resource upload startup
    if (PropertyUtils.getResUploadStartupState()){
      String tenantPath = HadoopUtils.getHdfsDataBasePath() + "/" + tenant.getTenantCode();

      if (HadoopUtils.getInstance().exists(tenantPath)){
        HadoopUtils.getInstance().delete(tenantPath, true);
      }
    }

    tenantMapper.deleteById(id);
    processInstanceMapper.updateProcessInstanceByTenantId(id, -1);
    putMsg(result, Status.SUCCESS);
    return result;
  }

  private List<ProcessInstance> getProcessInstancesByTenant(Tenant tenant) {
    return processInstanceMapper.queryByTenantIdAndStatus(tenant.getId(), org.apache.dolphinscheduler.common.Constants.NOT_TERMINATED_STATES);
  }

  /**
   * query tenant list
   *
   * @param loginUser login user
   * @return tenant list
   */
  public Map<String, Object> queryTenantList(User loginUser) {

    Map<String, Object> result = new HashMap<>(5);

    List<Tenant> resourceList = tenantMapper.selectList(null);
    result.put(Constants.DATA_LIST, resourceList);
    putMsg(result, Status.SUCCESS);
    
    return result;
  }

  /**
   * query tenant list via tenant code
   * @param tenantCode tenant code
   * @return tenant list
   */
  public Map<String, Object> queryTenantList(String tenantCode) {
    Map<String, Object> result = new HashMap<>(5);

    List<Tenant> resourceList = tenantMapper.queryByTenantCode(tenantCode);
    if (CollectionUtils.isNotEmpty(resourceList)) {
      result.put(Constants.DATA_LIST, resourceList);
      putMsg(result, Status.SUCCESS);
    } else {
      putMsg(result, Status.TENANT_NOT_EXIST);
    }

    return result;
  }

  /**
   * verify tenant code
   *
   * @param tenantCode tenant code
   * @return true if tenant code can user, otherwise return false
   */
  public Result verifyTenantCode(String tenantCode) {
    Result result = new Result();
    if (checkTenantExists(tenantCode)) {
      logger.error("tenant {} has exist, can't create again.", tenantCode);
      putMsg(result, Status.TENANT_NAME_EXIST, tenantCode);
    } else {
      putMsg(result, Status.SUCCESS);
    }
    return result;
  }


  /**
   * check tenant exists
   *
   * @param tenantCode tenant code
   * @return ture if the tenant code exists, otherwise return false
   */
  private boolean checkTenantExists(String tenantCode) {
      List<Tenant> tenants = tenantMapper.queryByTenantCode(tenantCode);
      return CollectionUtils.isNotEmpty(tenants);
  }
}

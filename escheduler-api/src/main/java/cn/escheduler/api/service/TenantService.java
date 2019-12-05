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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.utils.HadoopUtils;
import cn.escheduler.common.utils.PropertyUtils;
import cn.escheduler.dao.mapper.TenantMapper;
import cn.escheduler.dao.model.Tenant;
import cn.escheduler.dao.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileStatus;
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

  /**
   * create tenant
   *
   * @param loginUser
   * @param tenantCode
   * @param tenantName
   * @param queueId
   * @param desc
   * @return
   */
  @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
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

    if (!checkTenant(tenantCode)){
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
    tenant.setDesc(desc);
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
   * @param loginUser
   * @param searchVal
   * @param pageNo
   * @param pageSize
   * @return
   */
  public Map<String,Object> queryTenantList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

    Map<String, Object> result = new HashMap<>(5);
    if (checkAdmin(loginUser, result)) {
      return result;
    }

    Integer count = tenantMapper.countTenantPaging(searchVal);

    PageInfo<Tenant> pageInfo = new PageInfo<>(pageNo, pageSize);

    List<Tenant> scheduleList = tenantMapper.queryTenantPaging(searchVal, pageInfo.getStart(), pageSize);

    pageInfo.setTotalCount(count);
    pageInfo.setLists(scheduleList);
    result.put(Constants.DATA_LIST, pageInfo);

    putMsg(result, Status.SUCCESS);

    return result;
  }

  /**
   * updateProcessInstance tenant
   *
   * @param loginUser
   * @param tenantCode
   * @param tenantName
   * @param queueId
   * @param desc
   * @return
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
      Tenant newTenant = tenantMapper.queryByTenantCode(tenantCode);
      if (newTenant == null){
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
    tenant.setDesc(desc);
    tenant.setUpdateTime(now);
    tenantMapper.update(tenant);

    result.put(Constants.STATUS, Status.SUCCESS);
    result.put(Constants.MSG, Status.SUCCESS.getMsg());
    return result;
  }

  /**
   * delete tenant 
   * 
   * @param loginUser
   * @param id
   * @return
   */
  @Transactional(value = "TransactionManager", rollbackFor = Exception.class)
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

    // if resource upload startup
    if (PropertyUtils.getResUploadStartupState()){
      String tenantPath = HadoopUtils.getHdfsDataBasePath() + "/" + tenant.getTenantCode();

      if (HadoopUtils.getInstance().exists(tenantPath)){
        String resourcePath = HadoopUtils.getHdfsResDir(tenant.getTenantCode());
        FileStatus[] fileStatus = HadoopUtils.getInstance().listFileStatus(resourcePath);
        if (fileStatus.length > 0) {
          putMsg(result, Status.HDFS_TERANT_RESOURCES_FILE_EXISTS);
          return result;
        }
        fileStatus = HadoopUtils.getInstance().listFileStatus(HadoopUtils.getHdfsUdfDir(tenant.getTenantCode()));
        if (fileStatus.length > 0) {
          putMsg(result, Status.HDFS_TERANT_UDFS_FILE_EXISTS);
          return result;
        }

        HadoopUtils.getInstance().delete(tenantPath, true);
      }
    }

    tenantMapper.deleteById(id);
    putMsg(result, Status.SUCCESS);
    return result;
  }

  /**
   * query tenant list
   * 
   * @param loginUser
   * @return
   */
  public Map<String, Object> queryTenantList(User loginUser) {

    Map<String, Object> result = new HashMap<>(5);

    List<Tenant> resourceList = tenantMapper.queryAllTenant();
    result.put(Constants.DATA_LIST, resourceList);
    putMsg(result, Status.SUCCESS);
    
    return result;
  }

  /**
   * verify tenant code
   * 
   * @param tenantCode
   * @return
   */
  public Result verifyTenantCode(String tenantCode) {
    Result result=new Result();
    Tenant tenant= tenantMapper.queryByTenantCode(tenantCode);
    if (tenant != null) {
      logger.error("tenant {} has exist, can't create again.", tenantCode);
      putMsg(result, Status.TENANT_NAME_EXIST);
    }else{
      putMsg(result, Status.SUCCESS);
    }
    return result;
  }


  /**
   * check tenant exists
   *
   * @param tenantCode
   * @return
   */
  private boolean checkTenant(String tenantCode) {
    return tenantMapper.queryByTenantCode(tenantCode) == null ? true : false;
  }
}

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.api.dto.ExtPlatformParam;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExtHttpType;
import org.apache.dolphinscheduler.common.enums.ExtPlatformType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.HttpMethod;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ExtPlatform;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ExtPlatformMapper;
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
 * ExtPlatform service
 */
@Service
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class ExtPlatformService extends BaseService{

  private static final Logger logger = LoggerFactory.getLogger(ExtPlatformService.class);

  @Autowired
  private ExtPlatformMapper schedulerExtPlatformMapper;

  @Autowired
  private UserMapper userMapper;


  /**
   *
   * @param loginUser
   * @param name
   * @param extPlatformType
   * @param connectParam
   * @param desc
   * @return
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String,Object> createExtPlatform(User loginUser,
                         String name,
                         int extPlatformType,
                         String connectParam,
                         String desc)  {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);

    if (checkExtPlatformExists(name)){
      putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, name);
      return result;
    }

    if (null == connectParam || connectParam.isEmpty()){
      putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, connectParam);
      return result;
    }

    ExtPlatform extPlatform = new ExtPlatform();
    Date now = new Date();

    extPlatform.setName(name);
    extPlatform.setPlatformType(ExtPlatformType.getEnum(extPlatformType));
    extPlatform.setConnectParam(connectParam);

    extPlatform.setUserId(loginUser.getId());
    extPlatform.setDescription(desc);
    extPlatform.setCreateTime(now);
    extPlatform.setUpdateTime(now);

    // save
    schedulerExtPlatformMapper.insert(extPlatform);

    putMsg(result, Status.SUCCESS);
    result.put(Constants.MSG, Status.SUCCESS.getMsg());


    return result;
}



  /**
   * query ExtPlatform list paging
   *
   * @param loginUser login user
   * @param searchVal search value
   * @param pageNo page number
   * @param pageSize page size
   * @return ExtPlatform list page
   */
  public Map<String,Object> queryExtPlatformList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

    Map<String, Object> result = new HashMap<>(5);
    if (checkAdmin(loginUser, result)) {
      return result;
    }

    Page<ExtPlatform> page = new Page(pageNo, pageSize);
    IPage<ExtPlatform> extPlatformIPage = schedulerExtPlatformMapper.queryExtPlatformPaging(page, searchVal,loginUser.getId());
    PageInfo<ExtPlatform> pageInfo = new PageInfo<>(pageNo, pageSize);
    pageInfo.setTotalCount((int)extPlatformIPage.getTotal());
    pageInfo.setLists(extPlatformIPage.getRecords());
    result.put(Constants.DATA_LIST, pageInfo);

    putMsg(result, Status.SUCCESS);

    return result;
  }

  /**
   *
   * @param loginUser
   * @param id
   * @param name
   * @param desc
   * @return
   * @throws Exception
   */
  public Map<String, Object>  updateExtPlatform(User loginUser,
          int id,
          String name,
          int extPlatformType,
          String connectParam,
          String desc)  {

    Map<String, Object> result = new HashMap<>(5);
    result.put(Constants.STATUS, false);


    ExtPlatform extPlatform = schedulerExtPlatformMapper.selectById(id);

    if (extPlatform == null){
      putMsg(result, Status.EXTPLAFTORM_NOT_EXIST);
      return result;
    }

    // update ExtPlatform Instance
    Date now = new Date();

    if (StringUtils.isNotEmpty(name)){
      extPlatform.setName(name);
    }

    if (null != extPlatform.getConnectParam() ){
      extPlatform.setConnectParam(connectParam);
    }
    if (null != extPlatform.getPlatformType() & 0 != extPlatformType ){
      extPlatform.setPlatformType(ExtPlatformType.getEnum(extPlatformType));
    }

    extPlatform.setUserId(loginUser.getId());
    extPlatform.setDescription(desc);

    extPlatform.setUpdateTime(now);

    try{
      schedulerExtPlatformMapper.updateById(extPlatform);
      putMsg(result, Status.SUCCESS);
      result.put(Constants.MSG, Status.SUCCESS.getMsg());
    }catch (Exception e) {

      putMsg(result, Status.UPDATE_EXTPLAFTORM_ERROR);
      result.put(Constants.MSG, Status.UPDATE_EXTPLAFTORM_ERROR.getMsg());
    }

    return result;

  }

  /**
   * delete ExtPlatform
   *
   * @param loginUser login user
   * @param id ExtPlatform id
   * @return delete result code
   * @throws Exception exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> deleteExtPlatformById(User loginUser, int id) throws Exception {
    Map<String, Object> result = new HashMap<>(5);


    ExtPlatform extPlatform = schedulerExtPlatformMapper.selectById(id);
    if (extPlatform == null){
      putMsg(result, Status.EXTPLAFTORM_NOT_EXIST);
      return result;
    }

    schedulerExtPlatformMapper.deleteById(id);

    putMsg(result, Status.SUCCESS);
    return result;
  }


  /**
   * select ExtPlatform
   *
   * @param loginUser login user
   * @param id ExtPlatform id
   * @return delete result code
   * @throws Exception exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> selectById(User loginUser, int id)  {
    Map<String, Object> result = new HashMap<>(5);


    ExtPlatform extPlatform = schedulerExtPlatformMapper.selectById(id);

    if (extPlatform == null){
      putMsg(result, Status.EXTPLAFTORM_NOT_EXIST);
      return result;
    }else{
      result.put(Constants.DATA_LIST,extPlatform);
    }


    putMsg(result, Status.SUCCESS);
    return result;
  }

  /**
   *
   * @param loginUser
   * @return
   */
  public Map<String, Object> queryExtPlatformList(User loginUser) {
    Map<String, Object> result = new HashMap<>(5);

    List<ExtPlatform> resourceList = schedulerExtPlatformMapper.queryExtPlatformList(loginUser.getId());
    if (CollectionUtils.isNotEmpty(resourceList)) {
      result.put(Constants.DATA_LIST, resourceList);
      putMsg(result, Status.SUCCESS);
    } else {
      putMsg(result, Status.EXTPLAFTORM_NOT_EXIST);
    }

    return result;
  }

  /**
   * verify ExtPlatform name
   *
   * @param    name
   * @return true if ExtPlatform name can user, otherwise return false
   */
  public Result verifyExtPlatformName(String name) {
    Result result=new Result();
    if (checkExtPlatformExists(name)) {
      logger.error("ExtPlatform {} has exist, can't create again.", name);
      putMsg(result, Status.EXTPLAFTORM_NAME_EXIST);
    }else{
      putMsg(result, Status.SUCCESS);
    }
    return result;
  }


  /**
   * check ExtPlatform exists
   *
   * @param   name
   * @return ture if the ExtPlatform name exists, otherwise return false
   */
  private boolean checkExtPlatformExists(String name) {
      List<ExtPlatform> ExtPlatforms = schedulerExtPlatformMapper.queryByExtPlatformName(name);
      return CollectionUtils.isNotEmpty(ExtPlatforms);
  }


  public Result queryExtlist(User loginUser, int id) {
    Result result=new Result();
    putMsg(result, Status.SELECT_EXTPLAFTORM_BY_ID_ERROR);

    try{

      ExtPlatform extPlatform = schedulerExtPlatformMapper.selectById(id);

      if(null == extPlatform ){
        putMsg(result, Status.EXTPLAFTORM_NOT_EXIST);
      }

      String connectParam = extPlatform.getConnectParam();
      if(StringUtils.isBlank(connectParam)){
        return result;
      }

      ExtPlatformParam extPlatformParam = JSONUtils.parseObject(connectParam,ExtPlatformParam.class);
      if(null == extPlatformParam || StringUtils.isBlank(extPlatformParam.getUrl())){
        return result;
      }

      String responseContent = doRequest(extPlatformParam) ;

      if(null != responseContent && !responseContent.isEmpty()) {
        result = JSONUtils.parseObject(responseContent, Result.class);
      }

    }catch (Exception e) {
      e.printStackTrace();

    }

    return result;
  }

  public Result queryExtDetail(User loginUser, String connectParam) {
    Result result=new Result();
    putMsg(result, Status.SELECT_EXTPLAFTORM_BY_ID_ERROR);

    try{

      if(StringUtils.isBlank(connectParam)){
        return result;
      }

      ExtPlatformParam extPlatformParam = JSONUtils.parseObject(connectParam,ExtPlatformParam.class);
      if(null == extPlatformParam ||
              StringUtils.isBlank(extPlatformParam.getName()) ||
              StringUtils.isBlank(extPlatformParam.getUrl())){
        return result;
      }

      String responseContent = doRequest(extPlatformParam) ;


      if(null != responseContent && !responseContent.isEmpty()) {
        result = JSONUtils.parseObject(responseContent, Result.class);
      }

    }catch (Exception e) {
      e.printStackTrace();

    }

    return result;
  }

  private String doRequest(ExtPlatformParam extPlatformParam) {

    String responseContent = null ;
    try {
      responseContent = HttpUtils.request(ExtHttpType.getDescp(extPlatformParam.getUrlType()),extPlatformParam.getUrl(),null , null , null);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return responseContent;

  }

}

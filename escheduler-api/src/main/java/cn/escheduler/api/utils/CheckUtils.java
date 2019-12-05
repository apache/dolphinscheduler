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
package cn.escheduler.api.utils;


import cn.escheduler.api.enums.Status;
import cn.escheduler.common.enums.ResUploadType;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.common.utils.PropertyUtils;
import cn.escheduler.common.utils.TaskParametersUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.escheduler.common.utils.PropertyUtils.getBoolean;


/**
 * check utils
 */
public class CheckUtils {


  /**
   * check username
   *
   * @param userName
   */
  public static boolean checkUserName(String userName) {
    return regexChecks(userName, cn.escheduler.common.Constants.REGEX_USER_NAME);
  }

  /**
   * check email
   *
   * @param email
   */
  public static boolean checkEmail(String email) {
    return email.length() > 5 && email.length() <= 40 && regexChecks(email, cn.escheduler.common.Constants.REGEX_MAIL_NAME) ;
  }

  /**
   * check project description
   *
   * @param desc
   */
  public static Map<String, Object> checkDesc(String desc) {
    Map<String, Object> result = new HashMap<>();
    if (StringUtils.isNotEmpty(desc) && desc.length() > 200) {
        result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
        result.put(Constants.MSG, MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), "desc length"));
    }else{
      result.put(Constants.STATUS, Status.SUCCESS);
    }
    return result;
  }

  /**
   * check extra info
   *
   * @param otherParams
   */
  public static boolean checkOtherParams(String otherParams) {
    return StringUtils.isNotEmpty(otherParams) && !JSONUtils.checkJsonVaild(otherParams);
  }

  /**
   * check password
   *
   * @param password
   */
  public static boolean checkPassword(String password) {
    return StringUtils.isNotEmpty(password) && password.length() >= 2 && password.length() <= 20;
  }

  /**
   * check phone
   *
   * @param phone
   */
  public static boolean checkPhone(String phone) {
    return StringUtils.isNotEmpty(phone) && phone.length() > 18;
  }


  /**
   * check task node parameter
   *
   * @param parameter
   * @param taskType
   * @return
   */
  public static boolean checkTaskNodeParameters(String parameter, String taskType) {
    AbstractParameters abstractParameters = TaskParametersUtils.getParameters(taskType, parameter);

    if (abstractParameters != null) {
      return abstractParameters.checkParameters();
    }

    return false;
  }

  /**
   * check params
   * @param userName
   * @param password
   * @param email
   * @param phone
   * @return
   */
  public static Map<String, Object> checkUserParams(String userName, String password, String email, String phone){
      Map<String, Object> result = new HashMap<>();
     try{
       CheckUtils.checkUserName(userName);
       CheckUtils.checkEmail(email);
       CheckUtils.checkPassword(password);
       CheckUtils.checkPhone(phone);
       result.put(Constants.STATUS, Status.SUCCESS);
       result.put(Constants.MSG, Status.SUCCESS.getMsg());
     }catch (Exception e){
       result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
       result.put(Constants.MSG, e.getMessage());
     }
      return result;
  }





  /**
   * 正则匹配
   *
   * @param str
   * @param pattern
   * @return
   */
  private static boolean regexChecks(String str, Pattern pattern) {
    if (org.apache.commons.lang3.StringUtils.isEmpty(str)) {
      return false;
    }

    return pattern.matcher(str).matches();
  }
}

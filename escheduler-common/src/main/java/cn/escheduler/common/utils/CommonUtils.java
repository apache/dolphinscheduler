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
package cn.escheduler.common.utils;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ResUploadType;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static cn.escheduler.common.Constants.*;
import static cn.escheduler.common.utils.PropertyUtils.getBoolean;
import static cn.escheduler.common.utils.PropertyUtils.getString;

/**
 * common utils
 */
public class CommonUtils {

  private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

  /**
   * @return get the path of system environment variables
   */
  public static String getSystemEnvPath() {
    String envPath = getString(ESCHEDULER_ENV_PATH);
    if (StringUtils.isEmpty(envPath)) {
      envPath = System.getProperty("user.home") + File.separator + ".bash_profile";
    }

    return envPath;
  }

  /**
   * @return get queue implementation name
   */
  public static String getQueueImplValue(){
    return getString(Constants.SCHEDULER_QUEUE_IMPL);
  }

  /**
   * 
   * @return is develop mode
   */
  public static boolean isDevelopMode() {
    return getBoolean(DEVELOPMENT_STATE);
  }



  /**
   * if upload resource is HDFS and kerberos startup is true , else false
   * @return
   */
  public static boolean getKerberosStartupState(){
    String resUploadStartupType = PropertyUtils.getString(cn.escheduler.common.Constants.RES_UPLOAD_STARTUP_TYPE);
    ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
    Boolean kerberosStartupState = getBoolean(cn.escheduler.common.Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE);
    return resUploadType == ResUploadType.HDFS && kerberosStartupState;
  }

  /**
   * load kerberos configuration
   * @throws Exception
   */
  public static void loadKerberosConf()throws Exception{
    if (CommonUtils.getKerberosStartupState())  {
      System.setProperty(JAVA_SECURITY_KRB5_CONF, getString(JAVA_SECURITY_KRB5_CONF_PATH));
      Configuration configuration = new Configuration();
      configuration.set(HADOOP_SECURITY_AUTHENTICATION, KERBEROS);
      UserGroupInformation.setConfiguration(configuration);
      UserGroupInformation.loginUserFromKeytab(getString(LOGIN_USER_KEY_TAB_USERNAME),
              getString(cn.escheduler.common.Constants.LOGIN_USER_KEY_TAB_PATH));
    }
  }
}

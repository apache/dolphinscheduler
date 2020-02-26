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
package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;

/**
 * common utils
 */
public class CommonUtils {
  private CommonUtils() {
    throw new IllegalStateException("CommonUtils class");
  }

  /**
   * @return get the path of system environment variables
   */
  public static String getSystemEnvPath() {
    String envPath = PropertyUtils.getString(Constants.DOLPHINSCHEDULER_ENV_PATH);
    if (StringUtils.isEmpty(envPath)) {
      envPath = System.getProperty("user.home") + File.separator + ".bash_profile";
    }

    return envPath;
  }

  /**
   * @return get queue implementation name
   */
  public static String getQueueImplValue(){
    return PropertyUtils.getString(Constants.SCHEDULER_QUEUE_IMPL);
  }

  /**
   * 
   * @return is develop mode
   */
  public static boolean isDevelopMode() {
    return PropertyUtils.getBoolean(Constants.DEVELOPMENT_STATE);
  }



  /**
   * if upload resource is HDFS and kerberos startup is true , else false
   * @return true if upload resource is HDFS and kerberos startup
   */
  public static boolean getKerberosStartupState(){
    String resUploadStartupType = PropertyUtils.getString(Constants.RES_UPLOAD_STARTUP_TYPE);
    ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
    Boolean kerberosStartupState = PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE);
    return resUploadType == ResUploadType.HDFS && kerberosStartupState;
  }

  /**
   * load kerberos configuration
   * @throws Exception errors
   */
  public static void loadKerberosConf()throws Exception{
    if (CommonUtils.getKerberosStartupState())  {
      System.setProperty(Constants.JAVA_SECURITY_KRB5_CONF, PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH));
      Configuration configuration = new Configuration();
      configuration.set(Constants.HADOOP_SECURITY_AUTHENTICATION, Constants.KERBEROS);
      UserGroupInformation.setConfiguration(configuration);
      UserGroupInformation.loginUserFromKeytab(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
              PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH));
    }
  }
}

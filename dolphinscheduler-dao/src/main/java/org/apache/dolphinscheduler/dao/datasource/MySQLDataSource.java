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
package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * data source of mySQL
 */
public class MySQLDataSource extends BaseDataSource {

  private final Logger logger = LoggerFactory.getLogger(MySQLDataSource.class);

  private final String sensitiveParam = "autoDeserialize=true";

  private final char symbol = '&';

  /**
   * gets the JDBC url for the data source connection
   * @return jdbc url
   */
  @Override
  public String driverClassSelector() {
    return Constants.COM_MYSQL_JDBC_DRIVER;
  }

  /**
   * @return db type
   */
  @Override
  public DbType dbTypeSelector() {
    return DbType.MYSQL;
  }

  @Override
  protected String filterOther(String other){
    if(StringUtils.isBlank(other)){
        return "";
    }
    if(other.contains(sensitiveParam)){
      int index = other.indexOf(sensitiveParam);
      String tmp = sensitiveParam;
      if(index == 0 || other.charAt(index + 1) == symbol){
        tmp = tmp + symbol;
      } else if(other.charAt(index - 1) == symbol){
        tmp = symbol + tmp;
      }
      logger.warn("sensitive param : {} in otherParams field is filtered", tmp);
      other = other.replace(tmp, "");
    }
    logger.debug("other : {}", other);
    return other;
  }

  @Override
  public String getUser() {
    if(user.contains(sensitiveParam)){
      logger.warn("sensitive param : {} in username field is filtered", sensitiveParam);
      user = user.replace(sensitiveParam, "");
    }
    logger.debug("username : {}", user);
    return user;
  }

  @Override
  public String getPassword() {
    if(password.contains(sensitiveParam)){
      logger.warn("sensitive param : {} in password field is filtered", sensitiveParam);
      password = password.replace(sensitiveParam, "");
    }
    return password;
  }
}

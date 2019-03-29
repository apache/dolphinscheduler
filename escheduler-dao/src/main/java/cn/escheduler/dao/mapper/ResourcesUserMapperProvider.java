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
package cn.escheduler.dao.mapper;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * resource user mapper provider
 */
public class ResourcesUserMapperProvider {

  private static final String TABLE_NAME = "t_escheduler_relation_resources_user";


  /**
   * insert resource user
   *
   * @param parameter
   * @return
   */
  public String insert(Map<String, Object> parameter) {
    return new SQL() {{
      INSERT_INTO(TABLE_NAME);

      VALUES("`resources_id`", "#{resourcesUser.resourcesId}");
      VALUES("`user_id`", "#{resourcesUser.userId}");
      VALUES("`perm`", "#{resourcesUser.perm}");
      VALUES("`create_time`", "#{resourcesUser.createTime}");
      VALUES("`update_time`", "#{resourcesUser.updateTime}");
    }}.toString();
  }


  /**
   * delete resource relation by user id
   *
   * @param parameter
   * @return
   */
  public String deleteByUserId(Map<String, Object> parameter) {
    return new SQL() {{
      DELETE_FROM(TABLE_NAME);

      WHERE("`user_id` = #{userId}");
    }}.toString();
  }

  /**
   * delete resource relation by resource id
   *
   * @param parameter
   * @return
   */
  public String deleteByResourceId(Map<String, Object> parameter) {
    return new SQL() {{
      DELETE_FROM(TABLE_NAME);

      WHERE("`resources_id` = #{resourceId}");
    }}.toString();
  }
}

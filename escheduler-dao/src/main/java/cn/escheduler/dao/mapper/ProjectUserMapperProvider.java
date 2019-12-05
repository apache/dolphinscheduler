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
 *  project user mapper provider
 */
public class ProjectUserMapperProvider {

  private static final String TABLE_NAME = "t_escheduler_relation_project_user";
  private static final String USER_TABLE_NAME = "t_escheduler_user";
  private static final String PROJECT_TABLE_NAME = "t_escheduler_project";


  /**
   * insert project user relation
   *
   * @param parameter
   * @return
   */
  public String insert(Map<String, Object> parameter) {
    return new SQL() {{
      INSERT_INTO(TABLE_NAME);

      VALUES("`project_id`", "#{projectUser.projectId}");
      VALUES("`user_id`", "#{projectUser.userId}");
      VALUES("`perm`", "#{projectUser.perm}");
      VALUES("`create_time`", "#{projectUser.createTime}");
      VALUES("`update_time`", "#{projectUser.updateTime}");
    }}.toString();
  }

  /**
   * update project user relation
   *
   * @param parameter
   * @return
   */
  public String update(Map<String, Object> parameter) {
    return new SQL() {{
      UPDATE(TABLE_NAME);

      SET("`perm`=#{projectUser.perm}");
      SET("`update_time`=#{projectUser.updateTime}");

      WHERE("`project_id` = #{projectUser.projectId}");
      WHERE("`user_id` = #{projectUser.userId}");
    }}.toString();
  }

  /**
   * delete project user relation
   *
   * @param parameter
   * @return
   */
  public String delete(Map<String, Object> parameter) {
    return new SQL() {{
      DELETE_FROM(TABLE_NAME);

      WHERE("`project_id` = #{projectId}");
      WHERE("`user_id` = #{userId}");
    }}.toString();
  }

  /**
   * query project user relation by project id and user id
   *
   * @param parameter
   * @return
   */
  public String query(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("p_u.*");
      SELECT("u.user_name as user_name, p.name as project_name");

      FROM(TABLE_NAME + " p_u");

      JOIN(USER_TABLE_NAME + " u on p_u.user_id = u.id");
      JOIN(PROJECT_TABLE_NAME + " p on p_u.project_id = p.id");
      WHERE("p_u.project_id = #{projectId} ");
      WHERE("p_u.user_id = #{userId}");
    }}.toString();
  }

  /**
   * delete project relation by user id
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
}

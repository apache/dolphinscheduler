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
 * udf user mapper provider
 */
public class UDFUserMapperProvider {

  private static final String TABLE_NAME = "t_escheduler_relation_udfs_user";


  /**
   * insert udf user
   *
   * @param parameter
   * @return
   */
  public String insert(Map<String, Object> parameter) {
    return new SQL() {{
      INSERT_INTO(TABLE_NAME);

      VALUES("`udf_id`", "#{udfUser.udfId}");
      VALUES("`user_id`", "#{udfUser.userId}");
      VALUES("`perm`", "#{udfUser.perm}");
      VALUES("`create_time`", "#{udfUser.createTime}");
      VALUES("`update_time`", "#{udfUser.updateTime}");
    }}.toString();
  }


  /**
   * delete by user id
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
   * delete by udf function id
   *
   * @param parameter
   * @return
   */
  public String deleteByUdfFuncId(Map<String, Object> parameter) {
    return new SQL() {{
      DELETE_FROM(TABLE_NAME);

      WHERE("`udf_id` = #{udfFuncId}");
    }}.toString();
  }
}

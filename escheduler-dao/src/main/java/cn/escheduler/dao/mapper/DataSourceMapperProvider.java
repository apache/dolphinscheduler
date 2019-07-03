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

import cn.escheduler.common.enums.DbType;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.text.MessageFormat;
import java.util.Map;

/**
 * data source mapper provider
 */
public class DataSourceMapperProvider {

  public static final String TABLE_NAME = "t_escheduler_datasource";
  public static final String USER_TABLE_NAME = "t_escheduler_user";
  public static final String PROJECT_TABLE_NAME = "t_escheduler_project";
  public static final String USER_DATASOURCE_RELATION_TABLE_NAME = "t_escheduler_relation_datasource_user";

  /**
   * insert data source
   *
   * @param parameter
   * @return
   */
  public String insert(Map<String, Object> parameter) {
    return new SQL() {{
      INSERT_INTO(TABLE_NAME);

      VALUES("`name`", "#{dataSource.name}");
      VALUES("`note`", "#{dataSource.note}");
      VALUES("`type`", EnumFieldUtil.genFieldStr("dataSource.type", DbType.class));
      VALUES("`user_id`", "#{dataSource.userId}");
      VALUES("`connection_params`", "#{dataSource.connectionParams}");
      VALUES("`create_time`", "#{dataSource.createTime}");
      VALUES("`update_time`", "#{dataSource.updateTime}");
    }}.toString();
  }

  /**
   *
   * update data source
   * @param parameter
   * @return
   */
  public String update(Map<String, Object> parameter) {
    return new SQL() {{
      UPDATE(TABLE_NAME);

      SET("`name` = #{dataSource.name}");
      SET("`note` = #{dataSource.note}");
      SET("`user_id` = #{dataSource.userId}");
      SET("`type` = "+ EnumFieldUtil.genFieldStr("dataSource.type", DbType.class));
      SET("`connection_params` = #{dataSource.connectionParams}");
      SET("`update_time` = #{dataSource.updateTime}");

      WHERE("`id` = #{dataSource.id}");
    }}.toString();
  }

  /**
   * delete datasource by id
   * @param parameter
   * @return
   */
  public String deleteDataSourceById(Map<String, Object> parameter) {
    return new SQL() {{
      DELETE_FROM(TABLE_NAME);

      WHERE("`id` = #{id}");
    }}.toString();
  }


  /**
   * query datasource list by type
   * @param parameter
   * @return
   */
  public String queryDataSourceByType(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME );
      WHERE("type = #{type}");
      WHERE("id in (select datasource_id from "+USER_DATASOURCE_RELATION_TABLE_NAME+" where user_id=#{userId} union select id as datasource_id  from "+TABLE_NAME+" where user_id=#{userId})");
    }}.toString();
  }

  /**
   * query data source by id
   *
   * @param parameter
   * @return
   */
  public String queryById(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("r.*,u.user_name");

      FROM(TABLE_NAME + " r");

      JOIN(new MessageFormat("{0} as u on u.id = r.user_id").format(new Object[]{USER_TABLE_NAME}));

      WHERE("r.id = #{id}");
    }}.toString();
  }

  /**
   * query data source paging
   * @param parameter
   * @return
   */
  public String queryDataSourcePaging(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME );
      WHERE("id in (select datasource_id from "+USER_DATASOURCE_RELATION_TABLE_NAME+" where user_id=#{userId} union select id as datasource_id  from "+TABLE_NAME+" where user_id=#{userId})");
      Object searchVal = parameter.get("searchVal");
      if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
        WHERE( " name like concat('%', #{searchVal}, '%') ");
      }
      ORDER_BY("update_time desc limit #{offset},#{pageSize} ");

    }}.toString();
  }

  /**
   *
   * query data source list paging
   * @param parameter
   * @return
   */
  public String queryAllDataSourcePaging(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME );
      Object searchVal = parameter.get("searchVal");
      if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
        WHERE( " name like concat('%', #{searchVal}, '%') ");
      }
      ORDER_BY("update_time desc limit #{offset},#{pageSize} ");

    }}.toString();
  }

  /**
   * count data source by user id
   *
   * @param parameter
   * @return
   */
  public String countUserDatasource(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("count(0)");

      FROM(TABLE_NAME);
      WHERE("id in (select datasource_id from "+USER_DATASOURCE_RELATION_TABLE_NAME+" where user_id=#{userId} union select id as datasource_id  from "+TABLE_NAME+" where user_id=#{userId})");
    }}.toString();
  }

  /**
   * Query the total number of data sources
   * @param parameter
   * @return
   */
  public String countAllDatasource(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("count(0)");
      FROM(TABLE_NAME);
    }}.toString();
  }

  /**
   * query data source by name
   * @param parameter
   * @return
   */
  public String queryDataSourceByName(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME );
      WHERE("name = #{name}");
    }}.toString();
  }

  /**
   * authed data source to user
   *
   * @param parameter
   * @return
   */
  public String authedDatasource(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("d.*");
      FROM(TABLE_NAME + " d,t_escheduler_relation_datasource_user rel");
      WHERE(" d.id = rel.datasource_id AND rel.user_id = #{userId}");
    }}.toString();
  }


  /**
   * query data source except user
   *
   * @param parameter
   * @return
   */
  public String queryDatasourceExceptUserId(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME);
      WHERE("user_id <> #{userId}");
    }}.toString();
  }


  /**
   * list all data source by type
   *
   * @param parameter
   * @return
   */
  public String listAllDataSourceByType(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME);
      WHERE("type = #{type}");
    }}.toString();
  }

}

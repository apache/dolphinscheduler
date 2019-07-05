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

import cn.escheduler.common.enums.ResourceType;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * resource mapper provider
 */
public class ResourceMapperProvider {

  private final String TABLE_NAME = "t_escheduler_resources";

  public static final String USER_TABLE_NAME = "t_escheduler_user";
  public static final String USER_RESOURCE_RELATION_TABLE_NAME = "t_escheduler_relation_resources_user";
  public static final String PROJECT_TABLE_NAME = "t_escheduler_project";
  public static final String TENANT_TABLE_NAME="t_escheduler_tenant";

  /**
   * insert resource
   * @param parameter
   * @return
   */
  public String insert(Map<String, Object> parameter) {
    return new SQL() {
      {
        INSERT_INTO(TABLE_NAME);

        VALUES("`alias`", "#{resource.alias}");
        VALUES("`file_name`", "#{resource.fileName}");
        VALUES("`desc`", "#{resource.desc}");
        VALUES("`user_id`", "#{resource.userId}");
        VALUES("`create_time`", "#{resource.createTime}");
        VALUES("`update_time`", "#{resource.updateTime}");
        VALUES("`type`", EnumFieldUtil.genFieldStr("resource.type", ResourceType.class));
        VALUES("`size`", "#{resource.size}");
      }
    }.toString();
  }

  /**
   * query resource by name
   *
   * @param parameter
   * @return
   */
  public String queryResource(Map<String, Object> parameter) {
    return new SQL() {
      {
        SELECT("*");
        FROM(TABLE_NAME);
        WHERE("alias = #{alias}");
      }
    }.toString();
  }

  /**
   * query resource by name and resource type
   * @param parameter
   * @return
   */
  public String queryResourceByNameAndType(Map<String, Object> parameter) {
    return new SQL() {
      {
        SELECT("*");
        FROM(TABLE_NAME);
        WHERE("alias = #{alias}");
        WHERE("type = #{type}");
      }
    }.toString();
  }

  /**
   * query resource by id
   *
   * @param parameter
   * @return
   */
  public String queryResourceById(Map<String, Object> parameter) {
    return new SQL() {
      {
        SELECT("*");
        FROM(TABLE_NAME);
        WHERE("id = #{id}");
      }
    }.toString();
  }

  /**
   * update resource
   *
   * @param parameter
   * @return
   */
  public String update(Map<String, Object> parameter) {
    return new SQL() {{
      UPDATE(TABLE_NAME);

      SET("`alias` = #{resource.alias}");
      SET("`desc` = #{resource.desc}");
      SET("`update_time` = #{resource.updateTime}");
      SET("`size` = #{resource.size}");
      WHERE("`id` = #{resource.id}");
    }}.toString();
  }

  /**
   * delete resource by id
   *
   * @param parameter
   * @return
   */
  public String delete(Map<String, Object> parameter) {
    return new SQL() {
      {
        DELETE_FROM(TABLE_NAME);

        WHERE("`id` = #{resourceId}");
      }
    }.toString();
  }

  /**
   * query resource list by user id
   *
   * @param parameter
   * @return
   */
  public String queryResourceListAuthored(Map<String, Object> parameter) {
    return new SQL() {{

      SELECT("*");

      FROM(TABLE_NAME);
      WHERE("type=#{type}");
      WHERE("id in (select resources_id from "+USER_RESOURCE_RELATION_TABLE_NAME+" where user_id=#{userId} union select id as resources_id  from "+TABLE_NAME+" where user_id=#{userId})");

    }}.toString();
  }

  /**
   *  query resource list paging by user id
   * @param parameter
   * @return
   */
  public String queryResourceAuthoredPaging(Map<String, Object> parameter) {

    return new SQL() {{
      SELECT("*");

      FROM(TABLE_NAME );
      WHERE("type=#{type}");
      WHERE("id in (select resources_id from "+USER_RESOURCE_RELATION_TABLE_NAME+" where user_id=#{userId} union select id as resources_id  from "+TABLE_NAME+" where user_id=#{userId})");
      Object searchVal = parameter.get("searchVal");
      if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
        WHERE( " alias like concat('%', #{searchVal}, '%') ");
      }
      ORDER_BY("update_time desc limit #{offset},#{pageSize} ");
    }}.toString();
  }

  /**
   *
   *  query all resource list paging
   * @param parameter
   * @return
   */
  public String queryAllResourceListPaging(Map<String, Object> parameter) {

    return new SQL() {{
      SELECT("*");

      FROM(TABLE_NAME);
      WHERE("type=#{type}");
      Object searchVal = parameter.get("searchVal");
      if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
        WHERE( " alias like concat('%', #{searchVal}, '%') ");
      }
      ORDER_BY("update_time desc limit #{offset},#{pageSize} ");
    }}.toString();
  }

  /**
   * count resource number by user id
   * @param parameter
   * @return
   */
  public String countResourceNumber(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("count(0)");

      FROM("(select resources_id from t_escheduler_relation_resources_user where user_id=#{userId} union select id as resources_id  from t_escheduler_resources where user_id=#{userId}) t");

    }}.toString();
  }

  /**
   * count resource number by user id and type
   * @param parameter
   * @return
   */
  public String countResourceNumberByType(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("count(0)");
      FROM(TABLE_NAME );
      WHERE("type=#{type}");
      WHERE("id in (select resources_id from " + USER_RESOURCE_RELATION_TABLE_NAME + " where user_id=#{userId} union select id as resources_id  from " + TABLE_NAME + " where user_id=#{userId})");

    }}.toString();
  }

  /**
   * count resource number by type
   * @param parameter
   * @return
   */
  public String countAllResourceNumberByType(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("count(0)");
      FROM(TABLE_NAME);
      WHERE("type=#{type}");
    }}.toString();
  }



  /**
   * query resource list authorized appointed user
   * @param parameter
   * @return
   */
  public String queryAuthorizedResourceList(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("r.*");
      FROM(TABLE_NAME + " r,t_escheduler_relation_resources_user rel");
      WHERE(" r.id = rel.resources_id AND rel.user_id = #{userId}");
    }}.toString();
  }


  /**
   * query all resource list  except user
   * @param parameter
   * @return
   */
  public String queryResourceExceptUserId(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME);
      WHERE("user_id <> #{userId}");
    }}.toString();
  }

  /**
   * query tenant code by resource name
   * @param parameter
   * @return
   */
  public String queryTenantCodeByResourceName(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("tenant_code");
      FROM(TENANT_TABLE_NAME + " t," + USER_TABLE_NAME + " u," + TABLE_NAME + " res");
      WHERE(" t.id = u.tenant_id and u.id = res.user_id and res.type=0 and res.alias= #{resName}");
    }}.toString();
  }

  /**
   * query resource list that created by the appointed user
   * @param parameter
   * @return
   */
  public String queryResourceCreatedByUser(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME);
      WHERE("type = #{type} and user_id = #{userId}");
    }}.toString();
  }

  /**
   * list all resource by type
   *
   * @param parameter
   * @return
   */
  public String listAllResourceByType(Map<String, Object> parameter) {
    return new SQL() {{
      SELECT("*");
      FROM(TABLE_NAME);
      WHERE("type = #{type}");
    }}.toString();
  }

}

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

import cn.escheduler.dao.model.ProjectUser;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

/**
 * project user relation mapper
 */
public interface ProjectUserMapper {

  /**
   * insert project user relation
   *
   * @param projectUser
   * @return
   */
  @InsertProvider(type = ProjectUserMapperProvider.class, method = "insert")
  int insert(@Param("projectUser") ProjectUser projectUser);

  /**
   * delete project user relation
   * @param projectId
   * @param userId
   * @return
   */
  @DeleteProvider(type = ProjectUserMapperProvider.class, method = "delete")
  int delete(@Param("projectId") int projectId, @Param("userId") int userId);

  /**
   * update project user relation
   *
   * @param projectUser
   * @return
   */
  @UpdateProvider(type = ProjectUserMapperProvider.class, method = "update")
  int update(@Param("projectUser") ProjectUser projectUser);

  /**
   * query project user relation by project id and user id
   *
   * @param projectId
   * @param userId
   * @return
   */
  @Results(value = {@Result(property = "projectId", column = "project_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "perm", column = "perm", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
          @Result(property = "updateTime", column = "update_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)})
  @SelectProvider(type = ProjectUserMapperProvider.class, method = "query")
  ProjectUser query(@Param("projectId") int projectId, @Param("userId") int userId);


  /**
     * delete project relation by user id
     * @param userId
     * @return
     */
    @DeleteProvider(type = ProjectUserMapperProvider.class, method = "deleteByUserId")
    int deleteByUserId(@Param("userId") int userId);

}

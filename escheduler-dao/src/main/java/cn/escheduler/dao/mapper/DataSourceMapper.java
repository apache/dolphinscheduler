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
import cn.escheduler.dao.model.DataSource;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

/**
 * data source mapper
 */
public interface DataSourceMapper {
  /**
   * insert data source
   *
   * @param dataSource
   * @return
   */
  @InsertProvider(type = DataSourceMapperProvider.class, method = "insert")
  @SelectKey(statement = "SELECT LAST_INSERT_ID() as id", resultType = Integer.class, keyProperty = "dataSource.id", before = false)
  int insert(@Param("dataSource") DataSource dataSource);

  /**
   * update data source
   *
   * @param dataSource
   * @return
   */
  @UpdateProvider(type = DataSourceMapperProvider.class, method = "update")
  int update(@Param("dataSource") DataSource dataSource);

  /**
   * delete data source by id
   *
   * @param id
   * @return
   */
  @DeleteProvider(type = DataSourceMapperProvider.class, method = "deleteDataSourceById")
  int deleteDataSourceById(@Param("id") int id);

  /**
   * query data source list by type
   * @param userId
   * @param type
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "note", column = "note", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = DbType.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "connectionParams", column = "connection_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = DataSourceMapperProvider.class, method = "queryDataSourceByType")
  List<DataSource> queryDataSourceByType(@Param("userId") int userId, @Param("type") Integer type);

  /**
   * query data source by id
   *
   * @param id
   * @return
   */
  @Results(value = {
      @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "note", column = "note", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = DbType.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "projectId", column = "project_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "projectName", column = "project_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "connectionParams", column = "connection_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
      @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = DataSourceMapperProvider.class, method = "queryById")
  DataSource queryById(@Param("id") Integer id);

  /**
   * count data source by user id
   *
   * @param userId
   * @return
   */
  @SelectProvider(type = DataSourceMapperProvider.class, method = "countUserDatasource")
  int countUserDatasource(@Param("userId") int userId);

  /**
   * count data source number
   *
   * @return
   */
  @SelectProvider(type = DataSourceMapperProvider.class, method = "countAllDatasource")
  int countAllDatasource();

  /**
   * query data source list paging
   *
   * @param userId
   * @param searchVal
   * @param offset
   * @param pageSize
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "note", column = "note", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = DbType.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "connectionParams", column = "connection_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = DataSourceMapperProvider.class, method = "queryDataSourcePaging")
  List<DataSource> queryDataSourcePaging(@Param("userId") int userId, @Param("searchVal") String searchVal,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);

  /**
   * query data source list paging
   *
   * @param searchVal
   * @param offset
   * @param pageSize
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "note", column = "note", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = DbType.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "connectionParams", column = "connection_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = DataSourceMapperProvider.class, method = "queryAllDataSourcePaging")
  List<DataSource> queryAllDataSourcePaging(@Param("searchVal") String searchVal,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);

  /**
   * query data source by name
   * @param name
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "note", column = "note", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = DbType.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "connectionParams", column = "connection_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = DataSourceMapperProvider.class, method = "queryDataSourceByName")
  List<DataSource> queryDataSourceByName(@Param("name") String name);

  /**
   * authed data source to user
   * @param userId
   * @return
   */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "note", column = "note", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = DbType.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "connectionParams", column = "connection_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = DataSourceMapperProvider.class, method = "authedDatasource")
    List<DataSource> authedDatasource(@Param("userId") int userId);


    /**
     * query data source except user
     * @param userId
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "note", column = "note", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = DbType.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "connectionParams", column = "connection_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = DataSourceMapperProvider.class, method = "queryDatasourceExceptUserId")
    List<DataSource> queryDatasourceExceptUserId(@Param("userId") int userId);

    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "name", column = "name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "note", column = "note", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = DbType.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "connectionParams", column = "connection_params", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = DataSourceMapperProvider.class, method = "listAllDataSourceByType")
    List<DataSource> listAllDataSourceByType(@Param("type") Integer type);

}

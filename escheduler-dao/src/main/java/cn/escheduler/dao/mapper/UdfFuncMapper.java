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

import cn.escheduler.common.enums.UdfType;
import cn.escheduler.dao.model.UdfFunc;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

/**
 * und function mapper
 */
public interface UdfFuncMapper {

  /**
   * insert udf function
   *
   * @param udf
   * @return
   */
  @InsertProvider(type = UdfFuncMapperProvider.class, method = "insert")
  @SelectKey(statement = "SELECT LAST_INSERT_ID() AS id", keyProperty = "udf.id", resultType = int.class, before = false)
  int insert(@Param("udf") UdfFunc udf);

  /**
   * update udf function
   *
   * @param udf
   * @return
   */
  @UpdateProvider(type = UdfFuncMapperProvider.class, method = "update")
  int update(@Param("udf") UdfFunc udf);

  /**
   * query udf function by id
   *
   * @param id
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "funcName", column = "func_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "className", column = "class_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "argTypes", column = "arg_types", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "database", column = "database", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "resourceId", column = "resource_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "resourceName", column = "resource_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UdfType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = UdfFuncMapperProvider.class, method = "queryUdfById")
  UdfFunc queryUdfById(@Param("id") int id);

  /**
   * query udf list by id string
   *
   * @param ids
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "funcName", column = "func_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "className", column = "class_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "argTypes", column = "arg_types", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "database", column = "database", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "resourceId", column = "resource_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "resourceName", column = "resource_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UdfType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = UdfFuncMapperProvider.class, method = "queryUdfByIdStr")
  List<UdfFunc> queryUdfByIdStr(@Param("ids") String ids);

  /**
   * count udf number by user id
   *
   * @param userId
   * @return
   */
  @SelectProvider(type = UdfFuncMapperProvider.class, method = "countUserUdfFunc")
  int countUserUdfFunc(@Param("userId") int userId);

  /**
   * count udf number
   *
   * @return
   */
  @SelectProvider(type = UdfFuncMapperProvider.class, method = "countAllUdfFunc")
  int countAllUdfFunc();

  /**
   * query udf function paging
   * @param userId
   * @param searchVal
   * @param offset
   * @param pageSize
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "funcName", column = "func_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "className", column = "class_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "argTypes", column = "arg_types", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "database", column = "database", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "resourceId", column = "resource_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "resourceName", column = "resource_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UdfType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = UdfFuncMapperProvider.class, method = "queryUdfFuncPaging")
  List<UdfFunc> queryUdfFuncPaging(@Param("userId") int userId, @Param("searchVal") String searchVal,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);

  /**
   * query all udf function paging
   * @param searchVal
   * @param offset
   * @param pageSize
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "funcName", column = "func_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "className", column = "class_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "argTypes", column = "arg_types", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "database", column = "database", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "resourceId", column = "resource_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "resourceName", column = "resource_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UdfType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = UdfFuncMapperProvider.class, method = "queryAllUdfFuncPaging")
  List<UdfFunc> queryAllUdfFuncPaging(@Param("searchVal") String searchVal,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);


  /**
   * query udf function by type
   * @param userId
   * @param type
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "funcName", column = "func_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "className", column = "class_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "argTypes", column = "arg_types", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "database", column = "database", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "resourceId", column = "resource_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "resourceName", column = "resource_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UdfType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = UdfFuncMapperProvider.class, method = "getUdfFuncByType")
  List<UdfFunc> getUdfFuncByType(@Param("userId") int userId,@Param("type") Integer type);


  /**
   * query udf function by name
   * @param funcName
   * @return
   */
  @Results(value = {
          @Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "funcName", column = "func_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "className", column = "class_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "argTypes", column = "arg_types", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "database", column = "database", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "resourceId", column = "resource_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "resourceName", column = "resource_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UdfType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = UdfFuncMapperProvider.class, method = "queryUdfFuncByName")
  UdfFunc queryUdfFuncByName(@Param("func_name") String funcName);

  /**
   * delete udf function
   *
   * @param udfFuncId
   * @return
   */
  @DeleteProvider(type = UdfFuncMapperProvider.class, method = "delete")
  int delete(@Param("udfFuncId") int udfFuncId);



    /**
     * query udf function except user
     * @param userId
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "funcName", column = "func_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "className", column = "class_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "argTypes", column = "arg_types", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "database", column = "database", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "resourceId", column = "resource_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "resourceName", column = "resource_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UdfType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UdfFuncMapperProvider.class, method = "queryUdfFuncExceptUserId")
    List<UdfFunc> queryUdfFuncExceptUserId(@Param("userId") int userId);



    /**
     * query udf function authorized to user
     * @param userId
     * @return
     */
    @Results(value = {
            @Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "funcName", column = "func_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "className", column = "class_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "argTypes", column = "arg_types", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "database", column = "database", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "resourceId", column = "resource_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "resourceName", column = "resource_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = UdfType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = UdfFuncMapperProvider.class, method = "authedUdfFunc")
    List<UdfFunc> authedUdfFunc(@Param("userId") int userId);


}

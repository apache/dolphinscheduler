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
import cn.escheduler.dao.model.Resource;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

/**
 * resource mapper
 */
public interface ResourceMapper {

  /**
   * insert resource
   *
   * @param resource
   * @return
   */
  @InsertProvider(type = ResourceMapperProvider.class, method = "insert")
  @SelectKey(statement = "SELECT LAST_INSERT_ID() AS id", keyProperty = "resource.id", resultType = int.class, before = false)
  int insert(@Param("resource") Resource resource);

  /**
   * query resource by alias
   *
   * @param alias
   * @return
   */
  @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
      @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
      @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = ResourceMapperProvider.class, method = "queryResource")
  Resource queryResource(@Param("alias") String alias);

  /**
   * query resource by name and resource type
   * @param alias
   * @param type
   * @return
   */
  @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = ResourceMapperProvider.class, method = "queryResourceByNameAndType")
  Resource queryResourceByNameAndType(@Param("alias") String alias,@Param("type") int type);

  /**
   * query resource by id
   *
   * @param id
   * @return
   */
  @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = ResourceMapperProvider.class, method = "queryResourceById")
  Resource queryResourceById(@Param("id") int id);

  /**
   * update resource
   *
   * @param resource
   * @return
   */
  @UpdateProvider(type = ResourceMapperProvider.class, method = "update")
  int update(@Param("resource") Resource resource);

  /**
   * delete resource
   *
   * @param resourceId
   * @return
   */
  @DeleteProvider(type = ResourceMapperProvider.class, method = "delete")
  int delete(@Param("resourceId") int resourceId);

  /**
   * query resource list that the appointed user has permission
   * @param userId
   * @param type
   * @return
   */
  @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
      @Result(property = "size", column = "size", javaType = Long.class, jdbcType = JdbcType.BIGINT),
      @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
      @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = ResourceMapperProvider.class, method = "queryResourceListAuthored")
  List<Resource> queryResourceListAuthored(@Param("userId") int userId, @Param("type") int type);

  /**
   *  query resource list paging by user id
   * @param userId
   * @param type
   * @param searchVal
   * @param offset
   * @param pageSize
   * @return
   */
  @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
      @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
      @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
      @Result(property = "size", column = "size", javaType = Long.class, jdbcType = JdbcType.BIGINT),
      @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
      @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = ResourceMapperProvider.class, method = "queryResourceAuthoredPaging")
  List<Resource> queryResourceAuthoredPaging(@Param("userId") int userId, @Param("type") int type,
                                             @Param("searchVal") String searchVal,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

  /**
   * query all resource list paging
   * @param type
   * @param searchVal
   * @param offset
   * @param pageSize
   * @return
   */
  @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "size", column = "size", javaType = Long.class, jdbcType = JdbcType.BIGINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = ResourceMapperProvider.class, method = "queryAllResourceListPaging")
  List<Resource> queryAllResourceListPaging(@Param("type") int type,
                                            @Param("searchVal") String searchVal,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

  /**
   * count resource number by user id
   *
   * @param userId
   * @return
   */
  @SelectProvider(type = ResourceMapperProvider.class, method = "countResourceNumber")
  int countResourceNumber(@Param("userId") int userId);

  /**
   * count resource number by user id and type
   *
   * @param userId
   * @param type
   * @return
   */
  @SelectProvider(type = ResourceMapperProvider.class, method = "countResourceNumberByType")
  int countResourceNumberByType(@Param("userId") int userId,@Param("type") int type);

  /**
   * count resource number by type
   *
   * @param type
   * @return
   */
  @SelectProvider(type = ResourceMapperProvider.class, method = "countAllResourceNumberByType")
  int countAllResourceNumberByType(@Param("type") int type);

    /**
     * query resource list authorized appointed user
     * @param userId
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "size", column = "size", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = ResourceMapperProvider.class, method = "queryAuthorizedResourceList")
    List<Resource> queryAuthorizedResourceList(@Param("userId") int userId);

  /**
   *
   * query all resource list except user
   * @param userId
   * @return
   */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "size", column = "size", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = ResourceMapperProvider.class, method = "queryResourceExceptUserId")
    List<Resource> queryResourceExceptUserId(@Param("userId") int userId);

  /**
   * query resource list that created by the appointed user
   * @param userId
   * @param type
   * @return
   */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "size", column = "size", javaType = Long.class, jdbcType = JdbcType.BIGINT),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = ResourceMapperProvider.class, method = "queryResourceCreatedByUser")
    List<Resource> queryResourceCreatedByUser(@Param("userId") int userId, @Param("type") int type);


    /**
     * query tenant code by resource name
     *
     * @param resName
     * @return
     */
    @SelectProvider(type = ResourceMapperProvider.class, method = "queryTenantCodeByResourceName")
    String queryTenantCodeByResourceName(@Param("resName") String  resName);

  /**
   * query resource list that the appointed user has permission
   * @param type
   * @return
   */
  @Results(value = {@Result(property = "id", column = "id", id = true, javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "alias", column = "alias", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "fileName", column = "file_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "desc", column = "desc", javaType = String.class, jdbcType = JdbcType.VARCHAR),
          @Result(property = "userId", column = "user_id", javaType = int.class, jdbcType = JdbcType.INTEGER),
          @Result(property = "type", column = "type", typeHandler = EnumOrdinalTypeHandler.class, javaType = ResourceType.class, jdbcType = JdbcType.TINYINT),
          @Result(property = "size", column = "size", javaType = Long.class, jdbcType = JdbcType.BIGINT),
          @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
          @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
  })
  @SelectProvider(type = ResourceMapperProvider.class, method = "listAllResourceByType")
  List<Resource> listAllResourceByType(@Param("type") Integer type);
}

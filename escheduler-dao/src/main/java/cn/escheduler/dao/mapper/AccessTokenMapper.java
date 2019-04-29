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

import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.model.AccessToken;
import cn.escheduler.dao.model.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Timestamp;
import java.util.List;

public interface AccessTokenMapper {

    /**
     * insert accessToken
     * @param accessToken
     * @return
     */
    @InsertProvider(type = AccessTokenMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "accessToken.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "accessToken.id", before = false, resultType = int.class)
    int insert(@Param("accessToken") AccessToken accessToken);


    /**
     * delete accessToken
     * @param accessTokenId
     * @return
     */
    @DeleteProvider(type = AccessTokenMapperProvider.class, method = "delete")
    int delete(@Param("accessTokenId") int accessTokenId);


    /**
     * update accessToken
     *
     * @param accessToken
     * @return
     */
    @UpdateProvider(type = AccessTokenMapperProvider.class, method = "update")
    int update(@Param("accessToken") AccessToken accessToken);


    /**
     * query access token list paging
     * @param searchVal
     * @param offset
     * @param pageSize
     * @return
     */
    @Results(value = {@Result(property = "id", column = "id", id = true, javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "userId", column = "user_id", javaType = Integer.class, jdbcType = JdbcType.INTEGER),
            @Result(property = "token", column = "token", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "userName", column = "user_name", javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "expireTime", column = "expire_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "createTime", column = "create_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE),
            @Result(property = "updateTime", column = "update_time", javaType = Timestamp.class, jdbcType = JdbcType.DATE)
    })
    @SelectProvider(type = AccessTokenMapperProvider.class, method = "queryAccessTokenPaging")
    List<AccessToken> queryAccessTokenPaging(@Param("userId") Integer userId,
                                             @Param("searchVal") String searchVal,
                                             @Param("offset") Integer offset,
                                             @Param("pageSize") Integer pageSize);

    /**
     * count access token by search value
     * @param searchVal
     * @return
     */
    @SelectProvider(type = AccessTokenMapperProvider.class, method = "countAccessTokenPaging")
    Integer countAccessTokenPaging(@Param("userId") Integer userId
                            ,@Param("searchVal") String searchVal);
}

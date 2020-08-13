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
package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * udf function mapper interface
 */
public interface UdfFuncMapper extends BaseMapper<UdfFunc> {

    /**
     * select udf by id
     * @param id udf id
     * @return UdfFunc
     */
    UdfFunc selectUdfById(@Param("id") int id);

    /**
     * query udf function by ids and function name
     * @param ids ids
     * @param funcNames funcNames
     * @return udf function list
     */
    List<UdfFunc> queryUdfByIdStr(@Param("ids") int[] ids,
                                  @Param("funcNames") String funcNames);

    /**
     * udf function page
     * @param page page
     * @param userId userId
     * @param searchVal searchVal
     * @return udf function IPage
     */
    IPage<UdfFunc> queryUdfFuncPaging(IPage<UdfFunc> page,
                                      @Param("userId") int userId,
                                      @Param("searchVal") String searchVal);

    /**
     * query udf function by type
     * @param userId userId
     * @param type type
     * @return udf function list
     */
    List<UdfFunc> getUdfFuncByType(@Param("userId") int userId,
                                   @Param("type") Integer type);

    /**
     * query udf function except userId
     * @param userId userId
     * @return udf function list
     */
    List<UdfFunc> queryUdfFuncExceptUserId(@Param("userId") int userId);

    /**
     *  query authed udf function
     * @param userId userId
     * @return udf function list
     */
    List<UdfFunc> queryAuthedUdfFunc(@Param("userId") int userId);

    /**
     * list authorized UDF function
     * @param userId userId
     * @param udfIds UDF function id array
     * @return UDF function list
     */
    <T> List<UdfFunc> listAuthorizedUdfFunc (@Param("userId") int userId,@Param("udfIds")T[] udfIds);

    /**
     * list UDF by resource id
     * @param   resourceIds  resource id array
     * @return  UDF function list
     */
    List<UdfFunc> listUdfByResourceId(@Param("resourceIds") Integer[] resourceIds);

    /**
     * list authorized UDF by resource id
     * @param   resourceIds  resource id array
     * @return  UDF function list
     */
    List<UdfFunc> listAuthorizedUdfByResourceId(@Param("userId") int userId,@Param("resourceIds") int[] resourceIds);

    /**
     * batch update udf func
     * @param udfFuncList  udf list
     * @return update num
     */
    int batchUpdateUdfFunc(@Param("udfFuncList") List<UdfFunc> udfFuncList);


}

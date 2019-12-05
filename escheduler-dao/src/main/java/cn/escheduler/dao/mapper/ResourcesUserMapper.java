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

import cn.escheduler.dao.model.ResourcesUser;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;

/**
 * resource user mapper
 */
public interface ResourcesUserMapper {
    /**
     * insert resource user
     *
     * @param resourcesUser
     * @return
     */
     @InsertProvider(type = ResourcesUserMapperProvider.class, method = "insert")
     int insert(@Param("resourcesUser") ResourcesUser resourcesUser);


    /**
     * delete resource relation by user id
     * @param userId
     * @return
     */
    @DeleteProvider(type = ResourcesUserMapperProvider.class, method = "deleteByUserId")
    int deleteByUserId(@Param("userId") int userId);

    /**
     * delete resource relation by resource id
     * @param resourceId
     * @return
     */
    @DeleteProvider(type = ResourcesUserMapperProvider.class, method = "deleteByResourceId")
    int deleteByResourceId(@Param("resourceId") int resourceId);

}

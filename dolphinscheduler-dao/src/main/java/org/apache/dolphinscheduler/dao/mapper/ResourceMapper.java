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

import org.apache.dolphinscheduler.dao.entity.Resource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ResourceMapper extends BaseMapper<Resource> {

    /**
     *
     * @param alias query all if null
     * @param userId query all if -1
     * @param type query all type if -1
     * @return
     */
    List<Resource> queryResourceList(@Param("alias") String alias,
                                     @Param("userId") int userId,
                                     @Param("type") int type);


    /**
     *
     * @param page
     * @param userId query all if 0, then query the authed resources
     * @param type
     * @param searchVal
     * @return
     */
    IPage<Resource> queryResourcePaging(IPage<Resource> page,
                                        @Param("userId") int userId,
                                        @Param("type") int type,
                                        @Param("searchVal") String searchVal);

    /**
     *
     * @param userId
     * @param type query all if -1
     * @return
     */
    List<Resource> queryResourceListAuthored(@Param("userId") int userId, @Param("type") int type);

    /**
     *
     * @param userId
     * @return
     */
    List<Resource> queryAuthorizedResourceList(@Param("userId") int userId);

    List<Resource> queryResourceExceptUserId(@Param("userId") int userId);


    String queryTenantCodeByResourceName(@Param("resName") String resName);
}

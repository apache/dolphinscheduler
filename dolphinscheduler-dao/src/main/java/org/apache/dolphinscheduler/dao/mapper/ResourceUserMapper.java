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

import org.apache.dolphinscheduler.dao.entity.ResourcesUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * resource user relation mapper interface
 */
public interface ResourceUserMapper extends BaseMapper<ResourcesUser> {

    /**
     * delete resource user relation
     * @param userId userId
     * @param resourceId resourceId
     * @return delete result
     */
    int deleteResourceUser(@Param("userId") int userId,
                           @Param("resourceId") int resourceId);

    /**
     * delete resource user relation
     * @param userId userId
     * @param resIds resource Ids
     * @return delete result
     */
    int deleteResourceUserArray(@Param("userId") int userId,
                           @Param("resIds") Integer[] resIds);

}

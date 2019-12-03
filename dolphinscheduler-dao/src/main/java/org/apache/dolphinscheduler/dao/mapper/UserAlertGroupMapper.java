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

import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserAlertGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * user alertgroup relation mapper interface
 */
public interface UserAlertGroupMapper extends BaseMapper<UserAlertGroup> {

    /**
     * delete user alertgroup relation by alertgroupId
     * @param alertgroupId alertgroupId
     * @return delete result
     */
    int deleteByAlertgroupId(@Param("alertgroupId") int alertgroupId);

    /**
     *  list user by alertgroupId
     * @param alertgroupId alertgroupId
     * @return user list
     */
    List<User> listUserByAlertgroupId(@Param("alertgroupId") int alertgroupId);

}

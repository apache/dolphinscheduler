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

package org.apache.dolphinscheduler.service.cache.processor.impl;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.cache.processor.UserCacheProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheNames = "user")
public class UserCacheProcessorImpl implements UserCacheProcessor {

    @Autowired
    private UserMapper userMapper;

    @Override
    @CacheEvict
    public void update(int userId) {
        // just evict cache
    }

    @Override
    @Cacheable(sync = true)
    public User selectById(int userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void cacheExpire(Class updateObjClass, String updateObjJson) {
        User user = (User) JSONUtils.parseObject(updateObjJson, updateObjClass);
        if (user == null) {
            return;
        }
        SpringApplicationContext.getBean(UserCacheProcessor.class).update(user.getId());
    }
}

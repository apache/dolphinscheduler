package org.apache.dolphinscheduler.service.cache.impl;

import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.cache.UserCacheProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheResolver = "cacheResolver", cacheNames = "user")
public class UserCacheProxyImpl implements UserCacheProxy {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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
        logger.info("userCacheProxy queryById:{}", userId);
        return userMapper.selectById(userId);
    }

    @Override
    public void cacheExpire(Object updateObj) {
        User user = (User) updateObj;
        SpringApplicationContext.getBean(UserCacheProxy.class).update(user.getId());
    }
}

package org.apache.dolphinscheduler.service.cache;

import org.apache.dolphinscheduler.dao.entity.User;

public interface UserCacheProxy extends BaseCacheProxy{
    void update(int userId);

    User selectById(int userId);
}

package org.apache.dolphinscheduler.service.cache.service;

import org.apache.dolphinscheduler.remote.command.Command;

public interface CacheNotifyService {
    void notifyMaster(Command command);
}

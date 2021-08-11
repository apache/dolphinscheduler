package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import org.apache.curator.utils.ZookeeperFactory;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ZKClientConfig;

public class DefaultZookeeperFactory implements ZookeeperFactory {
    @Override
    public ZooKeeper newZooKeeper(String s, int i, Watcher watcher, boolean b) throws Exception {
        ZKClientConfig config = new ZKClientConfig();
        config.setProperty(ZKClientConfig.ENABLE_CLIENT_SASL_KEY,"false");
        return new ZooKeeper(s,i,watcher,b,config);
    }
}

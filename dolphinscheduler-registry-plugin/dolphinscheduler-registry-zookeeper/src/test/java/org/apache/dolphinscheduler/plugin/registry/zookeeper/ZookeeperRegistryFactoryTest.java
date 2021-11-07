package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import org.apache.dolphinscheduler.spi.plugin.DolphinSPILoader;
import org.apache.dolphinscheduler.spi.register.RegistryFactory;

import org.junit.Assert;
import org.junit.Test;

public class ZookeeperRegistryFactoryTest {

    @Test
    public void loadPlugin() {
        ZookeeperRegistryFactory zookeeper = (ZookeeperRegistryFactory) DolphinSPILoader.loadSPI(RegistryFactory.class, "zookeeper");
        Assert.assertNotNull(zookeeper);
    }
}
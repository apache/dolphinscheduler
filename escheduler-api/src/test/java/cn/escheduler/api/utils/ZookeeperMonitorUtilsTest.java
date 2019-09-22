package cn.escheduler.api.utils;

import cn.escheduler.common.model.MasterServer;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;

/**
 * zookeeper monitor utils test
 */
public class ZookeeperMonitorUtilsTest {


    @Test
    public void testGetMasterLsit(){

        ZookeeperMonitor zookeeperMonitor = new ZookeeperMonitor();


        List<MasterServer> masterServerList = zookeeperMonitor.getMasterServers();

        List<MasterServer> workerServerList = zookeeperMonitor.getWorkerServers();

        Assert.assertTrue(masterServerList.size() >= 0);
        Assert.assertTrue(workerServerList.size() >= 0);


    }

}
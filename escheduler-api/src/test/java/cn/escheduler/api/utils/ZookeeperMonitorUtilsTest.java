package cn.escheduler.api.utils;

import cn.escheduler.common.model.MasterServer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


import java.util.List;

@Ignore
public class ZookeeperMonitorUtilsTest {


    @Test
    public void testGetMasterLsit(){

        ZookeeperMonitor zookeeperMonitor = new ZookeeperMonitor();


        List<MasterServer> masterServerList = zookeeperMonitor.getMasterServers();

        List<MasterServer> workerServerList = zookeeperMonitor.getWorkerServers();

        Assert.assertEquals(masterServerList.size(), 1);
        Assert.assertEquals(workerServerList.size(), 1);


    }

}
package cn.escheduler.api.utils;

import cn.escheduler.dao.model.MasterServer;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.MacSpi;

import java.util.List;

import static org.junit.Assert.*;

public class ZookeeperMonitorUtilsTest {


    @Test
    public void testGetMasterLsit(){

        ZookeeperMonitor zookeeperMonitor = new ZookeeperMonitor();


        List<MasterServer> masterServerList = zookeeperMonitor.getMasterServers();

        List<MasterServer> workerServerList = zookeeperMonitor.getWorkerServers();

        System.out.println("master:" + masterServerList);
        System.out.println("worker:" + workerServerList);
        Assert.assertEquals(masterServerList.size(), 1);
        Assert.assertEquals(workerServerList.size(), 1);


    }

}
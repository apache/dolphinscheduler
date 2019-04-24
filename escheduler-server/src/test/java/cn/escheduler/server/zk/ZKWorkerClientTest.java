package cn.escheduler.server.zk;

import cn.escheduler.common.Constants;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class ZKWorkerClientTest {

    @Test
    public void getZKWorkerClient() throws Exception {


        ZKWorkerClient zkWorkerClient = ZKWorkerClient.getZKWorkerClient();
        zkWorkerClient.removeDeadServerByHost("127.0.0.1", Constants.WORKER_PREFIX);


    }

    @Test
    public void test(){
        String ips = "";

        List<String> ipList = Arrays.asList(ips.split(","));

        System.out.println(ipList);
    }
}
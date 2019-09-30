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
package cn.escheduler.server.zk;

import cn.escheduler.common.Constants;
import cn.escheduler.common.zk.AbstractZKClient;
import org.junit.Assert;
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


//        ZKWorkerClient zkWorkerClient = ZKWorkerClient.getZKWorkerClient();
//        zkWorkerClient.removeDeadServerByHost("127.0.0.1", Constants.WORKER_PREFIX);


    }

    @Test
    public void test(){
        String ips = "";

        List<String> ipList = Arrays.asList(ips.split(","));


        Assert.assertEquals(1, ipList.size());
    }
}

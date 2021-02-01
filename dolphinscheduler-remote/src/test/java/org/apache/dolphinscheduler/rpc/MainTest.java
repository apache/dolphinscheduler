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

package org.apache.dolphinscheduler.rpc;

import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.rpc.client.IRpcClient;
import org.apache.dolphinscheduler.rpc.client.RpcClient;
import org.apache.dolphinscheduler.rpc.remote.NettyClient;

import org.junit.Assert;

public class MainTest {


    private IUserService userService;

    private Host host;

    public static void main(String[] args) throws Exception {

        IRpcClient rpcClient = new RpcClient();
        Host host = new Host("127.0.0.1", 12346);
        IUserService userService = rpcClient.create(IUserService.class, host);

    }

    public void sendTest() {
        Integer result = userService.hi(3);
        Assert.assertSame(4, result);
        result = userService.hi(4);
        Assert.assertSame(5, result);
        userService.say("sync");
        NettyClient.getInstance().close();

    }

}

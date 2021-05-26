package org.apache.dolphinscheduler.plugin.register.zookeeper;/*
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

import static org.apache.dolphinscheduler.plugin.register.zookeeper.ZookeeperConfiguration.SERVERS;

import org.apache.dolphinscheduler.spi.register.Register;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {


    public static void main(String[] args) throws Exception {

        Map<String, String> registerConfig = new HashMap<>();


        registerConfig.put("servers", "127.0.0.1:2181");

        System.out.printf(SERVERS.getName());

        Register register = new ZookeeperRegister();
        register.init(registerConfig);
        register.persist("/xxx/sd", "kriis");
       // boolean sb = register.acquireLock("/kristen");
        sb(register);
        sb(register);


        // register.persist();
        // register.subscribe("/xxx", new TestListener());
        // register.delete("/xxx/sd");
        //register.
        while (true) {

        }


    }

    public static void sb(Register register) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                register.persist("/xxx/sd", "kriis");
                boolean sb = register.acquireLock("/kristen");
                System.out.printf(Thread.currentThread().getName());
                register.releaseLock("/kristen");

            }
        }).start();
    }


}

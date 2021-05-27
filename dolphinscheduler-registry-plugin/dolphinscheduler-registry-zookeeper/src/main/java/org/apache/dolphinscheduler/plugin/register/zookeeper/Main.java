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

import org.apache.dolphinscheduler.spi.register.Registry;

import org.apache.curator.framework.state.ConnectionState;

import java.util.HashMap;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws Exception {
        System.out.println("xxxxxxx<<<<<<<<<<<<<<<<<");
        Map<String, String> registerConfig = new HashMap<>();


        registerConfig.put("servers", "127.0.0.1:2181");

        System.out.printf(SERVERS.getName());

        ZookeeperRegistry registry = new ZookeeperRegistry();
        registry.init(registerConfig);
        registry.persist("/xxx/sd", "kriis");
        System.out.println("xxxxxxx<<<<<<<<<<<<<<<<<");
       registry.getClient().getConnectionStateListenable().addListener( (client,newState) -> {
           System.out.println("xxxxxxx<<<<<<<<<<<<<<<<<");
           if (newState == ConnectionState.LOST) {
              System.out.println("xxxxxxx<<<<<<<<<<<<<<<<<");
           } else if (newState == ConnectionState.RECONNECTED) {
               System.out.println("xxxxxxx<<<<<<<<<<<<<<<<<");
           } else if (newState == ConnectionState.SUSPENDED) {
               System.out.println("xxxxxxx<<<<<<<<<<<<<<<<<");
           }
       });

        // boolean sb = registry.acquireLock("/kristen");
        sb(registry);
        sb(registry);


        // registry.persist();
        // registry.subscribe("/xxx", new TestListener());
        // registry.delete("/xxx/sd");
        //registry.
        while (true) {

        }


    }

    public static void sb(Registry registry) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                registry.persist("/xxx/sd", "kriis");
                boolean sb = registry.acquireLock("/kristen");
                System.out.printf(Thread.currentThread().getName());
                registry.releaseLock("/kristen");

            }
        }).start();
    }


}

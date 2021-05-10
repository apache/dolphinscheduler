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


import org.apache.dolphinscheduler.plugin.register.api.AbstractRegister;
import org.apache.dolphinscheduler.plugin.register.api.RegisterExceptionHandler;
import org.apache.dolphinscheduler.spi.register.Register;
import org.apache.dolphinscheduler.spi.register.SubscribeListener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ZookeeperRegister extends AbstractRegister {

    private CuratorFramework client;


    @Override
    public void register(Map<String, Object> registerData) {
        ZookeeperConfiguration.initConfiguration(registerData);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(ZookeeperConfiguration.SERVERS)
                .retryPolicy(new ExponentialBackoffRetry(ZookeeperConfiguration.MAX_SLEEP_TIME_MILLI_SECONDS, ZookeeperConfiguration.MAX_RETRIES, ZookeeperConfiguration.MAX_SLEEP_TIME_MILLI_SECONDS))
                .namespace(ZookeeperConfiguration.NAMESPACE);

        client = builder.build();
        client.start();
        super.register(registerData);

    }

    @Override
    public void upRegister() {

    }

    @Override
    public void subscribe(String key, SubscribeListener subscribeListener) {

    }

    @Override
    public void unsubscribe(String key, SubscribeListener subscribeListener) {

    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void remove(String key) {

        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (Exception e) {
            RegisterExceptionHandler.handleException(e);
        }
    }

    @Override
    public boolean isExisted(String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (Exception e) {
            RegisterExceptionHandler.handleException(e);
            return false;
        }
    }

    @Override
    public void persist(String key, String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
        } catch (Exception e) {
            RegisterExceptionHandler.handleException(e);
        }
    }

    @Override
    public void update(String key, String value) {
        try {
            TransactionOp transactionOp = client.transactionOp();
            client.transaction().forOperations(transactionOp.check().forPath(key), transactionOp.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            RegisterExceptionHandler.handleException(e);
        }
    }



    @Override
    public List<String> getChildren(String path) {
        return null;
    }

    @Override
    public String getData(String key) {
        return null;
    }
}

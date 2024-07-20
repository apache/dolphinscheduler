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

package org.apache.dolphinscheduler.plugin.registry.raft.server;

import org.apache.dolphinscheduler.plugin.registry.raft.RaftRegistryProperties;

import lombok.extern.slf4j.Slf4j;

import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;

@Slf4j
public class RaftRegisterServer {

    private final RheaKVStore rheaKVStore;
    private final RheaKVStoreOptions options;
    private volatile boolean started;
    public RaftRegisterServer(RaftRegistryProperties raftRegistryProperties) {
        final PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true) // use a fake pd
                .config();
        final StoreEngineOptions storeOpts = StoreEngineOptionsConfigured.newConfigured()
                .withStorageType(StorageType.Memory)
                .withRaftDataPath(raftRegistryProperties.getLogStorageDir())
                .withServerAddress(
                        new Endpoint(raftRegistryProperties.getServerAddress(), raftRegistryProperties.getServerPort()))
                .config();
        this.options = RheaKVStoreOptionsConfigured.newConfigured()
                .withClusterName(raftRegistryProperties.getClusterName())
                .withInitialServerList(raftRegistryProperties.getServerAddressList())
                .withStoreEngineOptions(storeOpts)
                .withPlacementDriverOptions(pdOpts)
                .config();
        this.rheaKVStore = new DefaultRheaKVStore();
    }

    public void start() {
        if (this.started) {
            log.info("raft register server is already started");
            return;
        }
        log.info("starting raft register server...");
        this.rheaKVStore.init(this.options);
        log.info("raft register server started successfully");
        this.started = true;
    }

    public void stop() {
        log.info("stopping raft register server");
        if (this.rheaKVStore != null) {
            this.rheaKVStore.shutdown();
        }
        this.started = false;
        log.info("raft register server stopped successfully");
    }
}

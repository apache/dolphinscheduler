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

package org.apache.dolphinscheduler.plugin.registry.etcd;

import org.apache.dolphinscheduler.registry.api.RegistryException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;

@Slf4j
public class EtcdKeepAliveLeaseManager {

    private final Map<String, Long> keyLeaseCache = new ConcurrentHashMap<>();

    private final Client client;

    EtcdKeepAliveLeaseManager(Client client) {
        this.client = client;
    }

    long getOrCreateKeepAliveLease(String key, long timeToLive) {
        return keyLeaseCache.computeIfAbsent(key, $ -> {
            try {
                long leaseId = client.getLeaseClient().grant(timeToLive).get().getID();
                client.getLeaseClient().keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {

                    @Override
                    public void onNext(LeaseKeepAliveResponse value) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Lease {} keep alive error, remove cache with key:{}", leaseId, key, t);
                        keyLeaseCache.remove(key);
                    }

                    @Override
                    public void onCompleted() {
                        log.error("Lease {} keep alive complete, remove cache with key:{}", leaseId, key);
                        keyLeaseCache.remove(key);
                    }
                });
                log.info("Lease {} keep alive create with key:{}", leaseId, key);
                return leaseId;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RegistryException("Failed to create lease key: " + key, e);
            } catch (ExecutionException e) {
                throw new RegistryException("Failed to create lease key: " + key, e);
            }
        });
    }

    Optional<Long> getKeepAliveLease(String key) {
        return Optional.ofNullable(keyLeaseCache.get(key));
    }
}

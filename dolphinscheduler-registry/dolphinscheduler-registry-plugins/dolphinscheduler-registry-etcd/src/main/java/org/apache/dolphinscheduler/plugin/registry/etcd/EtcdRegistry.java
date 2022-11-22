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

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.base.Splitter;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.support.Observers;
import io.etcd.jetcd.watch.WatchEvent;

/**
 * This is one of the implementation of {@link Registry}, with this implementation, you need to rely on Etcd cluster to
 * store the DolphinScheduler master/worker's metadata and do the server registry/unRegistry.
 */
@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "etcd")
public class EtcdRegistry implements Registry {

    private static Logger LOGGER = LoggerFactory.getLogger(EtcdRegistry.class);
    private final Client client;
    private EtcdConnectionStateListener etcdConnectionStateListener;
    public static final String FOLDER_SEPARATOR = "/";
    // save the lock info for thread
    // key:lockKey Value:leaseId
    private static final ThreadLocal<Map<String, Long>> threadLocalLockMap = new ThreadLocal<>();

    private final Map<String, Watch.Watcher> watcherMap = new ConcurrentHashMap<>();

    private static final long TIME_TO_LIVE_SECONDS = 30L;
    public EtcdRegistry(EtcdRegistryProperties registryProperties) {
        ClientBuilder clientBuilder = Client.builder()
                .endpoints(Util.toURIs(Splitter.on(",").trimResults().splitToList(registryProperties.getEndpoints())))
                .namespace(byteSequence(registryProperties.getNamespace()))
                .connectTimeout(registryProperties.getConnectionTimeout())
                .retryChronoUnit(ChronoUnit.MILLIS)
                .retryDelay(registryProperties.getRetryDelay().toMillis())
                .retryMaxDelay(registryProperties.getRetryMaxDelay().toMillis())
                .retryMaxDuration(registryProperties.getRetryMaxDuration());
        if (StringUtils.hasLength(registryProperties.getUser())
                && StringUtils.hasLength(registryProperties.getPassword())) {
            clientBuilder.user(byteSequence(registryProperties.getUser()));
            clientBuilder.password(byteSequence(registryProperties.getPassword()));
        }
        if (StringUtils.hasLength(registryProperties.getLoadBalancerPolicy())) {
            clientBuilder.loadBalancerPolicy(registryProperties.getLoadBalancerPolicy());
        }
        if (StringUtils.hasLength(registryProperties.getAuthority())) {
            clientBuilder.authority(registryProperties.getAuthority());
        }
        client = clientBuilder.build();
        LOGGER.info("Started Etcd Registry...");
        etcdConnectionStateListener = new EtcdConnectionStateListener(client);
    }

    /**
     * Start the etcd Connection stateListeer
     */
    @PostConstruct
    public void start() {
        LOGGER.info("Starting Etcd ConnectionListener...");
        etcdConnectionStateListener.start();
        LOGGER.info("Started Etcd ConnectionListener...");
    }

    @Override
    public void connectUntilTimeout(@NonNull Duration timeout) throws RegistryException {
        // The connectTimeout has been set in the constructor
    }

    /**
     *
     * @param path The prefix of the key being listened to
     * @param listener
     * @return if subcribe Returns true if no exception was thrown
     */
    @Override
    public boolean subscribe(String path, SubscribeListener listener) {
        try {
            ByteSequence watchKey = byteSequence(path);
            WatchOption watchOption = WatchOption.newBuilder().isPrefix(true).build();
            watcherMap.computeIfAbsent(path,
                    $ -> client.getWatchClient().watch(watchKey, watchOption, watchResponse -> {
                        for (WatchEvent event : watchResponse.getEvents()) {
                            listener.notify(new EventAdaptor(event, path));
                        }
                    }));
        } catch (Exception e) {
            throw new RegistryException("Failed to subscribe listener for key: " + path, e);
        }
        return true;
    }

    /**
     * @throws throws an exception if the unsubscribe path does not exist
     * @param path The prefix of the key being listened to
     */
    @Override
    public void unsubscribe(String path) {
        try {
            watcherMap.get(path).close();
            watcherMap.remove(path);
        } catch (Exception e) {
            throw new RegistryException("Failed to unsubscribe listener for key: " + path, e);
        }
    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {
        etcdConnectionStateListener.addConnectionListener(listener);
    }

    /**
     *
     * @return Returns the value corresponding to the key
     * @throws throws an exception if the key does not exist
     */
    @Override
    public String get(String key) {
        try {
            List<KeyValue> keyValues = client.getKVClient().get(byteSequence(key)).get().getKvs();
            return keyValues.iterator().next().getValue().toString(StandardCharsets.UTF_8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("etcd get data error", e);
        } catch (ExecutionException e) {
            throw new RegistryException("etcd get data error, key = " + key, e);
        }
    }

    /**
     *
     * @param deleteOnDisconnect Does the put data disappear when the client disconnects
     */
    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        try {
            if (deleteOnDisconnect) {
                // keep the key by lease, if disconnected, the lease will expire and the key will delete
                long leaseId = client.getLeaseClient().grant(TIME_TO_LIVE_SECONDS).get().getID();
                client.getLeaseClient().keepAlive(leaseId, Observers.observer(response -> {
                }));
                PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
                client.getKVClient().put(byteSequence(key), byteSequence(value), putOption).get();
            } else {
                client.getKVClient().put(byteSequence(key), byteSequence(value)).get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("Failed to put registry key: " + key, e);
        } catch (ExecutionException e) {
            throw new RegistryException("Failed to put registry key: " + key, e);
        }
    }

    /**
     * delete all keys that contain the prefix {@Code key}
     */
    @Override
    public void delete(String key) {
        try {
            DeleteOption deleteOption = DeleteOption.newBuilder().isPrefix(true).build();
            client.getKVClient().delete(byteSequence(key), deleteOption).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("Failed to delete registry key: " + key, e);
        } catch (ExecutionException e) {
            throw new RegistryException("Failed to delete registry key: " + key, e);
        }
    }

    /**
     * Get all child objects, split by "/"
     */
    @Override
    public Collection<String> children(String key) {
        // Make sure the string end with '/'
        // eg:change key = /nodes to /nodes/
        String prefix = key.endsWith(FOLDER_SEPARATOR) ? key : key + FOLDER_SEPARATOR;
        GetOption getOption = GetOption.newBuilder().isPrefix(true).withSortField(GetOption.SortTarget.KEY)
                .withSortOrder(GetOption.SortOrder.ASCEND).build();
        try {
            List<KeyValue> keyValues = client.getKVClient().get(byteSequence(prefix), getOption).get().getKvs();
            return keyValues.stream().map(e -> getSubNodeKeyName(prefix, e.getKey().toString(StandardCharsets.UTF_8)))
                    .distinct().collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("etcd get children error", e);
        } catch (ExecutionException e) {
            throw new RegistryException("etcd get children error, key: " + key, e);
        }
    }

    /**
     * If "/" exists in the child object, get the string prefixed with "/"
     */
    private String getSubNodeKeyName(final String prefix, final String fullPath) {
        String pathWithoutPrefix = fullPath.substring(prefix.length());
        return pathWithoutPrefix.contains(FOLDER_SEPARATOR)
                ? pathWithoutPrefix.substring(0, pathWithoutPrefix.indexOf(FOLDER_SEPARATOR))
                : pathWithoutPrefix;
    }

    @Override
    public boolean exists(String key) {
        GetOption getOption = GetOption.newBuilder().withCountOnly(true).build();
        try {
            if (client.getKVClient().get(byteSequence(key), getOption).get().getCount() >= 1) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("etcd check key is existed error", e);
        } catch (ExecutionException e) {
            throw new RegistryException("etcd check key is existed error, key: " + key, e);
        }
        return false;
    }

    /**
     * get the lock with a lease
     */
    @Override
    public boolean acquireLock(String key) {
        Lock lockClient = client.getLockClient();
        Lease leaseClient = client.getLeaseClient();
        // get the lock with a lease
        try {
            long leaseId = leaseClient.grant(TIME_TO_LIVE_SECONDS).get().getID();
            // keep the lease
            client.getLeaseClient().keepAlive(leaseId, Observers.observer(response -> {
            }));
            lockClient.lock(byteSequence(key), leaseId).get();

            // save the leaseId for release Lock
            if (null == threadLocalLockMap.get()) {
                threadLocalLockMap.set(new HashMap<>());
            }
            threadLocalLockMap.get().put(key, leaseId);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryException("etcd get lock error", e);
        } catch (ExecutionException e) {
            throw new RegistryException("etcd get lock error, lockKey: " + key, e);
        }
    }

    /**
     * release the lock by revoking the leaseId
     */
    @Override
    public boolean releaseLock(String key) {
        try {
            Long leaseId = threadLocalLockMap.get().get(key);
            client.getLeaseClient().revoke(leaseId);
            threadLocalLockMap.get().remove(key);
            if (threadLocalLockMap.get().isEmpty()) {
                threadLocalLockMap.remove();
            }
        } catch (Exception e) {
            throw new RegistryException("etcd release lock error, lockKey: " + key, e);
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        // When the client closes, the watch also closes.
        client.close();
    }

    private static ByteSequence byteSequence(String val) {
        return ByteSequence.from(val, StandardCharsets.UTF_8);
    }

    static final class EventAdaptor extends Event {

        public EventAdaptor(WatchEvent event, String key) {
            key(key);

            switch (event.getEventType()) {
                case PUT:
                    type(Type.ADD);
                    break;
                case DELETE:
                    type(Type.REMOVE);
                    break;
                default:
                    break;
            }
            KeyValue keyValue = event.getKeyValue();
            if (keyValue != null) {
                path(keyValue.getKey().toString(StandardCharsets.UTF_8));
                data(keyValue.getValue().toString(StandardCharsets.UTF_8));
            }
        }
    }
}

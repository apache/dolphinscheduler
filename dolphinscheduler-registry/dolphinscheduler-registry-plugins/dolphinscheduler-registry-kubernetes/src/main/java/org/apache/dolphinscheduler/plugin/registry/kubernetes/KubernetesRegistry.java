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

package org.apache.dolphinscheduler.plugin.registry.kubernetes;

import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;

@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "k8s")
public class KubernetesRegistry implements Registry {

    private static Logger LOGGER =
            LoggerFactory.getLogger(org.apache.dolphinscheduler.plugin.registry.kubernetes.KubernetesRegistry.class);
    private final KubernetesClient client;
    private KubernetesConnectionStateListener kubernetesConnectionStateListener;
    public static final String FOLDER_SEPARATOR = "/";
    // save the lock info for thread
    // key:lockKey Value:leaseId
    private static final ThreadLocal<Map<String, Long>> threadLocalLockMap = new ThreadLocal<>();

    private final Map<String, Watch> watcherMap = new ConcurrentHashMap<>();

    private static final long TIME_TO_LIVE_SECONDS = 30L;
    public KubernetesRegistry(KubernetesProperties registryProperties) {
        client = new DefaultKubernetesClient();
        LOGGER.info("Kubernetes Registry starting...");
        kubernetesConnectionStateListener = new KubernetesConnectionStateListener(client);
    }

    /**
     * Start the ConnectionState Listener
     */
    @PostConstruct
    public void start() {
        LOGGER.info("Kubernetes Connection Listener starting ...");
        kubernetesConnectionStateListener.start();
        LOGGER.info("Kubernetes Connection Listener started...");
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
            // ByteSequence watchKey = byteSequence(path);
            // WatchOption watchOption = WatchOption.newBuilder().isPrefix(true).build();
            // TODO: change this, should read from config
            final String namespace = "dolphinscheduler";
            watcherMap.computeIfAbsent(path,
                    $ -> client.pods().inNamespace(namespace).watch(new Watcher<Pod>() {

                        @Override
                        public void eventReceived(Action action, Pod pod) {
                            // TODO: complete this, notify listeners
                            switch (action.name()) {
                                case "ADDED":
                                    LOGGER.info("{}/{} got added", pod.getMetadata().getNamespace(),
                                            pod.getMetadata().getName());
                                    break;
                                case "DELETED":
                                    LOGGER.info("{}/{} got deleted", pod.getMetadata().getNamespace(),
                                            pod.getMetadata().getName());
                                    break;
                                case "MODIFIED":
                                    LOGGER.info("{}/{} got modified", pod.getMetadata().getNamespace(),
                                            pod.getMetadata().getName());
                                    break;
                                default:
                                    LOGGER.error("Unrecognized event: {}", action.name());
                            }
                        }

                        @Override
                        public void onClose() {
                            LOGGER.info("Watch closed");
                        }

                        @Override
                        public void onClose(WatcherException e) {
                            LOGGER.info("Watched closed due to exception ", e);
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
        kubernetesConnectionStateListener.addConnectionListener(listener);
    }

    /**
     *
     * @return Returns the value corresponding to the key
     * @throws throws an exception if the key does not exist
     */
    @Override
    public String get(String key) {
        // TODO: use pod selector to get services
        return null;
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        LOGGER.info("Kubernetes has already been tracking the pod since created it, no more actions here");
    }

    @Override
    public void delete(String key) {
        // TODO: replace the log message with a more meaningful one
        LOGGER.info("No more actions here");
    }

    /**
     * key -> label, use k8s pod selector
     */
    @Override
    public Collection<String> children(String key) {
        // Make sure the string end with '/'
        // eg:change key = /nodes to /nodes/
        String prefix = key.endsWith(FOLDER_SEPARATOR) ? key : key + FOLDER_SEPARATOR;
        // TODO: key -> label
        return null;
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
        // TODO: pods with xxx labels exists?
        return false;
    }

    /**
     * leader election by resource lock
     */
    @Override
    public boolean acquireLock(String key) {
        // TODO: use lease lock here for leader election
        return false;
    }

    /**
     * delete leader
     */
    @Override
    public boolean releaseLock(String key) {
        // TODO: release lease lock here
        return true;
    }

    @Override
    public void close() throws IOException {
        // When the client closes, the watch also closes.
        client.close();
    }

    static final class EventAdaptor extends Event {

        public EventAdaptor(Watcher.Action action, String key) {
            key(key);

            switch (action.name()) {
                case "ADDED":
                    type(Type.ADD);
                    break;
                case "DELETED":
                    type(Type.REMOVE);
                    break;
                default:
                    break;
            }
            // TODO: map key to pod label
            // KeyValue keyValue = event.getKeyValue();
            // if (keyValue != null) {
            // path(keyValue.getKey().toString(StandardCharsets.UTF_8));
            // data(keyValue.getValue().toString(StandardCharsets.UTF_8));
            // }
        }
    }
}

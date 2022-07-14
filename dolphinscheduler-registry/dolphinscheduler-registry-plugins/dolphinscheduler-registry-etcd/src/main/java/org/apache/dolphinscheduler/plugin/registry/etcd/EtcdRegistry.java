package org.apache.dolphinscheduler.plugin.registry.etcd;

import com.google.common.base.Strings;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.support.Observers;
import io.etcd.jetcd.watch.WatchEvent;
import org.apache.dolphinscheduler.registry.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.apache.dolphinscheduler.common.Constants.FOLDER_SEPARATOR;


@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "etcd")
public class EtcdRegistry implements Registry {
    private static Logger LOGGER = LoggerFactory.getLogger(EtcdRegistry.class);
    private final Client client;
    private EtcdConnectionStateListener etcdConnectionStateListener;
    // save the lock info for thread
    // key:lockKey Value:leaseId
    private static final ThreadLocal<Map<String, Long>> threadLocalLockMap = new ThreadLocal<>();

    private final Map<String, Watch.Watcher> watcherMap = new ConcurrentHashMap<>();

    private static Long TIME_TO_LIVE_SECONDS=30L;
    public EtcdRegistry(EtcdRegistryProperties registryProperties) {
        LOGGER.info("Starting Etcd Registry...");
        ClientBuilder clientBuilder = Client.builder()
                .endpoints(registryProperties.getEndpoints())
                .namespace(byteSequence(registryProperties.getNamespace()))
                .connectTimeout(registryProperties.getConnectionTimeout())
                .retryChronoUnit(ChronoUnit.MILLIS)
                .retryDelay(registryProperties.getRetryDelay())
                .retryMaxDelay(registryProperties.getRetryMaxDelay())
                .retryMaxDuration(registryProperties.getRetryMaxDuration());
        if(!Strings.isNullOrEmpty(registryProperties.getUser())&&(!Strings.isNullOrEmpty(registryProperties.getPassword()))){
            clientBuilder.user(byteSequence(registryProperties.getUser()));
            clientBuilder.password(byteSequence(registryProperties.getPassword()));
        }
        if(!Strings.isNullOrEmpty(registryProperties.getLoadBalancerPolicy())){
            clientBuilder.loadBalancerPolicy(registryProperties.getLoadBalancerPolicy());
        }
        if(!Strings.isNullOrEmpty(registryProperties.getAuthority())){
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
            watcherMap.computeIfAbsent(path, $ -> client.getWatchClient().watch(watchKey, watchOption,watchResponse -> {
                for (WatchEvent event : watchResponse.getEvents()) {
                    listener.notify(new EventAdaptor(event, path));
                }
            }));
        } catch (Exception e){
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
     * @param key
     * @return Returns the value corresponding to the key
     * @throws throws an exception if the key does not exist
     */
    @Override
    public String get(String key) {
        try {
            List<KeyValue> keyValues = client.getKVClient().get(byteSequence(key)).get().getKvs();
            return keyValues.iterator().next().getValue().toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RegistryException("etcd get data error", e);
        }
    }

    /**
     *
     * @param key
     * @param value
     * @param deleteOnDisconnect Does the put data disappear when the client disconnects
     */
    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {
        try{
            if(deleteOnDisconnect) {
                // keep the key by lease, if disconnected, the lease will ,the key will delete
                long leaseId = client.getLeaseClient().grant(TIME_TO_LIVE_SECONDS).get().getID();
                client.getLeaseClient().keepAlive(leaseId, Observers.observer(response -> {
                }));
                PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
                client.getKVClient().put(byteSequence(key), byteSequence(value),putOption).get();
            }else{
                client.getKVClient().put(byteSequence(key), byteSequence(value)).get();
            }
        } catch (Exception e){
            throw new RegistryException("Failed to put registry key: " + key, e);
        }
    }

    /**
     * delete all keys that contain the prefix
     * @param key the prrefix
     */
    @Override
    public void delete(String key) {
        try {
            DeleteOption deleteOption =DeleteOption.newBuilder().isPrefix(true).build();
            client.getKVClient().delete(byteSequence(key), deleteOption).get();
        }  catch (Exception e) {
            throw new RegistryException("Failed to delete registry key: " + key, e);
        }
    }

    /**
     * Get all child objects, split by "/"
     * @param key
     * @return
     */
    @Override
    public Collection<String> children(String key) {
        // Make sure the string end with '/'
        // eg:change key = /nodes to /nodes/
        String prefix = key.endsWith(FOLDER_SEPARATOR)?key:key+FOLDER_SEPARATOR;
        GetOption getOption = GetOption.newBuilder().isPrefix(true).withSortField(GetOption.SortTarget.KEY).withSortOrder(GetOption.SortOrder.ASCEND).build();
        try {
            List<KeyValue> keyValues = client.getKVClient().get(byteSequence(prefix),getOption).get().getKvs();
            return keyValues.stream().map(e -> getSubNodeKeyName(prefix, e.getKey().toString(StandardCharsets.UTF_8))).distinct().collect(Collectors.toList());
        } catch (Exception e){
            throw new RegistryException("etcd get children error", e);
        }
    }

    /**
     * If "/" exists in the child object, get the string prefixed with "/"
     * @param prefix
     * @param fullPath
     * @return
     */
    private String getSubNodeKeyName(final String prefix, final String fullPath) {
        String pathWithoutPrefix = fullPath.substring(prefix.length());
        return pathWithoutPrefix.contains(FOLDER_SEPARATOR) ? pathWithoutPrefix.substring(0, pathWithoutPrefix.indexOf(FOLDER_SEPARATOR)) : pathWithoutPrefix;
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public boolean exists(String key) {
        GetOption getOption = GetOption.newBuilder().withCountOnly(true).build();
        try {
            if (client.getKVClient().get(byteSequence(key),getOption).get().getCount() >= 1)
                return true;
        }catch (Exception e) {
            throw new RegistryException("etcd check key is existed error", e);
        }
        return false;
    }

    /**
     * get the lock with a lease
     * @param key
     * @return
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
            lockClient.lock(byteSequence(key),leaseId).get();

            // save the leaseId for release Lock
            if(null == threadLocalLockMap.get()){
                threadLocalLockMap.set(new HashMap<>());
            }
            threadLocalLockMap.get().put(key,leaseId);
            return true;
        } catch (Exception e) {
            throw new RegistryException("etcd get lock error", e);
        }
    }

    /**
     * release the lock by revoking the leaseId
     * @param key
     * @return
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
        } catch (Exception e){
            throw new RegistryException("etcd release lock error", e);
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        // When the client closes, the watch also closes.
        client.close();
    }
    private static ByteSequence byteSequence(String val){
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


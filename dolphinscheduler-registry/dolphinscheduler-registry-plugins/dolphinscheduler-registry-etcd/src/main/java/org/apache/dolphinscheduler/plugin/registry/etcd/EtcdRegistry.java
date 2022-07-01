package org.apache.dolphinscheduler.plugin.registry.etcd;

import com.google.common.base.Strings;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.support.Observers;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.apache.dolphinscheduler.common.Constants.FOLDER_SEPARATOR;


@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "etcd")
public class EtcdRegistry implements Registry {
    private final Client client;

    // save the lock info for thread
    // key:lockKey Value:leaseId
    private static final ThreadLocal<Map<String, Long>> threadLocalLockMap = new ThreadLocal<>();

    private static Long TIME_TO_LIVE_SECONDS=30L;
    public EtcdRegistry(EtcdRegistryProperties registryProperties) {
        ClientBuilder clientBuilder = Client.builder()
                .endpoints(registryProperties.getEndpoints())
                .namespace(byteSequence(registryProperties.getNamespace()))
                .connectTimeout(registryProperties.getConnectionTimeout())
                .retryChronoUnit(ChronoUnit.MILLIS)
                .retryDelay(registryProperties.getRetryDelay())
                .retryMaxDelay(registryProperties.getRetryMaxDelay())
                .retryMaxDuration(registryProperties.getRetryMaxDuration());
        if(!Strings.isNullOrEmpty(registryProperties.getUser())){
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
    }
    @Override
    public boolean subscribe(String path, SubscribeListener listener) {
        return false;
    }

    @Override
    public void unsubscribe(String path) {

    }

    @Override
    public void addConnectionStateListener(ConnectionListener listener) {

    }

    @Override
    public String get(String key) {
        try {
            List<KeyValue> keyValues = client.getKVClient().get(byteSequence(key)).get().getKvs();
            return keyValues.iterator().next().getValue().toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RegistryException("zookeeper get data error", e);
        }
    }

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

    @Override
    public void delete(String key) {
        try {
            DeleteOption deleteOption =DeleteOption.newBuilder().isPrefix(true).build();
            client.getKVClient().delete(byteSequence(key), deleteOption).get();
        }  catch (Exception e) {
            throw new RegistryException("Failed to delete registry key: " + key, e);
        }
    }

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
            throw new RegistryException("zookeeper get children error", e);
        }
    }

    private String getSubNodeKeyName(final String prefix, final String fullPath) {
        String pathWithoutPrefix = fullPath.substring(prefix.length());
        return pathWithoutPrefix.contains(FOLDER_SEPARATOR) ? pathWithoutPrefix.substring(0, pathWithoutPrefix.indexOf(FOLDER_SEPARATOR)) : pathWithoutPrefix;
    }

    @Override
    public boolean exists(String key) {
        GetOption getOption = GetOption.newBuilder().withCountOnly(true).build();
        try {
            if (client.getKVClient().get(byteSequence(key),getOption).get().getCount() >= 1)
                return true;
        }catch (Exception e) {
            throw new RegistryException("zookeeper check key is existed error", e);
        }
        return false;
    }

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

            // save the leaseId for releaseLock
            if(null == threadLocalLockMap.get()){
                threadLocalLockMap.set(new HashMap<>());
            }
            threadLocalLockMap.get().put(key,leaseId);
            return true;
        } catch (Exception e) {
            throw new RegistryException("zookeeper get lock error", e);
        }
    }

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
            throw new RegistryException("zookeeper release lock error", e);
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
    private static ByteSequence byteSequence(String val){
        return ByteSequence.from(val, StandardCharsets.UTF_8);
    }
}


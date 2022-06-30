package org.apache.dolphinscheduler.plugin.registry.etcd;

import com.google.common.base.Strings;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Collection;


@Component
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "etcd")
public class EtcdRegistry implements Registry {
    private final Client client;
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
        return null;
    }

    @Override
    public void put(String key, String value, boolean deleteOnDisconnect) {

    }

    @Override
    public void delete(String key) {

    }

    @Override
    public Collection<String> children(String key) {
        return null;
    }

    @Override
    public boolean exists(String key) {
        return false;
    }

    @Override
    public boolean acquireLock(String key) {
        return false;
    }

    @Override
    public boolean releaseLock(String key) {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
    private static ByteSequence byteSequence(String val){
        return ByteSequence.from(val, StandardCharsets.UTF_8);
    }
}


package org.apache.dolphinscheduler.server.registry;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;

public class ZookeeperTest {

    public static void main(String[] args) throws InterruptedException {

        RegistryClient registryClient = RegistryClient.getInstance();
        registryClient.persistEphemeral("/test", Long.toString(System.currentTimeMillis()));


        while (true) {
            Thread.sleep(5 * 1000);
            System.out.println("ready to update");

            try {
                registryClient.update("/test", Long.toString(System.currentTimeMillis()));
            }catch (Throwable e) {
                System.out.println(e);
            }

        }
    }
}

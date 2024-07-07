package org.apache.dolphinscheduler.plugin.registry.consul;

import org.apache.dolphinscheduler.plugin.registry.RegistryTestCase;

import org.apache.commons.lang3.RandomUtils;

import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.Lists;

@SpringBootTest(classes = ConsulRegistryProperties.class)
@SpringBootApplication(scanBasePackageClasses = ConsulRegistryProperties.class)
public class ConsulRegistryTestCase extends RegistryTestCase<ConsulRegistry> {

    @Autowired
    private ConsulRegistryProperties consulRegistryProperties;

    private static final Network NETWORK = Network.newNetwork();

    private static GenericContainer<?> consulContainer;

    @SneakyThrows
    @BeforeAll
    public static void setUpTestingServer() {
        consulContainer = new GenericContainer<>(DockerImageName.parse("consul:1.11.1"))
                .withNetwork(NETWORK);
        int randomPort = RandomUtils.nextInt(10000, 65535);
        consulContainer.setPortBindings(Lists.newArrayList(randomPort + ":8500"));
        Startables.deepStart(Stream.of(consulContainer)).join();
        System.setProperty("registry.url", "http://localhost:" + randomPort);
    }

    @SneakyThrows
    @Override
    public ConsulRegistry createRegistry() {
        return new ConsulRegistry(consulRegistryProperties);
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {

    }
}

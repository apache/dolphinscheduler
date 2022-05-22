/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.e2e.core;

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;
import static org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat.MP4;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.utility.DockerImageName;

@Slf4j
final class DolphinSchedulerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
    private final boolean LOCAL_MODE = Objects.equals(System.getProperty("local"), "true");

    private final boolean M1_CHIP_FLAG = Objects.equals(System.getProperty("m1_chip"), "true");

    private RemoteWebDriver driver;
    private DockerComposeContainer<?> compose;
    private BrowserWebDriverContainer<?> browser;
    private Network network;
    private HostAndPort address;
    private String rootPath;

    private Path record;

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void beforeAll(ExtensionContext context) throws IOException {
        Awaitility.setDefaultTimeout(Duration.ofSeconds(60));
        Awaitility.setDefaultPollInterval(Duration.ofSeconds(10));

        setRecordPath();

        if (LOCAL_MODE) {
            runInLocal();
        } else {
            runInDockerContainer(context);
        }

        setBrowserContainerByOsName();

        if (network != null) {
            browser.withNetwork(network);
        }
        browser.start();

        driver = browser.getWebDriver();

        driver.manage().timeouts()
              .implicitlyWait(5, TimeUnit.SECONDS)
              .pageLoadTimeout(5, TimeUnit.SECONDS);
        driver.manage().window()
              .maximize();

        driver.get(new URL("http", address.getHost(), address.getPort(), rootPath).toString());

        browser.beforeTest(new TestDescription(context));

        final Class<?> clazz = context.getRequiredTestClass();
        Stream.of(clazz.getDeclaredFields())
              .filter(it -> Modifier.isStatic(it.getModifiers()))
              .filter(f -> WebDriver.class.isAssignableFrom(f.getType()))
              .forEach(it -> setDriver(clazz, it));
    }

    private void runInLocal() {
        Testcontainers.exposeHostPorts(3000);
        address = HostAndPort.fromParts("host.testcontainers.internal", 3000);
        rootPath = "/";
    }

    private void runInDockerContainer(ExtensionContext context) {
        compose = createDockerCompose(context);
        compose.start();

        final ContainerState dsContainer = compose.getContainerByServiceName("dolphinscheduler_1")
                .orElseThrow(() -> new RuntimeException("Failed to find a container named 'dolphinscheduler'"));
        final String networkId = dsContainer.getContainerInfo().getNetworkSettings().getNetworks().keySet().iterator().next();
        network = new Network() {
            @Override
            public String getId() {
                return networkId;
            }

            @Override
            public void close() {
            }

            @Override
            public Statement apply(Statement base, Description description) {
                return null;
            }
        };
        address = HostAndPort.fromParts("dolphinscheduler", 12345);
        rootPath = "/dolphinscheduler/ui/";
    }

    private void setBrowserContainerByOsName() {
        DockerImageName imageName;

        if (LOCAL_MODE && M1_CHIP_FLAG) {
            imageName = DockerImageName.parse("seleniarm/standalone-chromium:4.1.2-20220227")
                    .asCompatibleSubstituteFor("selenium/standalone-chrome");

            browser = new BrowserWebDriverContainer<>(imageName)
                    .withCapabilities(new ChromeOptions())
                    .withCreateContainerCmdModifier(cmd -> cmd.withUser("root"))
                    .withFileSystemBind(Constants.HOST_CHROME_DOWNLOAD_PATH.toFile().getAbsolutePath(),
                            Constants.SELENIUM_CONTAINER_CHROME_DOWNLOAD_PATH);
        } else {
            browser = new BrowserWebDriverContainer<>()
                    .withCapabilities(new ChromeOptions())
                    .withCreateContainerCmdModifier(cmd -> cmd.withUser("root"))
                    .withFileSystemBind(Constants.HOST_CHROME_DOWNLOAD_PATH.toFile().getAbsolutePath(),
                            Constants.SELENIUM_CONTAINER_CHROME_DOWNLOAD_PATH)
                    .withRecordingMode(RECORD_ALL, record.toFile(), MP4);
        }
    }

    private void setRecordPath() throws IOException {
        if (!Strings.isNullOrEmpty(System.getenv("RECORDING_PATH"))) {
            record = Paths.get(System.getenv("RECORDING_PATH"));
            if (!record.toFile().exists()) {
                if (!record.toFile().mkdir()) {
                    throw new IOException("Failed to create recording directory: " + record.toAbsolutePath());
                }
            }
        } else {
            record = Files.createTempDirectory("record-");
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        browser.afterTest(new TestDescription(context), Optional.empty());
        browser.stop();
        if (compose != null) {
            compose.stop();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        final Object instance = context.getRequiredTestInstance();
        Stream.of(instance.getClass().getDeclaredFields())
              .filter(f -> WebDriver.class.isAssignableFrom(f.getType()))
              .forEach(it -> setDriver(instance, it));
    }

    private void setDriver(Object object, Field field) {
        try {
            field.setAccessible(true);
            field.set(object, driver);
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to inject web driver to field: {}", field.getName(), e);
        }
    }

    private DockerComposeContainer<?> createDockerCompose(ExtensionContext context) {
        final Class<?> clazz = context.getRequiredTestClass();
        final DolphinScheduler annotation = clazz.getAnnotation(DolphinScheduler.class);
        final List<File> files = Stream.of(annotation.composeFiles())
                                       .map(it -> DolphinScheduler.class.getClassLoader().getResource(it))
                                       .filter(Objects::nonNull)
                                       .map(URL::getPath)
                                       .map(File::new)
                                       .collect(Collectors.toList());
        compose = new DockerComposeContainer<>(files)
            .withPull(true)
            .withTailChildContainers(true)
            .withLogConsumer("dolphinscheduler_1", outputFrame -> LOGGER.info(outputFrame.getUtf8String()))
            .waitingFor("dolphinscheduler_1", Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(180)));

        return compose;
    }
}

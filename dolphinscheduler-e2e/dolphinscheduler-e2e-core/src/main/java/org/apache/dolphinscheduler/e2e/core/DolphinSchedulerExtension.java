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

package org.apache.dolphinscheduler.e2e.core;

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;
import static org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat.MP4;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;

@Slf4j
final class DolphinSchedulerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, TestWatcher {

    private final boolean LOCAL_MODE = Objects.equals(System.getProperty("local"), "true");

    private final boolean M1_CHIP_FLAG = Objects.equals(System.getProperty("m1_chip"), "true");

    private final int LOCAL_PORT = 5173;

    private final int DOCKER_PORT = 12345;

    private RemoteWebDriver driver;
    private ComposeContainer compose;
    private BrowserWebDriverContainer<?> browser;
    private HostAndPort address;
    private String rootPath;

    private Path record;
    private Throwable firstFailure;

    private final String serviceName = "dolphinscheduler";

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void beforeAll(ExtensionContext context) throws IOException {
        Awaitility.setDefaultTimeout(Duration.ofSeconds(120));
        Awaitility.setDefaultPollInterval(Duration.ofMillis(500));

        setRecordPath();

        if (LOCAL_MODE) {
            runInLocal();
        } else {
            runInDockerContainer(context);
        }

        setBrowserContainerByOsName();

        if (compose != null) {
            Testcontainers.exposeHostPorts(compose.getServicePort(serviceName, DOCKER_PORT));
            browser.withAccessToHost(true);
        }
        browser.start();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("prefs", Map.of("profile.default_content_setting_values.clipboard", 1));
        LoggingPreferences logging = new LoggingPreferences();
        logging.enable(LogType.BROWSER, Level.ALL);
        chromeOptions.setCapability("goog:loggingPrefs", logging);
        chromeOptions.addArguments("--allow-running-insecure-content");
        chromeOptions.addArguments(String.format("--unsafely-treat-insecure-origin-as-secure=http://%s:%s",
                address.getHost(), address.getPort()));
        driver = new RemoteWebDriver(browser.getSeleniumAddress(), chromeOptions);

        log.info("E2E environment: local={}, m1={}, address={}, browser={}, recording={}",
                LOCAL_MODE, M1_CHIP_FLAG, address, driver.getCapabilities(), record);

        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(1))
                .pageLoadTimeout(Duration.ofSeconds(5));
        driver.manage().window()
                .maximize();

        driver.get(new URL("http", address.getHost(), address.getPort(), rootPath).toString());

        browser.beforeTest(new TestDescription(context));

        final Class<?> clazz = context.getRequiredTestClass();
        Stream.of(clazz.getDeclaredFields())
                .filter(it -> Modifier.isStatic(it.getModifiers()))
                .filter(f -> WebDriver.class.isAssignableFrom(f.getType()))
                .forEach(it -> setDriver(clazz, it));
        WebDriverHolder.setWebDriver(driver);
    }

    private void runInLocal() {
        Testcontainers.exposeHostPorts(LOCAL_PORT);
        address = HostAndPort.fromParts("host.testcontainers.internal", LOCAL_PORT);
        rootPath = "/";
    }

    private void runInDockerContainer(ExtensionContext context) {
        compose = createDockerCompose(context);
        compose.start();

        address =
                HostAndPort.fromParts("host.testcontainers.internal", compose.getServicePort(serviceName, DOCKER_PORT));
        rootPath = "/dolphinscheduler/ui/";
    }

    private void setBrowserContainerByOsName() {
        DockerImageName imageName;

        if (M1_CHIP_FLAG) {
            imageName = DockerImageName.parse("seleniarm/standalone-chromium:124.0-chromedriver-124.0")
                    .asCompatibleSubstituteFor("selenium/standalone-chrome");

            if (!Files.exists(Constants.HOST_CHROME_DOWNLOAD_PATH)) {
                try {
                    Files.createDirectories(Constants.HOST_CHROME_DOWNLOAD_PATH);
                } catch (IOException e) {
                    log.error("Failed to create chrome download directory: {}", Constants.HOST_CHROME_DOWNLOAD_PATH);
                    throw new RuntimeException(e);
                }
            }

            browser = new BrowserWebDriverContainer<>(imageName)
                    .withCapabilities(new ChromeOptions())
                    .withCreateContainerCmdModifier(cmd -> cmd.withUser("root"))
                    .withFileSystemBind(Constants.HOST_CHROME_DOWNLOAD_PATH.toFile().getAbsolutePath(),
                            Constants.SELENIUM_CONTAINER_CHROME_DOWNLOAD_PATH, BindMode.READ_WRITE)
                    .withRecordingMode(RECORD_ALL, record.toFile(), MP4)
                    .withStartupTimeout(Duration.ofSeconds(300));
        } else {
            browser = new BrowserWebDriverContainer<>()
                    .withCapabilities(new ChromeOptions())
                    .withCreateContainerCmdModifier(cmd -> cmd.withUser("root"))
                    .withFileSystemBind(Constants.HOST_CHROME_DOWNLOAD_PATH.toFile().getAbsolutePath(),
                            Constants.SELENIUM_CONTAINER_CHROME_DOWNLOAD_PATH, BindMode.READ_WRITE)
                    .withRecordingMode(RECORD_ALL, record.toFile(), MP4)
                    .withStartupTimeout(Duration.ofSeconds(300));
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
        browser.afterTest(new TestDescription(context), Optional.ofNullable(firstFailure));
        browser.stop();
        if (compose != null) {
            compose.stop();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        log.info("Starting E2E test: {}, url={}", context.getUniqueId(), driver.getCurrentUrl());
        final Object instance = context.getRequiredTestInstance();
        Stream.of(instance.getClass().getDeclaredFields())
                .filter(f -> WebDriver.class.isAssignableFrom(f.getType()))
                .forEach(it -> setDriver(instance, it));
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (firstFailure == null) {
            firstFailure = cause;
        }
        log.error("E2E test failed: {}", context.getUniqueId(), cause);
        String name = context.getRequiredTestClass().getSimpleName() + "-"
                + context.getRequiredTestMethod().getName();
        try {
            Files.write(record.resolve(name + ".png"), driver.getScreenshotAs(OutputType.BYTES));
            Files.write(record.resolve(name + ".html"), driver.getPageSource().getBytes(StandardCharsets.UTF_8));
            log.error("E2E failure screenshot and DOM saved: {}", record.resolve(name));
            WebElement focused = driver.switchTo().activeElement();
            log.error("E2E browser state: url={}, window={}, focusTag={}, focusClass={}",
                    driver.getCurrentUrl(), driver.manage().window().getSize(),
                    focused.getTagName(), focused.getDomAttribute("class"));
            for (WebElement modal : driver.findElements(By.cssSelector("[data-modal-ready]"))) {
                if (modal.isDisplayed()) {
                    log.error("E2E visible modal: class={}, ready={}, location={}, size={}",
                            modal.getDomAttribute("class"), modal.getDomAttribute("data-modal-ready"),
                            modal.getLocation(), modal.getSize());
                }
            }
            for (WebElement feedback : driver.findElements(By.cssSelector(".n-form-item-feedback-wrapper"))) {
                if (feedback.isDisplayed() && !feedback.getText().isEmpty()) {
                    log.error("E2E form feedback: {}", feedback.getText());
                }
            }
            for (WebElement editor : driver.findElements(By.cssSelector("[data-editor-model-version]"))) {
                log.error("E2E editor: version={}, characters={}",
                        editor.getDomAttribute("data-editor-model-version"),
                        editor.getDomAttribute("data-editor-content-length"));
            }
        } catch (Exception diagnosticFailure) {
            cause.addSuppressed(diagnosticFailure);
            log.error("Failed to capture E2E browser state for {}", context.getUniqueId(), diagnosticFailure);
        }
        try {
            String browserLogs = driver.manage().logs().get(LogType.BROWSER).getAll().toString();
            log.error("E2E browser console: {}", browserLogs);
            Files.write(record.resolve(name + "-browser.log"), browserLogs.getBytes(StandardCharsets.UTF_8));
        } catch (Exception diagnosticFailure) {
            cause.addSuppressed(diagnosticFailure);
            log.error("Failed to capture E2E browser console for {}", context.getUniqueId(), diagnosticFailure);
        }
    }

    private void setDriver(Object object, Field field) {
        try {
            field.setAccessible(true);
            field.set(object, driver);
        } catch (IllegalAccessException e) {
            log.error("Failed to inject web driver to field: {}", field.getName(), e);
        }
    }

    private ComposeContainer createDockerCompose(ExtensionContext context) {
        final Class<?> clazz = context.getRequiredTestClass();
        final DolphinScheduler annotation = clazz.getAnnotation(DolphinScheduler.class);
        final List<File> files = Stream.of(annotation.composeFiles())
                .map(it -> DolphinScheduler.class.getClassLoader().getResource(it))
                .filter(Objects::nonNull)
                .map(URL::getPath)
                .map(File::new)
                .collect(Collectors.toList());

        ComposeContainer compose = new ComposeContainer(files)
                .withPull(true)
                .withTailChildContainers(true)
                .withLocalCompose(true)
                .withExposedService(
                        serviceName,
                        DOCKER_PORT, Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(300)))
                .withLogConsumer(serviceName, outputFrame -> log.info(outputFrame.getUtf8String()))
                .waitingFor(serviceName, Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(300)));

        return compose;
    }
}

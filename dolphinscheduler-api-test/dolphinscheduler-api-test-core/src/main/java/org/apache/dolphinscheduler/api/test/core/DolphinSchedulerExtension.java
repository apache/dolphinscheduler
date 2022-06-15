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

package org.apache.dolphinscheduler.api.test.core;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DolphinSchedulerExtension implements BeforeAllCallback, AfterAllCallback {
    private final boolean LOCAL_MODE = Objects.equals(System.getProperty("local"), "true");

    private final String SERVICE_NAME = "dolphinscheduler_1";

    private DockerComposeContainer<?> compose;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!LOCAL_MODE) {
            compose = createDockerCompose(context);
            compose.start();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (compose != null) {
            compose.stop();
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
                .withLogConsumer(SERVICE_NAME, outputFrame -> LOGGER.info(outputFrame.getUtf8String()))
                .waitingFor(SERVICE_NAME, Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(Constants.DOCKER_COMPOSE_DEFAULT_TIMEOUT)));

        return compose;
    }
}

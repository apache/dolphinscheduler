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

package org.apache.dolphinscheduler.tools.datasource.jupiter;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class DolphinSchedulerDatabaseContainerExtension implements BeforeAllCallback, AfterAllCallback {

    private static GenericContainer<?> databaseContainer;

    @Override
    public void beforeAll(ExtensionContext context) {
        databaseContainer = getDataSourceContainer(context);
        log.info("Create {} successfully.", databaseContainer.getDockerImageName());
        databaseContainer.start();

        log.info("Starting {}...", databaseContainer.getDockerImageName());
        Startables.deepStart(Stream.of(databaseContainer)).join();
        log.info("{} started", databaseContainer.getDockerImageName());

    }

    private GenericContainer<?> getDataSourceContainer(ExtensionContext context) {
        Class<?> requiredTestClass = context.getRequiredTestClass();
        DolphinSchedulerDatabaseContainer annotation =
                requiredTestClass.getAnnotation(DolphinSchedulerDatabaseContainer.class);
        if (annotation == null) {
            throw new IllegalArgumentException("@DolphinSchedulerDataSourceContainer annotation not found");
        }
        Map<String, DatabaseContainerProvider> dataSourceContainerProviderMap = new HashMap<>();
        ServiceLoader.load(DatabaseContainerProvider.class)
                .forEach(databaseContainerProvider -> dataSourceContainerProviderMap
                        .put(databaseContainerProvider.getType(), databaseContainerProvider));

        DockerImageName dockerImageName = DockerImageName.parse(annotation.imageName());

        if (!dataSourceContainerProviderMap.containsKey(dockerImageName.getRepository())) {
            throw new IllegalArgumentException(
                    "DataSourceContainerProvider not found for type: " + annotation.imageName());
        }
        DatabaseContainerProvider databaseContainerProvider =
                dataSourceContainerProviderMap.get(dockerImageName.getRepository());
        return databaseContainerProvider.getContainer(annotation);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (databaseContainer != null) {
            databaseContainer.stop();
        }
    }
}

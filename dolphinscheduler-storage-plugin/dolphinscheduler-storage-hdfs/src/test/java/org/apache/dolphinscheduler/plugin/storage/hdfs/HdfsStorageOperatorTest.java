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

package org.apache.dolphinscheduler.plugin.storage.hdfs;

import static org.apache.dolphinscheduler.common.constants.Constants.FS_DEFAULT_FS;
import static org.apache.dolphinscheduler.common.constants.Constants.HDFS_ROOT_USER;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_UPLOAD_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.File;
import java.time.Duration;
import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import com.google.common.truth.Truth;

class HdfsStorageOperatorTest {

    private static HdfsStorageOperator storageOperator;

    private static ComposeContainer hdfsContainer;

    private static final String RESOURCE_BASE_DIRECTORY_ABSOLUTE_PATH = "hdfs://localhost/tmp/dolphinscheduler";

    @BeforeAll
    public static void setUp() throws InterruptedException {
        String hdfsDockerComposeFilePath =
                HdfsStorageOperatorTest.class.getResource("/hadoop-docker-compose/docker-compose.yaml").getFile();
        hdfsContainer = new ComposeContainer(new File(hdfsDockerComposeFilePath))
                .withPull(true)
                .withTailChildContainers(true)
                .withLocalCompose(true)
                .waitingFor("namenode", Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(60)))
                .waitingFor("datanode", Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(60)));

        Startables.deepStart(Stream.of(hdfsContainer)).join();

        System.setProperty(RESOURCE_UPLOAD_PATH, "/tmp/dolphinscheduler");
        System.setProperty(FS_DEFAULT_FS, "hdfs://localhost");
        System.setProperty(HDFS_ROOT_USER, "hadoop");

        HdfsStorageProperties hdfsStorageProperties = new HdfsStorageProperties();
        // The default replication factor is 3, which is too large for the test environment.
        // So we set it to 1.
        hdfsStorageProperties.setConfigurationProperties(ImmutableMap.of("dfs.replication", "1"));
        storageOperator = new HdfsStorageOperator(hdfsStorageProperties);
    }

    @BeforeEach
    public void initializeStorageFile() {
        storageOperator.delete("hdfs://localhost/tmp/dolphinscheduler/test-default", true);
        storageOperator.createStorageDir("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/empty");
        storageOperator.createStorageDir("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sql");
    }

    @Test
    public void testGetResourceMetaData() {
        ResourceMetadata resourceMetaData =
                storageOperator.getResourceMetaData(
                        "hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sqlDirectory/demo.sql");
        assertEquals("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sqlDirectory/demo.sql",
                resourceMetaData.getResourceAbsolutePath());
        assertEquals(RESOURCE_BASE_DIRECTORY_ABSOLUTE_PATH, resourceMetaData.getResourceBaseDirectory());
        assertEquals("test-default", resourceMetaData.getTenant());
        assertEquals(ResourceType.FILE, resourceMetaData.getResourceType());
        assertEquals("sqlDirectory/demo.sql", resourceMetaData.getResourceRelativePath());
        assertEquals("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sqlDirectory",
                resourceMetaData.getResourceParentAbsolutePath());
        assertFalse(resourceMetaData.isDirectory());
    }

    @Test
    public void testGetStorageBaseDirectory() {
        assertEquals(RESOURCE_BASE_DIRECTORY_ABSOLUTE_PATH, storageOperator.getStorageBaseDirectory());
    }

    @Test
    public void testExist_DirectoryExist() {
        Truth.assertThat(storageOperator.exists("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sql"))
                .isTrue();
    }

    @Test
    public void testExist_DirectoryNotExist() {
        Truth.assertThat(
                storageOperator.exists("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/notExist"))
                .isFalse();
    }

    @SneakyThrows
    @AfterAll
    public static void tearDown() {
        if (storageOperator != null) {
            storageOperator.close();
        }
        if (hdfsContainer != null) {
            hdfsContainer.stop();
        }
    }

}

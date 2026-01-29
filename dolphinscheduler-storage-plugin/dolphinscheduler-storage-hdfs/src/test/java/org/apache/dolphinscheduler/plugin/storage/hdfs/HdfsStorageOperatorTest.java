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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.time.Duration;
import java.util.List;
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

class HdfsStorageOperatorTest {

    private static HdfsStorageOperator storageOperator;

    private static ComposeContainer hdfsContainer;

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

        HdfsStorageProperties hdfsStorageProperties = HdfsStorageProperties.builder()
                .resourceUploadPath("/tmp/dolphinscheduler")
                .user("hadoop")
                .defaultFS("hdfs://localhost")
                // The default replication factor is 3, which is too large for the test environment.
                // So we set it to 1.
                .configurationProperties(ImmutableMap.of("dfs.replication", "1"))
                .build();
        storageOperator = new HdfsStorageOperator(hdfsStorageProperties);
    }

    @BeforeEach
    public void initializeStorageFile() {
        storageOperator.delete("hdfs://localhost/tmp/dolphinscheduler/test-default", true);
        storageOperator.createStorageDir("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/empty");
        storageOperator.createStorageDir("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sql");
        // todo: upload file and add file case
    }

    @Test
    public void testGetResourceMetaData() {
        ResourceMetadata resourceMetaData =
                storageOperator.getResourceMetaData(
                        "hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sqlDirectory/demo.sql");
        assertEquals("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sqlDirectory/demo.sql",
                resourceMetaData.getResourceAbsolutePath());
        assertEquals("hdfs://localhost/tmp/dolphinscheduler", resourceMetaData.getResourceBaseDirectory());
        assertEquals("test-default", resourceMetaData.getTenant());
        assertEquals(ResourceType.FILE, resourceMetaData.getResourceType());
        assertEquals("sqlDirectory/demo.sql", resourceMetaData.getResourceRelativePath());
        assertEquals("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sqlDirectory",
                resourceMetaData.getResourceParentAbsolutePath());
        assertFalse(resourceMetaData.isDirectory());
    }

    @Test
    public void testGetStorageBaseDirectory() {
        assertEquals("hdfs://localhost/tmp/dolphinscheduler", storageOperator.getStorageBaseDirectory());
    }

    @Test
    public void testGetStorageBaseDirectory_withTenantCode() {
        assertEquals("hdfs://localhost/tmp/dolphinscheduler/default",
                storageOperator.getStorageBaseDirectory("default"));
    }

    @Test
    public void testGetStorageBaseDirectory_withTenantCode_withFile() {
        assertEquals("hdfs://localhost/tmp/dolphinscheduler/default/resources",
                storageOperator.getStorageBaseDirectory("default", ResourceType.FILE));
    }

    @Test
    public void testGetStorageBaseDirectory_withTenantCode_withAll() {
        assertEquals("hdfs://localhost/tmp/dolphinscheduler/default",
                storageOperator.getStorageBaseDirectory("default", ResourceType.ALL));
    }

    @Test
    public void testGetStorageFileAbsolutePath() {
        assertEquals("hdfs://localhost/tmp/dolphinscheduler/default/resources/a.sql",
                storageOperator.getStorageFileAbsolutePath("default", "a.sql"));
    }

    @Test
    public void testCreateStorageDir_notExist() {
        storageOperator.createStorageDir("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/newDirectory");
        storageOperator.exists("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/newDirectory");
    }

    @Test
    public void testCreateStorageDir_exist() {
        assertThrows(FileAlreadyExistsException.class,
                () -> storageOperator
                        .createStorageDir("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/empty"));
    }

    @Test
    public void testExist_DirectoryExist() {
        assertThat(storageOperator.exists("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sql"))
                .isTrue();
    }

    @Test
    public void testExist_DirectoryNotExist() {
        assertThat(
                storageOperator.exists("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/notExist"))
                        .isFalse();
    }

    @Test
    public void testDelete_directoryExist() {
        storageOperator.delete("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sql", true);
        assertThat(storageOperator.exists("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sql"))
                .isFalse();
    }

    @Test
    public void testDelete_directoryNotExist() {
        storageOperator.delete("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/non", true);
        assertThat(storageOperator.exists("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/non"))
                .isFalse();
    }

    @Test
    public void testListStorageEntity_directory() {
        List<StorageEntity> storageEntities =
                storageOperator.listStorageEntity("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/");
        assertThat(storageEntities).hasSize(2);
    }

    @Test
    public void testGetStorageEntity_directory() {
        StorageEntity storageEntity =
                storageOperator.getStorageEntity("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sql");
        assertThat(storageEntity.getFullName())
                .isEqualTo("hdfs://localhost/tmp/dolphinscheduler/test-default/resources/sql");
        assertThat(storageEntity.isDirectory()).isTrue();
        assertThat(storageEntity.getPfullName())
                .isEqualTo("hdfs://localhost/tmp/dolphinscheduler/test-default/resources");
        assertThat(storageEntity.getType()).isEqualTo(ResourceType.FILE);
        assertThat(storageEntity.getFileName()).isEqualTo("sql");
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

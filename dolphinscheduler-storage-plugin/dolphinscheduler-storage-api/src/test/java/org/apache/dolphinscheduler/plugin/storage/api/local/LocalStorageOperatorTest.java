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

package org.apache.dolphinscheduler.plugin.storage.api.local;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.storage.api.constants.StorageConstants;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalStorageOperatorTest {

    private StorageOperator storageOperator;

    private static final String STORAGE_BASE_DIR =
            Paths.get(LocalStorageOperatorTest.class.getResource("/").getFile(), "localStorage").toString();
    private static final String TENANT_CODE = "default";
    private static final String TENANT_BASE_DIR =
            Paths.get(STORAGE_BASE_DIR, TENANT_CODE, StorageOperator.FILE_FOLDER_NAME).toString();

    @SneakyThrows
    @BeforeEach
    void setup() {
        // /localStorage/default/resources/sqlDirectory/demo.sql
        // /emptyDirectory
        Files.createDirectories(Paths.get(STORAGE_BASE_DIR));
        System.clearProperty(StorageConstants.RESOURCE_UPLOAD_PATH);
        System.setProperty(StorageConstants.RESOURCE_UPLOAD_PATH, STORAGE_BASE_DIR);

        LocalStorageOperatorFactory localStorageOperatorFactory = new LocalStorageOperatorFactory();
        storageOperator = localStorageOperatorFactory.createStorageOperate();
        // create file and directory
        Files.createDirectories(Paths.get(TENANT_BASE_DIR, "sqlDirectory"));
        Files.createDirectories(Paths.get(TENANT_BASE_DIR, "emptyDirectory"));
        Files.createFile(Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql"));
        Files.write(Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql"), "select * from demo".getBytes());

    }

    @Test
    void testGetResourceMetaData_directory() {
        ResourceMetadata resourceMetaData = storageOperator.getResourceMetaData(TENANT_BASE_DIR);
        assertThat(resourceMetaData.getResourceAbsolutePath()).isEqualTo(TENANT_BASE_DIR);
        assertThat(resourceMetaData.getResourceBaseDirectory()).isEqualTo(STORAGE_BASE_DIR);
        assertThat(resourceMetaData.getTenant()).isEqualTo("default");
        assertThat(resourceMetaData.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(resourceMetaData.getResourceRelativePath()).isEqualTo("/");
    }

    @Test
    void testGetResourceMetaData_file() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql").toString();

        ResourceMetadata resourceMetaData = storageOperator.getResourceMetaData(resourceFileAbsolutePath);
        assertThat(resourceMetaData.getResourceAbsolutePath()).isEqualTo(resourceFileAbsolutePath);
        assertThat(resourceMetaData.getResourceBaseDirectory()).isEqualTo(STORAGE_BASE_DIR);
        assertThat(resourceMetaData.getTenant()).isEqualTo("default");
        assertThat(resourceMetaData.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(resourceMetaData.getResourceRelativePath()).isEqualTo("sqlDirectory/demo.sql");
    }

    @Test
    void testGetResourceMetaData_invalidatedPath() {
        String resourceFileAbsolutePath = Paths.get("/", "sqlDirectory", "demo.sql").toString();

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> storageOperator.getResourceMetaData(resourceFileAbsolutePath));
        assertThat(illegalArgumentException.getMessage())
                .isEqualTo("Invalid resource path: " + resourceFileAbsolutePath);
    }

    @Test
    void testGetStorageBaseDirectory() {
        String storageBaseDirectory = storageOperator.getStorageBaseDirectory();
        assertThat(storageBaseDirectory).isEqualTo(STORAGE_BASE_DIR);
    }

    @Test
    void testGetStorageBaseDirectory_withTenant() {
        String storageBaseDirectory = storageOperator.getStorageBaseDirectory("default");
        assertThat(storageBaseDirectory).isEqualTo(Paths.get(STORAGE_BASE_DIR, TENANT_CODE).toString());
    }

    @Test
    void testGetStorageBaseDirectory_withTenant_withResourceTypeFile() {
        String storageBaseDirectory = storageOperator.getStorageBaseDirectory("default", ResourceType.FILE);
        assertThat(storageBaseDirectory)
                .isEqualTo(Paths.get(STORAGE_BASE_DIR, TENANT_CODE, StorageOperator.FILE_FOLDER_NAME).toString());
    }

    @Test
    void testGetStorageBaseDirectory_withTenant_withResourceTypeAll() {
        String storageBaseDirectory = storageOperator.getStorageBaseDirectory("default", ResourceType.ALL);
        assertThat(storageBaseDirectory).isEqualTo(Paths.get(STORAGE_BASE_DIR, TENANT_CODE).toString());
    }

    @Test
    void testGetStorageBaseDirectory_withEmptyTenant_withResourceType() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> storageOperator.getStorageBaseDirectory("", ResourceType.ALL));
        assertThat(illegalArgumentException.getMessage()).isEqualTo("Tenant code should not be empty");
    }

    @Test
    void testGetStorageBaseDirectory_withTenant_withEmptyResourceType() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> storageOperator.getStorageBaseDirectory("default", null));
        assertThat(illegalArgumentException.getMessage()).isEqualTo("Resource type should not be null");
    }

    @Test
    void testGetStorageFileAbsolutePath() {
        String fileAbsolutePath = storageOperator.getStorageFileAbsolutePath("default", "test.sh");
        assertThat(fileAbsolutePath).isEqualTo(
                Paths.get(STORAGE_BASE_DIR, TENANT_CODE, StorageOperator.FILE_FOLDER_NAME, "test.sh").toString());
    }

    @SneakyThrows
    @Test
    void testCreateStorageDir_notExists() {
        String testDirFileAbsolutePath =
                Paths.get(STORAGE_BASE_DIR, "root", StorageOperator.FILE_FOLDER_NAME, "testDir").toString();
        try {
            storageOperator.createStorageDir(testDirFileAbsolutePath);
            StorageEntity storageEntity = storageOperator.getStorageEntity(testDirFileAbsolutePath);
            assertThat(storageEntity.getFullName()).isEqualTo(testDirFileAbsolutePath);
            assertThat(storageEntity.getFileName()).isEqualTo("testDir");
            assertThat(storageEntity.getPfullName())
                    .isEqualTo(Paths.get(STORAGE_BASE_DIR, "root", StorageOperator.FILE_FOLDER_NAME).toString());
            assertThat(storageEntity.isDirectory()).isTrue();
            assertThat(storageEntity.getType()).isEqualTo(ResourceType.FILE);
        } finally {
            storageOperator.delete(testDirFileAbsolutePath, true);
        }
    }

    @SneakyThrows
    @Test
    void testCreateStorageDir_exists() {
        String testDirFileAbsolutePath =
                Paths.get(STORAGE_BASE_DIR, "default", StorageOperator.FILE_FOLDER_NAME, "sqlDirectory").toString();
        assertThrows(FileAlreadyExistsException.class, () -> storageOperator.createStorageDir(testDirFileAbsolutePath));
    }

    @Test
    void testExists_fileExist() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql").toString();
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isTrue();
    }

    @Test
    void testExists_fileNotExist() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sh").toString();
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    void testExists_directoryExist() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory").toString();
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isTrue();
    }

    @Test
    void testExists_directoryNotExist() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "shellDirectory").toString();
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    void testDelete_directoryExist() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory").toString();
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isTrue();

        storageOperator.delete(resourceFileAbsolutePath, true);
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    void testDelete_directoryNotExist() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "shellDirectory").toString();
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();

        storageOperator.delete(resourceFileAbsolutePath, true);
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    void testDelete_fileExist() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql").toString();
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isTrue();

        storageOperator.delete(resourceFileAbsolutePath, true);
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    void testDelete_fileNotExist() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sh").toString();
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();

        storageOperator.delete(resourceFileAbsolutePath, true);
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    void testFetchFileContent() {
        // todo: add large file test case
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql").toString();
        List<String> content = storageOperator.fetchFileContent(resourceFileAbsolutePath, 0, 10);
        assertThat(content).containsExactly("select * from demo");

    }

    @Test
    void testListStorageEntity_directoryNotEmpty() {
        List<StorageEntity> storageEntities = storageOperator.listStorageEntity(TENANT_BASE_DIR);
        assertThat(storageEntities.size()).isEqualTo(2);

        StorageEntity storageEntity1 = storageEntities.stream()
                .filter(v -> v.getFileName().contains("emptyDirectory"))
                .findFirst()
                .orElse(null);
        assertThat(storageEntity1.getFullName()).isEqualTo(TENANT_BASE_DIR + "/emptyDirectory");
        assertThat(storageEntity1.getFileName()).isEqualTo("emptyDirectory");
        assertThat(storageEntity1.getPfullName()).isEqualTo(TENANT_BASE_DIR);
        assertThat(storageEntity1.isDirectory()).isTrue();
        assertThat(storageEntity1.getType()).isEqualTo(ResourceType.FILE);

        StorageEntity storageEntity2 = storageEntities.stream()
                .filter(v -> v.getFileName().contains("sqlDirectory"))
                .findFirst()
                .orElse(null);
        assertThat(storageEntity2.getFullName()).isEqualTo(TENANT_BASE_DIR + "/sqlDirectory");
        assertThat(storageEntity2.getFileName()).isEqualTo("sqlDirectory");
        assertThat(storageEntity2.getPfullName()).isEqualTo(TENANT_BASE_DIR);
        assertThat(storageEntity2.isDirectory()).isTrue();
        assertThat(storageEntity2.getType()).isEqualTo(ResourceType.FILE);
    }

    @Test
    void testListStorageEntity_directoryEmpty() {
        String resourceFileAbsolutePath = TENANT_BASE_DIR + "/emptyDirectory";
        List<StorageEntity> storageEntities = storageOperator.listStorageEntity(resourceFileAbsolutePath);
        assertThat(storageEntities.size()).isEqualTo(0);
    }

    @Test
    void testListStorageEntity_directoryNotExist() {
        String resourceFileAbsolutePath = TENANT_BASE_DIR + "/notExistDirectory";
        assertThat(storageOperator.listStorageEntity(resourceFileAbsolutePath)).isEmpty();
    }

    @Test
    void testListStorageEntity_file() {
        String resourceFileAbsolutePath = Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql").toString();
        List<StorageEntity> storageEntities = storageOperator.listStorageEntity(resourceFileAbsolutePath);
        assertThat(storageEntities.size()).isEqualTo(1);

        StorageEntity storageEntity = storageEntities.get(0);
        assertThat(storageEntity.getFullName())
                .isEqualTo(Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql").toString());
        assertThat(storageEntity.getFileName()).isEqualTo("demo.sql");
        assertThat(storageEntity.getPfullName()).isEqualTo(Paths.get(TENANT_BASE_DIR, "sqlDirectory").toString());
        assertThat(storageEntity.isDirectory()).isFalse();
        assertThat(storageEntity.getType()).isEqualTo(ResourceType.FILE);

    }

    @Test
    void testListStorageEntityRecursively_directory() {
        List<StorageEntity> storageEntities = storageOperator.listFileStorageEntityRecursively(TENANT_BASE_DIR);
        assertThat(storageEntities.size()).isEqualTo(3);

        StorageEntity storageEntity2 = storageEntities.stream()
                .filter(storageEntity -> storageEntity.getFileName().equals("demo.sql"))
                .findFirst()
                .get();
        assertThat(storageEntity2.getFullName())
                .isEqualTo(Paths.get(TENANT_BASE_DIR, "sqlDirectory", "demo.sql").toString());
        assertThat(storageEntity2.getFileName()).isEqualTo("demo.sql");
        assertThat(storageEntity2.getPfullName()).isEqualTo(Paths.get(TENANT_BASE_DIR, "sqlDirectory").toString());
        assertThat(storageEntity2.isDirectory()).isFalse();
        assertThat(storageEntity2.getType()).isEqualTo(ResourceType.FILE);
    }

    @SneakyThrows
    @AfterEach
    void after() {
        FileUtils.deleteFile(STORAGE_BASE_DIR);
    }

}

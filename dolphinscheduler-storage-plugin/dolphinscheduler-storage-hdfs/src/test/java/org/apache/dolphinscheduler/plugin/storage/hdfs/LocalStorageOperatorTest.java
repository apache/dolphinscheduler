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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.common.constants.Constants;
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

    private static final String resourceBaseDir =
            Paths.get(LocalStorageOperatorTest.class.getResource("/").getFile(), "localStorage").toString();
    private static final String tenantCode = "default";
    private static final String baseDir =
            Paths.get(resourceBaseDir, tenantCode, StorageConstants.RESOURCE_TYPE_FILE).toString();

    @SneakyThrows
    @BeforeEach
    public void setup() {
        Files.createDirectories(Paths.get(resourceBaseDir));
        System.clearProperty(StorageConstants.RESOURCE_UPLOAD_PATH);
        System.setProperty(StorageConstants.RESOURCE_UPLOAD_PATH, resourceBaseDir);

        LocalStorageOperatorFactory localStorageOperatorFactory = new LocalStorageOperatorFactory();
        storageOperator = localStorageOperatorFactory.createStorageOperate();
        // create file and directory
        Files.createDirectories(Paths.get(baseDir, "sqlDirectory"));
        Files.createDirectories(Paths.get(baseDir, "emptyDirectory"));
        Files.createFile(Paths.get(baseDir, "sqlDirectory", "demo.sql"));
        Files.write(Paths.get(baseDir, "sqlDirectory", "demo.sql"), "select * from demo".getBytes());

    }

    @Test
    public void testGetResourceMetaData_directory() {
        String resourceFileAbsolutePath = "file:" + baseDir;

        ResourceMetadata resourceMetaData = storageOperator.getResourceMetaData(resourceFileAbsolutePath);
        assertThat(resourceMetaData.getResourceAbsolutePath()).isEqualTo("file:" + baseDir);
        assertThat(resourceMetaData.getResourceBaseDirectory()).isEqualTo("file:" + resourceBaseDir);
        assertThat(resourceMetaData.getTenant()).isEqualTo("default");
        assertThat(resourceMetaData.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(resourceMetaData.getResourceRelativePath()).isEqualTo("/");
    }

    @Test
    public void testGetResourceMetaData_file() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory", "demo.sql");

        ResourceMetadata resourceMetaData = storageOperator.getResourceMetaData(resourceFileAbsolutePath);
        assertThat(resourceMetaData.getResourceAbsolutePath()).isEqualTo(resourceFileAbsolutePath);
        assertThat(resourceMetaData.getResourceBaseDirectory()).isEqualTo("file:" + resourceBaseDir);
        assertThat(resourceMetaData.getTenant()).isEqualTo("default");
        assertThat(resourceMetaData.getResourceType()).isEqualTo(ResourceType.FILE);
        assertThat(resourceMetaData.getResourceRelativePath()).isEqualTo("sqlDirectory/demo.sql");
    }

    @Test
    public void testGetResourceMetaData_invalidatedPath() {
        String resourceFileAbsolutePath = Paths.get(baseDir, "sqlDirectory", "demo.sql").toString();

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> storageOperator.getResourceMetaData(resourceFileAbsolutePath));
        assertThat(illegalArgumentException.getMessage())
                .isEqualTo("Invalid resource path: " + resourceFileAbsolutePath);
    }

    @Test
    public void testGetStorageBaseDirectory() {
        String storageBaseDirectory = storageOperator.getStorageBaseDirectory();
        assertThat(storageBaseDirectory).isEqualTo("file:" + resourceBaseDir);
    }

    @Test
    public void testGetStorageBaseDirectory_withTenant() {
        String storageBaseDirectory = storageOperator.getStorageBaseDirectory("default");
        assertThat(storageBaseDirectory).isEqualTo("file:" + Paths.get(resourceBaseDir, tenantCode));
    }

    @Test
    public void testGetStorageBaseDirectory_withTenant_withResourceTypeFile() {
        String storageBaseDirectory = storageOperator.getStorageBaseDirectory("default", ResourceType.FILE);
        assertThat(storageBaseDirectory)
                .isEqualTo("file:" + Paths.get(resourceBaseDir, tenantCode, Constants.RESOURCE_TYPE_FILE));
    }

    @Test
    public void testGetStorageBaseDirectory_withTenant_withResourceTypeAll() {
        String storageBaseDirectory = storageOperator.getStorageBaseDirectory("default", ResourceType.ALL);
        assertThat(storageBaseDirectory).isEqualTo("file:" + Paths.get(resourceBaseDir, tenantCode));
    }

    @Test
    public void testGetStorageBaseDirectory_withEmptyTenant_withResourceType() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> storageOperator.getStorageBaseDirectory("", ResourceType.ALL));
        assertThat(illegalArgumentException.getMessage()).isEqualTo("Tenant code should not be empty");
    }

    @Test
    public void testGetStorageBaseDirectory_withTenant_withEmptyResourceType() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> storageOperator.getStorageBaseDirectory("default", null));
        assertThat(illegalArgumentException.getMessage()).isEqualTo("Resource type should not be null");
    }

    @Test
    public void testGetStorageFileAbsolutePath() {
        String fileAbsolutePath = storageOperator.getStorageFileAbsolutePath("default", "test.sh");
        assertThat(fileAbsolutePath).isEqualTo(
                "file:" + Paths.get(resourceBaseDir, tenantCode, Constants.RESOURCE_TYPE_FILE, "test.sh"));
    }

    @SneakyThrows
    @Test
    public void testCreateStorageDir_notExists() {
        String testDirFileAbsolutePath =
                "file:" + Paths.get(resourceBaseDir, "root", Constants.RESOURCE_TYPE_FILE, "testDir");
        try {
            storageOperator.createStorageDir(testDirFileAbsolutePath);
            StorageEntity storageEntity = storageOperator.getStorageEntity(testDirFileAbsolutePath);
            assertThat(storageEntity.getFullName()).isEqualTo(testDirFileAbsolutePath);
            assertThat(storageEntity.getFileName()).isEqualTo("testDir");
            assertThat(storageEntity.getPfullName())
                    .isEqualTo("file:" + Paths.get(resourceBaseDir, "root", Constants.RESOURCE_TYPE_FILE));
            assertThat(storageEntity.isDirectory()).isTrue();
            assertThat(storageEntity.getType()).isEqualTo(ResourceType.FILE);
        } finally {
            storageOperator.delete(testDirFileAbsolutePath, true);
        }
    }

    @SneakyThrows
    @Test
    public void testCreateStorageDir_exists() {
        String testDirFileAbsolutePath =
                "file:" + Paths.get(resourceBaseDir, "default", Constants.RESOURCE_TYPE_FILE, "sqlDirectory");
        assertThrows(FileAlreadyExistsException.class, () -> storageOperator.createStorageDir(testDirFileAbsolutePath));
    }

    @Test
    public void testExists_fileExist() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory", "demo.sql");
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isTrue();
    }

    @Test
    public void testExists_fileNotExist() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory", "demo.sh");
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    public void testExists_directoryExist() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory");
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isTrue();
    }

    @Test
    public void testExists_directoryNotExist() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "shellDirectory");
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    public void testDelete_directoryExist() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory");
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isTrue();

        storageOperator.delete(resourceFileAbsolutePath, true);
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    public void testDelete_directoryNotExist() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "shellDirectory");
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();

        storageOperator.delete(resourceFileAbsolutePath, true);
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    public void testDelete_fileExist() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory", "demo.sql");
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isTrue();

        storageOperator.delete(resourceFileAbsolutePath, true);
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    public void testDelete_fileNotExist() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory", "demo.sh");
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();

        storageOperator.delete(resourceFileAbsolutePath, true);
        assertThat(storageOperator.exists(resourceFileAbsolutePath)).isFalse();
    }

    @Test
    public void testFetchFileContent() {
        // todo: add large file test case
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory", "demo.sql");
        List<String> content = storageOperator.fetchFileContent(resourceFileAbsolutePath, 0, 10);
        assertThat(content).containsExactly("select * from demo");

    }

    @Test
    public void testListStorageEntity_directoryNotEmpty() {
        String resourceFileAbsolutePath = "file:" + baseDir;
        List<StorageEntity> storageEntities = storageOperator.listStorageEntity(resourceFileAbsolutePath);
        assertThat(storageEntities.size()).isEqualTo(2);

        StorageEntity storageEntity1 = storageEntities.get(0);
        assertThat(storageEntity1.getFullName()).isEqualTo("file:" + baseDir + "/emptyDirectory");
        assertThat(storageEntity1.getFileName()).isEqualTo("emptyDirectory");
        assertThat(storageEntity1.getPfullName()).isEqualTo("file:" + baseDir);
        assertThat(storageEntity1.isDirectory()).isTrue();
        assertThat(storageEntity1.getType()).isEqualTo(ResourceType.FILE);

        StorageEntity storageEntity2 = storageEntities.get(1);
        assertThat(storageEntity2.getFullName()).isEqualTo("file:" + baseDir + "/sqlDirectory");
        assertThat(storageEntity2.getFileName()).isEqualTo("sqlDirectory");
        assertThat(storageEntity2.getPfullName()).isEqualTo("file:" + baseDir);
        assertThat(storageEntity2.isDirectory()).isTrue();
        assertThat(storageEntity2.getType()).isEqualTo(ResourceType.FILE);
    }

    @Test
    public void testListStorageEntity_directoryEmpty() {
        String resourceFileAbsolutePath = "file:" + baseDir + "/emptyDirectory";
        List<StorageEntity> storageEntities = storageOperator.listStorageEntity(resourceFileAbsolutePath);
        assertThat(storageEntities.size()).isEqualTo(0);
    }

    @Test
    public void testListStorageEntity_directoryNotExist() {
        String resourceFileAbsolutePath = "file:" + baseDir + "/notExistDirectory";
        assertThat(storageOperator.listStorageEntity(resourceFileAbsolutePath)).isEmpty();
    }

    @Test
    public void testListStorageEntity_file() {
        String resourceFileAbsolutePath = "file:" + Paths.get(baseDir, "sqlDirectory", "demo.sql");
        List<StorageEntity> storageEntities = storageOperator.listStorageEntity(resourceFileAbsolutePath);
        assertThat(storageEntities.size()).isEqualTo(1);

        StorageEntity storageEntity = storageEntities.get(0);
        assertThat(storageEntity.getFullName()).isEqualTo("file:" + Paths.get(baseDir, "sqlDirectory", "demo.sql"));
        assertThat(storageEntity.getFileName()).isEqualTo("demo.sql");
        assertThat(storageEntity.getPfullName()).isEqualTo("file:" + Paths.get(baseDir, "sqlDirectory"));
        assertThat(storageEntity.isDirectory()).isFalse();
        assertThat(storageEntity.getType()).isEqualTo(ResourceType.FILE);

    }

    @Test
    public void testListStorageEntityRecursively_directory() {
        String resourceFileAbsolutePath = "file:" + baseDir;
        List<StorageEntity> storageEntities =
                storageOperator.listFileStorageEntityRecursively(resourceFileAbsolutePath);
        assertThat(storageEntities.size()).isEqualTo(3);

        StorageEntity storageEntity2 = storageEntities.stream()
                .filter(storageEntity -> storageEntity.getFileName().equals("demo.sql"))
                .findFirst()
                .get();
        assertThat(storageEntity2.getFullName())
                .isEqualTo("file:" + Paths.get(baseDir, "sqlDirectory", "demo.sql"));
        assertThat(storageEntity2.getFileName()).isEqualTo("demo.sql");
        assertThat(storageEntity2.getPfullName()).isEqualTo("file:" + Paths.get(baseDir, "sqlDirectory"));
        assertThat(storageEntity2.isDirectory()).isFalse();
        assertThat(storageEntity2.getType()).isEqualTo(ResourceType.FILE);
    }

    @SneakyThrows
    @AfterEach
    public void after() {
        FileUtils.deleteFile(resourceBaseDir);
    }

}

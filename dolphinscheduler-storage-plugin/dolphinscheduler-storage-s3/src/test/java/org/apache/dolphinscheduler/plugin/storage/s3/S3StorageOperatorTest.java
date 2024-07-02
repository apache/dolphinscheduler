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

package org.apache.dolphinscheduler.plugin.storage.s3;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3StorageOperatorTest {

    private static final String demoSql = S3StorageOperatorTest.class.getResource("/demo.sql").getFile();

    private static MinIOContainer minIOContainer;

    private static S3StorageOperator s3StorageOperator;

    @BeforeAll
    public static void setUp() throws Exception {
        String bucketName = "dolphinscheduler";
        String accessKey = "accessKey123";
        String secretKey = "secretKey123";
        String region = "us-east-1";

        minIOContainer = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
                .withEnv("MINIO_ACCESS_KEY", accessKey)
                .withEnv("MINIO_SECRET_KEY", secretKey)
                .withEnv("MINIO_REGION", region)
                .withNetworkAliases(bucketName + "." + "localhost");

        Startables.deepStart(Stream.of(minIOContainer)).join();

        String endpoint = minIOContainer.getS3URL();

        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withPathStyleAccessEnabled(true)
                .build();
        amazonS3.createBucket(bucketName);

        S3StorageProperties s3StorageProperties = S3StorageProperties.builder()
                .bucketName(bucketName)
                .resourceUploadPath("tmp/dolphinscheduler")
                .s3Configuration(ImmutableMap.of(
                        "access.key.id", accessKey,
                        "access.key.secret", secretKey,
                        "region", region,
                        "endpoint", endpoint))
                .build();
        s3StorageOperator = new S3StorageOperator(s3StorageProperties);
    }

    @BeforeEach
    public void initializeFiles() {
        s3StorageOperator.delete("tmp/dolphinscheduler/default/resources", true);
        s3StorageOperator.createStorageDir("tmp/dolphinscheduler/default/resources/sqlDirectory");
        s3StorageOperator.createStorageDir("tmp/dolphinscheduler/default/resources/multipleDirectories");
        s3StorageOperator.createStorageDir("tmp/dolphinscheduler/default/resources/multipleDirectories/1");
        s3StorageOperator.createStorageDir("tmp/dolphinscheduler/default/resources/multipleDirectories/2");
        s3StorageOperator.createStorageDir("tmp/dolphinscheduler/default/resources/multipleDirectories/3");
        s3StorageOperator.upload(demoSql, "tmp/dolphinscheduler/default/resources/multipleDirectories/1/demo.sql",
                false, true);
        s3StorageOperator.createStorageDir("tmp/dolphinscheduler/default/resources/emptyDirectory");
        s3StorageOperator.upload(demoSql, "tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql", false, true);
    }

    @Test
    public void testGetResourceMetaData() {
        ResourceMetadata resourceMetaData =
                s3StorageOperator.getResourceMetaData("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql");
        assertEquals("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql",
                resourceMetaData.getResourceAbsolutePath());
        assertEquals("tmp/dolphinscheduler", resourceMetaData.getResourceBaseDirectory());
        assertEquals("default", resourceMetaData.getTenant());
        assertEquals(ResourceType.FILE, resourceMetaData.getResourceType());
        assertEquals("sqlDirectory/demo.sql", resourceMetaData.getResourceRelativePath());
        assertEquals("tmp/dolphinscheduler/default/resources/sqlDirectory",
                resourceMetaData.getResourceParentAbsolutePath());
        assertFalse(resourceMetaData.isDirectory());
    }

    @Test
    public void testGetStorageBaseDirectory() {
        assertEquals("tmp/dolphinscheduler", s3StorageOperator.getStorageBaseDirectory());
    }

    @Test
    public void testGetStorageBaseDirectory_withTenant() {
        assertEquals("tmp/dolphinscheduler/default", s3StorageOperator.getStorageBaseDirectory("default"));
    }

    @Test
    public void testGetStorageBaseDirectory_withTenant_withResourceTypeFile() {
        String storageBaseDirectory = s3StorageOperator.getStorageBaseDirectory("default", ResourceType.FILE);
        assertThat(storageBaseDirectory).isEqualTo("tmp/dolphinscheduler/default/resources");
    }

    @Test
    public void testGetStorageBaseDirectory_withTenant_withResourceTypeAll() {
        String storageBaseDirectory = s3StorageOperator.getStorageBaseDirectory("default", ResourceType.ALL);
        assertThat(storageBaseDirectory).isEqualTo("tmp/dolphinscheduler/default");
    }

    @Test
    public void testGetStorageFileAbsolutePath() {
        assertThat(s3StorageOperator.getStorageFileAbsolutePath("default", "demo.sql"))
                .isEqualTo("tmp/dolphinscheduler/default/resources/demo.sql");
    }

    @Test
    public void testCreateStorageDir_notExist() {
        String dirName = "tmp/dolphinscheduler/default/resources/testDirectory";
        s3StorageOperator.createStorageDir(dirName);
        assertTrue(s3StorageOperator.exists(dirName));

    }

    @Test
    public void testCreateStorageDir_exist() {
        final String dirName = "tmp/dolphinscheduler/default/resources/emptyDirectory";
        Assertions.assertThrows(FileAlreadyExistsException.class, () -> s3StorageOperator.createStorageDir(dirName));
    }

    @Test
    public void testExists_fileExist() {
        assertTrue(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql"));
    }

    @Test
    public void testExists_fileNotExist() {
        assertFalse(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/sqlDirectory/notExist.sql"));
    }

    @Test
    public void testExists_directoryExist() {
        assertTrue(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/sqlDirectory"));
    }

    @Test
    public void testExists_directoryNotExist() {
        assertFalse(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/notExistDirectory"));
    }

    @Test
    public void delete_fileExist() {
        s3StorageOperator.delete("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql", true);
        assertFalse(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql"));
    }

    @Test
    public void delete_fileNotExist() {
        s3StorageOperator.delete("tmp/dolphinscheduler/default/resources/sqlDirectory/notExist.sql", true);
        assertFalse(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/sqlDirectory/notExist.sql"));
    }

    @Test
    public void delete_directoryExist() {
        s3StorageOperator.delete("tmp/dolphinscheduler/default/resources/sqlDirectory", true);
        assertFalse(s3StorageOperator.exists("/tmp/dolphinscheduler/default/resources/sqlDirectory"));
    }

    @Test
    public void delete_directoryNotExist() {
        s3StorageOperator.delete("tmp/dolphinscheduler/default/resources/notExist", true);
        assertFalse(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/notExist"));
    }

    @Test
    public void copy_file() {
        s3StorageOperator.copy("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql",
                "tmp/dolphinscheduler/default/resources/sqlDirectory/demo_copy.sql", true, true);
        assertTrue(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/sqlDirectory/demo_copy.sql"));
        assertFalse(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql"));
    }

    @Test
    public void copy_directory() {
        assertThrows(UnsupportedOperationException.class,
                () -> s3StorageOperator.copy("tmp/dolphinscheduler/default/resources/sqlDirectory",
                        "tmp/dolphinscheduler/default/resources/sqlDirectory_copy", true, true));
    }

    @Test
    public void testUpload_file() {
        String file = S3StorageOperatorTest.class.getResource("/student.sql").getFile();
        s3StorageOperator.upload(file, "tmp/dolphinscheduler/default/resources/sqlDirectory/student.sql", false, true);
        assertTrue(s3StorageOperator.exists("tmp/dolphinscheduler/default/resources/sqlDirectory/student.sql"));
    }

    @Test
    public void testFetchFileContent() {
        List<String> strings = s3StorageOperator
                .fetchFileContent("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql", 0, 2);
        assertThat(strings).hasSize(2);
    }

    @Test
    public void testListStorageEntity_file() {
        List<StorageEntity> storageEntities =
                s3StorageOperator.listStorageEntity("tmp/dolphinscheduler/default/resources/sqlDirectory");
        assertThat(storageEntities).hasSize(1);

        StorageEntity storageEntity = storageEntities.get(0);
        assertThat(storageEntity.getFullName())
                .isEqualTo("tmp/dolphinscheduler/default/resources/sqlDirectory/demo.sql");
        assertThat(storageEntity.getFileName())
                .isEqualTo("demo.sql");
        assertThat(storageEntity.isDirectory()).isFalse();
        assertThat(storageEntity.getPfullName()).isEqualTo("tmp/dolphinscheduler/default/resources/sqlDirectory");
        assertThat(storageEntity.getType()).isEqualTo(ResourceType.FILE);
    }

    @Test
    public void testListStorageEntity_directory() {
        List<StorageEntity> storageEntities =
                s3StorageOperator.listStorageEntity("tmp/dolphinscheduler/default/resources");
        assertThat(storageEntities).hasSize(3);

    }

    @Test
    public void testListStorageEntity_directoryNotExist() {
        List<StorageEntity> storageEntities =
                s3StorageOperator.listStorageEntity("tmp/dolphinscheduler/notExist/resources");
        assertThat(storageEntities).isEmpty();

    }

    @Test
    public void testListStorageEntityRecursively() {
        List<StorageEntity> storageEntities =
                s3StorageOperator
                        .listFileStorageEntityRecursively("tmp/dolphinscheduler/default/resources/multipleDirectories");
        assertThat(storageEntities).hasSize(1);

        StorageEntity storageEntity = storageEntities.get(0);
        assertThat(storageEntity.getFullName())
                .isEqualTo("tmp/dolphinscheduler/default/resources/multipleDirectories/1/demo.sql");
        assertThat(storageEntity.getFileName())
                .isEqualTo("demo.sql");
        assertThat(storageEntity.isDirectory()).isFalse();
        assertThat(storageEntity.getPfullName())
                .isEqualTo("tmp/dolphinscheduler/default/resources/multipleDirectories/1");
        assertThat(storageEntity.getType()).isEqualTo(ResourceType.FILE);

    }

    @Test
    public void testExceptionWhenBucketNameNotExists() {
        Assertions.assertDoesNotThrow(() -> s3StorageOperator.exceptionWhenBucketNameNotExists("dolphinscheduler"));
    }

    @SneakyThrows
    @AfterAll
    public static void tearDown() {
        if (s3StorageOperator != null) {
            s3StorageOperator.close();
        }
        if (minIOContainer != null) {
            minIOContainer.stop();
        }
    }

}

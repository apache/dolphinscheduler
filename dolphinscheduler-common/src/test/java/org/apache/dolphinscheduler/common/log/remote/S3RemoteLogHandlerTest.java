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

package org.apache.dolphinscheduler.common.log.remote;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.authentication.aws.AmazonS3ClientFactory;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * Download contract of a real remote handler (S3 is the one that copies from a raw object
 * stream): a transfer failing mid-way propagates and is NEVER published as the local archive —
 * not even when a previously downloaded archive is already cached at that path.
 */
@ExtendWith(MockitoExtension.class)
public class S3RemoteLogHandlerTest {

    private static final String BUCKET = "bucket";
    private static final String OBJECT_NAME = "logs/20261003/1/1.log";

    @Mock
    private AmazonS3 s3Client;

    @BeforeEach
    public void resetHandlerSingleton() throws Exception {
        // The handler is a JVM-wide singleton holding the client built at first use: reset it so
        // every test binds to its own mocked client.
        final Field instance = S3RemoteLogHandler.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void getRemoteLog_midTransferFailure_propagatesAndPublishesNothing(@TempDir Path tempDir) throws Exception {
        final Path archive = prepareArchivePath(tempDir);

        try (
                MockedStatic<PropertyUtils> propertyUtils = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<LogUtils> logUtils = Mockito.mockStatic(LogUtils.class);
                MockedStatic<AmazonS3ClientFactory> s3ClientFactory =
                        Mockito.mockStatic(AmazonS3ClientFactory.class)) {
            stubS3Client(propertyUtils, logUtils, s3ClientFactory, tempDir);
            // 100 bytes announced, the transfer dies after 50 bytes.
            when(s3Client.getObject(BUCKET, OBJECT_NAME)).thenReturn(s3Object(failingStreamAfter(50), 100));

            final IOException thrown = assertThrows(IOException.class,
                    () -> S3RemoteLogHandler.getInstance().getRemoteLog(archive.toString()));

            assertTrue(thrown.getMessage().contains("connection reset mid-transfer"), thrown.getMessage());
        }
        assertTrue(Files.notExists(archive), "The truncated download must not be published");
        assertFileCount(tempDir, 0, "The partial staging file must be deleted");
    }

    /**
     * The scenario the review flagged: an archive from an earlier download already sits at the
     * log path, and the re-download fails mid-transfer. The cached archive must survive
     * byte-identical — the failed transfer must not truncate it and must not be reported as a
     * successful download of a shorter log.
     */
    @Test
    public void getRemoteLog_midTransferFailure_keepsExistingCachedArchive(@TempDir Path tempDir) throws Exception {
        final Path archive = prepareArchivePath(tempDir);
        final byte[] cached = "previously downloaded complete log".getBytes(StandardCharsets.UTF_8);
        Files.write(archive, cached);

        try (
                MockedStatic<PropertyUtils> propertyUtils = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<LogUtils> logUtils = Mockito.mockStatic(LogUtils.class);
                MockedStatic<AmazonS3ClientFactory> s3ClientFactory =
                        Mockito.mockStatic(AmazonS3ClientFactory.class)) {
            stubS3Client(propertyUtils, logUtils, s3ClientFactory, tempDir);
            when(s3Client.getObject(BUCKET, OBJECT_NAME)).thenReturn(s3Object(failingStreamAfter(50), 100));

            assertThrows(IOException.class,
                    () -> S3RemoteLogHandler.getInstance().getRemoteLog(archive.toString()));
        }
        assertArrayEquals(cached, Files.readAllBytes(archive),
                "A failed re-download must not touch the cached archive");
        assertFileCount(tempDir, 1, "Only the cached archive must remain");
    }

    /**
     * A stream that ends early WITHOUT reporting an error (the SDK silently returning a short
     * body) must still fail: the announced object size is checked against the bytes received, so
     * a short transfer can never be published as a complete log.
     */
    @Test
    public void getRemoteLog_silentlyTruncatedStream_propagatesAndPublishesNothing(@TempDir Path tempDir) throws Exception {
        final Path archive = prepareArchivePath(tempDir);

        try (
                MockedStatic<PropertyUtils> propertyUtils = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<LogUtils> logUtils = Mockito.mockStatic(LogUtils.class);
                MockedStatic<AmazonS3ClientFactory> s3ClientFactory =
                        Mockito.mockStatic(AmazonS3ClientFactory.class)) {
            stubS3Client(propertyUtils, logUtils, s3ClientFactory, tempDir);
            when(s3Client.getObject(BUCKET, OBJECT_NAME))
                    .thenReturn(s3Object(new ByteArrayInputStream(new byte[50]), 100));

            final IOException thrown = assertThrows(IOException.class,
                    () -> S3RemoteLogHandler.getInstance().getRemoteLog(archive.toString()));

            assertTrue(thrown.getMessage().contains("Truncated download"), thrown.getMessage());
        }
        assertTrue(Files.notExists(archive));
        assertFileCount(tempDir, 0, "The partial staging file must be deleted");
    }

    @Test
    public void getRemoteLog_completeTransfer_publishesArchive(@TempDir Path tempDir) throws Exception {
        final Path archive = prepareArchivePath(tempDir);
        final byte[] content = "complete remote log".getBytes(StandardCharsets.UTF_8);

        try (
                MockedStatic<PropertyUtils> propertyUtils = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<LogUtils> logUtils = Mockito.mockStatic(LogUtils.class);
                MockedStatic<AmazonS3ClientFactory> s3ClientFactory =
                        Mockito.mockStatic(AmazonS3ClientFactory.class)) {
            stubS3Client(propertyUtils, logUtils, s3ClientFactory, tempDir);
            when(s3Client.getObject(BUCKET, OBJECT_NAME))
                    .thenReturn(s3Object(new ByteArrayInputStream(content), content.length));

            S3RemoteLogHandler.getInstance().getRemoteLog(archive.toString());
        }
        assertArrayEquals(content, Files.readAllBytes(archive));
        assertFileCount(tempDir, 1, "Only the published archive must remain");
    }

    private void stubS3Client(final MockedStatic<PropertyUtils> propertyUtils,
                              final MockedStatic<LogUtils> logUtils,
                              final MockedStatic<AmazonS3ClientFactory> s3ClientFactory,
                              final Path tempDir) {
        propertyUtils.when(() -> PropertyUtils.getString(Constants.AWS_S3_BUCKET_NAME)).thenReturn(BUCKET);
        propertyUtils.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_BASE_DIR)).thenReturn("logs");
        propertyUtils.when(() -> PropertyUtils.getByPrefix("aws.s3.", "")).thenReturn(Collections.emptyMap());
        logUtils.when(LogUtils::getLocalLogBaseDir).thenReturn(tempDir.resolve("logs").toString());
        s3ClientFactory.when(() -> AmazonS3ClientFactory.createAmazonS3Client(any())).thenReturn(s3Client);
        when(s3Client.doesBucketExistV2(BUCKET)).thenReturn(true);
    }

    /**
     * Builds {@code <tempDir>/logs/20261003/1/1.log} (the parent dirs are created by
     * {@code RemoteLogUtils#getRemoteLog} in production) and returns the archive path.
     */
    private static Path prepareArchivePath(final Path tempDir) throws IOException {
        return Files.createDirectories(tempDir.resolve("logs/20261003/1")).resolve("1.log");
    }

    private static S3Object s3Object(final InputStream content, final long announcedLength) {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(announcedLength);
        final S3Object s3Object = new S3Object();
        s3Object.setObjectMetadata(metadata);
        s3Object.setObjectContent(new S3ObjectInputStream(content, null));
        return s3Object;
    }

    private static InputStream failingStreamAfter(final int bytesBeforeFailure) {
        return new InputStream() {

            private int remaining = bytesBeforeFailure;

            @Override
            public int read() throws IOException {
                if (remaining > 0) {
                    remaining--;
                    return 'a';
                }
                throw new IOException("connection reset mid-transfer");
            }
        };
    }

    private static void assertFileCount(final Path dir, final long expected, final String message) {
        try (Stream<Path> entries = Files.walk(dir)) {
            assertEquals(expected, entries.filter(Files::isRegularFile).count(), message);
        } catch (IOException e) {
            throw new AssertionError("Failed to walk " + dir, e);
        }
    }
}

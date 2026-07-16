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

package org.apache.dolphinscheduler.common.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Zip helpers backed by the JDK {@link java.util.zip} APIs. Zip is used (rather than tar.gz) because the
 * central directory lets us random-read a single entry cheaply, which keeps online viewing of an archived
 * task log fast even when the archive holds a full day of logs.
 */
@Slf4j
@UtilityClass
public class CompressionUtils {

    private static final int BUFFER_SIZE = 8192;

    /**
     * Recursively compress every regular file under {@code sourceDir} into {@code targetZip}. Entry names are
     * the file paths relative to {@code sourceDir}, always using '/' as the separator so archives are portable
     * across platforms and stable regardless of the OS that produced them.
     *
     * @param sourceDir the directory whose contents are archived
     * @param targetZip the zip file to create (parent directories are created if missing)
     */
    public static void zipDirectory(Path sourceDir, Path targetZip) throws IOException {
        if (!Files.isDirectory(sourceDir)) {
            throw new IOException("Source is not a directory: " + sourceDir);
        }
        if (targetZip.getParent() != null) {
            Files.createDirectories(targetZip.getParent());
        }
        try (
                OutputStream fileOut = Files.newOutputStream(targetZip);
                ZipOutputStream zipOut = new ZipOutputStream(fileOut)) {
            try (Stream<Path> walk = Files.walk(sourceDir)) {
                List<Path> files = new ArrayList<>();
                walk.filter(Files::isRegularFile).forEach(files::add);
                for (Path file : files) {
                    String entryName = toEntryName(sourceDir.relativize(file));
                    zipOut.putNextEntry(new ZipEntry(entryName));
                    try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
                        copy(in, zipOut);
                    }
                    zipOut.closeEntry();
                }
            }
        }
    }

    /**
     * Read the full content of a single entry from a zip file.
     *
     * @param zipFile   the archive
     * @param entryName the '/'-separated entry name
     * @return the entry bytes, or {@code null} if the entry does not exist
     */
    public static byte[] readEntryBytes(Path zipFile, String entryName) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                return null;
            }
            try (InputStream in = zip.getInputStream(entry)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                copy(in, out);
                return out.toByteArray();
            }
        }
    }

    /**
     * Read a range of lines from a single zip entry, mirroring {@code Stream#skip}/{@code #limit} semantics used
     * for paginated log viewing.
     *
     * @param zipFile   the archive
     * @param entryName the '/'-separated entry name
     * @param skipLine  number of leading lines to skip
     * @param limit     maximum number of lines to return
     * @return the selected lines, or {@code null} if the entry does not exist
     */
    public static List<String> readEntryLines(Path zipFile, String entryName, int skipLine,
                                              int limit) throws IOException {
        byte[] bytes = readEntryBytes(zipFile, entryName);
        if (bytes == null) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        String content = new String(bytes, StandardCharsets.UTF_8);
        int skipped = 0;
        int collected = 0;
        int start = 0;
        int len = content.length();
        while (start <= len && collected < limit) {
            int nl = content.indexOf('\n', start);
            int end = nl == -1 ? len : nl;
            if (start == len && nl == -1) {
                break;
            }
            String line = content.substring(start, end);
            if (!line.isEmpty() && line.charAt(line.length() - 1) == '\r') {
                line = line.substring(0, line.length() - 1);
            }
            if (skipped < skipLine) {
                skipped++;
            } else {
                lines.add(line);
                collected++;
            }
            if (nl == -1) {
                break;
            }
            start = nl + 1;
        }
        return lines;
    }

    private static String toEntryName(Path relativePath) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < relativePath.getNameCount(); i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(relativePath.getName(i).toString());
        }
        return sb.toString();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}

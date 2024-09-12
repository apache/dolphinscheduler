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

package org.apache.dolphinscheduler.spi.utils;

import static org.apache.dolphinscheduler.spi.constants.Constants.FORMAT_S_S;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class FileUtils {

    public static final String KUBE_CONFIG_FILE = "config";

    private static final Set<PosixFilePermission> PERMISSION_755 = PosixFilePermissions.fromString("rwxr-xr-x");

    /**
     * absolute path of kubernetes configuration file
     *
     * @param execPath
     * @return
     */
    public static String getKubeConfigPath(String execPath) {
        return String.format(FORMAT_S_S, execPath, KUBE_CONFIG_FILE);
    }

    public static void createFileWith755(@NonNull Path path) throws IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            Files.createFile(path);
        } else {
            Files.createFile(path);
            Files.setPosixFilePermissions(path, PERMISSION_755);
        }
    }

    public static void createDirectoryWith755(@NonNull Path path) throws IOException {
        if (path.toFile().exists()) {
            return;
        }
        if (OSUtils.isWindows()) {
            Files.createDirectories(path);
        } else {
            Path parent = path.getParent();
            if (parent != null && !parent.toFile().exists()) {
                createDirectoryWith755(parent);
            }

            try {
                Files.createDirectory(path);
                Files.setPosixFilePermissions(path, PERMISSION_755);
            } catch (FileAlreadyExistsException fileAlreadyExistsException) {
                // Catch the FileAlreadyExistsException here to avoid create the same parent directory in parallel
                log.debug("The directory: {} already exists", path);
            }

        }
    }

    public static String concatFilePath(String... paths) {
        if (paths.length == 0) {
            throw new IllegalArgumentException("At least one path should be provided");
        }
        StringBuilder finalPath = new StringBuilder(paths[0]);
        if (StringUtils.isEmpty(finalPath)) {
            throw new IllegalArgumentException("The path should not be empty");
        }
        String separator = File.separator;
        for (int i = 1; i < paths.length; i++) {
            String path = paths[i];
            if (StringUtils.isEmpty(path)) {
                throw new IllegalArgumentException("The path should not be empty");
            }
            if (finalPath.toString().endsWith(separator) && path.startsWith(separator)) {
                finalPath.append(path.substring(separator.length()));
                continue;
            }
            if (!finalPath.toString().endsWith(separator) && !path.startsWith(separator)) {
                finalPath.append(separator).append(path);
                continue;
            }
            finalPath.append(path);
        }
        return finalPath.toString();
    }

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param filename file name
     */
    public static void deleteFile(String filename) {
        org.apache.commons.io.FileUtils.deleteQuietly(new File(filename));
    }
}

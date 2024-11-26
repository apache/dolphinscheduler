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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.dolphinscheduler.common.constants.Constants.DATA_BASEDIR_PATH;
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_VIEW_SUFFIXES;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class FileUtils {

    public static final String DATA_BASEDIR = PropertyUtils.getString(DATA_BASEDIR_PATH, "/tmp/dolphinscheduler");

    public static final String APPINFO_PATH = "appInfo.log";

    public static final String KUBE_CONFIG_FILE = "config";

    private static final Set<PosixFilePermission> PERMISSION_755 = PosixFilePermissions.fromString("rwxr-xr-x");

    /**
     * get download file absolute path and name
     *
     * @param filename file name
     * @return download file name
     */
    public static String getDownloadFilename(String filename) {
        return Paths.get(DATA_BASEDIR, "tmp", CodeGenerateUtils.genCode() + "-" + filename).toString();
    }

    /**
     * Generate a local tmp absolute path of the uploaded file
     */
    public static String getUploadFileLocalTmpAbsolutePath() {
        return Paths.get(DATA_BASEDIR, "tmp", String.valueOf(CodeGenerateUtils.genCode())).toString();
    }

    /**
     * directory of process execution
     *
     * @param taskInstanceId       task instance id
     * @return directory of process execution
     */
    public static String getTaskInstanceWorkingDirectory(int taskInstanceId) {
        return String.format("%s/exec/process/%d", DATA_BASEDIR, taskInstanceId);
    }

    /**
     * absolute path of kubernetes configuration file
     *
     * @param execPath
     * @return
     */
    public static String getKubeConfigPath(String execPath) {
        return String.format(FORMAT_S_S, execPath, KUBE_CONFIG_FILE);
    }

    /**
     * absolute path of appInfo file
     *
     * @param execPath directory of process execution
     * @return
     */
    public static String getAppInfoPath(String execPath) {
        return String.format("%s/%s", execPath, APPINFO_PATH);
    }

    /**
     * @return get suffixes for resource files that support online viewing
     */
    public static String getResourceViewSuffixes() {
        return PropertyUtils.getString(RESOURCE_VIEW_SUFFIXES, RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE);
    }

    /**
     * write content to file ,if parent path not exists, it will do one's utmost to mkdir
     *
     * @param content  content
     * @param filePath target file path
     * @return true if write success
     */
    public static boolean writeContent2File(String content, String filePath) {
        FileOutputStream fos = null;
        try {
            File distFile = new File(filePath);
            if (!distFile.getParentFile().exists() && !distFile.getParentFile().mkdirs()) {
                log.error("mkdir parent failed");
                return false;
            }
            fos = new FileOutputStream(filePath);
            IOUtils.write(content, fos, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            IOUtils.closeQuietly(fos);
        }
        return true;
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

    /**
     * Get Content
     *
     * @param inputStream input stream
     * @return string of input stream
     */
    public static String readFile2Str(InputStream inputStream) {

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Check whether the given string type of path can be traversal or not, return true if path could
     * traversal, and return false if it is not.
     *
     * @param filename String type of filename
     * @return whether file path could be traversal or not
     */
    public static boolean directoryTraversal(String filename) {
        if (filename.contains(FOLDER_SEPARATOR)) {
            return true;
        }
        File file = new File(filename);
        try {
            File canonical = file.getCanonicalFile();
            File absolute = file.getAbsoluteFile();
            return !canonical.equals(absolute);
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * Calculate file checksum with CRC32 algorithm
     *
     * @param pathName
     * @return checksum of file/dir
     */
    public static String getFileChecksum(String pathName) throws IOException {
        CRC32 crc32 = new CRC32();
        File file = new File(pathName);
        String crcString = "";
        if (file.isDirectory()) {
            // file system interface remains the same order
            String[] subPaths = file.list();
            StringBuilder concatenatedCRC = new StringBuilder();
            for (String subPath : subPaths) {
                concatenatedCRC.append(getFileChecksum(pathName + FOLDER_SEPARATOR + subPath));
            }
            crcString = concatenatedCRC.toString();
        } else {
            try (
                    FileInputStream fileInputStream = new FileInputStream(pathName);
                    CheckedInputStream checkedInputStream = new CheckedInputStream(fileInputStream, crc32);) {
                while (checkedInputStream.read() != -1) {
                }
            } catch (IOException e) {
                throw new IOException("Calculate checksum error.");
            }
            crcString = Long.toHexString(crc32.getValue());
        }

        return crcString;
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

    public static void setFileTo755(File file) throws IOException {
        if (OSUtils.isWindows()) {
            return;
        }
        if (file.isFile()) {
            Files.setPosixFilePermissions(file.toPath(), PERMISSION_755);
            return;
        }
        Files.setPosixFilePermissions(file.toPath(), PERMISSION_755);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                setFileTo755(f);
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

    public static String getClassPathAbsolutePath(Class clazz) {
        checkNotNull(clazz, "class is null");
        return Optional.ofNullable(clazz.getResource("/"))
                .map(URL::getPath)
                .orElseThrow(() -> new IllegalArgumentException("class path: " + clazz + " is null"));
    }

    /**
     * copy input stream to file, if the file already exists, will append the content to the beginning of the file, otherwise will create a new file.
     */
    @SneakyThrows
    public static void copyInputStreamToFile(InputStream inputStream, String destFilename) {
        org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, new File(destFilename));
    }
}

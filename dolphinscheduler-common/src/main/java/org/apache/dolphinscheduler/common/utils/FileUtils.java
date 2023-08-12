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

import static org.apache.dolphinscheduler.common.constants.Constants.DATA_BASEDIR_PATH;
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_VIEW_SUFFIXES;
import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE;
import static org.apache.dolphinscheduler.common.constants.Constants.UTF_8;
import static org.apache.dolphinscheduler.common.constants.DateConstants.YYYYMMDDHHMMSS;

import org.apache.dolphinscheduler.common.constants.TenantConstants;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * file utils
 */
@Slf4j
public class FileUtils {

    public static final String DATA_BASEDIR = PropertyUtils.getString(DATA_BASEDIR_PATH, "/tmp/dolphinscheduler");

    public static final String APPINFO_PATH = "appInfo.log";

    public static final String KUBE_CONFIG_FILE = "config";

    private static final String RWXR_XR_X = "rwxr-xr-x";

    private static final FileAttribute<Set<PosixFilePermission>> PERMISSION_755 =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(RWXR_XR_X));

    private FileUtils() {
        throw new UnsupportedOperationException("Construct FileUtils");
    }

    /**
     * get download file absolute path and name
     *
     * @param filename file name
     * @return download file name
     */
    public static String getDownloadFilename(String filename) {
        String fileName =
                String.format("%s/download/%s/%s", DATA_BASEDIR, DateUtils.getCurrentTime(YYYYMMDDHHMMSS), filename);

        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * get upload file absolute path and name
     *
     * @param tenantCode tenant code
     * @param filename file name
     * @return local file path
     */
    public static String getUploadFilename(String tenantCode, String filename) {
        String fileName = String.format("%s/%s/resources/%s", DATA_BASEDIR, tenantCode, filename);
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * directory of process execution
     *
     * @param tenant               tenant
     * @param projectCode          project code
     * @param processDefineCode    process definition Code
     * @param processDefineVersion process definition version
     * @param processInstanceId    process instance id
     * @param taskInstanceId       task instance id
     * @return directory of process execution
     */
    public static String getProcessExecDir(String tenant,
                                           long projectCode,
                                           long processDefineCode,
                                           int processDefineVersion,
                                           int processInstanceId,
                                           int taskInstanceId) {
        return String.format(
                "%s/exec/process/%s/%d/%d_%d/%d/%d",
                DATA_BASEDIR,
                tenant,
                projectCode,
                processDefineCode,
                processDefineVersion,
                processInstanceId,
                taskInstanceId);
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
     * @param execPath  directory of process execution
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
     * create directory if absent
     *
     * @param execLocalPath execute local path
     * @throws IOException errors
     */
    public static void createWorkDirIfAbsent(String execLocalPath) throws IOException {
        // if work dir exists, first delete
        File execLocalPathFile = new File(execLocalPath);

        if (execLocalPathFile.exists()) {
            try {
                org.apache.commons.io.FileUtils.forceDelete(execLocalPathFile);
            } catch (Exception ex) {
                if (ex instanceof NoSuchFileException || ex.getCause() instanceof NoSuchFileException) {
                    // this file is already be deleted.
                } else {
                    throw ex;
                }
            }
        }

        // create work dir
        org.apache.commons.io.FileUtils.forceMkdir(execLocalPathFile);
        String mkdirLog = "create dir success " + execLocalPath;
        log.info(mkdirLog);
    }

    /**
     * write content to file ,if parent path not exists, it will do one's utmost to mkdir
     *
     * @param content content
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
     * Gets all the parent subdirectories of the parentDir directory
     *
     * @param parentDir parent dir
     * @return all dirs
     */
    public static File[] getAllDir(String parentDir) {
        if (parentDir == null || "".equals(parentDir)) {
            throw new RuntimeException("parentDir can not be empty");
        }

        File file = new File(parentDir);
        if (!file.exists() || !file.isDirectory()) {
            throw new RuntimeException("parentDir not exist, or is not a directory:" + parentDir);
        }

        return file.listFiles(File::isDirectory);
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
            return output.toString(UTF_8);
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

    public static void setFileOwner(Path path, String tenant) {
        try {
            if (TenantConstants.DEFAULT_TENANT_CODE.equals(tenant)) {
                log.debug("The current tenant: {} is the default tenant, no need to set the owner for file: {}", tenant,
                        path);
                return;
            }
            UserPrincipalLookupService userPrincipalLookupService =
                    FileSystems.getDefault().getUserPrincipalLookupService();
            UserPrincipal tenantPrincipal = userPrincipalLookupService.lookupPrincipalByName(tenant);
            Files.setOwner(path, tenantPrincipal);
        } catch (IOException e) {
            log.error("Set file: {} owner to: {} failed", path, tenant, e);
        }
    }

    public static void createDirectoryIfNotPresent(Path path) throws IOException {
        if (Files.exists(path)) {
            return;
        }
        Files.createDirectories(path);
    }

    /**
     * Create a file with '755'.
     */
    public static void createFileWith755(@NonNull Path path) throws IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            Files.createFile(path);
        } else {
            Files.createFile(path, PERMISSION_755);
        }
    }

}

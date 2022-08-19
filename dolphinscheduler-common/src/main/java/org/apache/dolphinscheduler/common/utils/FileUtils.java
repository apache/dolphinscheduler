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

import static org.apache.dolphinscheduler.common.Constants.DATA_BASEDIR_PATH;
import static org.apache.dolphinscheduler.common.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.Constants.RESOURCE_VIEW_SUFFIXES;
import static org.apache.dolphinscheduler.common.Constants.RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE;
import static org.apache.dolphinscheduler.common.Constants.UTF_8;
import static org.apache.dolphinscheduler.common.Constants.YYYYMMDDHHMMSS;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * file utils
 */
public class FileUtils {

    public static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final String DATA_BASEDIR = PropertyUtils.getString(DATA_BASEDIR_PATH, "/tmp/dolphinscheduler");

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
        String fileName = String.format("%s/download/%s/%s", DATA_BASEDIR, DateUtils.getCurrentTime(YYYYMMDDHHMMSS), filename);

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
     * @param projectCode project code
     * @param processDefineCode process definition Code
     * @param processDefineVersion process definition version
     * @param processInstanceId process instance id
     * @param taskInstanceId task instance id
     * @return directory of process execution
     */
    public static String getProcessExecDir(long projectCode, long processDefineCode, int processDefineVersion, int processInstanceId, int taskInstanceId) {
        String fileName = String.format("%s/exec/process/%d/%s/%d/%d", DATA_BASEDIR,
                projectCode, processDefineCode + "_" + processDefineVersion, processInstanceId, taskInstanceId);
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
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
        //if work dir exists, first delete
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

        //create work dir
        org.apache.commons.io.FileUtils.forceMkdir(execLocalPathFile);
        String mkdirLog = "create dir success " + execLocalPath;
        logger.info(mkdirLog);
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
                logger.error("mkdir parent failed");
                return false;
            }
            fos = new FileOutputStream(filePath);
            IOUtils.write(content, fos, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
    public static boolean directoryTraversal(String filename){
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

}

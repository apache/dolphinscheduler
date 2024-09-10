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

package org.apache.dolphinscheduler.plugin.storage.api;

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;

public interface StorageOperator {

    String FILE_FOLDER_NAME = "resources";
    String UDF_FOLDER_NAME = "udfs";

    ResourceMetadata getResourceMetaData(String resourceAbsolutePath);

    /**
     * Get the absolute path of base directory.
     *
     * @return the base directory. e.g. file:///tmp/dolphinscheduler/, /tmp/dolphinscheduler/
     */
    String getStorageBaseDirectory();

    /**
     * Get the absolute path of directory which will be used by the given tenant. the tenant directory is under the base directory.
     *
     * @param tenantCode the tenant code, cannot be empty
     * @return the tenant directory. e.g. file:///tmp/dolphinscheduler/default/
     */
    String getStorageBaseDirectory(String tenantCode);

    /**
     * Get the absolute path of directory which will be used by the given tenant and resource type. the resource directory is under the tenant directory.
     * <p> If the resource type is FILE, will be 'file:///tmp/dolphinscheduler/default/resources/'.
     * <p> If the resource type is UDF, will be 'is file:///tmp/dolphinscheduler/default/udfs/'.
     * <p> If the resource type is ALL, will be 'is file:///tmp/dolphinscheduler/default/'.
     *
     * @param tenantCode   the tenant code, cannot be empty
     * @param resourceType the resource type, cannot be null
     * @return the resource directory. e.g. file:///tmp/dolphinscheduler/default/resources/
     */
    String getStorageBaseDirectory(String tenantCode, ResourceType resourceType);

    /**
     * Get the absolute path of the file in the storage. the file will under the file resource directory.
     *
     * @param tenantCode the tenant code, cannot be empty
     * @param fileName   the file name, cannot be empty
     * @return the file absolute path. e.g. file:///tmp/dolphinscheduler/default/resources/test.sh
     */
    String getStorageFileAbsolutePath(String tenantCode, String fileName);

    /**
     * Create a directory if the directory is already exists will throw exception(Dependent on the storage implementation).
     * <p> If the directory is not exists, will create the directory.
     * <p> If the parent directory is not exists, will create the parent directory.
     * <p> If the directory is already exists, will throw {@link FileAlreadyExistsException}.
     *
     * @param directoryAbsolutePath the directory absolute path
     */
    void createStorageDir(String directoryAbsolutePath);

    /**
     * Check if the resource exists.
     *
     * @param resourceAbsolutePath the resource absolute path
     * @return true if the resource exists, otherwise false
     */
    boolean exists(String resourceAbsolutePath);

    /**
     * Delete the resource, if the resourceAbsolutePath is not exists, will do nothing.
     *
     * @param resourceAbsolutePath the resource absolute path
     * @param recursive            whether to delete all the sub file/directory under the given resource
     */
    void delete(String resourceAbsolutePath, boolean recursive);

    /**
     * Copy the resource from the source path to the destination path.
     *
     * @param srcAbsolutePath the source path
     * @param dstAbsolutePath the destination path
     * @param deleteSource    whether to delete the source path after copying
     * @param overwrite       whether to overwrite the destination path if it exists
     */
    void copy(String srcAbsolutePath, String dstAbsolutePath, boolean deleteSource, boolean overwrite);

    /**
     * Move the resource from the source path to the destination path.
     *
     * @param srcLocalFileAbsolutePath the source local file
     * @param dstAbsolutePath          the destination path
     * @param deleteSource             whether to delete the source path after moving
     * @param overwrite                whether to overwrite the destination path if it exists
     */
    void upload(String srcLocalFileAbsolutePath, String dstAbsolutePath, boolean deleteSource, boolean overwrite);

    /**
     * Download the resource from the source path to the destination path.
     *
     * @param srcFileAbsolutePath the source path
     * @param dstAbsoluteFile     the destination file
     * @param overwrite           whether to overwrite the destination file if it exists
     */
    void download(String srcFileAbsolutePath, String dstAbsoluteFile, boolean overwrite);

    /**
     * Fetch the content of the file.
     *
     * @param fileAbsolutePath the file path
     * @param skipLineNums     the number of lines to skip
     * @param limit            the number of lines to read
     * @return the content of the file
     */
    List<String> fetchFileContent(String fileAbsolutePath, int skipLineNums, int limit);

    /**
     * Return the {@link StorageEntity} under the given path.
     * <p>If the path is a file, return the file status.
     * <p>If the path is a directory, return the file/directory under the directory.
     * <p>If the path is not exist, will return empty.
     *
     * @param resourceAbsolutePath the resource absolute path, cannot be empty
     */
    List<StorageEntity> listStorageEntity(String resourceAbsolutePath);

    /**
     * Return the {@link StorageEntity} which is file under the given path
     *
     * @param resourceAbsolutePath the resource absolute path, cannot be empty
     */
    List<StorageEntity> listFileStorageEntityRecursively(String resourceAbsolutePath);

    /**
     * Return the {@link StorageEntity} under the current directory
     *
     * @param resourceAbsolutePath the resource absolute path, cannot be empty
     */
    StorageEntity getStorageEntity(String resourceAbsolutePath);

}

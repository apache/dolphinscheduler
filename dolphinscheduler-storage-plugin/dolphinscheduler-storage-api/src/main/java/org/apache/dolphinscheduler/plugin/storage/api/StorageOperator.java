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
     * Check whether the resource exists for a recursive delete.
     * <p> Object-store prefixes can still contain child objects after the directory
     * marker has been removed. Implementations should return {@code true} in that
     * case so Resource Center can delete the leftover objects. Other callers, such
     * as rename or create, should keep using {@link #exists(String)}.
     *
     * @param resourceAbsolutePath the resource absolute path
     * @return true if the resource exists, or a directory prefix still has children
     */
    default boolean existsForRecursiveDelete(String resourceAbsolutePath) {
        return exists(resourceAbsolutePath);
    }

    /**
     * Check whether the resource of the given type exists for a recursive delete. A storage that can hold a file and a
     * directory with the same name overrides this, the default ignores the type and calls
     * {@link #existsForRecursiveDelete(String)}.
     *
     * @param resourceAbsolutePath the resource absolute path
     * @param isDirectory          whether the resource is a directory
     * @return true if the resource of the given type exists, or a directory prefix still has children
     */
    default boolean existsForRecursiveDelete(String resourceAbsolutePath, boolean isDirectory) {
        return existsForRecursiveDelete(resourceAbsolutePath);
    }

    /**
     * Delete the resource, if the resourceAbsolutePath is not exists, will do nothing.
     *
     * @param resourceAbsolutePath the resource absolute path
     * @param recursive            whether to delete all the sub file/directory under the given resource
     */
    void delete(String resourceAbsolutePath, boolean recursive);

    /**
     * Delete the resource of the given type, if it does not exist, will do nothing. A storage that can hold a file and a
     * directory with the same name overrides this, the default ignores the type.
     *
     * @param resourceAbsolutePath the resource absolute path
     * @param recursive            whether to delete all the sub file/directory under the given resource
     * @param isDirectory          whether the resource is a directory
     */
    default void delete(String resourceAbsolutePath, boolean recursive, boolean isDirectory) {
        delete(resourceAbsolutePath, recursive);
    }

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
     * List the resource of the given type, so a file and a directory with the same name can be told apart. A storage
     * that can hold both overrides this, the default ignores the type and calls {@link #listStorageEntity(String)}.
     *
     * @param resourceAbsolutePath the resource absolute path
     * @param isDirectory          whether the resource is a directory
     * @return the resource itself if it is a file, otherwise the entries directly under the directory
     */
    default List<StorageEntity> listStorageEntity(String resourceAbsolutePath, boolean isDirectory) {
        return listStorageEntity(resourceAbsolutePath);
    }

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

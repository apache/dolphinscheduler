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

package org.apache.dolphinscheduler.common.storage;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.IOException;
import java.util.List;


public interface StorageOperate {

    String RESOURCE_UPLOAD_PATH = PropertyUtils.getString(Constants.RESOURCE_UPLOAD_PATH, "/dolphinscheduler");

    /**
     * if the resource of tenant 's exist, the resource of folder will be created
     * @param tenantCode
     * @throws Exception
     */
    void createTenantDirIfNotExists(String tenantCode) throws Exception;

    /**
     * get the resource directory of tenant
     * @param tenantCode
     * @return
     */
    String getResDir(String tenantCode);

    /**
     * return the udf directory of tenant
     * @param tenantCode
     * @return
     */

    String getUdfDir(String tenantCode);

    /**
     * create the directory that the path of tenant wanted to create
     * @param tenantCode
     * @param path
     * @return
     * @throws IOException
     */
    boolean mkdir(String tenantCode, String path) throws IOException;

    /**
     * get the path of the resource file
     * @param tenantCode
     * @param fullName
     * @return
     */
    String getResourceFileName(String tenantCode, String fullName);

    /**
     * get the path of the file
     * @param resourceType
     * @param tenantCode
     * @param fileName
     * @return
     */
    String getFileName(ResourceType resourceType, String tenantCode, String fileName);

    /**
     * predicate  if the resource of tenant exists
     * @param tenantCode
     * @param fileName
     * @return
     * @throws IOException
     */
    boolean exists(String tenantCode, String fileName) throws IOException;

    /**
     * delete the resource of  filePath
     * todo if the filePath is the type of directory ,the files in the filePath need to be deleted at all
     * @param tenantCode
     * @param filePath
     * @param recursive
     * @return
     * @throws IOException
     */
    boolean delete(String tenantCode, String filePath, boolean recursive) throws IOException;

    /**
     * copy the file from srcPath to dstPath
     * @param srcPath
     * @param dstPath
     * @param deleteSource if need to delete the file of srcPath
     * @param overwrite
     * @return
     * @throws IOException
     */
    boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException;

    /**
     * get the root path of the tenant with resourceType
     * @param resourceType
     * @param tenantCode
     * @return
     */
    String getDir(ResourceType resourceType, String tenantCode);

    /**
     * upload the local srcFile to dstPath
     * @param tenantCode
     * @param srcFile
     * @param dstPath
     * @param deleteSource
     * @param overwrite
     * @return
     * @throws IOException
     */
    boolean upload(String tenantCode, String srcFile, String dstPath, boolean deleteSource, boolean overwrite) throws IOException;

    /**
     * download the srcPath to local
     * @param tenantCode
     * @param srcFilePath the full path of the srcPath
     * @param dstFile
     * @param deleteSource
     * @param overwrite
     * @throws IOException
     */
    void download(String tenantCode, String srcFilePath, String dstFile, boolean deleteSource, boolean overwrite)throws IOException;

    /**
     * vim the context of filePath
     * @param tenantCode
     * @param filePath
     * @param skipLineNums
     * @param limit
     * @return
     * @throws IOException
     */
    List<String> vimFile(String tenantCode, String filePath, int skipLineNums, int limit) throws IOException;

    /**
     * delete the files and directory of the tenant
     *
     * @param tenantCode
     * @throws Exception
     */
    void deleteTenant(String tenantCode) throws Exception;

    /**
     * return the storageType
     *
     * @return
     */
    ResUploadType returnStorageType();

}

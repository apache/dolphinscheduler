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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.dto.resources.DeleteDataTransferResponse;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * resources service
 */
public interface ResourcesService {

    /**
     * create directory
     *
     * @param loginUser login user
     * @param name alias
     * @param type type
     * @param pid parent id
     * @param currentDir current directory
     * @return create directory result
     */
    Result<Object> createDirectory(User loginUser,
                                   String name,
                                   ResourceType type,
                                   int pid,
                                   String currentDir);

    /**
     * create resource
     *
     * @param loginUser login user
     * @param name alias
     * @param type type
     * @param file file
     * @param currentDir current directory
     * @return create result code
     */
    Result<Object> createResource(User loginUser,
                                  String name,
                                  ResourceType type,
                                  MultipartFile file,
                                  String currentDir);

    /**
     * update resource
     * @param loginUser     login user
     * @param name          name
     * @param type          resource type
     * @param file          resource file
     * @return  update result code
     */
    Result<Object> updateResource(User loginUser,
                                  String fullName,
                                  String tenantCode,
                                  String name,
                                  ResourceType type,
                                  MultipartFile file);

    /**
     * query resources list paging
     *
     * @param loginUser login user
     * @param type resource type
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return resource list page
     */
    Result<PageInfo<StorageEntity>> queryResourceListPaging(User loginUser, String fullName, String resTenantCode,
                                                            ResourceType type, String searchVal, Integer pageNo,
                                                            Integer pageSize);

    /**
     * query resource list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    Map<String, Object> queryResourceList(User loginUser, ResourceType type, String fullName);

    /**
     * query resource list by program type
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    Result<Object> queryResourceByProgramType(User loginUser, ResourceType type, ProgramType programType);

    /**
     * delete resource
     *
     * @param loginUser login user
     * @return delete result code
     * @throws IOException exception
     */
    Result<Object> delete(User loginUser, String fullName, String tenantCode) throws IOException;

    /**
     * verify resource by name and type
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @return true if the resource name not exists, otherwise return false
     */
    Result<Object> verifyResourceName(String fullName, ResourceType type, User loginUser);

    /**
     * verify resource by file name
     * @param fileName  resource file name
     * @param type      resource type
     * @return true if the resource file name, otherwise return false
     */
    Result<Object> queryResourceByFileName(User loginUser, String fileName, ResourceType type, String resTenantCode);

    /**
     * view resource file online
     *
     * @param skipLineNum skip line number
     * @param limit limit
     * @param fullName fullName
     * @return resource content
     */
    Result<Object> readResource(User loginUser, String fullName, String tenantCode, int skipLineNum, int limit);

    /**
     * create resource file online
     *
     * @param loginUser login user
     * @param type resource type
     * @param fileName file name
     * @param fileSuffix file suffix
     * @param content content
     * @return create result code
     */
    Result<Object> onlineCreateResource(User loginUser, ResourceType type, String fileName, String fileSuffix,
                                        String content, String currentDirectory);

    /**
     * create or update resource.
     * If the folder is not already created, it will be ignored and directly create the new file
     *
     * @param userName user who create or update resource
     * @param fullName The fullname of resource.Includes path and suffix.
     * @param resourceContent content of resource
     */
    StorageEntity createOrUpdateResource(String userName, String fullName, String resourceContent) throws Exception;

    /**
     * updateProcessInstance resource
     *
     * @param loginUser login user
     * @param fullName full name
     * @param tenantCode tenantCode
     * @param content content
     * @return update result cod
     */
    Result<Object> updateResourceContent(User loginUser, String fullName, String tenantCode,
                                         String content);

    /**
     * download file
     *
     * @return resource content
     * @throws IOException exception
     */
    org.springframework.core.io.Resource downloadResource(User loginUser, String fullName) throws IOException;

    /**
     * list all file
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    Map<String, Object> authorizeResourceTree(User loginUser, Integer userId);

    /**
     * Get resource by given resource type and full name.
     * Useful in Python API create task which need processDefinition information.
     *
     * @param userName user who query resource
     * @param fullName full name of the resource
     */
    StorageEntity queryFileStatus(String userName, String fullName) throws Exception;

    /**
     * delete DATA_TRANSFER data in resource center
     *
     * @param loginUser user who query resource
     * @param days number of days
     */
    DeleteDataTransferResponse deleteDataTransferData(User loginUser, Integer days);

    /**
     * unauthorized file
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    Map<String, Object> unauthorizedFile(User loginUser, Integer userId);

    /**
     * unauthorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    Map<String, Object> unauthorizedUDFFunction(User loginUser, Integer userId);

    /**
     * authorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    Map<String, Object> authorizedUDFFunction(User loginUser, Integer userId);

    /**
     * authorized file
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result
     */
    Map<String, Object> authorizedFile(User loginUser, Integer userId);

    /**
     * get resource by id
     * @param fullName resource full name
     * @param tenantCode owner's tenant code of resource
     * @return resource
     */
    Result<Object> queryResourceByFullName(User loginUser, String fullName, String tenantCode,
                                           ResourceType type) throws IOException;

    /**
     * get resource base dir
     *
     * @param loginUser login user
     * @param type      resource type
     * @return
     */
    Result<Object> queryResourceBaseDir(User loginUser, ResourceType type);

}

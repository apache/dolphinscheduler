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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;
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
     * @param description description
     * @param type type
     * @param pid parent id
     * @param currentDir current directory
     * @return create directory result
     */
    Result<Object> createDirectory(User loginUser,
                                   String name,
                                   String description,
                                   ResourceType type,
                                   int pid,
                                   String currentDir);

    /**
     * create resource
     *
     * @param loginUser login user
     * @param name alias
     * @param desc description
     * @param file file
     * @param type type
     * @param pid parent id
     * @param currentDir current directory
     * @return create result code
     */
    Result<Object> createResource(User loginUser,
                                  String name,
                                  String desc,
                                  ResourceType type,
                                  MultipartFile file,
                                  int pid,
                                  String currentDir);

    /**
     * update resource
     * @param loginUser     login user
     * @param resourceId    resource id
     * @param name          name
     * @param desc          description
     * @param type          resource type
     * @param file          resource file
     * @return  update result code
     */
    Result<Object> updateResource(User loginUser,
                                  int resourceId,
                                  String name,
                                  String desc,
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
    Result queryResourceListPaging(User loginUser, int directoryId, ResourceType type, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query resource list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    Map<String, Object> queryResourceList(User loginUser, ResourceType type);

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
     * @param resourceId resource id
     * @return delete result code
     * @throws IOException exception
     */
    Result<Object> delete(User loginUser, int resourceId) throws IOException;

    /**
     * verify resource by name and type
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @return true if the resource name not exists, otherwise return false
     */
    Result<Object> verifyResourceName(String fullName, ResourceType type,User loginUser);

    /**
     * verify resource by full name or pid and type
     * @param fullName  resource full name
     * @param id        resource id
     * @param type      resource type
     * @return true if the resource full name or pid not exists, otherwise return false
     */
    Result<Object> queryResource(User loginUser,String fullName,Integer id,ResourceType type);

    /**
     * view resource file online
     *
     * @param resourceId resource id
     * @param skipLineNum skip line number
     * @param limit limit
     * @return resource content
     */
    Result<Object> readResource(User loginUser,int resourceId, int skipLineNum, int limit);

    /**
     * create resource file online
     *
     * @param loginUser login user
     * @param type resource type
     * @param fileName file name
     * @param fileSuffix file suffix
     * @param desc description
     * @param content content
     * @return create result code
     */
    Result<Object> onlineCreateResource(User loginUser, ResourceType type, String fileName, String fileSuffix, String desc, String content,int pid,String currentDirectory);

    /**
     * create or update resource.
     * If the folder is not already created, it will be
     *
     * @param loginUser user who create or update resource
     * @param fileFullName The full name of resource.Includes path and suffix.
     * @param desc description of resource
     * @param content content of resource
     * @return create result code
     */
    Result<Object> onlineCreateOrUpdateResourceWithDir(User loginUser, String fileFullName, String desc, String content);

    /**
     * create or update resource.
     * If the folder is not already created, it will be
     *
     * @param userName user who create or update resource
     * @param fullName The fullname of resource.Includes path and suffix.
     * @param description description of resource
     * @param resourceContent content of resource
     * @return id of resource
     */
    Integer createOrUpdateResource(String userName, String fullName, String description, String resourceContent);

    /**
     * updateProcessInstance resource
     *
     * @param resourceId resource id
     * @param content content
     * @return update result cod
     */
    Result<Object> updateResourceContent(User loginUser,int resourceId, String content);

    /**
     * download file
     *
     * @param resourceId resource id
     * @return resource content
     * @throws IOException exception
     */
    org.springframework.core.io.Resource downloadResource(User loginUser, int resourceId) throws IOException;

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
    Resource queryResourcesFileInfo(String userName, String fullName);

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
     * @param resourceId resource id
     * @return resource
     */
    Result<Object> queryResourceById(User loginUser, Integer resourceId);

}

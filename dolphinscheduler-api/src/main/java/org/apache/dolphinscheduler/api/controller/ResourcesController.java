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
package org.apache.dolphinscheduler.api.controller;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;
/**
 * resources controller
 */
@Api(tags = "RESOURCES_TAG", position = 1)
@RestController
@RequestMapping("resources")
public class ResourcesController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(ResourcesController.class);


    @Autowired
    private ResourcesService resourceService;
    @Autowired
    private UdfFuncService udfFuncService;

    /**
     * create resource
     *
     * @param loginUser login user
     * @param alias alias
     * @param description description
     * @param type type
     * @return create result code
     */

    /**
     *
     * @param loginUser     login user
     * @param type          type
     * @param alias         alias
     * @param description   description
     * @param pid           parent id
     * @param currentDir    current directory
     * @return
     */
    @ApiOperation(value = "createDirctory", notes= "CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType"),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC",  dataType ="String"),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataType = "MultipartFile")
    })
    @PostMapping(value = "/directory/create")
    public Result createDirectory(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "type") ResourceType type,
                                 @RequestParam(value ="name") String alias,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value ="pid") int pid,
                                 @RequestParam(value ="currentDir") String currentDir) {
        try {
            logger.info("login user {}, create resource, type: {}, resource alias: {}, desc: {}, file: {},{}",
                    loginUser.getUserName(),type, alias, description,pid,currentDir);
            return resourceService.createDirectory(loginUser,alias, description,type ,pid,currentDir);
        } catch (Exception e) {
            logger.error(CREATE_RESOURCE_ERROR.getMsg(),e);
            return error(CREATE_RESOURCE_ERROR.getCode(), CREATE_RESOURCE_ERROR.getMsg());
        }
    }

    /**
     * create resource
     *
     * @param loginUser login user
     * @param alias alias
     * @param description description
     * @param type type
     * @param file file
     * @return create result code
     */
    @ApiOperation(value = "createResource", notes= "CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType"),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC",  dataType ="String"),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataType = "MultipartFile")
    })
    @PostMapping(value = "/create")
    public Result createResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "type") ResourceType type,
                                 @RequestParam(value ="name") String alias,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam(value ="pid") int pid,
                                 @RequestParam(value ="currentDir") String currentDir) {
        try {
            logger.info("login user {}, create resource, type: {}, resource alias: {}, desc: {}, file: {},{}",
                    loginUser.getUserName(),type, alias, description, file.getName(), file.getOriginalFilename());
            return resourceService.createResource(loginUser,alias, description,type ,file,pid,currentDir);
        } catch (Exception e) {
            logger.error(CREATE_RESOURCE_ERROR.getMsg(),e);
            return error(CREATE_RESOURCE_ERROR.getCode(), CREATE_RESOURCE_ERROR.getMsg());
        }
    }

    /**
     * update resource
     *
     * @param loginUser login user
     * @param alias alias
     * @param resourceId resource id
     * @param type resource type
     * @param description description
     * @return update result code
     */
    @ApiOperation(value = "updateResource", notes= "UPDATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType"),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC",  dataType ="String"),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true,dataType = "MultipartFile")
    })
    @PostMapping(value = "/update")
    public Result updateResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value ="id") int resourceId,
                                 @RequestParam(value = "type") ResourceType type,
                                 @RequestParam(value ="name")String alias,
                                 @RequestParam(value = "description", required = false) String description) {
        try {
            logger.info("login user {}, update resource, type: {}, resource alias: {}, desc: {}",
                    loginUser.getUserName(),type, alias, description);
            return resourceService.updateResource(loginUser,resourceId,alias,description,type);
        } catch (Exception e) {
            logger.error(UPDATE_RESOURCE_ERROR.getMsg(),e);
            return error(Status.UPDATE_RESOURCE_ERROR.getCode(), Status.UPDATE_RESOURCE_ERROR.getMsg());
        }
    }

    /**
     * query resources list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    @ApiOperation(value = "queryResourceList", notes= "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType")
    })
    @GetMapping(value="/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryResourceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value ="type") ResourceType type
    ){
        try{
            logger.info("query resource list, login user:{}, resource type:{}", loginUser.getUserName(), type.toString());
            Map<String, Object> result = resourceService.queryResourceList(loginUser, type);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_RESOURCES_LIST_ERROR.getMsg(),e);
            return error(Status.QUERY_RESOURCES_LIST_ERROR.getCode(), Status.QUERY_RESOURCES_LIST_ERROR.getMsg());
        }
    }

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
    @ApiOperation(value = "queryResourceListPaging", notes= "QUERY_RESOURCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType"),
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType ="int"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType ="Int",example = "20")
    })
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryResourceListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value ="type") ResourceType type,
                                     @RequestParam(value ="id") int id,
                                     @RequestParam("pageNo") Integer pageNo,
                                     @RequestParam(value = "searchVal", required = false) String searchVal,
                                     @RequestParam("pageSize") Integer pageSize
    ){
        try{
            logger.info("query resource list, login user:{}, resource type:{}, search value:{}",
                    loginUser.getUserName(), type.toString(), searchVal);
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != Status.SUCCESS){
                return returnDataListPaging(result);
            }

            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = resourceService.queryResourceListPaging(loginUser,id,type,searchVal,pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_RESOURCES_LIST_PAGING.getMsg(),e);
            return error(Status.QUERY_RESOURCES_LIST_PAGING.getCode(), Status.QUERY_RESOURCES_LIST_PAGING.getMsg());
        }
    }


    /**
     * delete resource
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @return delete result code
     */
    @ApiOperation(value = "deleteResource", notes= "DELETE_RESOURCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value ="id") int resourceId
    ) {
        try{
            logger.info("login user {}, delete resource id: {}",
                    loginUser.getUserName(),resourceId);
            return resourceService.delete(loginUser,resourceId);
        }catch (Exception e){
            logger.error(DELETE_RESOURCE_ERROR.getMsg(),e);
            return error(Status.DELETE_RESOURCE_ERROR.getCode(), Status.DELETE_RESOURCE_ERROR.getMsg());
        }
    }


    /**
     * verify resource by alias and type
     *
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @return true if the resource name not exists, otherwise return false
     */
    @ApiOperation(value = "verifyResourceName", notes= "VERIFY_RESOURCE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType"),
            @ApiImplicitParam(name = "fullName", value = "RESOURCE_FULL_NAME", required = true, dataType ="String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyResourceName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value ="fullName") String fullName,
                                     @RequestParam(value ="type") ResourceType type
    ) {
        try {
            logger.info("login user {}, verfiy resource alias: {},resource type: {}",
                    loginUser.getUserName(), fullName,type);

            return resourceService.verifyResourceName(fullName,type,loginUser);
        } catch (Exception e) {
            logger.error(VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR.getMsg(), e);
            return error(Status.VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR.getCode(), Status.VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR.getMsg());
        }
    }

    /**
     * query resources jar list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    @ApiOperation(value = "queryResourceJarList", notes= "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType")
    })
    @GetMapping(value="/list/jar")
    @ResponseStatus(HttpStatus.OK)
    public Result queryResourceJarList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value ="type") ResourceType type
    ){
        try{
            logger.info("query resource list, login user:{}, resource type:{}", loginUser.getUserName(), type.toString());
            Map<String, Object> result = resourceService.queryResourceList(loginUser, type);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_RESOURCES_LIST_ERROR.getMsg(),e);
            return error(Status.QUERY_RESOURCES_LIST_ERROR.getCode(), Status.QUERY_RESOURCES_LIST_ERROR.getMsg());
        }
    }

    /**
     * query resource by full name and type
     *
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @return true if the resource name not exists, otherwise return false
     */
    @ApiOperation(value = "queryResource", notes= "QUERY_BY_RESOURCE_NAME")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType"),
            @ApiImplicitParam(name = "fullName", value = "RESOURCE_FULL_NAME", required = true, dataType ="String")
    })
    @GetMapping(value = "/queryResourceJar")
    @ResponseStatus(HttpStatus.OK)
    public Result queryResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value ="fullName",required = false) String fullName,
                                @RequestParam(value ="pid",required = false) Integer pid,
                                @RequestParam(value ="type") ResourceType type
    ) {
        try {
            logger.info("login user {}, query resource by full name: {} or pid: {},resource type: {}",
                    loginUser.getUserName(), fullName,pid,type);

            return resourceService.queryResource(fullName,pid,type);
        } catch (Exception e) {
            logger.error(RESOURCE_NOT_EXIST.getMsg(), e);
            return error(Status.RESOURCE_NOT_EXIST.getCode(), Status.RESOURCE_NOT_EXIST.getMsg());
        }
    }

    /**
     * view resource file online
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @param skipLineNum skip line number
     * @param limit limit
     * @return resource content
     */
    @ApiOperation(value = "viewResource", notes= "VIEW_RESOURCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "skipLineNum", value = "SKIP_LINE_NUM", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataType ="Int", example = "100")
    })
    @GetMapping(value = "/view")
    public Result viewResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "id") int resourceId,
                               @RequestParam(value = "skipLineNum") int skipLineNum,
                               @RequestParam(value = "limit") int limit
    ) {
        try{
            logger.info("login user {}, view resource : {}, skipLineNum {} , limit {}",
                    loginUser.getUserName(),resourceId,skipLineNum,limit);

            return resourceService.readResource(resourceId,skipLineNum,limit);
        }catch (Exception e){
            logger.error(VIEW_RESOURCE_FILE_ON_LINE_ERROR.getMsg(),e);
            return error(Status.VIEW_RESOURCE_FILE_ON_LINE_ERROR.getCode(), Status.VIEW_RESOURCE_FILE_ON_LINE_ERROR.getMsg());
        }
    }

    /**
     * create resource file online
     *
     * @param loginUser login user
     * @param type resource type
     * @param fileName file name
     * @param fileSuffix file suffix
     * @param description description
     * @param content content
     * @return create result code
     */
    @ApiOperation(value = "onlineCreateResource", notes= "ONLINE_CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType ="ResourceType"),
            @ApiImplicitParam(name = "fileName", value = "RESOURCE_NAME",required = true,  dataType ="String"),
            @ApiImplicitParam(name = "suffix", value = "SUFFIX", required = true, dataType ="String"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC",  dataType ="String"),
            @ApiImplicitParam(name = "content", value = "CONTENT",required = true,  dataType ="String")
    })
    @PostMapping(value = "/online-create")
    public Result onlineCreateResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "type") ResourceType type,
                                       @RequestParam(value ="fileName")String fileName,
                                       @RequestParam(value ="suffix")String fileSuffix,
                                       @RequestParam(value = "description", required = false) String description,
                                       @RequestParam(value = "content") String content,
                                       @RequestParam(value ="pid") int pid,
                                       @RequestParam(value ="currentDir") String currentDir
    ) {
        try{
            logger.info("login user {}, online create resource! fileName : {}, type : {}, suffix : {},desc : {},content : {}",
                    loginUser.getUserName(),fileName,type,fileSuffix,description,content,pid,currentDir);
            if(StringUtils.isEmpty(content)){
                logger.error("resource file contents are not allowed to be empty");
                return error(Status.RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
            }
            return resourceService.onlineCreateResource(loginUser,type,fileName,fileSuffix,description,content,pid,currentDir);
        }catch (Exception e){
            logger.error(CREATE_RESOURCE_FILE_ON_LINE_ERROR.getMsg(),e);
            return error(Status.CREATE_RESOURCE_FILE_ON_LINE_ERROR.getCode(), Status.CREATE_RESOURCE_FILE_ON_LINE_ERROR.getMsg());
        }
    }

    /**
     * edit resource file online
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @param content content
     * @return update result code
     */
    @ApiOperation(value = "updateResourceContent", notes= "UPDATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "content", value = "CONTENT",required = true,  dataType ="String")
    })
    @PostMapping(value = "/update-content")
    public Result updateResourceContent(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "id") int resourceId,
                                        @RequestParam(value = "content") String content
    ) {
        try{
            logger.info("login user {}, updateProcessInstance resource : {}",
                    loginUser.getUserName(),resourceId);
            if(StringUtils.isEmpty(content)){
                logger.error("The resource file contents are not allowed to be empty");
                return error(Status.RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
            }
            return resourceService.updateResourceContent(resourceId,content);
        }catch (Exception e){
            logger.error(EDIT_RESOURCE_FILE_ON_LINE_ERROR.getMsg(),e);
            return error(Status.EDIT_RESOURCE_FILE_ON_LINE_ERROR.getCode(), Status.EDIT_RESOURCE_FILE_ON_LINE_ERROR.getMsg());
        }
    }

    /**
     * download resource file
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @return resource content
     */
    @ApiOperation(value = "downloadResource", notes= "DOWNLOAD_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100")
    })
    @GetMapping(value = "/download")
    @ResponseBody
    public ResponseEntity downloadResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam(value = "id") int resourceId) {
        try{
            logger.info("login user {}, download resource : {}",
                    loginUser.getUserName(), resourceId);
            Resource file = resourceService.downloadResource(resourceId);
            if (file == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Status.RESOURCE_NOT_EXIST.getMsg());
            }
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        }catch (RuntimeException e){
            logger.error(e.getMessage(),e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            logger.error(DOWNLOAD_RESOURCE_FILE_ERROR.getMsg(),e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Status.DOWNLOAD_RESOURCE_FILE_ERROR.getMsg());
        }
    }


    /**
     * create udf function
     * @param loginUser login user
     * @param type udf type
     * @param funcName function name
     * @param argTypes argument types
     * @param database database
     * @param description description
     * @param className  class name
     * @param resourceId resource id
     * @return create result code
     */
    @ApiOperation(value = "createUdfFunc", notes= "CREATE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataType ="UdfType"),
            @ApiImplicitParam(name = "funcName", value = "FUNC_NAME",required = true,  dataType ="String"),
            @ApiImplicitParam(name = "suffix", value = "CLASS_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "argTypes", value = "ARG_TYPES",  dataType ="String"),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME",  dataType ="String"),
            @ApiImplicitParam(name = "description", value = "UDF_DESC", dataType ="String"),
            @ApiImplicitParam(name = "resourceId", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100")

    })
    @PostMapping(value = "/udf-func/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "type") UdfType type,
                                @RequestParam(value ="funcName")String funcName,
                                @RequestParam(value ="className")String className,
                                @RequestParam(value ="argTypes", required = false)String argTypes,
                                @RequestParam(value ="database", required = false)String database,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "resourceId") int resourceId) {
        logger.info("login user {}, create udf function, type: {},  funcName: {},argTypes: {} ,database: {},desc: {},resourceId: {}",
                loginUser.getUserName(),type, funcName, argTypes,database,description, resourceId);
        Result result = new Result();

        try {
            return udfFuncService.createUdfFunction(loginUser,funcName,className,argTypes,database,description,type,resourceId);
        } catch (Exception e) {
            logger.error(CREATE_UDF_FUNCTION_ERROR.getMsg(),e);
            return error(Status.CREATE_UDF_FUNCTION_ERROR.getCode(), Status.CREATE_UDF_FUNCTION_ERROR.getMsg());
        }
    }

    /**
     * view udf function
     *
     * @param loginUser login user
     * @param id resource id
     * @return udf function detail
     */
    @ApiOperation(value = "viewUIUdfFunction", notes= "VIEW_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "resourceId", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100")

    })
    @GetMapping(value = "/udf-func/update-ui")
    @ResponseStatus(HttpStatus.OK)
    public Result viewUIUdfFunction(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam("id") int id) {
        logger.info("login user {}, query udf{}",
                loginUser.getUserName(), id);
        try {
            Map<String, Object> map = udfFuncService.queryUdfFuncDetail(id);
            return returnDataList(map);
        } catch (Exception e) {
            logger.error(VIEW_UDF_FUNCTION_ERROR.getMsg(),e);
            return error(Status.VIEW_UDF_FUNCTION_ERROR.getCode(), Status.VIEW_UDF_FUNCTION_ERROR.getMsg());
        }
    }

    /**
     * update udf function
     *
     * @param loginUser login user
     * @param type  resource type
     * @param funcName function name
     * @param argTypes argument types
     * @param database data base
     * @param description description
     * @param resourceId resource id
     * @param className class name
     * @param udfFuncId  udf function id
     * @return update result code
     */
    @ApiOperation(value = "updateUdfFunc", notes= "UPDATE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataType ="UdfType"),
            @ApiImplicitParam(name = "funcName", value = "FUNC_NAME",required = true,  dataType ="String"),
            @ApiImplicitParam(name = "suffix", value = "CLASS_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "argTypes", value = "ARG_TYPES",  dataType ="String"),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME",  dataType ="String"),
            @ApiImplicitParam(name = "description", value = "UDF_DESC", dataType ="String"),
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100")

    })
    @PostMapping(value = "/udf-func/update")
    public Result updateUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "id") int udfFuncId,
                                @RequestParam(value = "type") UdfType type,
                                @RequestParam(value ="funcName")String funcName,
                                @RequestParam(value ="className")String className,
                                @RequestParam(value ="argTypes", required = false)String argTypes,
                                @RequestParam(value ="database", required = false)String database,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "resourceId") int resourceId) {
        try {
            logger.info("login user {}, updateProcessInstance udf function id: {},type: {},  funcName: {},argTypes: {} ,database: {},desc: {},resourceId: {}",
                    loginUser.getUserName(),udfFuncId,type, funcName, argTypes,database,description, resourceId);
            Map<String, Object> result = udfFuncService.updateUdfFunc(udfFuncId,funcName,className,argTypes,database,description,type,resourceId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(UPDATE_UDF_FUNCTION_ERROR.getMsg(),e);
            return error(Status.UPDATE_UDF_FUNCTION_ERROR.getCode(), Status.UPDATE_UDF_FUNCTION_ERROR.getMsg());
        }
    }

    /**
     * query udf function list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return udf function list page
     */
    @ApiOperation(value = "queryUdfFuncListPaging", notes= "QUERY_UDF_FUNCTION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType ="Int",example = "20")
    })
    @GetMapping(value="/udf-func/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryUdfFuncList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("pageNo") Integer pageNo,
                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                   @RequestParam("pageSize") Integer pageSize
    ){
        try{
            logger.info("query udf functions list, login user:{},search value:{}",
                    loginUser.getUserName(), searchVal);
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != Status.SUCCESS){
                return returnDataListPaging(result);
            }

            result = udfFuncService.queryUdfFuncListPaging(loginUser,searchVal,pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_UDF_FUNCTION_LIST_PAGING_ERROR.getMsg(),e);
            return error(Status.QUERY_UDF_FUNCTION_LIST_PAGING_ERROR.getCode(), Status.QUERY_UDF_FUNCTION_LIST_PAGING_ERROR.getMsg());
        }
    }

    /**
     * query resource list by type
     *
     * @param loginUser login user
     * @param type  resource type
     * @return resource list
     */
    @ApiOperation(value = "queryResourceList", notes= "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataType ="UdfType")
    })
    @GetMapping(value="/udf-func/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryResourceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("type") UdfType type){
        try{
            logger.info("query datasource list, user:{}, type:{}", loginUser.getUserName(), type.toString());
            Map<String, Object> result = udfFuncService.queryResourceList(loginUser,type.ordinal());
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_DATASOURCE_BY_TYPE_ERROR.getMsg(),e);
            return error(Status.QUERY_DATASOURCE_BY_TYPE_ERROR.getCode(),QUERY_DATASOURCE_BY_TYPE_ERROR.getMsg());
        }
    }

    /**
     * verify udf function name can use or not
     *
     * @param loginUser login user
     * @param name name
     * @return true if the name can user, otherwise return false
     */
    @ApiOperation(value = "verifyUdfFuncName", notes= "VERIFY_UDF_FUNCTION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "FUNC_NAME",required = true,  dataType ="String")

    })
    @GetMapping(value = "/udf-func/verify-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyUdfFuncName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value ="name") String name
    ) {
        logger.info("login user {}, verfiy udf function name: {}",
                loginUser.getUserName(),name);

        try{

            return udfFuncService.verifyUdfFuncByName(name);
        }catch (Exception e){
            logger.error(VERIFY_UDF_FUNCTION_NAME_ERROR.getMsg(),e);
            return error(Status.VERIFY_UDF_FUNCTION_NAME_ERROR.getCode(), Status.VERIFY_UDF_FUNCTION_NAME_ERROR.getMsg());
        }
    }

    /**
     * delete udf function
     *
     * @param loginUser login user
     * @param udfFuncId udf function id
     * @return delete result code
     */
    @ApiOperation(value = "deleteUdfFunc", notes= "DELETE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType ="Int", example = "100")
    })
    @GetMapping(value = "/udf-func/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value ="id") int udfFuncId
    ) {
        try{

            logger.info("login user {}, delete udf function id: {}", loginUser.getUserName(),udfFuncId);
            return udfFuncService.delete(udfFuncId);
        }catch (Exception e){
            logger.error(DELETE_UDF_FUNCTION_ERROR.getMsg(),e);
            return error(Status.DELETE_UDF_FUNCTION_ERROR.getCode(), Status.DELETE_UDF_FUNCTION_ERROR.getMsg());
        }
    }

    /**
     * authorized file resource list
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result
     */
    @ApiOperation(value = "authorizedFile", notes= "AUTHORIZED_FILE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType ="Int", example = "100")
    })
    @GetMapping(value = "/authed-file")
    @ResponseStatus(HttpStatus.CREATED)
    public Result authorizedFile(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("userId") Integer userId) {
        try{
            logger.info("authorized file resource, user: {}, user id:{}", loginUser.getUserName(), userId);
            Map<String, Object> result =  resourceService.authorizedFile(loginUser, userId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(AUTHORIZED_FILE_RESOURCE_ERROR.getMsg(),e);
            return error(Status.AUTHORIZED_FILE_RESOURCE_ERROR.getCode(), Status.AUTHORIZED_FILE_RESOURCE_ERROR.getMsg());
        }
    }


    /**
     * unauthorized file resource list
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    @ApiOperation(value = "unauthorizedFile", notes= "UNAUTHORIZED_FILE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType ="Int", example = "100")
    })
    @GetMapping(value = "/unauth-file")
    @ResponseStatus(HttpStatus.CREATED)
    public Result unauthorizedFile(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("userId") Integer userId) {
        try{
            logger.info("resource unauthorized file, user:{}, unauthorized user id:{}", loginUser.getUserName(), userId);
            Map<String, Object> result =  resourceService.unauthorizedFile(loginUser, userId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(UNAUTHORIZED_FILE_RESOURCE_ERROR.getMsg(),e);
            return error(Status.UNAUTHORIZED_FILE_RESOURCE_ERROR.getCode(), Status.UNAUTHORIZED_FILE_RESOURCE_ERROR.getMsg());
        }
    }


    /**
     * unauthorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    @ApiOperation(value = "unauthUDFFunc", notes= "UNAUTHORIZED_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType ="Int", example = "100")
    })
    @GetMapping(value = "/unauth-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    public Result unauthUDFFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("userId") Integer userId) {
        try{
            logger.info("unauthorized udf function, login user:{}, unauthorized user id:{}", loginUser.getUserName(), userId);

            Map<String, Object>  result =  resourceService.unauthorizedUDFFunction(loginUser, userId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(UNAUTHORIZED_UDF_FUNCTION_ERROR.getMsg(),e);
            return error(Status.UNAUTHORIZED_UDF_FUNCTION_ERROR.getCode(), Status.UNAUTHORIZED_UDF_FUNCTION_ERROR.getMsg());
        }
    }


    /**
     * authorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    @ApiOperation(value = "authUDFFunc", notes= "AUTHORIZED_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType ="Int", example = "100")
    })
    @GetMapping(value = "/authed-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    public Result authorizedUDFFunction(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam("userId") Integer userId) {
        try{
            logger.info("auth udf function, login user:{}, auth user id:{}", loginUser.getUserName(), userId);
            Map<String, Object> result =  resourceService.authorizedUDFFunction(loginUser, userId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(AUTHORIZED_UDF_FUNCTION_ERROR.getMsg(),e);
            return error(Status.AUTHORIZED_UDF_FUNCTION_ERROR.getCode(), Status.AUTHORIZED_UDF_FUNCTION_ERROR.getMsg());
        }
    }
}
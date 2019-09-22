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
package cn.escheduler.api.controller;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.service.ProcessDefinitionService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.model.User;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static cn.escheduler.api.enums.Status.*;
import static cn.escheduler.api.enums.Status.EXPORT_PROCESS_DEFINE_BY_ID_ERROR;


/**
 * process definition controller
 */
@Api(tags = "PROCESS_DEFINITION_TAG", position = 2)
@RestController
@RequestMapping("projects/{projectName}/process")
public class ProcessDefinitionController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionController.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * create process definition
     * 
     * @param loginUser
     * @param projectName
     * @param name
     * @param json process definition json
     * @param desc
     * @return
     */
    @ApiOperation(value = "save", notes= "CREATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processDefinitionJson", value = "PROCESS_DEFINITION_JSON", required = true, type ="String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type ="String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_DEFINITION_CONNECTS", required = true, type ="String"),
            @ApiImplicitParam(name = "desc", value = "PROCESS_DEFINITION_DESC", required = false, type ="String"),
    })
    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "processDefinitionJson", required = true) String json,
                                          @RequestParam(value = "locations", required = true) String locations,
                                          @RequestParam(value = "connects", required = true) String connects,
                                          @RequestParam(value = "desc", required = false) String desc) {

        try {
            logger.info("login user {}, create  process definition, project name: {}, process definition name: {}, " +
                            "process_definition_json: {}, desc: {} locations:{}, connects:{}",
                    loginUser.getUserName(), projectName, name, json, desc, locations, connects);
            Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser, projectName, name, json,
                    desc, locations, connects);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(CREATE_PROCESS_DEFINITION.getMsg(), e);
            return error(CREATE_PROCESS_DEFINITION.getCode(), CREATE_PROCESS_DEFINITION.getMsg());
        }
    }

    /**
     * verify process definition name unique
     * 
     * @param loginUser
     * @param projectName
     * @param name
     * @return
     */
    @ApiOperation(value = "verify-name", notes = "VERIFY_PROCCESS_DEFINITION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyProccessDefinitionName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
                                                             @RequestParam(value = "name", required = true) String name){
        try {
            logger.info("verify process definition name unique, user:{}, project name:{}, process definition name:{}",
                    loginUser.getUserName(), projectName, name);
            Map<String, Object> result = processDefinitionService.verifyProccessDefinitionName(loginUser, projectName, name);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR.getMsg(),e);
            return error(VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR.getCode(), Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR.getMsg());
        }
    }

    /**
     * update process definition
     *
     * @param loginUser
     * @param projectName
     * @param name
     * @param id
     * @param processDefinitionJson
     * @param desc
     * @return
     */
    @ApiOperation(value = "updateProccessDefinition", notes= "UPDATE_PROCCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "id", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processDefinitionJson", value = "PROCESS_DEFINITION_JSON", required = true, type ="String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type ="String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_DEFINITION_CONNECTS", required = true, type ="String"),
            @ApiImplicitParam(name = "desc", value = "PROCESS_DEFINITION_DESC", required = false, type ="String"),
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateProccessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
                                                         @RequestParam(value = "name", required = true) String name,
                                                         @RequestParam(value = "id", required = true) int id,
                                                         @RequestParam(value = "processDefinitionJson", required = true) String processDefinitionJson,
                                                         @RequestParam(value = "locations", required = false) String locations,
                                                         @RequestParam(value = "connects", required = false) String connects,
                                                         @RequestParam(value = "desc", required = false) String desc) {

        try {
            logger.info("login user {}, update process define, project name: {}, process define name: {}, " +
                            "process_definition_json: {}, desc: {}, locations:{}, connects:{}",
                    loginUser.getUserName(), projectName, name, processDefinitionJson,desc, locations, connects);
            Map<String, Object> result = processDefinitionService.updateProcessDefinition(loginUser, projectName, id, name,
                    processDefinitionJson, desc, locations, connects);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(UPDATE_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(UPDATE_PROCESS_DEFINITION_ERROR.getCode(), Status.UPDATE_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }

    /**
     * release process definition
     *
     * @param loginUser
     * @param projectName
     * @param processId
     * @param releaseState
     * @return
     */
    @ApiOperation(value = "releaseProccessDefinition", notes= "RELEASE_PROCCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "releaseState", value = "PROCESS_DEFINITION_CONNECTS", required = true, dataType = "Int", example = "100"),
    })
    @PostMapping(value = "/release")
    @ResponseStatus(HttpStatus.OK)
    public Result releaseProccessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
                                                          @RequestParam(value = "processId", required = true) int processId,
                                                          @RequestParam(value = "releaseState", required = true) int releaseState) {

        try {
            logger.info("login user {}, release process definition, project name: {}, release state: {}",
                    loginUser.getUserName(), projectName, releaseState);
            Map<String, Object> result = processDefinitionService.releaseProcessDefinition(loginUser, projectName, processId, releaseState);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(RELEASE_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(RELEASE_PROCESS_DEFINITION_ERROR.getCode(), Status.RELEASE_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }


    /**
     * query datail of process definition
     *
     * @param loginUser
     * @param projectName
     * @param processId
     * @return
     */
    @ApiOperation(value = "queryProccessDefinitionById", notes= "QUERY_PROCCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value="/select-by-id")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProccessDefinitionById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
                                              @RequestParam("processId") Integer processId
    ){
        try{
            logger.info("query datail of process definition, login user:{}, project name:{}, process definition id:{}",
                    loginUser.getUserName(), projectName, processId);
            Map<String, Object> result = processDefinitionService.queryProccessDefinitionById(loginUser, projectName, processId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR.getCode(), Status.QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }


    /**
     * query proccess definition list
     *
     * @param loginUser
     * @param projectName
     * @return
     */
    @ApiOperation(value = "queryProccessDefinitionList", notes= "QUERY_PROCCESS_DEFINITION_LIST_NOTES")
    @GetMapping(value="/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProccessDefinitionList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName
    ){
        try{
            logger.info("query proccess definition list, login user:{}, project name:{}",
                    loginUser.getUserName(), projectName);
            Map<String, Object> result = processDefinitionService.queryProccessDefinitionList(loginUser, projectName);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_PROCCESS_DEFINITION_LIST.getMsg(),e);
            return error(QUERY_PROCCESS_DEFINITION_LIST.getCode(), QUERY_PROCCESS_DEFINITION_LIST.getMsg());
        }
    }

    /**
     * query proccess definition list paging
     * @param loginUser
     * @param projectName
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "queryProcessDefinitionListPaging", notes= "QUERY_PROCCESS_DEFINITION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", required = false, type = "String"),
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = false, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProcessDefinitionListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
                                                   @RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                                   @RequestParam(value = "userId", required = false, defaultValue = "0") Integer userId,
                                                   @RequestParam("pageSize") Integer pageSize){
        try{
            logger.info("query proccess definition list paging, login user:{}, project name:{}", loginUser.getUserName(), projectName);
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != Status.SUCCESS){
                return returnDataListPaging(result);
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = processDefinitionService.queryProcessDefinitionListPaging(loginUser, projectName, searchVal, pageNo, pageSize, userId);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_PROCCESS_DEFINITION_LIST_PAGING_ERROR.getMsg(),e);
            return error(QUERY_PROCCESS_DEFINITION_LIST_PAGING_ERROR.getCode(), QUERY_PROCCESS_DEFINITION_LIST_PAGING_ERROR.getMsg());
        }
    }


    /**
     * encapsulation treeview structure
     *
     * @param loginUser
     * @param projectName
     * @param id
     * @return
     */
    @ApiOperation(value = "viewTree", notes= "VIEW_TREE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value="/view-tree")
    @ResponseStatus(HttpStatus.OK)
    public Result viewTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
                                                   @RequestParam("processId") Integer id,
                                                   @RequestParam("limit") Integer limit){
        try{
            Map<String, Object> result = processDefinitionService.viewTree(id, limit);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR.getMsg(),e);
            return error(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR.getCode(),ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR.getMsg());
        }
    }


    /**
     * 
     * get tasks list by process definition id
     *  
     *
     * @param loginUser
     * @param projectName
     * @param processDefinitionId
     * @return
     */
    @ApiOperation(value = "getNodeListByDefinitionId", notes= "GET_NODE_LIST_BY_DEFINITION_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value="gen-task-list")
    @ResponseStatus(HttpStatus.OK)
    public Result getNodeListByDefinitionId(
            @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
            @RequestParam("processDefinitionId") Integer processDefinitionId){
        try {
            logger.info("query task node name list by definitionId, login user:{}, project name:{}, id : {}",
                    loginUser.getUserName(), projectName, processDefinitionId);
            Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionId(processDefinitionId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR.getMsg(), e);
            return error(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR.getCode(), GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR.getMsg());
        }
    }

    /**
     *
     * get tasks list by process definition id
     *
     *
     * @param loginUser
     * @param projectName
     * @param processDefinitionIdList
     * @return
     */
    @ApiOperation(value = "getNodeListByDefinitionIdList", notes= "GET_NODE_LIST_BY_DEFINITION_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIdList", value = "PROCESS_DEFINITION_ID_LIST", required = true, type = "String")
    })
    @GetMapping(value="get-task-list")
    @ResponseStatus(HttpStatus.OK)
    public Result getNodeListByDefinitionIdList(
            @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
            @RequestParam("processDefinitionIdList") String processDefinitionIdList){

        try {
            logger.info("query task node name list by definitionId list, login user:{}, project name:{}, id list: {}",
                    loginUser.getUserName(), projectName, processDefinitionIdList);
            Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionIdList(processDefinitionIdList);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR.getMsg(), e);
            return error(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR.getCode(), GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR.getMsg());
        }
    }

    /**
     * delete process definition by id
     *
     * @param loginUser
     * @param projectName
     * @param processDefinitionId
     * @return
     */
    @ApiOperation(value = "deleteProcessDefinitionById", notes= "DELETE_PROCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteProcessDefinitionById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                            @RequestParam("processDefinitionId") Integer processDefinitionId
    ){
        try{
            logger.info("delete process definition by id, login user:{}, project name:{}, process definition id:{}",
                    loginUser.getUserName(), projectName, processDefinitionId);
            Map<String, Object> result = processDefinitionService.deleteProcessDefinitionById(loginUser, projectName, processDefinitionId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(DELETE_PROCESS_DEFINE_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR.getCode(), Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR.getMsg());
        }
    }

    /**
     * batch delete process definition by ids
     *
     * @param loginUser
     * @param projectName
     * @param processDefinitionIds
     * @return
     */
    @ApiOperation(value = "batchDeleteProcessDefinitionByIds", notes= "BATCH_DELETE_PROCESS_DEFINITION_BY_IDS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_IDS", type = "String")
    })
    @GetMapping(value="/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    public Result batchDeleteProcessDefinitionByIds(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                              @RequestParam("processDefinitionIds") String processDefinitionIds
    ){
        try{
            logger.info("delete process definition by ids, login user:{}, project name:{}, process definition ids:{}",
                    loginUser.getUserName(), projectName, processDefinitionIds);
            Map<String, Object> result = processDefinitionService.batchDeleteProcessDefinitionByIds(loginUser, projectName, processDefinitionIds);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR.getMsg(),e);
            return error(Status.BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR.getCode(), Status.BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR.getMsg());
        }
    }

    /**
     * export process definition by id
     *
     * @param loginUser
     * @param projectName
     * @param processDefinitionId
     * @return
     */
    @ApiOperation(value = "exportProcessDefinitionById", notes= "EXPORT_PROCCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value="/export")
    @ResponseBody
    public void exportProcessDefinitionById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @PathVariable String projectName,
                                            @RequestParam("processDefinitionId") Integer processDefinitionId,
                                            HttpServletResponse response){
        try{
            logger.info("export process definition by id, login user:{}, project name:{}, process definition id:{}",
                    loginUser.getUserName(), projectName, processDefinitionId);
            processDefinitionService.exportProcessDefinitionById(loginUser, projectName, processDefinitionId,response);
        }catch (Exception e){
            logger.error(EXPORT_PROCESS_DEFINE_BY_ID_ERROR.getMsg(),e);
        }
    }



    /**
     * query proccess definition all by project id
     *
     * @param loginUser
     * @return
     */
    @ApiOperation(value = "queryProccessDefinitionAllByProjectId", notes= "QUERY_PROCCESS_DEFINITION_All_BY_PROJECT_ID_NOTES")
    @GetMapping(value="/queryProccessDefinitionAllByProjectId")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProccessDefinitionAllByProjectId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @RequestParam("projectId") Integer projectId){
        try{
            logger.info("query proccess definition list, login user:{}, project id:{}",
                    loginUser.getUserName(),projectId);
            Map<String, Object> result = processDefinitionService.queryProccessDefinitionAllByProjectId(projectId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_PROCCESS_DEFINITION_LIST.getMsg(),e);
            return error(QUERY_PROCCESS_DEFINITION_LIST.getCode(), QUERY_PROCCESS_DEFINITION_LIST.getMsg());
        }
    }

}

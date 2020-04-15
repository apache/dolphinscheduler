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


import java.util.Map;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.GlobalVariableService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;


/**
 * variable controller
 */
@Api(tags = "GLOBAL_VARIABLE_TAG", position = 1)
@RestController
@RequestMapping("/variable")
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class GlobalVariableController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(GlobalVariableController.class);


    @Autowired
    private GlobalVariableService globalVariableService;

    @ApiOperation(value = "createVariable", notes= "CREATE_GLOBAL_VARIABLE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "VARIABLE_PROJECTID", required = true, dataType ="Int"),
            @ApiImplicitParam(name = "name", value = "VARIABLE_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "key", value = "VARIABLE_KEY", required = true, dataType ="String"),
            @ApiImplicitParam(name = "value", value = "VARIABLE_VALUE", required = true, dataType ="String")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createVariable(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestParam(value = "projectId",required = true) Integer projectId,
                                                       @RequestParam(value = "name",required = true) String name,
                                                       @RequestParam(value = "key",required = true) String key,
                                                       @RequestParam(value = "value",required = true) String value) {
        try {

            Map<String, Object> result = globalVariableService.createVariable(loginUser,projectId,name,key,value);
            return returnDataList(result);

        }catch (Exception e){
            logger.error(Status.CREATE_VARIABLE_ERROR.getMsg(),e);
            return error(Status.CREATE_VARIABLE_ERROR.getCode(), Status.CREATE_VARIABLE_ERROR.getMsg());
        }
    }


    /**
     * query variable list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return variable list page
     */
    @ApiOperation(value = "queryVariablelistPaging", notes= "QUERY_GLOBAL_VARIABLE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "PROJECT_ID", dataType ="Int"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType ="Int",example = "20")
    })
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryVariablelistPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam(value = "projectId",required = true) Integer projectId,
                                                     @RequestParam("pageNo") Integer pageNo,
                                                     @RequestParam(value = "searchVal", required = false) String searchVal,
                                                     @RequestParam("pageSize") Integer pageSize){
        logger.info("login user {}, list paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(),pageNo,searchVal,pageSize);
        try{
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != Status.SUCCESS){
                return returnDataListPaging(result);
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = globalVariableService.queryVariableList(loginUser, searchVal,projectId, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(Status.QUERY_VARIABLE_LIST_PAGING_ERROR.getMsg(),e);
            return error(Status.QUERY_VARIABLE_LIST_PAGING_ERROR.getCode(), Status.QUERY_VARIABLE_LIST_PAGING_ERROR.getMsg());
        }
    }


    /**
     * variable list
     *
     * @param loginUser login user
     * @return variable list
     */
    @ApiOperation(value = "queryVariablelist", notes= "QUERY_GLOBAL_VARIABLE_LIST_NOTES")
    @GetMapping(value="/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryVariablelist(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser ,
                                     @RequestParam(value = "projectId",required = true) Integer projectId){
        logger.info("login user {}, query variable list", loginUser.getUserName());
        try{
            Map<String, Object> result = globalVariableService.queryVariableList(loginUser,projectId,null);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.QUERY_VARIABLE_LIST_ERROR.getMsg(),e);
            return error(Status.QUERY_VARIABLE_LIST_ERROR.getCode(), Status.QUERY_VARIABLE_LIST_ERROR.getMsg());
        }
    }

    /**
     * udpate variable
     * @param loginUser
     * @param id
     * @param projectId
     * @param name
     * @param key
     * @param value
     * @return update result code
     */
    @ApiOperation(value = "updateVariable", notes= "UPDATE_GLOBAL_VARIABLE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "VARIABLE_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "projectId", value = "VARIABLE_PROJECTID", required = true, dataType ="Int"),
            @ApiImplicitParam(name = "name", value = "VARIABLE_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "key", value = "VARIABLE_KEY", required = true, dataType ="String"),
            @ApiImplicitParam(name = "value", value = "VARIABLE_VALUE", required = true, dataType ="String")

    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateVariable(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @RequestParam(value = "id") int id,
                                                        @RequestParam(value = "projectId",required = true) Integer projectId,
                                                        @RequestParam(value = "name",required = true) String name,
                                                        @RequestParam(value = "key",required = true) String key,
                                                        @RequestParam(value = "value",required = true) String value)  {

        try {
            Map<String, Object> result = globalVariableService.updateVariable(loginUser,id,name,key,value);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.UPDATE_VARIABLE_ERROR.getMsg(),e);
            return error(Status.UPDATE_VARIABLE_ERROR.getCode(), Status.UPDATE_VARIABLE_ERROR.getMsg());
        }
    }

    /**
     * select variable by id
     *
     * @param loginUser login user
     * @param id variable id
     * @return delete result code
     */
    @ApiOperation(value = "selectVariableById", notes= "GET_GLOBAL_VARIABLE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "PROJECT_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "id", value = "VARIABLE_ID", required = true, dataType ="Int", example = "100")

    })
    @PostMapping(value = "/selectVariableById")
    @ResponseStatus(HttpStatus.OK)
    public Result selectVariableById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "projectId") int projectId,
            @RequestParam(value = "id") int id) {
        logger.info("login user {}, delete variable, projectId: {} , variableId: {},", loginUser.getUserName(), projectId,id);
        try {
            Map<String, Object> result = globalVariableService.selectById(loginUser,projectId,id);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.DELETE_VARIABLE_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_VARIABLE_BY_ID_ERROR.getCode(), Status.DELETE_VARIABLE_BY_ID_ERROR.getMsg());
        }
    }




    /**
     * delete variable by id
     *
     * @param loginUser login user
     * @param id variable id
     * @return delete result code
     */
    @ApiOperation(value = "deleteVariableById", notes= "DELETE_GLOBAL_VARIABLE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "VARIABLE_ID", required = true, dataType ="Int", example = "100")

    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteVariableById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "id") int id) {
        logger.info("login user {}, delete variable, variableId: {},", loginUser.getUserName(), id);
        try {
            Map<String, Object> result = globalVariableService.deleteVariableById(loginUser,id);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.DELETE_VARIABLE_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_VARIABLE_BY_ID_ERROR.getCode(), Status.DELETE_VARIABLE_BY_ID_ERROR.getMsg());
        }
    }

    /**
     * verify variable key
     * @param loginUser
     * @param projectId
     * @param variableKey
     * @return true if variable code can user, otherwise return false
     */
    @ApiOperation(value = "verifyVariableKey", notes= "VERIFY_GLOBAL_VARIABLE_KEY_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "variableKey", value = "VARIABLE_KEY", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-variable-key")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyVariableCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value ="projectId") int projectId,
                                   @RequestParam(value ="variableKey") String variableKey
    ) {

        try{
            logger.info("login user {}, verfiy variable projectId: {} ,  key: {} ",
                    loginUser.getUserName(),projectId,variableKey);
            return globalVariableService.verifyVariableKey(projectId,variableKey);
        }catch (Exception e){
            logger.error(Status.VERIFY_VARIABLE_NAME_ERROR.getMsg(),e);
            return error(Status.VERIFY_VARIABLE_NAME_ERROR.getCode(), Status.VERIFY_VARIABLE_NAME_ERROR.getMsg());
        }
    }





}

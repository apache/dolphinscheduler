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


import static org.apache.dolphinscheduler.api.enums.Status.*;

import java.util.Map;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExtPlatformService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExtPlatformType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.ExtPlatform;
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
 * extPlatform controller
 */
@Api(tags = "EXTPLAFTORM_TAG", position = 1)
@RestController
@RequestMapping("/platform")
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class ExtPlatformController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(ExtPlatformController.class);


    @Autowired
    private ExtPlatformService extPlatformService;

    @ApiOperation(value = "createExtPlatform", notes= "CREATE_EXTPLAFTORM_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "EXTPLAFTORM_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "extPlatformType", value = "EXT_PLATFORM_TYPE", required = true, dataType ="int"),
            @ApiImplicitParam(name = "connectParam", value = "CONNECT_PARAM", required = true, dataType ="String",example = "{'url' : 'http://192.158.20.23/list','urlType' : '0' "),
            @ApiImplicitParam(name = "description", value = "EXTPLAFTORM_DESC", dataType ="String")

    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_EXTPLAFTORM_ERROR)
    public Result createExtPlatform(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestParam(value = "name") String name,
                                                       @RequestParam(value = "platformType")  int extPlatformType,
                                                       @RequestParam(value = "connectParam") String connectParam,
                                                       @RequestParam(value = "description",required = false) String description) {
        Map<String, Object> result = extPlatformService.createExtPlatform(loginUser,name,extPlatformType,connectParam,description);
        return returnDataList(result);
    }


    /**
     * query extPlatform list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return extPlatform list page
     */
    @ApiOperation(value = "queryExtPlatformlistPaging", notes= "QUERY_EXTPLAFTORM_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType ="Int",example = "20")
    })
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXTPLAFTORM_LIST_PAGING_ERROR)
    public Result queryExtPlatformlistPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam("pageNo") Integer pageNo,
                                                     @RequestParam(value = "searchVal", required = false) String searchVal,
                                                     @RequestParam("pageSize") Integer pageSize){
        logger.info("login user {}, list paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(),pageNo,searchVal,pageSize);
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if(result.get(Constants.STATUS) != Status.SUCCESS){
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = extPlatformService.queryExtPlatformList(loginUser, searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }


    /**
     * extPlatform list
     *
     * @param loginUser login user
     * @return extPlatform list
     */
    @ApiOperation(value = "queryExtPlatformlist", notes= "QUERY_EXTPLAFTORM_LIST_NOTES")
    @GetMapping(value="/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXTPLAFTORM_LIST_ERROR)
    public Result queryExtPlatformlist(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser){
        logger.info("login user {}, query extPlatform list", loginUser.getUserName());
        Map<String, Object> result = extPlatformService.queryExtPlatformList(loginUser);
        return returnDataList(result);
    }


    /**
     * udpate extPlatform
     * @param loginUser
     * @param id
     * @param name
     * @param extPlatformType
     * @param connectParam
     * @param description
     * @return
     */
    @ApiOperation(value = "updateExtPlatform", notes= "UPDATE_EXTPLAFTORM_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "EXTPLAFTORM_ID", required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "name", value = "EXTPLAFTORM_NAME", required = true, dataType ="String"),
            @ApiImplicitParam(name = "extPlatformType", value = "EXT_PLATFORM_TYPE", required = true, dataType ="int"),
            @ApiImplicitParam(name = "connectParam", value = "CONNECT_PARAM", required = true, dataType ="String",example = "{'url' : 'http://192.158.20.23/list','urlType' : '0' "),
            @ApiImplicitParam(name = "description", value = "EXTPLAFTORM_DESC", dataType ="String")

    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_EXTPLAFTORM_ERROR)
    public Result updateExtPlatform(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @RequestParam(value = "id") int id,
                                                        @RequestParam(value = "name") String name,
                                                        @RequestParam(value = "platformType")  int extPlatformType,
                                                        @RequestParam(value = "connectParam") String connectParam,
                                                        @RequestParam(value = "description",required = false) String description) {

        Map<String, Object> result = extPlatformService.updateExtPlatform(loginUser,id,name,extPlatformType,connectParam,description);
        return returnDataList(result);
    }

    /**
     * select extPlatform by id
     *
     * @param loginUser login user
     * @param id extPlatform id
     * @return delete result code
     */
    @ApiOperation(value = "selectExtPlatformById", notes= "GET_EXTPLAFTORM_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "EXTPLAFTORM_ID", required = true, dataType ="Int", example = "100")

    })
    @PostMapping(value = "/selectExtPlatformById")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_EXTPLAFTORM_BY_ID_ERROR)
    public Result selectExtPlatformById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "id") int id) {
        logger.info("login user {}, delete extPlatform, extPlatformId: {},", loginUser.getUserName(), id);
        Map<String, Object> result = extPlatformService.selectById(loginUser,id);
        return returnDataList(result);
    }




    /**
     * delete extPlatform by id
     *
     * @param loginUser login user
     * @param id extPlatform id
     * @return delete result code
     */
    @ApiOperation(value = "deleteExtPlatformById", notes= "DELETE_EXTPLAFTORM_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "EXTPLAFTORM_ID", required = true, dataType ="Int", example = "100")

    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_EXTPLAFTORM_BY_ID_ERROR)
    public Result deleteExtPlatformById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "id") int id) throws Exception {
        logger.info("login user {}, delete extPlatform, extPlatformId: {},", loginUser.getUserName(), id);
        Map<String, Object> result = extPlatformService.deleteExtPlatformById(loginUser,id);
        return returnDataList(result);
    }


    /**
     * verify extPlatform code
     *
     * @param loginUser login user
     * @param extPlatformName extPlatform code
     * @return true if extPlatform code can user, otherwise return false
     */
    @ApiOperation(value = "verifyExtPlatformName", notes= "VERIFY_EXTPLAFTORM_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "verifyExtPlatformName", value = "EXTPLAFTORM_CODE", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-extPlatform-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_EXTPLAFTORM_NAME_ERROR)
    public Result verifyExtPlatformName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value ="extPlatformName") String extPlatformName
    ) {

        logger.info("login user {}, verfiy extPlatform code: {}",
                loginUser.getUserName(),extPlatformName);
        return extPlatformService.verifyExtPlatformName(extPlatformName);
    }


    /**
     * extPlatform list
     *
     * @param loginUser login user
     * @return extPlatform list
     */
    @ApiOperation(value = "queryExtlist", notes= "QUERY_EXT_LIST_NOTES")
    @GetMapping(value="/extlist")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXTPLAFTORM_LIST_ERROR)
    public Result queryExtlist(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser, int id){
        logger.info("login user {}, query extPlatform list", loginUser.getUserName());
        Result result= extPlatformService.queryExtlist(loginUser, id);
        return result ;
    }


    /**
     * extPlatform list
     *
     * @param loginUser login user
     * @return extPlatform list
     */
    @ApiOperation(value = "queryDetail", notes= "QUERY_EXT_LIST_NOTES")
    @GetMapping(value="/extDetail")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXTPLAFTORM_LIST_ERROR)
    public Result queryExtDetail(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser, String connectParam){
        logger.info("login user {}, query extPlatform list", loginUser.getUserName());
        Result result= extPlatformService.queryExtDetail(loginUser,connectParam);
        return result ;
    }
}

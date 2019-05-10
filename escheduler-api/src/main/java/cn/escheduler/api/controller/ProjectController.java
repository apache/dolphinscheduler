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
import cn.escheduler.api.service.ProjectService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.escheduler.api.enums.Status.*;

/**
 * project controller
 */
@RestController
@RequestMapping("projects")
public class ProjectController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    /**
     * create project
     *
     * @param loginUser
     * @param projectName
     * @param desc
     * @return returns an error if it exists
     */
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createProject(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("projectName") String projectName,
                                @RequestParam(value = "desc", required = false) String desc) {

        try {
            logger.info("login user {}, create project name: {}, desc: {}", loginUser.getUserName(), projectName, desc);
            Map<String, Object> result = projectService.createProject(loginUser, projectName, desc);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(CREATE_PROJECT_ERROR.getMsg(), e);
            return error(CREATE_PROJECT_ERROR.getCode(), CREATE_PROJECT_ERROR.getMsg());
        }
    }

    /**
     * updateProcessInstance project
     *
     * @param loginUser
     * @param projectId
     * @param projectName
     * @param desc
     * @return
     */
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateProject(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("projectId") Integer projectId,
                                @RequestParam("projectName") String projectName,
                                @RequestParam(value = "desc", required = false) String desc) {
        try {
            logger.info("login user {} , updateProcessInstance project name: {}, desc: {}", loginUser.getUserName(), projectName, desc);
            Map<String, Object> result = projectService.update(loginUser, projectId, projectName, desc);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(UPDATE_PROJECT_ERROR.getMsg(), e);
            return error(UPDATE_PROJECT_ERROR.getCode(), UPDATE_PROJECT_ERROR.getMsg());
        }
    }

    /**
     * query project details by id
     *
     * @param loginUser
     * @param projectId
     * @return
     */
    @GetMapping(value = "/query-by-id")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProjectById(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("projectId") Integer projectId) {
        logger.info("login user {}, query project by id: {}", loginUser.getUserName(), projectId);

        try {
            Map<String, Object> result = projectService.queryById(projectId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_PROJECT_DETAILS_BY_ID_ERROR.getMsg(), e);
            return error(QUERY_PROJECT_DETAILS_BY_ID_ERROR.getCode(), QUERY_PROJECT_DETAILS_BY_ID_ERROR.getMsg());
        }
    }

    /**
     * query project list paging
     *
     * @param loginUser
     * @param searchVal
     * @param pageSize
     * @param pageNo
     * @return
     */
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProjectListPaging(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "searchVal", required = false) String searchVal,
                                         @RequestParam("pageSize") Integer pageSize,
                                         @RequestParam("pageNo") Integer pageNo
    ) {

        try {
            logger.info("login user {}, query project list paging", loginUser.getUserName());
            searchVal = ParameterUtils.handleEscapes(searchVal);
            Map<String, Object> result = projectService.queryProjectListPaging(loginUser, pageSize, pageNo, searchVal);
            return returnDataListPaging(result);
        } catch (Exception e) {
            logger.error(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR.getMsg(), e);
            return error(Status.LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR.getCode(), Status.LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR.getMsg());
        }
    }

    /**
     * delete project by id
     *
     * @param loginUser
     * @param projectId
     * @return
     */
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteProject(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("projectId") Integer projectId
    ) {

        try {
            logger.info("login user {}, delete project: {}.", loginUser.getUserName(), projectId);
            Map<String, Object> result = projectService.deleteProject(loginUser, projectId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(DELETE_PROJECT_ERROR.getMsg(), e);
            return error(DELETE_PROJECT_ERROR.getCode(), DELETE_PROJECT_ERROR.getMsg());
        }
    }

    /**
     * query unauthorized project
     *
     * @param loginUser
     * @param userId
     * @return
     */
    @GetMapping(value = "/unauth-project")
    @ResponseStatus(HttpStatus.OK)
    public Result queryUnauthorizedProject(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam("userId") Integer userId) {
        try {
            logger.info("login user {}, query unauthorized project by user id: {}.", loginUser.getUserName(), userId);
            Map<String, Object> result = projectService.queryUnauthorizedProject(loginUser, userId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_UNAUTHORIZED_PROJECT_ERROR.getMsg(), e);
            return error(QUERY_UNAUTHORIZED_PROJECT_ERROR.getCode(), QUERY_UNAUTHORIZED_PROJECT_ERROR.getMsg());
        }
    }


    /**
     * query authorized project
     *
     * @param loginUser
     * @param userId
     * @return
     */
    @GetMapping(value = "/authed-project")
    @ResponseStatus(HttpStatus.OK)
    public Result queryAuthorizedProject(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam("userId") Integer userId) {
        try {
            logger.info("login user {}, query authorized project by user id: {}.", loginUser.getUserName(), userId);
            Map<String, Object> result = projectService.queryAuthorizedProject(loginUser, userId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_AUTHORIZED_PROJECT.getMsg(), e);
            return error(QUERY_AUTHORIZED_PROJECT.getCode(), QUERY_AUTHORIZED_PROJECT.getMsg());
        }
    }


}

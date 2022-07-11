package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROJECT_DETAILS_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROJECT_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.project.ProjectCreateRequest;
import org.apache.dolphinscheduler.api.dto.project.ProjectQueryRequest;
import org.apache.dolphinscheduler.api.dto.project.ProjectResponse;
import org.apache.dolphinscheduler.api.dto.project.ProjectUpdateRequest;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * project controller
 */
@Api(tags = "PROJECT_TAG")
@RestController
@RequestMapping("/v2/projects")
public class ProjectV2Controller extends BaseController {

    @Autowired
    private ProjectService projectService;

    /**
     * create project
     *
     * @param loginUser            login user
     * @param projectCreateRequest projectCreateRequest
     * @return ProjectResponse ProjectResponse
     */
    @ApiOperation(value = "create", notes = "CREATE_PROJECT_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestBody ProjectCreateRequest projectCreateRequest) {
        Map<String, Object> result = projectService.createProject(loginUser, projectCreateRequest.getProjectName(),
            projectCreateRequest.getDescription());
        return new ProjectResponse(returnDataList(result));
    }

    /**
     * update project
     *
     * @param loginUser        login user
     * @param code             project code
     * @param projectUpdateReq projectUpdateRequest
     * @return result Result
     */
    @ApiOperation(value = "update", notes = "UPDATE_PROJECT_NOTES")
    @PutMapping(value = "/{code}", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROJECT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable("code") Long code,
                                @RequestBody ProjectUpdateRequest projectUpdateReq) {
        Map<String, Object> result = projectService.update(loginUser, code, projectUpdateReq.getProjectName(),
            projectUpdateReq.getDescription(), projectUpdateReq.getUserName());
        return returnDataList(result);
    }

    /**
     * query project details by project code
     *
     * @param loginUser login user
     * @param code      project code
     * @return project detail information
     */
    @ApiOperation(value = "queryProjectByCode", notes = "QUERY_PROJECT_BY_ID_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", value = "PROJECT_CODE", dataType = "Long", example = "123456")
    })
    @GetMapping(value = "/{code}", consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROJECT_DETAILS_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @PathVariable("code") long code) {
        Map<String, Object> result = projectService.queryByCode(loginUser, code);
        return new ProjectResponse(returnDataList(result));
    }

    /**
     * query project list paging
     *
     * @param loginUser       login user
     * @param projectQueryReq projectQueryReq
     * @return project list which the login user have permission to see
     */
    @ApiOperation(value = "queryProjectListPaging", notes = "QUERY_PROJECT_LIST_PAGING_NOTES")
    @GetMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         ProjectQueryRequest projectQueryReq) {
        Result result = checkPageParams(projectQueryReq.getPageNo(), projectQueryReq.getPageSize());
        if (!result.checkResult()) {
            return result;
        }
        String searchVal = ParameterUtils.handleEscapes(projectQueryReq.getSearchVal());
        result = projectService.queryProjectListPaging(loginUser, projectQueryReq.getPageSize(),
            projectQueryReq.getPageNo(), searchVal);
        return result;
    }
}

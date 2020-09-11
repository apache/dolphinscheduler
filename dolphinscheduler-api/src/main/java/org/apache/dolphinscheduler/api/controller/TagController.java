package org.apache.dolphinscheduler.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TagService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;

@Api(tags = "PROCESS_DEFINITION'TAG_TAG")
@RestController
@RequestMapping("projects/{projectName}/tag")
public class TagController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionController.class);

    @Autowired
    private TagService tagService;

    /**
     * create tag
     *
     * @param loginUser login user
     * @param projectName project name
     * @param tagname tag name
     * @return create result code
     */
    @ApiOperation(value = "createTag", notes = "CREATE_TAG_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tagName", value = "TAG_NAME", dataType = "String"),
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TAG_ERROR)
    public Result createTag (@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                             @RequestParam(value = "tagname", required = true) String tagname){
        logger.info("login user {}, create  tag, project name: {}, tag name: {} ",
                loginUser.getUserName(), projectName, tagname);
        Map<String, Object> result = tagService.createTag(loginUser, projectName, tagname);
        return returnDataList(result);
    }

    /**
     * delete tag by id
     * @param tagId tag id
     * @param loginUser   login user
     * @param projectName project name
     * @return delete result code
     */
    @ApiOperation(value = "deleteTag", notes = "DELETE_TAG_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tagId", value = "TAG_ID", required = true, dataType = "Int", example = "100"),
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TAG_ERROR)
    public Result deleteTag(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                 @RequestParam(value = "tagId") int tagId) {
        logger.info("login user {}, update tag, project name: {}, tag id: {}, ",
                loginUser.getUserName(), projectName, tagId);
        Map<String, Object> result = tagService.deleteTag(loginUser, projectName, tagId);
        return returnDataList(result);
    }

    /**
     * update Tag
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param tagId tag id
     * @param tagName tag name
     * @return update result code
     */
    @ApiOperation(value = "updateTag", notes = "UPDATE_TAG_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tagName", value = "TAG_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "tagId", value = "TAG_ID", required = true, dataType = "Int", example = "100"),
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TAG_ERROR)
    public Result updateTag(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                          @RequestParam(value = "tagName", required = true) String tagName,
                                          @RequestParam(value = "tagId", required = true) int tagId
                                          ) {

        logger.info("login user {}, update tag, project name: {}, tag name: {}, ",
                loginUser.getUserName(), projectName, tagName);
        Map<String, Object> result = tagService.updateTag(loginUser, projectName, tagId, tagName);
        return returnDataList(result);
    }

    /**
     * admin can view all tags
     *
     * @param loginUser login user
     * @param projectName project name
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return tag list which the login user have permission to see
     */
    @ApiOperation(value = "queryTagListPaging", notes = "QUERY_TAG_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", required = false, type = "String"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TAG_LIST_PAGING_ERROR)
    public Result queryTagListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                   @RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                                   @RequestParam("pageSize") Integer pageSize) {
        logger.info("query tag list paging, login user:{}, project name:{}", loginUser.getUserName(), projectName);
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = tagService.queryTagListPaging(loginUser, projectName, pageSize, pageNo, searchVal);
        return returnDataListPaging(result);
    }


}

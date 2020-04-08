package org.apache.dolphinscheduler.api.controller;


import static org.apache.dolphinscheduler.api.enums.Status.*;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.service.queue.ITaskQueue;
import org.apache.dolphinscheduler.service.queue.TaskQueueFactory;
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

@RestController
@RequestMapping("remote")
public class RemoteController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(RemoteController.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    ProcessInstanceService processInstanceService;


    @Autowired
    TaskInstanceService taskInstanceService;


    @Autowired
    private ExecutorService execService;


    @Autowired
    private ResourcesService resourceService;

    // ========================= 流程定义 接口 start ===========================================

    /**
     * create process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param json process definition json
     * @param description description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return create result code
     */
    @ApiOperation(value = "save", notes= "CREATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processDefinitionJson", value = "PROCESS_DEFINITION_JSON", required = true, type ="String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type ="String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_DEFINITION_CONNECTS", required = true, type ="String"),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, type ="String"),
    })
    @PostMapping(value = "/projects/{projectName}/process/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "processDefinitionJson", required = true) String json,
            @RequestParam(value = "locations", required = true) String locations,
            @RequestParam(value = "connects", required = true) String connects,
            @RequestParam(value = "description", required = false) String description) {

        try {
            logger.info("login user {}, create  process definition, project name: {}, process definition name: {}, " +
                            "process_definition_json: {}, desc: {} locations:{}, connects:{}",
                    loginUser.getUserName(), projectName, name, json, description, locations, connects);
            Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser, projectName, name, json,
                    description, locations, connects);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(Status.CREATE_PROCESS_DEFINITION.getMsg(), e);
            return error(Status.CREATE_PROCESS_DEFINITION.getCode(), Status.CREATE_PROCESS_DEFINITION.getMsg());
        }
    }

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    @ApiOperation(value = "verify-name", notes = "VERIFY_PROCCESS_DEFINITION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String")
    })
    @GetMapping(value = "/projects/{projectName}/process/verify-name")
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
            logger.error(Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR.getMsg(),e);
            return error(Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR.getCode(), Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR.getMsg());
        }
    }

    /**
     * update process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param id process definition id
     * @param processDefinitionJson process definition json
     * @param description description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return update result code
     */
    @ApiOperation(value = "updateProccessDefinition", notes= "UPDATE_PROCCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "id", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processDefinitionJson", value = "PROCESS_DEFINITION_JSON", required = true, type ="String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type ="String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_DEFINITION_CONNECTS", required = true, type ="String"),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, type ="String"),
    })
    @PostMapping(value = "/projects/{projectName}/process/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateProccessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME",required = true) @PathVariable String projectName,
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "id", required = true) int id,
            @RequestParam(value = "processDefinitionJson", required = true) String processDefinitionJson,
            @RequestParam(value = "locations", required = false) String locations,
            @RequestParam(value = "connects", required = false) String connects,
            @RequestParam(value = "description", required = false) String description) {

        try {
            logger.info("login user {}, update process define, project name: {}, process define name: {}, " +
                            "process_definition_json: {}, desc: {}, locations:{}, connects:{}",
                    loginUser.getUserName(), projectName, name, processDefinitionJson,description, locations, connects);
            Map<String, Object> result = processDefinitionService.updateProcessDefinition(loginUser, projectName, id, name,
                    processDefinitionJson, description, locations, connects);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(Status.UPDATE_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(Status.UPDATE_PROCESS_DEFINITION_ERROR.getCode(), Status.UPDATE_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }



    /**
     * release process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process definition id
     * @param releaseState release state
     * @return release result code
     */
    @ApiOperation(value = "releaseProccessDefinition", notes= "RELEASE_PROCCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "releaseState", value = "PROCESS_DEFINITION_CONNECTS", required = true, dataType = "Int", example = "100"),
    })
    @PostMapping(value = "/projects/{projectName}/process/release")
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
            logger.error(Status.RELEASE_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(Status.RELEASE_PROCESS_DEFINITION_ERROR.getCode(), Status.RELEASE_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }



    /**
     * query datail of process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process definition id
     * @return process definition detail
     */
    @ApiOperation(value = "queryProccessDefinitionById", notes= "QUERY_PROCCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value="/projects/{projectName}/process/select-by-id")
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
            logger.error(Status.QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(Status.QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR.getCode(), Status.QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }


    /**
     * query proccess definition list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return process definition list
     */
    @ApiOperation(value = "queryProccessDefinitionList", notes= "QUERY_PROCCESS_DEFINITION_LIST_NOTES")
    @GetMapping(value="/projects/{projectName}/process/list")
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
            logger.error(Status.QUERY_PROCCESS_DEFINITION_LIST.getMsg(),e);
            return error(Status.QUERY_PROCCESS_DEFINITION_LIST.getCode(), Status.QUERY_PROCCESS_DEFINITION_LIST.getMsg());
        }
    }

    /**
     * query proccess definition list paging
     * @param loginUser login user
     * @param projectName project name
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    @ApiOperation(value = "queryProcessDefinitionListPaging", notes= "QUERY_PROCCESS_DEFINITION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", required = false, type = "String"),
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = false, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value="/projects/{projectName}/process/list-paging")
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
            logger.error(Status.QUERY_PROCCESS_DEFINITION_LIST_PAGING_ERROR.getMsg(),e);
            return error(Status.QUERY_PROCCESS_DEFINITION_LIST_PAGING_ERROR.getCode(), Status.QUERY_PROCCESS_DEFINITION_LIST_PAGING_ERROR.getMsg());
        }
    }

    /**
     * delete process definition by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    @ApiOperation(value = "deleteProcessDefinitionById", notes= "DELETE_PROCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/projects/{projectName}/process/delete")
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
            logger.error(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR.getCode(), Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR.getMsg());
        }
    }




    // ========================= 流程定义 接口 end ===========================================




    // ========================= 流程实例 接口 start ===========================================

    /**
     * query process instance list paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefinitionId process definition id
     * @param searchVal search value
     * @param stateType state type
     * @param host host
     * @param startTime start time
     * @param endTime end time
     * @return process instance list
     */
    @ApiOperation(value = "queryProcessInstanceList", notes= "QUERY_PROCESS_INSTANCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type ="String"),
            @ApiImplicitParam(name = "executorName", value = "EXECUTOR_NAME", type ="String"),
            @ApiImplicitParam(name = "stateType", value = "EXECUTION_STATUS", type ="ExecutionStatus"),
            @ApiImplicitParam(name = "host", value = "HOST", type ="String"),
            @ApiImplicitParam(name = "startDate", value = "START_DATE", type ="String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", type ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "100")
    })
    @GetMapping(value="/projects/{projectName}/instance/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProcessInstanceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam(value = "processDefinitionId", required = false, defaultValue = "0") Integer processDefinitionId,
            @RequestParam(value = "searchVal", required = false) String searchVal,
            @RequestParam(value = "executorName", required = false) String executorName,
            @RequestParam(value = "stateType", required = false) ExecutionStatus stateType,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "startDate", required = false) String startTime,
            @RequestParam(value = "endDate", required = false) String endTime,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize){
        try{
            logger.info("query all process instance list, login user:{},project name:{}, define id:{}," +
                            "search value:{},executor name:{},state type:{},host:{},start time:{}, end time:{},page number:{}, page size:{}",
                    loginUser.getUserName(), projectName, processDefinitionId, searchVal, executorName,stateType,host,
                    startTime, endTime, pageNo, pageSize);
            searchVal = ParameterUtils.handleEscapes(searchVal);
            Map<String, Object> result = processInstanceService.queryProcessInstanceList(
                    loginUser, projectName, processDefinitionId, startTime, endTime, searchVal, executorName, stateType, host, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR.getMsg(),e);
            return error(Status.QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR.getCode(), Status.QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR.getMsg());
        }
    }

    /**
     * query task list by process instance id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return task list for the process instance
     */
    @ApiOperation(value = "queryTaskListByProcessId", notes= "QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/projects/{projectName}/instance/task-list-by-process-id")
    @ResponseStatus(HttpStatus.OK)
    public Result queryTaskListByProcessId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam("processInstanceId") Integer processInstanceId
    ) {
        try{
            logger.info("query task instance list by process instance id, login user:{}, project name:{}, process instance id:{}",
                    loginUser.getUserName(), projectName, processInstanceId);
            Map<String, Object> result = processInstanceService.queryTaskListByProcessId(loginUser, projectName, processInstanceId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getMsg(),e);
            return error(QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getCode(), QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR.getMsg());
        }
    }

    /**
     * update process instance
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceJson process instance json
     * @param processInstanceId process instance id
     * @param scheduleTime schedule time
     * @param syncDefine sync define
     * @param flag flag
     * @param locations locations
     * @param connects connects
     * @return update result code
     */
    @ApiOperation(value = "updateProcessInstance", notes= "UPDATE_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceJson", value = "PROCESS_INSTANCE_JSON", type = "String"),
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", type = "String"),
            @ApiImplicitParam(name = "syncDefine", value = "SYNC_DEFINE", type = "Boolean"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_INSTANCE_LOCATIONS", type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_INSTANCE_CONNECTS", type = "String"),
            @ApiImplicitParam(name = "flag", value = "RECOVERY_PROCESS_INSTANCE_FLAG", type = "Flag"),
    })
    @PostMapping(value="/projects/{projectName}/instance/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam( value = "processInstanceJson", required = false) String processInstanceJson,
            @RequestParam( value = "processInstanceId") Integer processInstanceId,
            @RequestParam( value = "scheduleTime", required = false) String scheduleTime,
            @RequestParam( value = "syncDefine", required = true) Boolean syncDefine,
            @RequestParam(value = "locations", required = false) String locations,
            @RequestParam(value = "connects", required = false) String connects,
            @RequestParam( value = "flag", required = false) Flag flag
    ){
        try{
            logger.info("updateProcessInstance process instance, login user:{}, project name:{}, process instance json:{}," +
                            "process instance id:{}, schedule time:{}, sync define:{}, flag:{}, locations:{}, connects:{}",
                    loginUser.getUserName(), projectName, processInstanceJson, processInstanceId, scheduleTime,
                    syncDefine, flag, locations, connects);
            Map<String, Object> result = processInstanceService.updateProcessInstance(loginUser, projectName,
                    processInstanceId, processInstanceJson, scheduleTime, syncDefine, flag, locations, connects);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(UPDATE_PROCESS_INSTANCE_ERROR.getMsg(),e);
            return error(Status.UPDATE_PROCESS_INSTANCE_ERROR.getCode(), Status.UPDATE_PROCESS_INSTANCE_ERROR.getMsg());
        }
    }

    /**
     * query process instance by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return process instance detail
     */
    @ApiOperation(value = "queryProcessInstanceById", notes= "QUERY_PROCESS_INSTANCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/projects/{projectName}/instance/select-by-id")
    @ResponseStatus(HttpStatus.OK)
    public Result queryProcessInstanceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam("processInstanceId") Integer processInstanceId
    ){
        try{
            logger.info("query process instance detail by id, login user:{},project name:{}, process instance id:{}",
                    loginUser.getUserName(), projectName, processInstanceId);
            Map<String, Object> result = processInstanceService.queryProcessInstanceById(loginUser, projectName, processInstanceId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(QUERY_PROCESS_INSTANCE_BY_ID_ERROR.getMsg(),e);
            return error(Status.QUERY_PROCESS_INSTANCE_BY_ID_ERROR.getCode(), Status.QUERY_PROCESS_INSTANCE_BY_ID_ERROR.getMsg());
        }
    }

    /**
     * delete process instance by id, at the same time,
     * delete task instance and their mapping relation data
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return delete result code
     */
    @ApiOperation(value = "deleteProcessInstanceById", notes= "DELETE_PROCESS_INSTANCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value="/projects/{projectName}/instance/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteProcessInstanceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam("processInstanceId") Integer processInstanceId
    ){
        try{
            logger.info("delete process instance by id, login user:{}, project name:{}, process instance id:{}",
                    loginUser.getUserName(), projectName, processInstanceId);
            // task queue
            ITaskQueue tasksQueue = TaskQueueFactory.getTaskQueueInstance();
            Map<String, Object> result = processInstanceService.deleteProcessInstanceById(loginUser, projectName, processInstanceId,tasksQueue);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(DELETE_PROCESS_INSTANCE_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR.getCode(), Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR.getMsg());
        }
    }



    // ========================= 流程实例 接口 end ===========================================



    // ========================= 任务实例 接口 start ===========================================

    /**
     * query task list paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param searchVal search value
     * @param taskName task name
     * @param stateType state type
     * @param host host
     * @param startTime start time
     * @param endTime end time
     * @param pageNo page number
     * @param pageSize page size
     * @return task list page
     */
    @ApiOperation(value = "queryTaskListPaging", notes= "QUERY_TASK_INSTANCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID",required = false, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type ="String"),
            @ApiImplicitParam(name = "taskName", value = "TASK_NAME", type ="String"),
            @ApiImplicitParam(name = "executorName", value = "EXECUTOR_NAME", type ="String"),
            @ApiImplicitParam(name = "stateType", value = "EXECUTION_STATUS", type ="ExecutionStatus"),
            @ApiImplicitParam(name = "host", value = "HOST", type ="String"),
            @ApiImplicitParam(name = "startDate", value = "START_DATE", type ="String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", type ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping("/projects/{projectName}/task-instance/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryTaskListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam(value = "processInstanceId", required = false, defaultValue = "0") Integer processInstanceId,
            @RequestParam(value = "searchVal", required = false) String searchVal,
            @RequestParam(value = "taskName", required = false) String taskName,
            @RequestParam(value = "executorName", required = false) String executorName,
            @RequestParam(value = "stateType", required = false) ExecutionStatus stateType,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "startDate", required = false) String startTime,
            @RequestParam(value = "endDate", required = false) String endTime,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize){

        try{
            logger.info("query task instance list, project name:{},process instance:{}, search value:{},task name:{}, executor name: {},state type:{}, host:{}, start:{}, end:{}",
                    projectName, processInstanceId, searchVal, taskName, executorName, stateType, host, startTime, endTime);
            searchVal = ParameterUtils.handleEscapes(searchVal);
            Map<String, Object> result = taskInstanceService.queryTaskListPaging(
                    loginUser, projectName, processInstanceId, taskName, executorName, startTime, endTime, searchVal, stateType, host, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(Status.QUERY_TASK_LIST_PAGING_ERROR.getMsg(),e);
            return error(Status.QUERY_TASK_LIST_PAGING_ERROR.getCode(), Status.QUERY_TASK_LIST_PAGING_ERROR.getMsg());
        }

    }


    // ========================= 任务实例 接口 end ===========================================




    // ========================= 任务执行&状态 接口 start ===========================================
    /**
     * execute process instance
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @param scheduleTime schedule time
     * @param failureStrategy failure strategy
     * @param startNodeList start nodes list
     * @param taskDependType task depend type
     * @param execType execute type
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param receivers receivers
     * @param receiversCc receivers cc
     * @param runMode run mode
     * @param processInstancePriority process instance priority
     * @param workerGroupId worker group id
     * @param timeout timeout
     * @return start process result code
     */
    @ApiOperation(value = "startProcessInstance", notes= "RUN_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", required = true, dataType ="FailureStrategy"),
            @ApiImplicitParam(name = "startNodeList", value = "START_NODE_LIST", dataType ="String"),
            @ApiImplicitParam(name = "taskDependType", value = "TASK_DEPEND_TYPE", dataType ="TaskDependType"),
            @ApiImplicitParam(name = "execType", value = "COMMAND_TYPE", dataType ="CommandType"),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE",required = true, dataType ="WarningType"),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID",required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "receivers", value = "RECEIVERS",dataType ="String" ),
            @ApiImplicitParam(name = "receiversCc", value = "RECEIVERS_CC",dataType ="String" ),
            @ApiImplicitParam(name = "runMode", value = "RUN_MODE",dataType ="RunMode" ),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", required = true, dataType = "Priority" ),
            @ApiImplicitParam(name = "workerGroupId", value = "WORKER_GROUP_ID", dataType = "Int",example = "100"),
            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataType = "Int",example = "100"),
    })
    @PostMapping(value = "/projects/{projectName}/executors/start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    public Result startProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam(value = "processDefinitionId") int processDefinitionId,
            @RequestParam(value = "scheduleTime", required = false) String scheduleTime,
            @RequestParam(value = "failureStrategy", required = true) FailureStrategy failureStrategy,
            @RequestParam(value = "startNodeList", required = false) String startNodeList,
            @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
            @RequestParam(value = "execType", required = false) CommandType execType,
            @RequestParam(value = "warningType", required = true) WarningType warningType,
            @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
            @RequestParam(value = "receivers", required = false) String receivers,
            @RequestParam(value = "receiversCc", required = false) String receiversCc,
            @RequestParam(value = "runMode", required = false) RunMode runMode,
            @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
            @RequestParam(value = "workerGroupId", required = false, defaultValue = "-1") int workerGroupId,
            @RequestParam(value = "timeout", required = false) Integer timeout) {
        try {
            logger.info("login user {}, start process instance, project name: {}, process definition id: {}, schedule time: {}, "
                            + "failure policy: {}, node name: {}, node dep: {}, notify type: {}, "
                            + "notify group id: {},receivers:{},receiversCc:{}, run mode: {},process instance priority:{}, workerGroupId: {}, timeout: {}",
                    loginUser.getUserName(), projectName, processDefinitionId, scheduleTime,
                    failureStrategy, startNodeList, taskDependType, warningType, warningGroupId,receivers,receiversCc,runMode,processInstancePriority,
                    workerGroupId, timeout);

            if (timeout == null) {
                timeout = Constants.MAX_TASK_TIMEOUT;
            }

            Map<String, Object> result = execService.execProcessInstance(loginUser, projectName, processDefinitionId, scheduleTime, execType, failureStrategy,
                    startNodeList, taskDependType, warningType,
                    warningGroupId,receivers,receiversCc, runMode,processInstancePriority, workerGroupId, timeout);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(Status.START_PROCESS_INSTANCE_ERROR.getMsg(),e);
            return error(Status.START_PROCESS_INSTANCE_ERROR.getCode(), Status.START_PROCESS_INSTANCE_ERROR.getMsg());
        }
    }


    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param executeType execute type
     * @return execute result code
     */
    @ApiOperation(value = "execute", notes= "EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "executeType", value = "EXECUTE_TYPE", required = true, dataType = "ExecuteType")
    })
    @PostMapping(value = "/projects/{projectName}/executors/execute")
    @ResponseStatus(HttpStatus.OK)
    public Result execute(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam("processInstanceId") Integer processInstanceId,
            @RequestParam("executeType") ExecuteType executeType
    ) {
        try {
            logger.info("execute command, login user: {}, project:{}, process instance id:{}, execute type:{}",
                    loginUser.getUserName(), projectName, processInstanceId, executeType);
            Map<String, Object> result = execService.execute(loginUser, projectName, processInstanceId, executeType);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(Status.EXECUTE_PROCESS_INSTANCE_ERROR.getMsg(),e);
            return error(Status.EXECUTE_PROCESS_INSTANCE_ERROR.getCode(), Status.EXECUTE_PROCESS_INSTANCE_ERROR.getMsg());
        }
    }

    /**
     * check process definition and all of the son process definitions is on line.
     *
     * @param loginUser login user
     * @param processDefinitionId process definition id
     * @return check result code
     */
    @ApiOperation(value = "startCheckProcessDefinition", notes= "START_CHECK_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping(value = "/projects/{projectName}/executors/start-check")
    @ResponseStatus(HttpStatus.OK)
    public Result startCheckProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "processDefinitionId") int processDefinitionId) {
        logger.info("login user {}, check process definition {}", loginUser.getUserName(), processDefinitionId);
        try {
            Map<String, Object> result = execService.startCheckByProcessDefinedId(processDefinitionId);
            return returnDataList(result);

        } catch (Exception e) {
            logger.error(Status.CHECK_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(Status.CHECK_PROCESS_DEFINITION_ERROR.getCode(), Status.CHECK_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }



    // ========================= 任务执行&状态 end ===========================================



    // ========================= 文件上传 start ===========================================
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
    @PostMapping(value = "/resources/create")
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
    @PostMapping(value = "/resources/update")
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
    @GetMapping(value="/resources/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryResourceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value ="type") ResourceType type
    ){
        try{
            logger.info("query resource list, login user:{}, resource type:{}", loginUser.getUserName(), type);
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
    @GetMapping(value="/resources/list-paging")
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
                    loginUser.getUserName(), type, searchVal);
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
    @GetMapping(value = "/resources/delete")
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
    @GetMapping(value = "/resources/verify-name")
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
    @GetMapping(value = "/resources/download")
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



    // ========================= 文件上传 end ===========================================



}

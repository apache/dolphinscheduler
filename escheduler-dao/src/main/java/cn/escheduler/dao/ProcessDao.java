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
package cn.escheduler.dao;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.*;
import cn.escheduler.common.model.DateInterval;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.queue.ITaskQueue;
import cn.escheduler.common.queue.TaskQueueFactory;
import cn.escheduler.common.task.subprocess.SubProcessParameters;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.common.utils.IpUtils;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.mapper.*;
import cn.escheduler.dao.model.*;
import cn.escheduler.dao.utils.cron.CronUtils;
import com.alibaba.fastjson.JSONObject;
import com.cronutils.model.Cron;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static cn.escheduler.common.Constants.*;
import static cn.escheduler.dao.datasource.ConnectionFactory.getMapper;

/**
 * process relative dao that some mappers in this.
 */
@Component
public class ProcessDao extends AbstractBaseDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int[] stateArray = new int[]{ExecutionStatus.SUBMITTED_SUCCESS.ordinal(),
            ExecutionStatus.RUNNING_EXEUTION.ordinal(),
            ExecutionStatus.READY_PAUSE.ordinal(),
//            ExecutionStatus.NEED_FAULT_TOLERANCE.ordinal(),
            ExecutionStatus.READY_STOP.ordinal()};

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProcessDefinitionMapper processDefineMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private ProcessInstanceMapMapper processInstanceMapMapper;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    @Autowired
    private ErrorCommandMapper errorCommandMapper;

    @Autowired
    private WorkerServerMapper workerServerMapper;

    @Autowired
    private TenantMapper tenantMapper;

    /**
     * task queue impl
     */
    protected ITaskQueue taskQueue;

    public ProcessDao(){
        init();
    }

    /**
     * initialize
     */
    @Override
    protected void init() {
        userMapper = getMapper(UserMapper.class);
        processDefineMapper = getMapper(ProcessDefinitionMapper.class);
        processInstanceMapper = getMapper(ProcessInstanceMapper.class);
        dataSourceMapper = getMapper(DataSourceMapper.class);
        processInstanceMapMapper = getMapper(ProcessInstanceMapMapper.class);
        taskInstanceMapper = getMapper(TaskInstanceMapper.class);
        commandMapper = getMapper(CommandMapper.class);
        scheduleMapper = getMapper(ScheduleMapper.class);
        udfFuncMapper = getMapper(UdfFuncMapper.class);
        resourceMapper = getMapper(ResourceMapper.class);
        workerGroupMapper = getMapper(WorkerGroupMapper.class);
        workerServerMapper = getMapper(WorkerServerMapper.class);
        taskQueue = TaskQueueFactory.getTaskQueueInstance();
        tenantMapper = getMapper(TenantMapper.class);
    }


    /**
     * find one command from command queue, construct process instance
     * @param logger
     * @param host
     * @param validThreadNum
     * @return
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public ProcessInstance scanCommand(Logger logger, String host, int validThreadNum){

        ProcessInstance processInstance = null;
        Command command = findOneCommand();
        if (command == null) {
            return null;
        }
        logger.info(String.format("find one command: id: %d, type: %s", command.getId(),command.getCommandType().toString()));

        try{
            processInstance = constructProcessInstance(command, host);
            //cannot construct process instance, return null;
            if(processInstance == null){
                logger.error("scan command, command parameter is error: %s", command.toString());
                delCommandByid(command.getId());
                saveErrorCommand(command, "process instance is null");
                return null;
            }else if(!checkThreadNum(command, validThreadNum)){
                    logger.info("there is not enough thread for this command: {}",command.toString() );
                    return setWaitingThreadProcess(command, processInstance);
            }else{
                    processInstance.setCommandType(command.getCommandType());
                    processInstance.addHistoryCmd(command.getCommandType());
                    saveProcessInstance(processInstance);
                    this.setSubProcessParam(processInstance);
                    delCommandByid(command.getId());
                    return processInstance;
            }
        }catch (Exception e){
            logger.error("scan command error ", e);
            saveErrorCommand(command, e.toString());
            delCommandByid(command.getId());
        }
        return null;
    }

    private void saveErrorCommand(Command command, String message) {

        ErrorCommand errorCommand = new ErrorCommand(command, message);
        this.errorCommandMapper.insert(errorCommand);
    }

    /**
     * set process waiting thread
     * @param command
     * @param processInstance
     * @return
     */
    private ProcessInstance setWaitingThreadProcess(Command command, ProcessInstance processInstance) {
        processInstance.setState(ExecutionStatus.WAITTING_THREAD);
        if(command.getCommandType() != CommandType.RECOVER_WAITTING_THREAD){
            processInstance.addHistoryCmd(command.getCommandType());
        }
        saveProcessInstance(processInstance);
        this.setSubProcessParam(processInstance);
        createRecoveryWaitingThreadCommand(command, processInstance);
        return null;
    }

    private boolean checkThreadNum(Command command, int validThreadNum) {
        int commandThreadCount = this.workProcessThreadNumCount(command.getProcessDefinitionId());
        return validThreadNum >= commandThreadCount;
    }

    /**
     * insert one command
     */
    public int createCommand(Command command) {
        int result = 0;
        if (command != null){
            result = commandMapper.insert(command);
        }
        return result;
    }

    /**
     *
     * find one command from queue list
     * @return
     */
    public Command findOneCommand(){
        return commandMapper.queryOneCommand();
    }

    /**
     * check the input command exists in queue list
     * @param command
     * @return
     */
    public Boolean verifyIsNeedCreateCommand(Command command){
        Boolean isNeedCreate = true;
        Map<CommandType,Integer> cmdTypeMap = new HashMap<CommandType,Integer>();
        cmdTypeMap.put(CommandType.REPEAT_RUNNING,1);
        cmdTypeMap.put(CommandType.RECOVER_SUSPENDED_PROCESS,1);
        cmdTypeMap.put(CommandType.START_FAILURE_TASK_PROCESS,1);
        CommandType commandType = command.getCommandType();

        if(cmdTypeMap.containsKey(commandType)){
            JSONObject cmdParamObj = (JSONObject) JSONObject.parse(command.getCommandParam());
            JSONObject tempObj;
            int processInstanceId = cmdParamObj.getInteger(CMDPARAM_RECOVER_PROCESS_ID_STRING);

            List<Command> commands = commandMapper.queryAllCommand();
            //遍历所有命令
            for (Command tmpCommand:commands){
                if(cmdTypeMap.containsKey(tmpCommand.getCommandType())){
                    tempObj = (JSONObject) JSONObject.parse(tmpCommand.getCommandParam());
                    if(tempObj != null && processInstanceId == tempObj.getInteger(CMDPARAM_RECOVER_PROCESS_ID_STRING)){
                        isNeedCreate = false;
                        break;
                    }
                }
            }
        }
        return  isNeedCreate;
    }

    /**
     * find process instance detail by id
     * @param processId
     * @return
     */
    public ProcessInstance findProcessInstanceDetailById(int processId){
        return processInstanceMapper.queryDetailById(processId);
    }

    /**
     * find process instance by id
     * @param processId
     * @return
     */
    public ProcessInstance findProcessInstanceById(int processId){

        return processInstanceMapper.queryById(processId);
    }

    /**
     * find process instance by scheduler time.
     * @param defineId
     * @param scheduleTime
     * @return
     */
    public ProcessInstance findProcessInstanceByScheduleTime(int defineId, Date scheduleTime){

        return processInstanceMapper.queryByScheduleTime(defineId,
                DateUtils.dateToString(scheduleTime), 0, null, null);
    }

    /**
     * find process define by id.
     * @param processDefinitionId
     * @return
     */
    public ProcessDefinition findProcessDefineById(int processDefinitionId) {
        return processDefineMapper.queryByDefineId(processDefinitionId);
    }

    /**
     * delete work process instance by id
     * @param processInstanceId
     * @return
     */
    public int deleteWorkProcessInstanceById(int processInstanceId){
        return processInstanceMapper.delete(processInstanceId);
    }

    /**
     *
     * delete all sub process by parent instance id
     * @return
     */
    public int deleteAllSubWorkProcessByParentId(int processInstanceId){

        List<Integer> subProcessIdList = processInstanceMapper.querySubIdListByParentId(processInstanceId);

        for(Integer subId : subProcessIdList ){
            deleteAllSubWorkProcessByParentId(subId);
            deleteWorkProcessMapByParentId(subId);
            deleteWorkProcessInstanceById(subId);
        }
        return 1;
    }

    /**
     * create process define
     * @param processDefinition
     * @return
     */
    public int createProcessDefine(ProcessDefinition processDefinition){
        int count = 0;
        if(processDefinition != null){
            count = this.processDefineMapper.insert(processDefinition);
        }
        return count;
    }


    /**
     * calculate sub process number in the process define.
     * @param processDefinitionId
     * @return
     */
    private Integer workProcessThreadNumCount(Integer processDefinitionId){
        List<String> ids = new ArrayList<>();
        recurseFindSubProcessId(processDefinitionId, ids);
        return ids.size()+1;
    }

    /**
     * recursive query sub process definition id by parent id.
     * @param parentId
     * @param ids
     */
    public void recurseFindSubProcessId(int parentId, List<String> ids){
        ProcessDefinition processDefinition = processDefineMapper.queryByDefineId(parentId);
        String processDefinitionJson = processDefinition.getProcessDefinitionJson();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        List<TaskNode> taskNodeList = processData.getTasks();

        if (taskNodeList != null && taskNodeList.size() > 0){

            for (TaskNode taskNode : taskNodeList){
                String parameter = taskNode.getParams();
                if (parameter.contains(CMDPARAM_SUB_PROCESS_DEFINE_ID)){
                    SubProcessParameters subProcessParam = JSONObject.parseObject(parameter, SubProcessParameters.class);
                    ids.add(String.valueOf(subProcessParam.getProcessDefinitionId()));
                    recurseFindSubProcessId(subProcessParam.getProcessDefinitionId(),ids);
                }
            }
        }
    }

    /**
     * create recovery waiting thread command when thread pool is not enough for the process instance.
     * sub work process instance need not to create recovery command.
     * create recovery waiting thread  command and delete origin command at the same time.
     * if the recovery command is exists, only update the field update_time
     * @param originCommand
     * @param processInstance
     */
    public void createRecoveryWaitingThreadCommand(Command originCommand, ProcessInstance processInstance) {

        // sub process doesnot need to create wait command
        if(processInstance.getIsSubProcess() == Flag.YES){
            if(originCommand != null){
                commandMapper.delete(originCommand.getId());
            }
            return;
        }
        Map<String, String> cmdParam = new HashMap<>();
        cmdParam.put(Constants.CMDPARAM_RECOVERY_WAITTING_THREAD, String.valueOf(processInstance.getId()));
        // process instance quit by "waiting thread" state
        if(originCommand == null){
            Command command = new Command(
                    CommandType.RECOVER_WAITTING_THREAD,
                    processInstance.getTaskDependType(),
                    processInstance.getFailureStrategy(),
                    processInstance.getExecutorId(),
                    processInstance.getProcessDefinitionId(),
                    JSONUtils.toJson(cmdParam),
                    processInstance.getWarningType(),
                    processInstance.getWarningGroupId(),
                    processInstance.getScheduleTime(),
                    processInstance.getProcessInstancePriority()
            );
            saveCommand(command);
            return ;
        }

        // update the command time if current command if recover from waiting
        if(originCommand.getCommandType() == CommandType.RECOVER_WAITTING_THREAD){
            originCommand.setUpdateTime(new Date());
            saveCommand(originCommand);
        }else{
            // delete old command and create new waiting thread command
            commandMapper.delete(originCommand.getId());
            originCommand.setId(0);
            originCommand.setCommandType(CommandType.RECOVER_WAITTING_THREAD);
            originCommand.setUpdateTime(new Date());
            originCommand.setCommandParam(JSONUtils.toJson(cmdParam));
            originCommand.setProcessInstancePriority(processInstance.getProcessInstancePriority());
            saveCommand(originCommand);
        }
    }

    /**
     * get schedule time from command
     * @param command
     * @param cmdParam
     * @return
     */
    private Date getScheduleTime(Command command, Map<String, String> cmdParam){
        Date scheduleTime = command.getScheduleTime();
        if(scheduleTime == null){
            if(cmdParam != null && cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE)){
                scheduleTime = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
            }
        }
        return scheduleTime;
    }

    /**
     * generate a new work process instance from command.
     * @param processDefinition
     * @param command
     * @param cmdParam
     * @return
     */
    private ProcessInstance generateNewProcessInstance(ProcessDefinition processDefinition,
                                                       Command command,
                                                       Map<String, String> cmdParam){
        ProcessInstance processInstance = new ProcessInstance(processDefinition);
        processInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
        processInstance.setRecovery(Flag.NO);
        processInstance.setStartTime(new Date());
        processInstance.setRunTimes(1);
        processInstance.setMaxTryTimes(0);
        processInstance.setProcessDefinitionId(command.getProcessDefinitionId());
        processInstance.setCommandParam(command.getCommandParam());
        processInstance.setCommandType(command.getCommandType());
        processInstance.setIsSubProcess(Flag.NO);
        processInstance.setTaskDependType(command.getTaskDependType());
        processInstance.setFailureStrategy(command.getFailureStrategy());
        processInstance.setExecutorId(command.getExecutorId());
        WarningType warningType = command.getWarningType() == null ? WarningType.NONE : command.getWarningType();
        processInstance.setWarningType(warningType);
        Integer warningGroupId = command.getWarningGroupId() == null ? 0 : command.getWarningGroupId();
        processInstance.setWarningGroupId(warningGroupId);

        // schedule time
        Date scheduleTime = getScheduleTime(command, cmdParam);
        if(scheduleTime != null){
            processInstance.setScheduleTime(scheduleTime);
        }
        processInstance.setCommandStartTime(command.getStartTime());
        processInstance.setLocations(processDefinition.getLocations());
        processInstance.setConnects(processDefinition.getConnects());
        // curing global params
        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                getCommandTypeIfComplement(processInstance, command),
                processInstance.getScheduleTime()));

        //copy process define json to process instance
        processInstance.setProcessInstanceJson(processDefinition.getProcessDefinitionJson());
        // set process instance priority
        processInstance.setProcessInstancePriority(command.getProcessInstancePriority());
        int workerGroupId = command.getWorkerGroupId() == 0 ? -1 : command.getWorkerGroupId();
        processInstance.setWorkerGroupId(workerGroupId);
        processInstance.setTimeout(processDefinition.getTimeout());
        processInstance.setTenantId(processDefinition.getTenantId());
        return processInstance;
    }

    /**
     * get process tenant
     * there is tenant id in definition, use the tenant of the definition.
     * if there is not tenant id in the definiton or the tenant not exist
     * use definition creator's tenant.
     * @param tenantId
     * @param userId
     * @return
     */
    public Tenant getTenantForProcess(int tenantId, int userId){
        Tenant tenant = null;
        if(tenantId >= 0){
            tenant = tenantMapper.queryById(tenantId);
        }
        if(tenant == null){
            User user = userMapper.queryById(userId);
            tenant = tenantMapper.queryById(user.getTenantId());
        }
        return tenant;
    }

    /**
     * check command parameters is valid
     * @param command
     * @param cmdParam
     * @return
     */
    private Boolean checkCmdParam(Command command, Map<String, String> cmdParam){
        if(command.getTaskDependType() == TaskDependType.TASK_ONLY || command.getTaskDependType()== TaskDependType.TASK_PRE){
            if(cmdParam == null
                    || !cmdParam.containsKey(Constants.CMDPARAM_START_NODE_NAMES)
                    || cmdParam.get(Constants.CMDPARAM_START_NODE_NAMES).isEmpty()){
                logger.error(String.format("command node depend type is %s, but start nodes is null ", command.getTaskDependType().toString()));
                return false;
            }
        }
        return true;
    }

    /**
     * construct process instance according to one command.
     * @param command
     * @param host
     * @return
     */
    private ProcessInstance constructProcessInstance(Command command, String host){

        ProcessInstance processInstance = null;
        CommandType commandType = command.getCommandType();
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());

        ProcessDefinition processDefinition = null;
        if(command.getProcessDefinitionId() != 0){
            processDefinition = processDefineMapper.queryByDefineId(command.getProcessDefinitionId());
            if(processDefinition == null){
                logger.error(String.format("cannot find the work process define! define id : %d", command.getProcessDefinitionId()));
                return null;
            }
        }

        if(cmdParam != null ){
            Integer processInstanceId = 0;
            // recover from failure or pause tasks
            if(cmdParam.containsKey(Constants.CMDPARAM_RECOVER_PROCESS_ID_STRING)) {
                String processId = cmdParam.get(Constants.CMDPARAM_RECOVER_PROCESS_ID_STRING);
                processInstanceId = Integer.parseInt(processId);
                if (processInstanceId == 0) {
                    logger.error("command parameter is error, [ ProcessInstanceId ] is 0");
                    return null;
                }
            }else if(cmdParam.containsKey(Constants.CMDPARAM_SUB_PROCESS)){
                // sub process map
                String pId = cmdParam.get(Constants.CMDPARAM_SUB_PROCESS);
                processInstanceId = Integer.parseInt(pId);
            }else if(cmdParam.containsKey(Constants.CMDPARAM_RECOVERY_WAITTING_THREAD)){
                // waiting thread command
                String pId = cmdParam.get(Constants.CMDPARAM_RECOVERY_WAITTING_THREAD);
                processInstanceId = Integer.parseInt(pId);
            }
            if(processInstanceId ==0){
                processInstance = generateNewProcessInstance(processDefinition, command, cmdParam);
            }else{
                processInstance = this.findProcessInstanceDetailById(processInstanceId);
            }
            processDefinition = processDefineMapper.queryByDefineId(processInstance.getProcessDefinitionId());
            processInstance.setProcessDefinition(processDefinition);

            //reset command parameter
            if(processInstance.getCommandParam() != null){
                Map<String, String> processCmdParam = JSONUtils.toMap(processInstance.getCommandParam());
                for(String key : processCmdParam.keySet()){
                    if(!cmdParam.containsKey(key)){
                        cmdParam.put(key,processCmdParam.get(key));
                    }
                }
            }
            // reset command parameter if sub process
            if(cmdParam.containsKey(Constants.CMDPARAM_SUB_PROCESS)){
                processInstance.setCommandParam(command.getCommandParam());
            }
        }else{
            // generate one new process instance
            processInstance = generateNewProcessInstance(processDefinition, command, cmdParam);
        }
        if(!checkCmdParam(command, cmdParam)){
            logger.error("command parameter check failed!");
            return null;
        }

        if(command.getScheduleTime() != null){
            processInstance.setScheduleTime(command.getScheduleTime());
        }
        processInstance.setHost(host);

        ExecutionStatus runStatus = ExecutionStatus.RUNNING_EXEUTION;
        int runTime = processInstance.getRunTimes();
        switch (commandType){
            case START_PROCESS:
                break;
            case START_FAILURE_TASK_PROCESS:
                // find failed tasks and init these tasks
                List<Integer> failedList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.FAILURE);
                List<Integer> toleranceList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.NEED_FAULT_TOLERANCE);
                List<Integer> killedList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.KILL);
                cmdParam.remove(Constants.CMDPARAM_RECOVERY_START_NODE_STRING);

                failedList.addAll(killedList);
                failedList.addAll(toleranceList);
                for(Integer taskId : failedList){
                    initTaskInstance(this.findTaskInstanceById(taskId));
                }
                cmdParam.put(Constants.CMDPARAM_RECOVERY_START_NODE_STRING,
                        String.join(Constants.COMMA, convertIntListToString(failedList)));
                processInstance.setCommandParam(JSONUtils.toJson(cmdParam));
                processInstance.setRunTimes(runTime +1 );
                break;
            case START_CURRENT_TASK_PROCESS:
                break;
            case RECOVER_WAITTING_THREAD:
                break;
            case RECOVER_SUSPENDED_PROCESS:
                // find pause tasks and init task's state
                cmdParam.remove(Constants.CMDPARAM_RECOVERY_START_NODE_STRING);
                List<Integer> suspendedNodeList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.PAUSE);
                List<Integer> stopNodeList = findTaskIdByInstanceState(processInstance.getId(),
                        ExecutionStatus.KILL);
                suspendedNodeList.addAll(stopNodeList);
                for(Integer taskId : suspendedNodeList){
                    // 把暂停状态初始化
                    initTaskInstance(this.findTaskInstanceById(taskId));
                }
                cmdParam.put(Constants.CMDPARAM_RECOVERY_START_NODE_STRING, String.join(",", convertIntListToString(suspendedNodeList)));
                processInstance.setCommandParam(JSONUtils.toJson(cmdParam));
                processInstance.setRunTimes(runTime +1);
                break;
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                // recover tolerance fault process
                processInstance.setRecovery(Flag.YES);
                runStatus = processInstance.getState();
                break;
            case COMPLEMENT_DATA:
                // delete all the valid tasks when complement data
                List<TaskInstance> taskInstanceList = this.findValidTaskListByProcessId(processInstance.getId());
                for(TaskInstance taskInstance : taskInstanceList){
                    taskInstance.setFlag(Flag.NO);
                    this.updateTaskInstance(taskInstance);
                }
                break;
            case REPEAT_RUNNING:
                // delete the recover task names from command parameter
                if(cmdParam.containsKey(Constants.CMDPARAM_RECOVERY_START_NODE_STRING)){
                    cmdParam.remove(Constants.CMDPARAM_RECOVERY_START_NODE_STRING);
                    processInstance.setCommandParam(JSONUtils.toJson(cmdParam));
                }
                // delete all the valid tasks when repeat running
                List<TaskInstance> validTaskList = findValidTaskListByProcessId(processInstance.getId());
                for(TaskInstance taskInstance : validTaskList){
                    taskInstance.setFlag(Flag.NO);
                    updateTaskInstance(taskInstance);
                }
                processInstance.setStartTime(new Date());
                processInstance.setEndTime(null);
                processInstance.setRunTimes(runTime +1);
                initComplementDataParam(processDefinition, processInstance, cmdParam);
                break;
            case SCHEDULER:
                break;
            default:
                break;
        }
        processInstance.setState(runStatus);
        return processInstance;
    }

    /**
     * return complement data if the process start with complement data
     */
    private CommandType getCommandTypeIfComplement(ProcessInstance processInstance, Command command){
        if(CommandType.COMPLEMENT_DATA == processInstance.getCmdTypeIfComplement()){
            return CommandType.COMPLEMENT_DATA;
        }else{
            return command.getCommandType();
        }
    }

    /**
     * initialize complement data parameters
     * @param processDefinition
     * @param processInstance
     * @param cmdParam
     */
    private void initComplementDataParam(ProcessDefinition processDefinition, ProcessInstance processInstance, Map<String, String> cmdParam) {
        if(!processInstance.isComplementData()){
            return;
        }

        Date startComplementTime = DateUtils.parse(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE),
                YYYY_MM_DD_HH_MM_SS);
        processInstance.setScheduleTime(startComplementTime);
        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime()));

    }

    /**
     * set sub work process parameters.
     * handle sub work process instance, update relation table and command parameters
     * set sub work process flag, extends parent work process command parameters.
     */
    public ProcessInstance setSubProcessParam(ProcessInstance subProcessInstance){
        String cmdParam = subProcessInstance.getCommandParam();
        if(StringUtils.isEmpty(cmdParam)){
            return subProcessInstance;
        }
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);
        // write sub process id into cmd param.
        if(paramMap.containsKey(CMDPARAM_SUB_PROCESS)
                && CMDPARAM_EMPTY_SUB_PROCESS.equals(paramMap.get(CMDPARAM_SUB_PROCESS))){
            paramMap.remove(CMDPARAM_SUB_PROCESS);
            paramMap.put(CMDPARAM_SUB_PROCESS, String.valueOf(subProcessInstance.getId()));
            subProcessInstance.setCommandParam(JSONUtils.toJson(paramMap));
            subProcessInstance.setIsSubProcess(Flag.YES);
            this.saveProcessInstance(subProcessInstance);
        }
        // copy parent instance user def params to sub process..
        String parentInstanceId = paramMap.get(CMDPARAM_SUB_PROCESS_PARENT_INSTANCE_ID);
        if(StringUtils.isNotEmpty(parentInstanceId)){
            ProcessInstance parentInstance = findProcessInstanceDetailById(Integer.parseInt(parentInstanceId));
            if(parentInstance != null){
                subProcessInstance.setGlobalParams(
                        joinGlobalParams(parentInstance.getGlobalParams(), subProcessInstance.getGlobalParams()));
                this.saveProcessInstance(subProcessInstance);
            }else{
                logger.error("sub process command params error, cannot find parent instance: {} ", cmdParam);
            }
        }
        ProcessInstanceMap processInstanceMap = JSONUtils.parseObject(cmdParam, ProcessInstanceMap.class);
        if(processInstanceMap == null || processInstanceMap.getParentProcessInstanceId() == 0){
            return subProcessInstance;
        }
        // update sub process id to process map table
        processInstanceMap.setProcessInstanceId(subProcessInstance.getId());

        this.updateWorkProcessInstanceMap(processInstanceMap);
        return subProcessInstance;
    }

    /**
     * join parent global params into sub process.
     *  only the keys doesn't in sub process global would be joined.
     * @param parentGlobalParams
     * @param subGlobalParams
     * @return
     */
    private String joinGlobalParams(String parentGlobalParams, String subGlobalParams){
        List<Property> parentPropertyList = JSONUtils.toList(parentGlobalParams, Property.class);
        List<Property> subPropertyList = JSONUtils.toList(subGlobalParams, Property.class);
        Map<String,String> subMap = subPropertyList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));

        for(Property parent : parentPropertyList){
            if(!subMap.containsKey(parent.getProp())){
                subPropertyList.add(parent);
            }
        }
        return JSONUtils.toJson(subPropertyList);
    }

    /**
     * initialize task instance
     * @param taskInstance
     */
    private void initTaskInstance(TaskInstance taskInstance){

        if(!taskInstance.isSubProcess()){
            if(taskInstance.getState().typeIsCancel() || taskInstance.getState().typeIsFailure()){
                taskInstance.setFlag(Flag.NO);
                updateTaskInstance(taskInstance);
                return;
            }
        }
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        updateTaskInstance(taskInstance);
    }

    /**
     *  submit task to mysql and task queue
     *  submit sub process to command
     * @param taskInstance
     * @return
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public TaskInstance submitTask(TaskInstance taskInstance, ProcessInstance processInstance){
        logger.info("start submit task : {}, instance id:{}, state: {}, ",
                taskInstance.getName(), processInstance.getId(), processInstance.getState() );
        processInstance = this.findProcessInstanceDetailById(processInstance.getId());
        //submit to mysql
        TaskInstance task= submitTaskInstanceToMysql(taskInstance, processInstance);
        if(task.isSubProcess() && !task.getState().typeIsFinished()){
            ProcessInstanceMap processInstanceMap = setProcessInstanceMap(processInstance, task);

            TaskNode taskNode = JSONUtils.parseObject(task.getTaskJson(), TaskNode.class);
            Map<String, String> subProcessParam = JSONUtils.toMap(taskNode.getParams());
            Integer defineId = Integer.parseInt(subProcessParam.get(Constants.CMDPARAM_SUB_PROCESS_DEFINE_ID));
            createSubWorkProcessCommand(processInstance, processInstanceMap, defineId, task);
        }else if(!task.getState().typeIsFinished()){
            //submit to task queue
            task.setProcessInstancePriority(processInstance.getProcessInstancePriority());
            submitTaskToQueue(task);
        }
        logger.info("submit task :{} state:{} complete, instance id:{} state: {}  ",
                taskInstance.getName(), task.getState(), processInstance.getId(), processInstance.getState());
        return task;
    }

    /**
     * set work process instance map
     * @param parentInstance
     * @param parentTask
     * @return
     */
    private ProcessInstanceMap setProcessInstanceMap(ProcessInstance parentInstance, TaskInstance parentTask){
        ProcessInstanceMap processMap = findWorkProcessMapByParent(parentInstance.getId(), parentTask.getId());
        if(processMap != null){
            return processMap;
        }else if(parentInstance.getCommandType() == CommandType.REPEAT_RUNNING
                || parentInstance.isComplementData()){
            // update current task id to map
            // repeat running  does not generate new sub process instance
            processMap = findPreviousTaskProcessMap(parentInstance, parentTask);
            if(processMap!= null){
                processMap.setParentTaskInstanceId(parentTask.getId());
                updateWorkProcessInstanceMap(processMap);
                return processMap;
            }
        }
        // new task
        processMap = new ProcessInstanceMap();
        processMap.setParentProcessInstanceId(parentInstance.getId());
        processMap.setParentTaskInstanceId(parentTask.getId());
        createWorkProcessInstanceMap(processMap);
        return processMap;
    }

    /**
     * find previous task work process map.
     * @param parentProcessInstance
     * @param parentTask
     * @return
     */
    private ProcessInstanceMap findPreviousTaskProcessMap(ProcessInstance parentProcessInstance,
                                                          TaskInstance parentTask) {

        Integer preTaskId = 0;
        List<TaskInstance> preTaskList = this.findPreviousTaskListByWorkProcessId(parentProcessInstance.getId());
        for(TaskInstance task : preTaskList){
            if(task.getName().equals(parentTask.getName())){
                preTaskId = task.getId();
                ProcessInstanceMap map = findWorkProcessMapByParent(parentProcessInstance.getId(), preTaskId);
                if(map!=null){
                    return map;
                }
            }
        }
        logger.info("sub process instance is not found,parent task:{},parent instance:{}",
                parentTask.getId(), parentProcessInstance.getId());
        return null;
    }

    /**
     * create sub work process command
     * @param parentProcessInstance
     * @param instanceMap
     * @param childDefineId
     * @param task
     */
    private void createSubWorkProcessCommand(ProcessInstance parentProcessInstance,
                                             ProcessInstanceMap instanceMap,
                                             Integer childDefineId, TaskInstance task){
        ProcessInstance childInstance = findSubProcessInstance(parentProcessInstance.getId(), task.getId());

        CommandType fatherType = parentProcessInstance.getCommandType();
        CommandType commandType = fatherType;
        if(childInstance == null || commandType == CommandType.REPEAT_RUNNING){
            String fatherHistoryCommand = parentProcessInstance.getHistoryCmd();
            // sub process must begin with schedule/complement data
            // if father begin with scheduler/complement data
            if(fatherHistoryCommand.startsWith(CommandType.SCHEDULER.toString()) ||
                    fatherHistoryCommand.startsWith(CommandType.COMPLEMENT_DATA.toString())){
                commandType = CommandType.valueOf(fatherHistoryCommand.split(Constants.COMMA)[0]);
            }
        }

        if(childInstance != null){
            childInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
            updateProcessInstance(childInstance);
        }
        // set sub work process command
        String processMapStr = JSONUtils.toJson(instanceMap);
        Map<String, String> cmdParam = JSONUtils.toMap(processMapStr);

        if(commandType == CommandType.COMPLEMENT_DATA ||
                (childInstance != null && childInstance.isComplementData())){
            Map<String, String> parentParam = JSONUtils.toMap(parentProcessInstance.getCommandParam());
            String endTime =  parentParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE);
            String startTime =  parentParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE);
            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, endTime);
            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, startTime);
            processMapStr = JSONUtils.toJson(cmdParam);
        }

        updateSubProcessDefinitionByParent(parentProcessInstance, childDefineId);

        Command command = new Command();
        command.setWarningType(parentProcessInstance.getWarningType());
        command.setWarningGroupId(parentProcessInstance.getWarningGroupId());
        command.setFailureStrategy(parentProcessInstance.getFailureStrategy());
        command.setProcessDefinitionId(childDefineId);
        command.setScheduleTime(parentProcessInstance.getScheduleTime());
        command.setExecutorId(parentProcessInstance.getExecutorId());
        command.setCommandParam(processMapStr);
        command.setCommandType(commandType);
        command.setProcessInstancePriority(parentProcessInstance.getProcessInstancePriority());
        createCommand(command);
        logger.info("sub process command created: {} ", command.toString());
    }

    private void updateSubProcessDefinitionByParent(ProcessInstance parentProcessInstance, int childDefinitionId) {
        ProcessDefinition fatherDefinition = this.findProcessDefineById(parentProcessInstance.getProcessDefinitionId());
        ProcessDefinition childDefinition = this.findProcessDefineById(childDefinitionId);
        if(childDefinition != null && fatherDefinition != null){
            childDefinition.setReceivers(fatherDefinition.getReceivers());
            childDefinition.setReceiversCc(fatherDefinition.getReceiversCc());
            processDefineMapper.update(childDefinition);
        }
    }

    /**
     * submit task to mysql
     * @param taskInstance
     * @return
     */
    public TaskInstance submitTaskInstanceToMysql(TaskInstance taskInstance, ProcessInstance processInstance){
        ExecutionStatus processInstanceState = processInstance.getState();

        if(taskInstance.getState().typeIsFailure()){
            if(taskInstance.isSubProcess()){
                taskInstance.setRetryTimes(taskInstance.getRetryTimes() + 1 );
            }else {

                if( processInstanceState != ExecutionStatus.READY_STOP
                        && processInstanceState != ExecutionStatus.READY_PAUSE){
                    // failure task set invalid
                    taskInstance.setFlag(Flag.NO);
                    updateTaskInstance(taskInstance);
                    // crate new task instance
                    if(taskInstance.getState() != ExecutionStatus.NEED_FAULT_TOLERANCE){
                        taskInstance.setRetryTimes(taskInstance.getRetryTimes() + 1 );
                    }
                    taskInstance.setEndTime(null);
                    taskInstance.setStartTime(new Date());
                    taskInstance.setFlag(Flag.YES);
                    taskInstance.setHost(null);
                    taskInstance.setId(0);
                }
            }
        }
        taskInstance.setProcessInstancePriority(processInstance.getProcessInstancePriority());
        taskInstance.setState(getSubmitTaskState(taskInstance, processInstanceState));
        taskInstance.setSubmitTime(new Date());
        saveTaskInstance(taskInstance);
        return taskInstance;
    }

    /**
     *  submit task to queue
     * @param task
     */
    public Boolean submitTaskToQueue(TaskInstance task) {

        try{
            // task cannot submit when running
            if(task.getState() == ExecutionStatus.RUNNING_EXEUTION){
                logger.info(String.format("submit to task queue, but task [%s] state already be running. ", task.getName()));
                return true;
            }
            if(checkTaskExistsInTaskQueue(task)){
                logger.info(String.format("submit to task queue, but task [%s] already exists in the queue.", task.getName()));
                return true;
            }
            logger.info("task ready to queue: {}" , task);
            taskQueue.add(SCHEDULER_TASKS_QUEUE, taskZkInfo(task));
            logger.info(String.format("master insert into queue success, task : %s", task.getName()) );
            return true;
        }catch (Exception e){
            logger.error("submit task to queue Exception: ", e);
            logger.error("task queue error : %s", JSONUtils.toJson(task));
            return false;

        }
    }

    /**
     * ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskInstanceId}_${task executed by ip1},${ip2}...
     *
     * The tasks with the highest priority are selected by comparing the priorities of the above four levels from high to low.
     *
     * 流程实例优先级_流程实例id_任务优先级_任务实例id_任务执行机器ip1，ip2...          high <- low
     *
     * @param taskInstance
     * @return
     */
    private String taskZkInfo(TaskInstance taskInstance) {

        int taskWorkerGroupId = getTaskWorkerGroupId(taskInstance);

        StringBuilder sb = new StringBuilder(100);

        sb.append(taskInstance.getProcessInstancePriority().ordinal()).append(Constants.UNDERLINE)
                .append(taskInstance.getProcessInstanceId()).append(Constants.UNDERLINE)
                .append(taskInstance.getTaskInstancePriority().ordinal()).append(Constants.UNDERLINE)
                .append(taskInstance.getId()).append(Constants.UNDERLINE);

        if(taskWorkerGroupId > 0){
            //not to find data from db
            WorkerGroup workerGroup = queryWorkerGroupById(taskWorkerGroupId);
            if(workerGroup == null ){
                logger.info("task {} cannot find the worker group, use all worker instead.", taskInstance.getId());

                sb.append(Constants.DEFAULT_WORKER_ID);
                return sb.toString();
            }

            String ips = workerGroup.getIpList();

            if(StringUtils.isBlank(ips)){
                logger.error("task:{} worker group:{} parameters(ip_list) is null, this task would be running on all workers",
                        taskInstance.getId(), workerGroup.getId());
                sb.append(Constants.DEFAULT_WORKER_ID);
                return sb.toString();
            }

            StringBuilder ipSb = new StringBuilder(100);
            String[] ipArray = ips.split(COMMA);

            for (String ip : ipArray) {
               long ipLong = IpUtils.ipToLong(ip);
                ipSb.append(ipLong).append(COMMA);
            }

            if(ipSb.length() > 0) {
                ipSb.deleteCharAt(ipSb.length() - 1);
            }

            sb.append(ipSb);
        }else{
            sb.append(Constants.DEFAULT_WORKER_ID);
        }


        return  sb.toString();
    }

    /**
     * get submit task instance state by the work process state
     * cannot modify the task state when running/kill/submit success, or this
     * task instance is already exists in task queue .
     * return pause if work process state is ready pause
     * return stop if work process state is ready stop
     * if all of above are not satisfied, return submit success
     *
     * @param taskInstance
     * @param processInstanceState
     * @return
     */
    public ExecutionStatus getSubmitTaskState(TaskInstance taskInstance, ExecutionStatus processInstanceState){
        ExecutionStatus state = taskInstance.getState();
        if(
                // running or killed
                // the task already exists in task queue
                // return state
                state == ExecutionStatus.RUNNING_EXEUTION
                        || state == ExecutionStatus.KILL
                        || checkTaskExistsInTaskQueue(taskInstance)
                ){
            return state;
        }
        //return pasue /stop if process instance state is ready pause / stop
        // or return submit success
        if( processInstanceState == ExecutionStatus.READY_PAUSE){
            state = ExecutionStatus.PAUSE;
        }else if(processInstanceState == ExecutionStatus.READY_STOP) {
            state = ExecutionStatus.KILL;
        }else{
            state = ExecutionStatus.SUBMITTED_SUCCESS;
        }
        return state;
    }

    /**
     * check the task instance existing in queue
     * @return
     */
    public boolean checkTaskExistsInTaskQueue(TaskInstance task){
        if(task.isSubProcess()){
            return false;
        }

        String taskZkInfo = taskZkInfo(task);

        return taskQueue.checkTaskExists(SCHEDULER_TASKS_QUEUE, taskZkInfo);
    }

    /**
     * create a new process instance
     * @param processInstance
     */
    public void createProcessInstance(ProcessInstance processInstance){

        if (processInstance != null){
            processInstanceMapper.insert(processInstance);
        }
    }

    /**
     * insert or update work process instance to data base
     * @param workProcessInstance
     */
    public void saveProcessInstance(ProcessInstance workProcessInstance){

        if (workProcessInstance == null){
            logger.error("save error, process instance is null!");
            return ;
        }
        //创建流程实例
        if(workProcessInstance.getId() != 0){
            processInstanceMapper.update(workProcessInstance);
        }else{
            createProcessInstance(workProcessInstance);
        }
    }

    /**
     * insert or update command
     * @param command
     * @return
     */
    public int saveCommand(Command command){
        if(command.getId() != 0){
            return commandMapper.update(command);
        }else{
            return commandMapper.insert(command);
        }
    }

    /**
     *  insert or update task instance
     * @param taskInstance
     * @return
     */
    public boolean saveTaskInstance(TaskInstance taskInstance){
        if(taskInstance.getId() != 0){
            return updateTaskInstance(taskInstance);
        }else{
            return createTaskInstance(taskInstance);
        }
    }

    /**
     * insert task instance
     * @param taskInstance
     * @return
     */
    public boolean createTaskInstance(TaskInstance taskInstance) {
        int count = taskInstanceMapper.insert(taskInstance);
        return count > 0;
    }

    /**
     * update task instance
     * @param taskInstance
     * @return
     */
    public boolean updateTaskInstance(TaskInstance taskInstance){
        int count = taskInstanceMapper.update(taskInstance);
        return count > 0;
    }
    /**
     * delete a command by id
     * @param id
     */
    public void delCommandByid(int id) {
        commandMapper.delete(id);
    }

    public TaskInstance findTaskInstanceById(Integer taskId){
        return taskInstanceMapper.queryById(taskId);
    }


    /**
     * package task instance，associate processInstance and processDefine
     * @param taskInstId
     * @return
     */
    public TaskInstance getTaskInstanceRelationByTaskId(int taskInstId){
        // get task instance
        TaskInstance taskInstance = findTaskInstanceById(taskInstId);
        // get process instance
        ProcessInstance processInstance = findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
        // get process define
        ProcessDefinition processDefine = findProcessDefineById(taskInstance.getProcessDefinitionId());

        taskInstance.setProcessInstance(processInstance);
        taskInstance.setProcessDefine(processDefine);
        return taskInstance;
    }


    /**
     * get id list by task state
     * @param instanceId
     * @param state
     * @return
     */
    public List<Integer> findTaskIdByInstanceState(int instanceId, ExecutionStatus state){
        return taskInstanceMapper.queryTaskByProcessIdAndState(instanceId, state.ordinal());
    }

    /**
     *
     * find valid task list by process definition id
     * @param processInstanceId
     * @return
     */
    public List<TaskInstance> findValidTaskListByProcessId(Integer processInstanceId){
         return taskInstanceMapper.findValidTaskListByProcessId(processInstanceId, Flag.YES);
    }

    /**
     * find previous task list by work process id
     * @param workProcessInstanceId
     * @return
     */
    public List<TaskInstance> findPreviousTaskListByWorkProcessId(Integer workProcessInstanceId){
        return taskInstanceMapper.findValidTaskListByProcessId(workProcessInstanceId, Flag.NO);
    }

    /**
     * update work process instance map
     * @param processInstanceMap
     * @return
     */
    public int updateWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap){
        return processInstanceMapMapper.update(processInstanceMap);
    }


    /**
     * create work process instance map
     * @param processInstanceMap
     * @return
     */
    public int createWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap){
        Integer count = 0;
        if(processInstanceMap !=null){
            return  processInstanceMapMapper.insert(processInstanceMap);
        }
        return count;
    }

    /**
     * find work process map by parent process id and parent task id.
     * @param parentWorkProcessId
     * @param parentTaskId
     * @return
     */
    public ProcessInstanceMap findWorkProcessMapByParent(Integer parentWorkProcessId, Integer parentTaskId){
        return processInstanceMapMapper.queryByParentId(parentWorkProcessId, parentTaskId);
    }

    /**
     * delete work process map by parent process id
     * @param parentWorkProcessId
     * @return
     */
    public int deleteWorkProcessMapByParentId(int parentWorkProcessId){
        return processInstanceMapMapper.deleteByParentProcessId(parentWorkProcessId);

    }

    public ProcessInstance findSubProcessInstance(Integer parentProcessId, Integer parentTaskId){
        ProcessInstance processInstance = null;
        ProcessInstanceMap processInstanceMap = processInstanceMapMapper.queryByParentId(parentProcessId, parentTaskId);
        if(processInstanceMap == null || processInstanceMap.getProcessInstanceId() == 0){
            return processInstance;
        }
        processInstance = findProcessInstanceById(processInstanceMap.getProcessInstanceId());
        return processInstance;
    }
    public ProcessInstance findParentProcessInstance(Integer subProcessId) {
        ProcessInstance processInstance = null;
        ProcessInstanceMap processInstanceMap = processInstanceMapMapper.queryBySubProcessId(subProcessId);
        if(processInstanceMap == null || processInstanceMap.getProcessInstanceId() == 0){
            return processInstance;
        }
        processInstance = findProcessInstanceById(processInstanceMap.getParentProcessInstanceId());
        return processInstance;
    }



    /**
     * change task state
     * @param state
     * @param startTime
     * @param host
     * @param executePath
     */
    public void changeTaskState(ExecutionStatus state, Date startTime, String host,
                                String executePath,
                                String logPath,
                                int taskInstId) {
        TaskInstance taskInstance = taskInstanceMapper.queryById(taskInstId);
        taskInstance.setState(state);
        taskInstance.setStartTime(startTime);
        taskInstance.setHost(host);
        taskInstance.setExecutePath(executePath);
        taskInstance.setLogPath(logPath);
        saveTaskInstance(taskInstance);
    }

    /**
     * update process instance
     * @param instance
     * @return
     */
    public int updateProcessInstance(ProcessInstance instance){
        return processInstanceMapper.update(instance);
    }

    /**
     * update the process instance
     * @param  processInstanceId
     * @param processJson
     * @param globalParams
     * @param scheduleTime
     * @param flag
     * @param locations
     * @param connects
     * @return
     */
    public int updateProcessInstance(Integer processInstanceId, String processJson,
                                     String globalParams, Date scheduleTime, Flag flag,
                                     String locations, String connects){
        return processInstanceMapper.updateProcessInstance(processInstanceId, processJson,
                globalParams, scheduleTime, locations, connects, flag);
    }

    /**
     * change task state
     * @param state
     * @param endTime
     */
    public void changeTaskState(ExecutionStatus state,
                                Date endTime,
                                int taskInstId) {
        TaskInstance taskInstance = taskInstanceMapper.queryById(taskInstId);
        taskInstance.setState(state);
        taskInstance.setEndTime(endTime);
        saveTaskInstance(taskInstance);
    }

    /**
     * convert integer list to string list
     * @param intList
     * @return
     */
    public List<String> convertIntListToString(List<Integer> intList){
        if(intList == null){
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<String>(intList.size());
        for(Integer intVar : intList){
            result.add(String.valueOf(intVar));
        }
        return result;
    }

    /**
     * set task
     * 根据任务实例id设置pid
     * @param taskInstId
     * @param pid
     */
    public void updatePidByTaskInstId(int taskInstId, int pid) {
        TaskInstance taskInstance = taskInstanceMapper.queryById(taskInstId);
        taskInstance.setPid(pid);
        taskInstance.setAppLink("");
        saveTaskInstance(taskInstance);
    }

    /**
     * update pid and app links field by task instance id
     * @param taskInstId
     * @param pid
     */
    public void updatePidByTaskInstId(int taskInstId, int pid,String appLinks) {

        TaskInstance taskInstance = taskInstanceMapper.queryById(taskInstId);
        taskInstance.setPid(pid);
        taskInstance.setAppLink(appLinks);
        saveTaskInstance(taskInstance);
    }

    /**
     * query  ProcessDefinition by name
     *
     * @see ProcessDefinition
     */
    public ProcessDefinition findProcessDefineByName(int projectId, String name) {
        ProcessDefinition projectFlow = processDefineMapper.queryByDefineName(projectId, name);
        return projectFlow;
    }

    /**
     * query Schedule <p>
     *
     * @see Schedule
     */
    public Schedule querySchedule(int id) {
        return scheduleMapper.queryById(id);
    }

    public List<ProcessInstance> queryNeedFailoverProcessInstances(String host){
        return processInstanceMapper.queryByHostAndStatus(host, stateArray);
    }


    /**
     * update host null
     * @param host
     * @return
     */
    public int updateNeddFailoverProcessInstances(String host){
        return processInstanceMapper.setFailoverByHostAndStateArray(host, stateArray);
    }

    /**
     * process need failover process instance
     * @param processInstance
     */
    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public void processNeedFailoverProcessInstances(ProcessInstance processInstance){


        //1 update processInstance host is null
        processInstance.setHost("null");
        processInstanceMapper.update(processInstance);

        //2 insert into recover command
        Command cmd = new Command();
        cmd.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        cmd.setCommandParam(String.format("{\"%s\":%d}", Constants.CMDPARAM_RECOVER_PROCESS_ID_STRING, processInstance.getId()));
        cmd.setExecutorId(processInstance.getExecutorId());
        cmd.setCommandType(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS);
        createCommand(cmd);

    }

    /**
     * query all need failover task instances by host
     * @param host
     * @return
     */
    public List<TaskInstance> queryNeedFailoverTaskInstances(String host){
        return taskInstanceMapper.queryByHostAndStatus(host, stateArray);
    }

    /**
     * update host null
     * @param host
     * @return
     */
    public int updateNeedFailoverTaskInstances(String host){
        return taskInstanceMapper.setFailoverByHostAndStateArray(host, stateArray);
    }

    /**
     * find data source by id
     * @param id
     * @return
     */
    public DataSource findDataSourceById(int id){
        return dataSourceMapper.queryById(id);
    }


    /**
     * update process instance state by id
     * @param processInstanceId
     * @param executionStatus
     * @return
     */
    public int updateProcessInstanceState(Integer processInstanceId, ExecutionStatus executionStatus) {
        return processInstanceMapper.updateState(processInstanceId, executionStatus);

    }

    /**
     * find process instance by the task id
     * @param taskId
     * @return
     */
    public ProcessInstance findProcessInstanceByTaskId(int taskId){
        return processInstanceMapper.queryByTaskId(taskId);
    }

    /**
     * find udf function list by id list string
     * @param ids
     * @return
     */
    public List<UdfFunc> queryUdfFunListByids(String ids){
        return udfFuncMapper.queryUdfByIdStr(ids);
    }

    /**
     * find tenant code by resource name
     * @param resName
     * @return
     */
    public String queryTenantCodeByResName(String resName){
        return resourceMapper.queryTenantCodeByResourceName(resName);
    }

    /**
     * find schedule list by process define id.
     * @param ids
     * @return
     */
    public List<Schedule> selectAllByProcessDefineId(int[] ids){
        return scheduleMapper.selectAllByProcessDefineArray(ids);
    }

    /**
     * get dependency cycle by work process define id and scheduler fire time
     *
     * @param masterId
     * @param processDefinitionId
     * @param scheduledFireTime 任务调度预计触发的时间
     * @return
     * @throws Exception
     */
    public CycleDependency getCycleDependency(int masterId, int processDefinitionId, Date scheduledFireTime) throws Exception {
        List<CycleDependency> list = getCycleDependencies(masterId,new int[]{processDefinitionId},scheduledFireTime);
        return list.size()>0 ? list.get(0) : null;

    }

    /**
     *
     * get dependency cycle list by work process define id list and scheduler fire time
     * @param masterId
     * @param ids
     * @param scheduledFireTime 任务调度预计触发的时间
     * @return
     * @throws Exception
     */
    public List<CycleDependency> getCycleDependencies(int masterId,int[] ids,Date scheduledFireTime) throws Exception {
        List<CycleDependency> cycleDependencyList =  new ArrayList<CycleDependency>();
        if(ArrayUtils.isEmpty(ids)){
            logger.warn("ids[] is empty!is invalid!");
            return cycleDependencyList;
        }
        if(scheduledFireTime == null){
            logger.warn("scheduledFireTime is null!is invalid!");
            return cycleDependencyList;
        }


        String strCrontab = "";
        CronExpression depCronExpression;
        Cron depCron;
        List<Date> list;
        List<Schedule> schedules = this.selectAllByProcessDefineId(ids);
        // 遍历所有的调度信息
        for(Schedule depSchedule:schedules){
            strCrontab = depSchedule.getCrontab();
            depCronExpression = CronUtils.parse2CronExpression(strCrontab);
            depCron = CronUtils.parse2Cron(strCrontab);
            CycleEnum cycleEnum = CronUtils.getMiniCycle(depCron);
            if(cycleEnum == null){
                logger.error("{} is not valid",strCrontab);
                continue;
            }
            Calendar calendar = Calendar.getInstance();
            switch (cycleEnum){
                /*case MINUTE:
                    calendar.add(Calendar.MINUTE,-61);*/
                case HOUR:
                    calendar.add(Calendar.HOUR,-25);
                    break;
                case DAY:
                    calendar.add(Calendar.DATE,-32);
                    break;
                case WEEK:
                    calendar.add(Calendar.DATE,-32);
                    break;
                case MONTH:
                    calendar.add(Calendar.MONTH,-13);
                    break;
                default:
                    logger.warn("Dependent process definition's  cycleEnum is {},not support!!", cycleEnum.name());
                    continue;
            }
            Date start = calendar.getTime();

            if(depSchedule.getProcessDefinitionId() == masterId){
                list = CronUtils.getSelfFireDateList(start, scheduledFireTime, depCronExpression);
            }else {
                list = CronUtils.getFireDateList(start, scheduledFireTime, depCronExpression);
            }
            if(list.size()>=1){
                start = list.get(list.size()-1);
                CycleDependency dependency = new CycleDependency(depSchedule.getProcessDefinitionId(),start, CronUtils.getExpirationTime(start, cycleEnum), cycleEnum);
                cycleDependencyList.add(dependency);
            }

        }
        return cycleDependencyList;
    }

    /**
     * find process instance by time interval
     * @param defineId
     * @param startTime
     * @param endTime
     * @return
     */
    public ProcessInstance findProcessInstanceByTimeInterval(int defineId, Date startTime, Date endTime, int excludeId) {

        return processInstanceMapper.queryByScheduleTime(defineId, null, excludeId,
                DateUtils.dateToString(startTime), DateUtils.dateToString(endTime));
    }

    public void selfFaultTolerant(int state){
        List<ProcessInstance> processInstanceList = processInstanceMapper.listByStatus(new int[]{state});
        for (ProcessInstance processInstance:processInstanceList){
            selfFaultTolerant(processInstance);
        }

    }

    /**
     * master starup fault tolerant
     */
    public void masterStartupFaultTolerant(){

        int[] readyStopAndKill=new int[]{ExecutionStatus.READY_PAUSE.ordinal(),ExecutionStatus.READY_STOP.ordinal(),
                ExecutionStatus.NEED_FAULT_TOLERANCE.ordinal(),ExecutionStatus.RUNNING_EXEUTION.ordinal()};
        List<ProcessInstance> processInstanceList = processInstanceMapper.listByStatus(readyStopAndKill);
        for (ProcessInstance processInstance:processInstanceList){
            processNeedFailoverProcessInstances(processInstance);
        }
    }

    @Transactional(value = "TransactionManager",rollbackFor = Exception.class)
    public void selfFaultTolerant(ProcessInstance processInstance){

        processInstance.setState(ExecutionStatus.FAILURE);
        processInstanceMapper.update(processInstance);
        // insert to command

        Command command = new Command();
        command.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        command.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        command.setCommandParam(String.format("{\"%s\":%d}",
                CMDPARAM_RECOVER_PROCESS_ID_STRING, processInstance.getId()));


        command.setExecutorId(processInstance.getExecutorId());
        command.setProcessInstancePriority(processInstance.getProcessInstancePriority());

        createCommand(command);

    }

    /**
     * find last scheduler process instance in the date interval
     * @param definitionId
     * @param dateInterval
     * @return
     */
    public ProcessInstance findLastSchedulerProcessInterval(int definitionId, DateInterval dateInterval) {
        return processInstanceMapper.queryLastSchedulerProcess(definitionId,
                DateUtils.dateToString(dateInterval.getStartTime()),
                DateUtils.dateToString(dateInterval.getEndTime()));
    }

    public ProcessInstance findLastManualProcessInterval(int definitionId, DateInterval dateInterval) {
        return processInstanceMapper.queryLastManualProcess(definitionId,
                DateUtils.dateToString(dateInterval.getStartTime()),
                DateUtils.dateToString(dateInterval.getEndTime()));
    }

    public ProcessInstance findLastRunningProcess(int definitionId, DateInterval dateInterval) {
        return processInstanceMapper.queryLastRunningProcess(definitionId,
                DateUtils.dateToString(dateInterval.getStartTime()),
                DateUtils.dateToString(dateInterval.getEndTime()),
                stateArray);
    }

    /**
     *  query user queue by process instance id
     * @param processInstanceId
     * @return
     */
    public String queryUserQueueByProcessInstanceId(int processInstanceId){
        return userMapper.queryQueueByProcessInstanceId(processInstanceId);
    }

    /**
     * query worker group by id
     * @param workerGroupId
     * @return
     */
    public WorkerGroup queryWorkerGroupById(int workerGroupId){
        return workerGroupMapper.queryById(workerGroupId);
    }

    /**
     * query worker server by host
     * @param host
     * @return
     */
    public List<WorkerServer> queryWorkerServerByHost(String host){

        return workerServerMapper.queryWorkerByHost(host);

    }


    /**
     * get task worker group id
     *
     * @param taskInstance
     * @return
     */
    public int getTaskWorkerGroupId(TaskInstance taskInstance) {
        int taskWorkerGroupId = taskInstance.getWorkerGroupId();
        int processInstanceId = taskInstance.getProcessInstanceId();

        ProcessInstance processInstance = findProcessInstanceById(processInstanceId);

        if(processInstance == null){
            logger.error("cannot find the task:{} process instance", taskInstance.getId());
            return Constants.DEFAULT_WORKER_ID;
        }
        int processWorkerGroupId = processInstance.getWorkerGroupId();

        taskWorkerGroupId = (taskWorkerGroupId <= 0 ? processWorkerGroupId : taskWorkerGroupId);
        return taskWorkerGroupId;
    }


}

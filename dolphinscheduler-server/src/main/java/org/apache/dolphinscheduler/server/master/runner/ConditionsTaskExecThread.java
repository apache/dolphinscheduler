package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConditionsTaskExecThread extends MasterBaseTaskExecThread {


    /**
     * dependent parameters
     */
    private DependentParameters dependentParameters;

    /**
     *  log record
     */
    protected Logger logger;

    /**
     * complete task map
     */
    private Map<String, ExecutionStatus> completeTaskList = new ConcurrentHashMap<>();

    /**
     * condition result
     */
    private DependResult conditionResult;

    /**
     * constructor of MasterBaseTaskExecThread
     *
     * @param taskInstance    task instance
     * @param processInstance process instance
     */
    public ConditionsTaskExecThread(TaskInstance taskInstance, ProcessInstance processInstance) {
        super(taskInstance, processInstance);
        logger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskInstance.getProcessDefinitionId(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));

        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, processService.formatTaskAppId(this.taskInstance));
        Thread.currentThread().setName(threadLoggerInfoName);
    }

    @Override
    public Boolean submitWaitComplete() {
        try{
            this.taskInstance = submit();
            initTaskParameters();
            logger.info("dependent task start");
            waitTaskQuit();
            updateTaskState();
        }catch (Exception e){
            logger.error("" + e);
        }
        return true;
    }

    private void waitTaskQuit() {

        List<DependResult> modelResultList = new ArrayList<>();
        for(DependentTaskModel dependentTaskModel : dependentParameters.getDependTaskList()){

            List<DependResult> itemDependResult = new ArrayList<>();
            for(DependentItem item : dependentTaskModel.getDependItemList()){
                itemDependResult.add(getDependResultForItem(item));
            }
            DependResult modelResult = DependentUtils.getDependResultForRelation(dependentTaskModel.getRelation(), itemDependResult);
            modelResultList.add(modelResult);
        }
        conditionResult = DependentUtils.getDependResultForRelation(
                dependentParameters.getRelation(), modelResultList
        );
        logger.info("the conditions task depend result : {}", conditionResult);
    }

    /**
     *
     */
    private void updateTaskState() {
        ExecutionStatus status;
        if(this.cancel){
            status = ExecutionStatus.KILL;
        }else{
            status = (conditionResult == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        }
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }

    private void initTaskParameters() {
        this.dependentParameters = JSONUtils.parseObject(this.taskInstance.getDependency(), DependentParameters.class);
    }


    private DependResult getDependResultForItem(DependentItem item){

        DependResult dependResult = DependResult.SUCCESS;
        if(!completeTaskList.containsKey(item.getDepTasks())){
            logger.info("depend item: {} have not completed yet.", item.getDepTasks());
            dependResult = DependResult.FAILED;
            return dependResult;
        }
        ExecutionStatus executionStatus = completeTaskList.get(item.getDepTasks());
        if(executionStatus != item.getStatus()){
            logger.info("depend item : {} expect status: {}, actual status: {}" ,item.getDepTasks(), item.getStatus().toString(), executionStatus.toString());
            dependResult = DependResult.FAILED;
        }
        logger.info("depend item: {}, depend result: {}",
                item.getDepTasks(), dependResult);
        return dependResult;
    }


}

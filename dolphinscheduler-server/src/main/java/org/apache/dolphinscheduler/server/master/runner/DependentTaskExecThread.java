package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.worker.task.dependent.DependentExecute;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.dolphinscheduler.common.Constants.DEPENDENT_SPLIT;

public class DependentTaskExecThread extends MasterBaseTaskExecThread {

    /**
     * logger of MasterBaseTaskExecThread
     */
    private static final Logger logger = LoggerFactory.getLogger(DependentTaskExecThread.class);

    private DependentParameters dependentParameters;

    /**
     * dependent task list
     */
    private List<DependentExecute> dependentTaskList = new ArrayList<>();

    /**
     * depend item result map
     * save the result to log file
     */
    private Map<String, DependResult> dependResultMap = new HashMap<>();


    /**
     * dependent date
     */
    private Date dependentDate;

    /**
     * constructor of MasterBaseTaskExecThread
     *
     * @param taskInstance    task instance
     * @param processInstance process instance
     */
    public DependentTaskExecThread(TaskInstance taskInstance, ProcessInstance processInstance) {
        super(taskInstance, processInstance);
    }

    /**
     * init dependent parameters
     */
    private void initDependParameters() {

        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, processService.formatTaskAppId(this.taskInstance));
        Thread.currentThread().setName(threadLoggerInfoName);

        this.dependentParameters = JSONUtils.parseObject(this.taskInstance.getDependency(),
                DependentParameters.class);

        for(DependentTaskModel taskModel : dependentParameters.getDependTaskList()){
            this.dependentTaskList.add(new DependentExecute(
                    taskModel.getDependItemList(), taskModel.getRelation()));
        }
        if(this.processInstance.getScheduleTime() != null){
            this.dependentDate = this.processInstance.getScheduleTime();
        }else{
            this.dependentDate = new Date();
        }
    }

    @Override
    public Boolean submitWaitComplete() {
        try{
            logger.info("dependent task start");
            this.taskInstance = submit();
            setTaskInstanceParameters();
            initDependParameters();
            waitTaskQuit();
            updateDependResultState();
        }catch (Exception e){
            logger.error("" + e);
        }
        return true;
    }

    /**
     *
     */
    private void updateDependResultState() {
        if(this.cancel){
            return;
        }
        DependResult result = getTaskDependResult();

        ExecutionStatus status = (result == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;

        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }

    /**
     *
     */
    private Boolean waitTaskQuit() {
        logger.info("wait depend task : {} complete", this.taskInstance.getName());
        if (taskInstance.getState().typeIsFinished()) {
            logger.info("task {} already complete. task state:{}",
                    this.taskInstance.getName(),
                    this.taskInstance.getState().toString());
            return true;
        }
        while (Stopper.isRunning()) {
            try{
                if(this.processInstance == null){
                    logger.error("process instance not exists , master task exec thread exit");
                    return true;
                }
                if(this.cancel || this.processInstance.getState() == ExecutionStatus.READY_STOP){
                    cancelTaskInstance();
                }

                if ( allDependentTaskFinish() || taskInstance.getState().typeIsFinished()){
                    break;
                }
                // updateProcessInstance task instance
                taskInstance = processService.findTaskInstanceById(taskInstance.getId());
                processInstance = processService.findProcessInstanceById(processInstance.getId());
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (Exception e) {
                logger.error("exception",e);
                if (processInstance != null) {
                    logger.error("wait task quit failed, instance id:{}, task id:{}",
                    processInstance.getId(), taskInstance.getId());
            }
            }
        }
        return true;
    }

    /**
     * cancel dependent task
     */
    private void cancelTaskInstance() {
        this.cancel = true;
        this.taskInstance.setState(ExecutionStatus.KILL);
        processService.updateTaskInstance(taskInstance);
    }

    private void setTaskInstanceParameters() {
        taskInstance.setHost(OSUtils.getHost());
        taskInstance.setState(ExecutionStatus.RUNNING_EXEUTION);
        taskInstance.setStartTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }

    /**
     * judge all dependent tasks finish
     * @return whether all dependent tasks finish
     */
    private boolean allDependentTaskFinish(){
        boolean finish = true;
        for(DependentExecute dependentExecute : dependentTaskList){
            for(Map.Entry<String, DependResult> entry: dependentExecute.getDependResultMap().entrySet()) {
                if(!dependResultMap.containsKey(entry.getKey())){
                    dependResultMap.put(entry.getKey(), entry.getValue());
                    //save depend result to log
                    logger.info("dependent item complete {} {},{}",
                            DEPENDENT_SPLIT, entry.getKey(), entry.getValue().toString());
                }
            }
            if(!dependentExecute.finish(dependentDate)){
                finish = false;
            }
        }
        return finish;
    }

    /**
     * get dependent result
     * @return DependResult
     */
    private DependResult getTaskDependResult(){
        List<DependResult> dependResultList = new ArrayList<>();
        for(DependentExecute dependentExecute : dependentTaskList){
            DependResult dependResult = dependentExecute.getModelDependResult(dependentDate);
            dependResultList.add(dependResult);
        }
        DependResult result = DependentUtils.getDependResultForRelation(
                this.dependentParameters.getRelation(), dependResultList
        );
        return result;
    }
}

package org.apache.dolphinscheduler.server.master.processor.queue;

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.DBTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.DBTaskResponseCommand;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public class TaskResponsePersistThread implements Callable<TaskResponsePersistThread> {

    /**
     * logger of TaskResponsePersistThread
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskResponsePersistThread.class);

    private final ConcurrentLinkedQueue<TaskResponseEvent>  events = new ConcurrentLinkedQueue<>();

    private final Integer processInstanceId;

    volatile boolean stop = false;

    /**
     * process service
     */
    private ProcessService processService;

    private ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceMapper;

    public TaskResponsePersistThread(ProcessService processService,
                                     ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceMapper,
                                     Integer processInstanceId) {
        this.processService = processService;
        this.processInstanceMapper = processInstanceMapper;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public TaskResponsePersistThread call() throws Exception {
        while (!this.events.isEmpty()) {
            TaskResponseEvent event = this.events.peek();
            try {
                boolean result = persist(event);
                if (!result) {
                    logger.error("persist meta error, task id:{}, instance id:{}", event.getTaskInstanceId(), event.getProcessInstanceId());
                }
            } catch (Exception e) {
                logger.error("persist error, task id:{}, instance id:{}", event.getTaskInstanceId(), event.getProcessInstanceId(), e);
            } finally {
                this.events.remove(event);
            }
        }
        return this;
    }

    /**
     * persist  taskResponseEvent
     *
     * @param taskResponseEvent taskResponseEvent
     */
    private boolean persist(TaskResponseEvent taskResponseEvent) {
        Event event = taskResponseEvent.getEvent();
        Channel channel = taskResponseEvent.getChannel();

        TaskInstance taskInstance = processService.findTaskInstanceById(taskResponseEvent.getTaskInstanceId());

        boolean result = true;

        switch (event) {
            case ACK:
                try {
                    if (taskInstance != null) {
                        ExecutionStatus status = taskInstance.getState().typeIsFinished() ? taskInstance.getState() : taskResponseEvent.getState();
                        processService.changeTaskState(taskInstance, status,
                                taskResponseEvent.getStartTime(),
                                taskResponseEvent.getWorkerAddress(),
                                taskResponseEvent.getExecutePath(),
                                taskResponseEvent.getLogPath(),
                                taskResponseEvent.getTaskInstanceId());
                        logger.debug("changeTaskState in ACK , changed in meta:{} ,task instance state:{}, task response event state:{}, taskInstance id:{},taskInstance host:{}",
                                result, taskInstance.getState(), taskResponseEvent.getState(), taskInstance.getId(), taskInstance.getHost());
                    }
                    // if taskInstance is null (maybe deleted) . retry will be meaningless . so ack success
                    DBTaskAckCommand taskAckCommand = new DBTaskAckCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                    channel.writeAndFlush(taskAckCommand.convert2Command());
                    logger.debug("worker ack master success, taskInstance id:{},taskInstance host:{}", taskInstance.getId(), taskInstance.getHost());
                } catch (Exception e) {
                    result = false;
                    logger.error("worker ack master error", e);
                    DBTaskAckCommand taskAckCommand = new DBTaskAckCommand(ExecutionStatus.FAILURE.getCode(), taskInstance == null ? -1 : taskInstance.getId());
                    channel.writeAndFlush(taskAckCommand.convert2Command());
                }
                break;
            case RESULT:
                try {
                    if (taskInstance != null) {
                        result = processService.changeTaskState(taskInstance, taskResponseEvent.getState(),
                                taskResponseEvent.getEndTime(),
                                taskResponseEvent.getProcessId(),
                                taskResponseEvent.getAppIds(),
                                taskResponseEvent.getTaskInstanceId(),
                                taskResponseEvent.getVarPool()
                        );
                        logger.debug("changeTaskState in RESULT , changed in meta:{} task instance state:{}, task response event state:{}, taskInstance id:{},taskInstance host:{}",
                                result, taskInstance.getState(), taskResponseEvent.getState(), taskInstance.getId(), taskInstance.getHost());
                    }
                    if (!result) {
                        DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.FAILURE.getCode(), taskResponseEvent.getTaskInstanceId());
                        channel.writeAndFlush(taskResponseCommand.convert2Command());
                        logger.debug("worker response master failure, taskInstance id:{},taskInstance host:{}", taskInstance.getId(), taskInstance.getHost());
                    } else {
                        // if taskInstance is null (maybe deleted) . retry will be meaningless . so response success
                        DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                        channel.writeAndFlush(taskResponseCommand.convert2Command());
                        logger.debug("worker response master success, taskInstance id:{},taskInstance host:{}", taskInstance.getId(), taskInstance.getHost());
                    }
                } catch (Exception e) {
                    result = false;
                    logger.error("worker response master error", e);
                    DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.FAILURE.getCode(), -1);
                    channel.writeAndFlush(taskResponseCommand.convert2Command());
                }
                break;
            default:
                throw new IllegalArgumentException("invalid event type : " + event);
        }

        WorkflowExecuteThread workflowExecuteThread = this.processInstanceMapper.get(taskResponseEvent.getProcessInstanceId());
        if (workflowExecuteThread != null) {
            StateEvent stateEvent = new StateEvent();
            stateEvent.setProcessInstanceId(taskResponseEvent.getProcessInstanceId());
            stateEvent.setTaskInstanceId(taskResponseEvent.getTaskInstanceId());
            stateEvent.setExecutionStatus(taskResponseEvent.getState());
            stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
            workflowExecuteThread.addStateEvent(stateEvent);
        }
        return result;
    }

    public boolean addEvent(TaskResponseEvent event) {
        if (event.getProcessInstanceId() != this.processInstanceId) {
            logger.info("event would be abounded, task instance id:{}, process instance id:{}, this.processInstanceId:{}",
                    event.getTaskInstanceId(), event.getProcessInstanceId(), this.processInstanceId);
            return false;
        }
        return this.events.add(event);
    }

    public int eventSize() {
        return this.events.size();
    }

    public boolean isEmpty() {
        return this.events.isEmpty();
    }

    public Integer getProcessInstanceId() {
        return processInstanceId;
    }
}
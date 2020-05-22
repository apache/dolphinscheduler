package org.apache.dolphinscheduler.server.master.processor;

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.server.master.cache.TaskInstanceCacheManager;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskResponseService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * @author 离歌笑
 * @desc
 * @date 2020-05-22
 */
public class TaskResponseProcessorTest {

    private TaskInstanceCacheManager taskInstanceCacheManager;

    private ProcessService processService;

    private TaskResponseService taskResponseService;

    private ApplicationContext applicationContext;

    @Before
    public void init() throws Exception {
        taskInstanceCacheManager = mock(TaskInstanceCacheManager.class);
        taskResponseService = mock(TaskResponseService.class);
        processService = mock(ProcessService.class);

        // mock constructor
        applicationContext = mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        when(applicationContext.getBean(TaskInstanceCacheManager.class)).thenReturn(taskInstanceCacheManager);
        when(applicationContext.getBean(TaskResponseService.class)).thenReturn(taskResponseService);
        when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
    }

    @Test
    public void process() {
        // prepare
        TaskResponseProcessor taskResponseProcessor=new TaskResponseProcessor();
        Channel channel=mock(Channel.class);
        Command command=mock(Command.class);
        TaskExecuteAckCommand taskExecuteAckCommand=new TaskExecuteAckCommand();
        taskExecuteAckCommand.setTaskInstanceId(1);
        taskExecuteAckCommand.setStatus(ExecutionStatus.SUCCESS.getCode());
        TaskInstance taskInstance = taskInstance();

        // when
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost",2345));
        when(command.getType()).thenReturn(CommandType.TASK_EXECUTE_RESPONSE);
        when(command.getBody()).thenReturn(FastJsonSerializer.serialize(taskExecuteAckCommand));
        when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        doNothing().when(taskInstanceCacheManager).cacheTaskInstance(taskInstance);

        // call
        taskResponseProcessor.process(channel,command);
    }

    private TaskInstance taskInstance(){
        TaskInstance taskInstance=new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setState(ExecutionStatus.SUCCESS);
        return taskInstance;
    }
}
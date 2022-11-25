package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.common.constants.Constants.DATA_LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceQueryRequest;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class WorkflowInstanceV2ControllerTest extends AbstractControllerTest {

    @InjectMocks
    private WorkflowInstanceV2Controller workflowInstanceV2Controller;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private ExecutorService execService;

    @Test
    public void testQueryWorkFlowInstanceListPaging() {
        User loginUser = getLoginUser();

        WorkflowInstanceQueryRequest workflowInstanceQueryRequest = new WorkflowInstanceQueryRequest();
        workflowInstanceQueryRequest.setProjectName("test");
        workflowInstanceQueryRequest.setWorkflowName("shell");
        workflowInstanceQueryRequest.setPageNo(1);
        workflowInstanceQueryRequest.setPageSize(10);

        Result result = new Result();
        PageInfo<ProcessInstance> pageInfo =
                new PageInfo<>(workflowInstanceQueryRequest.getPageNo(), workflowInstanceQueryRequest.getPageSize());
        pageInfo.setTotalList(Collections.singletonList(new ProcessInstance()));
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        Mockito.when(processInstanceService.queryProcessInstanceList(any(),
                any(WorkflowInstanceQueryRequest.class))).thenReturn(result);

        Result result1 =
                workflowInstanceV2Controller.queryWorkflowInstanceListPaging(loginUser, workflowInstanceQueryRequest);
        Assert.assertTrue(result1.isSuccess());
    }

    @Test
    public void testQueryWorkflowInstanceById() {
        User loginUser = getLoginUser();

        Map<String, Object> result = new HashMap<>();
        result.put(DATA_LIST, new ProcessInstance());
        putMsg(result, Status.SUCCESS);

        Mockito.when(processInstanceService.queryProcessInstanceById(any(), eq(1))).thenReturn(result);
        Result result1 = workflowInstanceV2Controller.queryWorkflowInstanceById(loginUser, 1);
        Assert.assertTrue(result1.isSuccess());
    }

    @Test
    public void testDeleteWorkflowInstanceById() {
        User loginUser = getLoginUser();

        Mockito.when(processInstanceService.deleteProcessInstanceById(any(), eq(1))).thenReturn(null);
        Result result = workflowInstanceV2Controller.deleteWorkflowInstance(loginUser, 1);
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void testExecuteWorkflowInstance() {
        User loginUser = getLoginUser();

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(execService.execute(any(), eq(1), any(ExecuteType.class))).thenReturn(result);

        Result result1 = workflowInstanceV2Controller.execute(loginUser, 1, ExecuteType.STOP);
        Assert.assertTrue(result1.isSuccess());
    }

    private User getLoginUser() {
        User user = new User();
        user.setId(1);
        user.setUserName("admin");
        return user;
    }
}

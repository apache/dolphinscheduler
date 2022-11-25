package org.apache.dolphinscheduler.api.dto.workflowInstance;

import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;

/**
 * workflow instance list paging response
 */
public class WorkflowInstanceListPagingResponse extends Result {

    private PageInfo<ProcessInstance> data;

    public WorkflowInstanceListPagingResponse(Result result) {
        super();
        this.setCode(result.getCode());
        this.setMsg(result.getMsg());
        this.setData(result.getData());
    }
}

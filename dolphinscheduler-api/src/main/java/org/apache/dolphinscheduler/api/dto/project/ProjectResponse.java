package org.apache.dolphinscheduler.api.dto.project;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.Project;

/**
 * project response
 */
public class ProjectResponse extends Result {
    private Project data;

    public ProjectResponse(Result result) {
        super();
        this.setCode(result.getCode());
        this.setMsg(result.getMsg());
        this.setData((Project) result.getData());
    }

    @Override
    public Project getData() {
        return data;
    }

    public void setData(Project data) {
        this.data = data;
    }
}

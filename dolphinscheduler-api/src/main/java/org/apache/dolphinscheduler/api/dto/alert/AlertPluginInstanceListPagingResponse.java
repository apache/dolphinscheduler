package org.apache.dolphinscheduler.api.dto.alert;

import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * alert plugin instance List paging response
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AlertPluginInstanceListPagingResponse extends Result {

    private PageInfo<AlertPluginInstanceVO> data;

    public AlertPluginInstanceListPagingResponse(Result result) {
        super();
        this.setCode(result.getCode());
        this.setMsg(result.getMsg());
        this.setData((PageInfo<AlertPluginInstanceVO>) result.getData());
    }
}

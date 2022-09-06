package org.apache.dolphinscheduler.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkerGroupDto {

    private int id;

    private String name;

    private String addrList;

    @Builder.Default
    private Date createTime = new Date();

    private Date updateTime;

    private String description;

    private boolean systemDefault;

    private @Nullable WorkerGroupExtraParam workerGroupExtraParam;

    public static WorkerGroupDto transformFromDo(@NonNull WorkerGroup workerGroup) {
        return WorkerGroupDto.builder()
                .id(workerGroup.getId())
                .name(workerGroup.getName())
                .addrList(workerGroup.getAddrList())
                .createTime(workerGroup.getCreateTime())
                .updateTime(workerGroup.getUpdateTime())
                .description(workerGroup.getDescription())
                .workerGroupExtraParam(
                        JSONUtils.parseObject(workerGroup.getWorkerGroupExtraParam(), WorkerGroupExtraParam.class))
                .build();
    }

    public WorkerGroup transformToDo() {
        return WorkerGroup.builder()
                .id(id)
                .name(name)
                .addrList(addrList)
                .createTime(createTime)
                .updateTime(updateTime)
                .description(description)
                .workerGroupExtraParam(JSONUtils.toJsonString(workerGroupExtraParam))
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerGroupExtraParam {

        private String filterList;
        private Map<String, String> extraParams;
    }

}

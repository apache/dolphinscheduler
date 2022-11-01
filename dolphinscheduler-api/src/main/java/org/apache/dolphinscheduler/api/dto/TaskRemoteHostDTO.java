package org.apache.dolphinscheduler.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskRemoteHostDTO {

    @Schema(example = "app01", description = "TASK_REMOTE_HOST_NAME", required = true)
    private String name;

    @Schema(example = "127.0.0.1", description = "TASK_REMOTE_HOST_IP", required = true)
    private String ip;

    @Schema(example = "22", implementation = int.class, description = "TASK_REMOTE_HOST_PORT", required = true)
    private Integer port;

    @Schema(example = "foo", description = "TASK_REMOTE_HOST_ACCOUNT", required = true)
    private String account;

    @Schema(example = "foo", description = "TASK_REMOTE_HOST_PASSWORD", required = true)
    private String password;

    @Schema(example = "this is a demo host", description = "TASK_REMOTE_HOST_DESC")
    private String description;

}

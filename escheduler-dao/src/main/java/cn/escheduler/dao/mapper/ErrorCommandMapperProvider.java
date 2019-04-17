package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.*;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class ErrorCommandMapperProvider {


    private static final String TABLE_NAME = "t_escheduler_error_command";


    /**
     * inert command
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`command_type`", EnumFieldUtil.genFieldStr("errorCommand.commandType", CommandType.class));
                VALUES("`process_definition_id`", "#{errorCommand.processDefinitionId}");
                VALUES("`executor_id`", "#{errorCommand.executorId}");
                VALUES("`command_param`", "#{errorCommand.commandParam}");
                VALUES("`task_depend_type`", EnumFieldUtil.genFieldStr("errorCommand.taskDependType", TaskDependType.class));
                VALUES("`failure_strategy`", EnumFieldUtil.genFieldStr("errorCommand.failureStrategy", FailureStrategy.class));
                VALUES("`warning_type`", EnumFieldUtil.genFieldStr("errorCommand.warningType", WarningType.class));
                VALUES("`process_instance_priority`", EnumFieldUtil.genFieldStr("errorCommand.processInstancePriority", Priority.class));
                VALUES("`warning_group_id`", "#{errorCommand.warningGroupId}");
                VALUES("`schedule_time`", "#{errorCommand.scheduleTime}");
                VALUES("`update_time`", "#{errorCommand.updateTime}");
                VALUES("`start_time`", "#{errorCommand.startTime}");
                VALUES("`message`", "#{errorCommand.message}");
            }
        }.toString();
    }
}

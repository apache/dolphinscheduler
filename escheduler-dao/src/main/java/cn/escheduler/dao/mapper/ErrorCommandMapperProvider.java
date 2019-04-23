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
                VALUES("`id`", "#{errorCommand.id}");
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
                VALUES("`worker_group_id`", "#{errorCommand.workerGroupId}");
                VALUES("`message`", "#{errorCommand.message}");
            }
        }.toString();
    }

    /**
     *
     * count command type
     * @param parameter
     * @return
     */
    public String countCommandState(Map<String, Object> parameter){
        return new SQL(){
            {
                SELECT("command_type as state,COUNT(*) AS count");
                FROM(TABLE_NAME + " cmd,t_escheduler_process_definition process");
                WHERE("cmd.process_definition_id = process.id");
                if(parameter.get("projectId") != null && (int)parameter.get("projectId") != 0){
                    WHERE( "process.project_id = #{projectId} ");
                }else{
                    if(parameter.get("userType") != null && String.valueOf(parameter.get("userType")) == "GENERAL_USER") {
                        AND();
                        WHERE("process.project_id in (select id as project_id from t_escheduler_project tp where tp.user_id= #{userId} " +
                                "union select project_id from t_escheduler_relation_project_user tr where tr.user_id= #{userId} )");

                    }
                }
                WHERE("cmd.start_time >= #{startTime} and cmd.update_time <= #{endTime}");
                GROUP_BY("cmd.command_type");
            }
        }.toString();
    }
}

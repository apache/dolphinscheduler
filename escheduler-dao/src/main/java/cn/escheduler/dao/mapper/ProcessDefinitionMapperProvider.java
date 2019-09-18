/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.dao.mapper;

import cn.escheduler.common.enums.Flag;
import cn.escheduler.common.enums.ReleaseState;
import cn.escheduler.common.utils.EnumFieldUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

/**
 * process definition mapper provider
 */
public class ProcessDefinitionMapperProvider {

    private static final String TABLE_NAME = "t_escheduler_process_definition";



    /**
     * 插入流程定义
     *
     * @param parameter
     * @return
     */
    public String insert(Map<String, Object> parameter) {
        return new SQL() {
            {
                INSERT_INTO(TABLE_NAME);
                VALUES("`name`", "#{processDefinition.name}");
                VALUES("`version`", "#{processDefinition.version}");
                VALUES("`release_state`", EnumFieldUtil.genFieldStr("processDefinition.releaseState", ReleaseState.class));
                VALUES("`project_id`", "#{processDefinition.projectId}");
                VALUES("`process_definition_json`", "#{processDefinition.processDefinitionJson}");
                VALUES("`desc`", "#{processDefinition.desc}");
                VALUES("`global_params`", "#{processDefinition.globalParams}");
                VALUES("`locations`", "#{processDefinition.locations}");
                VALUES("`connects`", "#{processDefinition.connects}");
                VALUES("`create_time`", "#{processDefinition.createTime}");
                VALUES("`update_time`", "#{processDefinition.updateTime}");
                VALUES("`receivers` ","#{processDefinition.receivers}");
                VALUES("`receivers_cc`", "#{processDefinition.receiversCc}");
                VALUES("`timeout`", "#{processDefinition.timeout}");
                VALUES("`tenant_id`", "#{processDefinition.tenantId}");
                VALUES("`flag`", EnumFieldUtil.genFieldStr("processDefinition.flag", ReleaseState.class));
                VALUES("`user_id`", "#{processDefinition.userId}");

            }
        }.toString();
    }

    /**
     * 删除流程定义
     *
     * @param parameter
     * @return
     */
    public String delete(Map<String, Object> parameter) {
        return new SQL() {
            {
                DELETE_FROM(TABLE_NAME);

                WHERE("`id`=#{processDefinitionId}");
            }
        }.toString();
    }

    /**
     * 更新流程定义
     *
     * @param parameter
     * @return
     */
    public String update(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`name`=#{processDefinition.name}");
                SET("`version`=#{processDefinition.version}");
                SET("`release_state`="+EnumFieldUtil.genFieldStr("processDefinition.releaseState", ReleaseState.class));
                SET("`project_id`=#{processDefinition.projectId}");
                SET("`process_definition_json`=#{processDefinition.processDefinitionJson}");
                SET("`desc`=#{processDefinition.desc}");
                SET("`locations`=#{processDefinition.locations}");
                SET("`connects`=#{processDefinition.connects}");
                SET("`global_params`=#{processDefinition.globalParams}");
                SET("`create_time`=#{processDefinition.createTime}");
                SET("`update_time`=#{processDefinition.updateTime}");
                SET("`receivers`=#{processDefinition.receivers}");
                SET("`receivers_cc`=#{processDefinition.receiversCc}");
                SET("`timeout`=#{processDefinition.timeout}");
                SET("`tenant_id`=#{processDefinition.tenantId}");
                SET("`flag`="+EnumFieldUtil.genFieldStr("processDefinition.flag", Flag.class));
                SET("`user_id`=#{processDefinition.userId}");

                WHERE("`id`=#{processDefinition.id}");

            }
        }.toString();
    }

    public String updateProcessDefinitionReleaseState(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);

                SET("`release_state`="+EnumFieldUtil.genFieldStr("releaseState", ReleaseState.class));


                WHERE("`id`=#{processDefinitionId}");

            }
        }.toString();
    }

    /**
     * 根据流程定义 id 查询 sql
     *
     * @param parameter
     * @return
     */
    public String queryByDefineId(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("pd.*,u.user_name,p.name as project_name");

                FROM(TABLE_NAME + " pd");
                JOIN("t_escheduler_user u ON pd.user_id = u.id");
                JOIN("t_escheduler_project p ON pd.project_id = p.id");
                WHERE("pd.id = #{processDefinitionId}");
            }
        }.toString();
    }


    /**
     * query By Define Name
     *
     * @param parameter
     * @return
     */
    public String queryByDefineName(Map<String, Object> parameter) {
        return new SQL() {
            {
                SELECT("pd.*,u.user_name,p.name as project_name,t.tenant_code,t.tenant_name,q.queue,q.queue_name");

                FROM(TABLE_NAME + " pd");
                JOIN("t_escheduler_user u ON pd.user_id = u.id");
                JOIN("t_escheduler_project p ON pd.project_id = p.id");
                JOIN("t_escheduler_tenant t ON t.id = u.tenant_id");
                JOIN("t_escheduler_queue q ON t.queue_id = q.id");
                WHERE("p.id = #{projectId}");
                WHERE("pd.name = #{processDefinitionName}");
            }
        }.toString();
    }


    /**
     * query define list paging
     * @param parameter
     * @return
     */
    public String queryDefineListPaging(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("td.*,sc.schedule_release_state");
            FROM(TABLE_NAME + " td");
            LEFT_OUTER_JOIN(" (select process_definition_id,release_state as schedule_release_state from `t_escheduler_schedules` " +
                    "group by `process_definition_id`,`release_state`) sc on sc.process_definition_id = td.id");

            WHERE("td.project_id = #{projectId} ");
            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " td.name like concat('%', #{searchVal}, '%') ");
            }
            Object userId = parameter.get("userId");
            if(userId != null && 0 != Integer.parseInt(userId.toString())){
                WHERE("td.user_id = #{userId}");
            }
            ORDER_BY(" sc.schedule_release_state desc,td.update_time desc limit #{offset},#{pageSize} ");
        }}.toString();
    }
    /**
     * query all define list by project id
     * @param parameter
     * @return
     */
    public String queryAllDefinitionList(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("id,name,version,release_state,project_id,user_id,`desc`,create_time,update_time,flag,global_params,receivers,receivers_cc");

            FROM(TABLE_NAME );

            WHERE("project_id = #{projectId}");
            ORDER_BY("create_time desc ");
        }}.toString();
    }
    /**
     * count definition number group by project id
     * @param parameter
     * @return
     */
    public String countDefineNumber(Map<String, Object> parameter) {
        return new SQL() {{
            SELECT("count(0)");

            FROM(TABLE_NAME);

            WHERE("project_id = #{projectId}");
            Object searchVal = parameter.get("searchVal");
            if(searchVal != null && StringUtils.isNotEmpty(searchVal.toString())){
                WHERE( " name like concat('%', #{searchVal}, '%') ");
            }
            Object userId = parameter.get("userId");
            if(userId != null && 0 != Integer.parseInt(userId.toString())){
                WHERE("user_id = #{userId}");
            }
        }}.toString();
    }

    /**
     * count definition number by user type
     * @param parameter
     * @return
     */
    public String countDefinitionGroupByUser(Map<String, Object> parameter) {
        return new SQL() {{

            SELECT("td.user_id as user_id, tu.user_name as user_name, count(0) as count");
            FROM(TABLE_NAME + " td");
            JOIN("t_escheduler_user tu on tu.id=td.user_id");

            if(parameter.get("projectId") != null && (int)parameter.get("projectId") != 0){
                WHERE( "td.project_id = #{projectId} ");
            }else{
                if(parameter.get("userType") != null && String.valueOf(parameter.get("userType")) == "GENERAL_USER") {
                    AND();
                    WHERE("td.project_id in (select id as project_id from t_escheduler_project tp where tp.user_id= #{userId} " +
                            "union select project_id from t_escheduler_relation_project_user tr where tr.user_id= #{userId} )");

                }
            }
            GROUP_BY("td.user_id");

        }}.toString();
    }

    /**
     * query definition list by define id list
     * @param parameter
     * @return
     */
    public String queryDefinitionListByIdList(Map<String, Object> parameter){
        List<String> ids = (List<String>) parameter.get("ids");

        return new SQL() {{
            SELECT("*");

            FROM(TABLE_NAME);

            WHERE("`id` in (" + String.join(",", ids) + ")");
        }}.toString();
    }

    /**
     * update receivers and cc by definition id
     *
     * @param parameter
     * @return
     */
    public String updateReceiversAndCcById(Map<String, Object> parameter) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                SET("`receivers`=#{receivers}");
                SET("`receivers_cc`=#{receiversCc}");
                WHERE("`id`=#{processDefinitionId}");

            }
        }.toString();
    }


    /**
     * query all
     * @return
     */
    public String queryAll() {
        return new SQL() {{
            SELECT("id,name,version,release_state,project_id,user_id,`desc`,create_time,update_time,flag,global_params,receivers,receivers_cc");

            FROM(TABLE_NAME );

            ORDER_BY("create_time desc ");
        }}.toString();
    }
}

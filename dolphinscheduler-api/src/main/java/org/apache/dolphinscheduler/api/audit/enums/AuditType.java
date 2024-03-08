package org.apache.dolphinscheduler.api.audit.enums;

import static org.apache.dolphinscheduler.common.enums.AuditObjectType.ALARM_GROUP;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.ALARM_INSTANCE;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.CLUSTER;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.DATASOURCE;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.ENVIRONMENT;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.FILE;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.FOLDER;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.K8S_NAMESPACE;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.PROCESS;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.PROCESS_INSTANCE;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.PROJECT;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.SCHEDULE;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.TASK;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.TASK_GROUP;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.TASK_INSTANCE;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.TENANT;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.TOKEN;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.UDF_FUNCTION;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.USER;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.WORKER_GROUP;
import static org.apache.dolphinscheduler.common.enums.AuditObjectType.YARN_QUEUE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.AUTHORIZE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.BATCH_DELETE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.BATCH_RERUN;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.BATCH_START;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.CLOSE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.COPY;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.CREATE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.DELETE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.DELETE_VERSION;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.EXECUTE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.EXPORT;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.FORCE_SUCCESS;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.IMPORT;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.MODIFY;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.OFFLINE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.ONLINE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.RELEASE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.RERUN;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.START;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.SWITCH_VERSION;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.UN_AUTHORIZE;
import static org.apache.dolphinscheduler.common.enums.AuditOperationType.UPDATE;

import org.apache.dolphinscheduler.api.audit.operator.Operator;
import org.apache.dolphinscheduler.api.audit.operator.impl.AlertGroupOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.AlertInstanceOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.ClusterOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.DatasourceOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.EnvironmentOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.K8sNamespaceOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.ProcessInstanceOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.ProcessOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.ProjectOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.ResourceOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.ScheduleOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.TaskGroupOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.TaskInstanceOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.TaskOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.TenantOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.TokenOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.UdfFunctionOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.UserOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.WorkerGroupOperatorImpl;
import org.apache.dolphinscheduler.api.audit.operator.impl.YarnQueueOperatorImpl;
import org.apache.dolphinscheduler.common.enums.AuditObjectType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;

import lombok.Getter;

@Getter
public enum AuditType {

    PROJECT_CREATE(PROJECT, CREATE, ProjectOperatorImpl.class, new String[]{}, new String[]{"code"}),
    PROJECT_UPDATE(PROJECT, UPDATE, ProjectOperatorImpl.class, new String[]{}, new String[]{"code"}),
    PROJECT_DELETE(PROJECT, DELETE, ProjectOperatorImpl.class, new String[]{"code"}, new String[]{}),

    PROCESS_CREATE(PROCESS, CREATE, ProcessOperatorImpl.class, new String[]{}, new String[]{"code"}),
    PROCESS_UPDATE(PROCESS, UPDATE, ProcessOperatorImpl.class, new String[]{}, new String[]{"code"}),
    PROCESS_SWITCH_VERSION(PROCESS, SWITCH_VERSION, ProcessOperatorImpl.class, new String[]{"code", "version"},
            new String[]{}),
    PROCESS_DELETE_VERSION(PROCESS, DELETE_VERSION, ProcessOperatorImpl.class, new String[]{"code", "version"},
            new String[]{}),
    PROCESS_RELEASE(PROCESS, RELEASE, ProcessOperatorImpl.class, new String[]{"workflowDefinitionCode"},
            new String[]{}),
    PROCESS_COPY(PROCESS, COPY, ProcessOperatorImpl.class, new String[]{"codes"}, new String[]{}),
    PROCESS_EXPORT(PROCESS, EXPORT, ProcessOperatorImpl.class, new String[]{"codes"}, new String[]{}),
    PROCESS_DELETE(PROCESS, DELETE, ProcessOperatorImpl.class, new String[]{"code"}, new String[]{}),
    PROCESS_BATCH_DELETE(PROCESS, BATCH_DELETE, ProcessOperatorImpl.class, new String[]{"codes"}, new String[]{}),
    PROCESS_START(PROCESS, START, ProcessOperatorImpl.class, new String[]{"processDefinitionCode"}, new String[]{}),
    PROCESS_BATCH_START(PROCESS, BATCH_START, ProcessOperatorImpl.class, new String[]{"processDefinitionCodes"},
            new String[]{}),
    PROCESS_RERUN(PROCESS, RERUN, ProcessOperatorImpl.class, new String[]{"processInstanceId"}, new String[]{}),
    PROCESS_BATCH_RERUN(PROCESS, BATCH_RERUN, ProcessOperatorImpl.class, new String[]{"processInstanceIds"},
            new String[]{}),
    PROCESS_EXECUTE(PROCESS, EXECUTE, ProcessOperatorImpl.class, new String[]{"processInstanceId"}, new String[]{}),

    // todo
    PROCESS_IMPORT(PROCESS, IMPORT, ProcessOperatorImpl.class, new String[]{}, new String[]{}),

    PROCESS_INSTANCE_UPDATE(PROCESS_INSTANCE, UPDATE, ProcessInstanceOperatorImpl.class, new String[]{"id"},
            new String[]{}),
    PROCESS_INSTANCE_DELETE(PROCESS_INSTANCE, DELETE, ProcessInstanceOperatorImpl.class, new String[]{"id"},
            new String[]{}),
    PROCESS_INSTANCE_BATCH_DELETE(PROCESS_INSTANCE, BATCH_DELETE, ProcessInstanceOperatorImpl.class,
            new String[]{"processInstanceIds"}, new String[]{}),

    TASK_CREATE(TASK, CREATE, TaskOperatorImpl.class, new String[]{}, new String[]{"code"}),
    TASK_UPDATE(TASK, UPDATE, TaskOperatorImpl.class, new String[]{}, new String[]{"code"}),
    TASK_SWITCH_VERSION(TASK, SWITCH_VERSION, TaskOperatorImpl.class, new String[]{"code", "version"}, new String[]{}),
    TASK_DELETE_VERSION(TASK, DELETE_VERSION, TaskOperatorImpl.class, new String[]{"code", "version"}, new String[]{}),
    TASK_DELETE(TASK, DELETE, TaskOperatorImpl.class, new String[]{"code"}, new String[]{}),
    // todo need test
    TASK_RELEASE(TASK, RELEASE, TaskOperatorImpl.class, new String[]{"code"}, new String[]{}),
    TASK_START(TASK, START, TaskOperatorImpl.class, new String[]{"code"}, new String[]{}),

    // todo need test
    TASK_INSTANCE_FORCE_SUCCESS(TASK_INSTANCE, FORCE_SUCCESS, TaskInstanceOperatorImpl.class, new String[]{"id"},
            new String[]{}),

    SCHEDULE_CREATE(SCHEDULE, CREATE, ScheduleOperatorImpl.class, new String[]{"processDefinitionCode"},
            new String[]{"id"}),
    SCHEDULE_UPDATE(SCHEDULE, UPDATE, ScheduleOperatorImpl.class, new String[]{"id"}, new String[]{}),
    SCHEDULE_ONLINE(SCHEDULE, ONLINE, ScheduleOperatorImpl.class, new String[]{"id"}, new String[]{}),
    SCHEDULE_OFFLINE(SCHEDULE, OFFLINE, ScheduleOperatorImpl.class, new String[]{"id"}, new String[]{}),
    // todo need test
    SCHEDULE_DELETE(SCHEDULE, DELETE, ScheduleOperatorImpl.class, new String[]{"id"}, new String[]{}),

    FOLDER_CREATE(FOLDER, CREATE, ResourceOperatorImpl.class, new String[]{"type", "alias"}, new String[]{}),
    FOLDER_UPDATE(FOLDER, UPDATE, ResourceOperatorImpl.class, new String[]{"type", "alias"}, new String[]{}),
    FILE_CREATE(FILE, CREATE, ResourceOperatorImpl.class, new String[]{"type", "fileName"}, new String[]{}),
    FILE_UPDATE(FILE, UPDATE, ResourceOperatorImpl.class, new String[]{"type", "fileName"}, new String[]{}),
    FILE_DELETE(FILE, DELETE, ResourceOperatorImpl.class, new String[]{"fileName"}, new String[]{}),
    // todo
    UDF_FUNCTION_CREATE(UDF_FUNCTION, CREATE, UdfFunctionOperatorImpl.class, new String[]{"funcName"}, new String[]{}),
    UDF_FUNCTION_UPDATE(UDF_FUNCTION, UPDATE, UdfFunctionOperatorImpl.class, new String[]{"funcName"}, new String[]{}),
    UDF_FUNCTION_DELETE(UDF_FUNCTION, DELETE, UdfFunctionOperatorImpl.class, new String[]{"udfFuncId"}, new String[]{}),
    UDF_FUNCTION_UN_AUTHORIZE(UDF_FUNCTION, UN_AUTHORIZE, UserOperatorImpl.class, new String[]{"userId"},
            new String[]{}),
    UDF_FUNCTION_AUTHORIZE(UDF_FUNCTION, AUTHORIZE, UserOperatorImpl.class, new String[]{"userId"}, new String[]{}),

    TASK_GROUP_CREATE(TASK_GROUP, CREATE, TaskGroupOperatorImpl.class, new String[]{"name"}, new String[]{}),
    TASK_GROUP_UPDATE(TASK_GROUP, UPDATE, TaskGroupOperatorImpl.class, new String[]{}, new String[]{"id"}),
    TASK_GROUP_CLOSE(TASK_GROUP, CLOSE, TaskGroupOperatorImpl.class, new String[]{"id"}, new String[]{}),
    TASK_GROUP_START(TASK_GROUP, START, TaskGroupOperatorImpl.class, new String[]{"id"}, new String[]{}),
    // todo test
    TASK_GROUP_MODIFY(TASK_GROUP, MODIFY, TaskGroupOperatorImpl.class, new String[]{"queueId", "priority"},
            new String[]{}),

    DATASOURCE_CREATE(DATASOURCE, CREATE, DatasourceOperatorImpl.class, new String[]{}, new String[]{"id"}),
    DATASOURCE_UPDATE(DATASOURCE, UPDATE, DatasourceOperatorImpl.class, new String[]{}, new String[]{"id"}),
    DATASOURCE_DELETE(DATASOURCE, DELETE, DatasourceOperatorImpl.class, new String[]{"id"}, new String[]{}),

    TENANT_CREATE(TENANT, CREATE, TenantOperatorImpl.class, new String[]{}, new String[]{"id"}),
    TENANT_UPDATE(TENANT, UPDATE, TenantOperatorImpl.class, new String[]{"id"}, new String[]{}),
    TENANT_DELETE(TENANT, DELETE, TenantOperatorImpl.class, new String[]{"id"}, new String[]{}),

    USER_CREATE(USER, CREATE, UserOperatorImpl.class, new String[]{}, new String[]{"id"}),
    USER_UPDATE(USER, UPDATE, UserOperatorImpl.class, new String[]{}, new String[]{"id"}),
    USER_DELETE(USER, DELETE, UserOperatorImpl.class, new String[]{"id"}, new String[]{}),

    ALARM_GROUP_CREATE(ALARM_GROUP, CREATE, AlertGroupOperatorImpl.class, new String[]{}, new String[]{"id"}),
    ALARM_GROUP_UPDATE(ALARM_GROUP, UPDATE, AlertGroupOperatorImpl.class, new String[]{}, new String[]{"id"}),
    ALARM_GROUP_DELETE(ALARM_GROUP, DELETE, AlertGroupOperatorImpl.class, new String[]{"id"}, new String[]{}),

    ALARM_INSTANCE_CREATE(ALARM_INSTANCE, CREATE, AlertInstanceOperatorImpl.class, new String[]{}, new String[]{"id"}),
    ALARM_INSTANCE_UPDATE(ALARM_INSTANCE, UPDATE, AlertInstanceOperatorImpl.class, new String[]{}, new String[]{"id"}),
    ALARM_INSTANCE_DELETE(ALARM_INSTANCE, DELETE, AlertInstanceOperatorImpl.class, new String[]{"id"}, new String[]{}),

    WORKER_GROUP_CREATE(WORKER_GROUP, CREATE, WorkerGroupOperatorImpl.class, new String[]{}, new String[]{"id"}),
    WORKER_GROUP_UPDATE(WORKER_GROUP, UPDATE, WorkerGroupOperatorImpl.class, new String[]{}, new String[]{"id"}),
    WORKER_GROUP_DELETE(WORKER_GROUP, DELETE, WorkerGroupOperatorImpl.class, new String[]{"id"}, new String[]{}),

    YARN_QUEUE_CREATE(YARN_QUEUE, CREATE, YarnQueueOperatorImpl.class, new String[]{}, new String[]{"id"}),
    YARN_QUEUE_UPDATE(YARN_QUEUE, UPDATE, YarnQueueOperatorImpl.class, new String[]{}, new String[]{"id"}),
    YARN_QUEUE_DELETE(YARN_QUEUE, DELETE, YarnQueueOperatorImpl.class, new String[]{"id"}, new String[]{}),

    ENVIRONMENT_CREATE(ENVIRONMENT, CREATE, EnvironmentOperatorImpl.class, new String[]{}, new String[]{"code"}),
    ENVIRONMENT_UPDATE(ENVIRONMENT, UPDATE, EnvironmentOperatorImpl.class, new String[]{}, new String[]{"code"}),
    ENVIRONMENT_DELETE(ENVIRONMENT, DELETE, EnvironmentOperatorImpl.class, new String[]{"environmentCode"},
            new String[]{}),

    CLUSTER_CREATE(CLUSTER, CREATE, ClusterOperatorImpl.class, new String[]{}, new String[]{"code"}),
    CLUSTER_UPDATE(CLUSTER, UPDATE, ClusterOperatorImpl.class, new String[]{}, new String[]{"code"}),
    CLUSTER_DELETE(CLUSTER, DELETE, ClusterOperatorImpl.class, new String[]{"clusterCode"}, new String[]{}),

    K8S_NAMESPACE_CREATE(K8S_NAMESPACE, CREATE, K8sNamespaceOperatorImpl.class, new String[]{}, new String[]{"id"}),
    K8S_NAMESPACE_DELETE(K8S_NAMESPACE, DELETE, K8sNamespaceOperatorImpl.class, new String[]{"id"}, new String[]{}),

    TOKEN_CREATE(TOKEN, CREATE, TokenOperatorImpl.class, new String[]{}, new String[]{"userId"}),
    TOKEN_UPDATE(TOKEN, UPDATE, TokenOperatorImpl.class, new String[]{}, new String[]{"userId"}),
    TOKEN_DELETE(TOKEN, DELETE, TokenOperatorImpl.class, new String[]{"id"}, new String[]{}),
    ;

    private final Class<? extends Operator> operatorClass;
    private final AuditObjectType auditObjectType;
    private final AuditOperationType auditOperationType;

    /**
     * The names of the fields in the API request to be recorded.
     * Represents an array of key-value pairs, e.g., ["id", "status"].
     */
    private final String[] requestParamName;

    /**
     * The names of the fields in the returned object to be recorded.
     * Represents an array of field names, e.g., ["id", "code"].
     * Specify the field names to record from the returned object.
     */
    private final String[] returnObjectFieldName;

    AuditType(AuditObjectType auditObjectType, AuditOperationType auditOperationType,
              Class<? extends Operator> operatorClass, String[] requestParamName, String[] returnObjectFieldName) {
        this.auditObjectType = auditObjectType;
        this.auditOperationType = auditOperationType;
        this.operatorClass = operatorClass;
        this.requestParamName = requestParamName;
        this.returnObjectFieldName = returnObjectFieldName;
    }
}

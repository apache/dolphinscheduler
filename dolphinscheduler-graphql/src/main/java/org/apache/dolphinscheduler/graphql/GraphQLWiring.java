package org.apache.dolphinscheduler.graphql;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import org.apache.dolphinscheduler.graphql.datafetcher.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GraphQLWiring {

    @Autowired
    private AlertGroupDataFetchers alertGroupDataFetchers;

    @Autowired
    private AccessTokenDataFetchers accessTokenDataFetchers;

    @Autowired
    private DataAnalysisDataFetchers dataAnalysisDataFetchers;

    @Autowired
    private DataSourceDataFetchers dataSourceDataFetchers;

    @Autowired
    private ExecutorDataFetchers executorDataFetchers;

    @Autowired
    private AlertPluginInstanceDataFetchers alertPluginInstanceDataFetchers;

    @Autowired
    private LoggerDataFetchers loggerDataFetchers;

    @Autowired
    private LoginDataFetchers loginDataFetchers;

    @Autowired
    private MonitorDataFetchers monitorDataFetchers;

    @Autowired
    private ProcessDefinitionDataFetchers processDefinitionDataFetchers;

    @Autowired
    private ProcessInstanceDataFetchers processInstanceDataFetchers;

    @Autowired
    private ProjectDataFetchers projectDataFetchers;

    @Autowired
    private QueueDataFetchers queueDataFetchers;

    @Autowired
    private ResourcesDataFetchers resourcesDataFetchers;

    @Autowired
    private ScheduleDataFetchers scheduleDataFetchers;

    @Autowired
    private TaskInstanceDataFetchers taskInstanceDataFetchers;

    @Autowired
    private TenantDataFetchers tenantDataFetchers;

    @Autowired
    private UiPluginDataFetchers uiPluginDataFetchers;

    @Autowired
    private UserDataFetchers userDataFetchers;


    protected RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                // Wiring every GraphQL type
                .type("Query", this::addWiringForQueryType)
                .type("Mutation", this::addWiringForMutationType)
                .type("DataSource", this::addWiringForDataSourceType)
                .type("BaseDataSourceParamDTO", this::addWiringForBaseDataSourceParamDTO)
                .build();
    } // buildWiring()


    protected TypeRuntimeWiring.Builder addWiringForQueryType(TypeRuntimeWiring.Builder typeWiring) {
        // AlertGroup GraphQL Query
        typeWiring.dataFetcher("queryAllGroupList",
                alertGroupDataFetchers.queryTypeQueryAllGroupList());
        typeWiring.dataFetcher("queryAlertGroupListPaging",
                alertGroupDataFetchers.queryTypeQueryAlertGroupListPaging());
        typeWiring.dataFetcher("verifyGroupName",
                alertGroupDataFetchers.queryTypeVerifyGroupName());

        // AccessToken GraphQL Query
        typeWiring.dataFetcher("generateToken",
                accessTokenDataFetchers.queryTypeGenerateToken());
        typeWiring.dataFetcher("queryAccessTokenList",
                accessTokenDataFetchers.queryTypeQueryAccessTokenList());

        // DataAnalysis GraphQL Query
        typeWiring.dataFetcher("countTaskStateByProject",
                dataAnalysisDataFetchers.queryTypeCountTaskState());
        typeWiring.dataFetcher("countProcessInstanceState",
                dataAnalysisDataFetchers.queryTypeCountProcessInstanceState());
        typeWiring.dataFetcher("countDefinitionByUser",
                dataAnalysisDataFetchers.queryTypeCountDefinitionByUser());
        typeWiring.dataFetcher("countCommandState",
                dataAnalysisDataFetchers.queryTypeCountCommandState());
        typeWiring.dataFetcher("countQueueState",
                dataAnalysisDataFetchers.queryTypeCountQueueState());

        // DataSource GraphQL Query
        typeWiring.dataFetcher("queryDataSource",
                dataSourceDataFetchers.queryTypeQueryDataSource());
        typeWiring.dataFetcher("queryDataSourceList",
                dataSourceDataFetchers.queryTypeQueryDataSourceList());
        typeWiring.dataFetcher("queryDataSourceListPaging",
                dataSourceDataFetchers.queryTypeQueryDataSourceListPaging());
        typeWiring.dataFetcher("connectDataSource",
                dataSourceDataFetchers.queryTypeConnectDataSource());
        typeWiring.dataFetcher("connectionTest",
                dataSourceDataFetchers.queryTypeConnectionTest());
        typeWiring.dataFetcher("verifyDataSourceName",
                dataSourceDataFetchers.queryTypeVerifyDataSourceName());
        typeWiring.dataFetcher("unauthDatasource",
                dataSourceDataFetchers.queryTypeUnauthDatasource());
        typeWiring.dataFetcher("authedDatasource",
                dataSourceDataFetchers.queryTypeAuthedDatasource());
        typeWiring.dataFetcher("getKerberosStartupState",
                dataSourceDataFetchers.queryTypeGetKerberosStartupState());

        // Executor GraphQL Query
        typeWiring.dataFetcher("startCheckProcessDefinition",
                executorDataFetchers.queryTypeStartCheckProcessDefinition());

        // AlertPluginInstance Query
        typeWiring.dataFetcher("getAlertPluginInstance",
                alertPluginInstanceDataFetchers.queryTypeGetAlertPluginInstance());
        typeWiring.dataFetcher("getAllAlertPluginInstance",
                alertPluginInstanceDataFetchers.queryTypeGetAllAlertPluginInstance());
        typeWiring.dataFetcher("verifyAlertInstanceName",
                alertPluginInstanceDataFetchers.queryTypeVerifyAlertInstanceName());
        typeWiring.dataFetcher("queryAlertPluginInstanceListPaging",
                alertPluginInstanceDataFetchers.queryTypeQueryAlertPluginInstanceListPaging());

        // Logger Query
        typeWiring.dataFetcher("queryLog",
                loggerDataFetchers.queryTypeQueryLog());
        typeWiring.dataFetcher("downloadTaskLog",
                loggerDataFetchers.queryTypeDownloadTaskLog());

        // Login Query
        typeWiring.dataFetcher("login",
                loginDataFetchers.queryTypeLogin());

        // Monitor Query
        typeWiring.dataFetcher("listMaster",
                monitorDataFetchers.queryTypeListMaster());
        typeWiring.dataFetcher("listWorker",
                monitorDataFetchers.queryTypeListWorker());
        typeWiring.dataFetcher("queryDatabaseState",
                monitorDataFetchers.queryTypeQueryDatabaseState());
        typeWiring.dataFetcher("queryZookeeperState",
                monitorDataFetchers.queryTypeQueryZookeeperState());

        // ProcessDefinition Query
        typeWiring.dataFetcher("copyProcessDefinition",
                processDefinitionDataFetchers.queryTypeCopyProcessDefinition());
        typeWiring.dataFetcher("moveProcessDefinition",
                processDefinitionDataFetchers.queryTypeMoveProcessDefinition());
        typeWiring.dataFetcher("verifyProcessDefinitionName",
                processDefinitionDataFetchers.queryTypeVerifyProcessDefinitionName());
        typeWiring.dataFetcher("queryProcessDefinitionVersions",
                processDefinitionDataFetchers.queryTypeQueryProcessDefinitionVersions());
        typeWiring.dataFetcher("queryProcessDefinitionById",
                processDefinitionDataFetchers.queryTypeQueryProcessDefinitionById());
        typeWiring.dataFetcher("queryProcessDefinitionByName",
                processDefinitionDataFetchers.queryTypeQueryProcessDefinitionByName());
        typeWiring.dataFetcher("queryProcessDefinitionList",
                processDefinitionDataFetchers.queryTypeQueryProcessDefinitionList());
        typeWiring.dataFetcher("queryProcessDefinitionListPaging",
                processDefinitionDataFetchers.queryTypeQueryProcessDefinitionListPaging());
        typeWiring.dataFetcher("viewTree",
                processDefinitionDataFetchers.queryTypeViewTree());
        typeWiring.dataFetcher("getNodeListByDefinitionCode",
                processDefinitionDataFetchers.queryTypeGetNodeListByDefinitionCode());
        typeWiring.dataFetcher("getNodeListByDefinitionCodeList",
                processDefinitionDataFetchers.queryTypeGetNodeListByDefinitionCodeList());
        typeWiring.dataFetcher("queryProcessDefinitionAllByProjectId",
                processDefinitionDataFetchers.queryTypeQueryProcessDefinitionAllByProjectId());

        // ProcessInstance Query
        typeWiring.dataFetcher("queryProcessInstanceList",
                processInstanceDataFetchers.queryTypeQueryProcessInstanceList());
        typeWiring.dataFetcher("queryTaskListByProcessId",
                processInstanceDataFetchers.queryTypeQueryTaskListByProcessId());
        typeWiring.dataFetcher("queryProcessInstanceById",
                processInstanceDataFetchers.queryTypeQueryProcessInstanceById());
        typeWiring.dataFetcher("queryTopNLongestRunningProcessInstance",
                processInstanceDataFetchers.queryTypeQueryTopNLongestRunningProcessInstance());
        typeWiring.dataFetcher("querySubProcessInstanceByTaskId",
                processInstanceDataFetchers.queryTypeQuerySubProcessInstanceByTaskId());
        typeWiring.dataFetcher("queryParentInstanceBySubId",
                processInstanceDataFetchers.queryTypeQueryParentInstanceBySubId());
        typeWiring.dataFetcher("viewVariables",
                processInstanceDataFetchers.queryTypeViewVariables());
        typeWiring.dataFetcher("processInstanceViewTree",
                processInstanceDataFetchers.queryTypeProcessInstanceViewTree());

        // Project Query
        typeWiring.dataFetcher("queryProjectById",
                projectDataFetchers.queryTypeQueryProjectById());
        typeWiring.dataFetcher("queryProjectListPaging",
                projectDataFetchers.queryTypeQueryProjectListPaging());
        typeWiring.dataFetcher("queryUnauthorizedProject",
                projectDataFetchers.queryTypeQueryUnauthorizedProject());
        typeWiring.dataFetcher("queryAuthorizedProject",
                projectDataFetchers.queryTypeQueryAuthorizedProject());
        typeWiring.dataFetcher("queryProjectCreatedAndAuthorizedByUser",
                projectDataFetchers.queryTypeQueryProjectCreatedAndAuthorizedByUser());
        typeWiring.dataFetcher("queryAllProjectList",
                projectDataFetchers.queryTypeQueryQueryAllProjectList());

        // Queue Query
        typeWiring.dataFetcher("queryQueueList",
                queueDataFetchers.queryTypeQueryQueueList());
        typeWiring.dataFetcher("queryQueueListPaging",
                queueDataFetchers.queryTypeQueryQueueListPaging());
        typeWiring.dataFetcher("verifyQueue",
                queueDataFetchers.queryTypeVerifyQueue());

        // Resource Query
        typeWiring.dataFetcher("queryResourceList",
                resourcesDataFetchers.queryTypeQueryResourceList());
        typeWiring.dataFetcher("queryResourceListPaging",
                resourcesDataFetchers.queryTypeQueryResourceListPaging());
        typeWiring.dataFetcher("queryResource",
                resourcesDataFetchers.queryTypeQueryResource());
        typeWiring.dataFetcher("viewResource",
                resourcesDataFetchers.queryTypeViewResource());
        typeWiring.dataFetcher("verifyResourceName",
                resourcesDataFetchers.queryTypeVerifyResourceName());
        typeWiring.dataFetcher("queryResourceJarList",
                resourcesDataFetchers.queryTypeQueryResourceJarList());
        typeWiring.dataFetcher("downloadResource",
                resourcesDataFetchers.queryTypeDownloadResource());
        typeWiring.dataFetcher("viewUIUdfFunction",
                resourcesDataFetchers.queryTypeViewUIUdfFunction());
        typeWiring.dataFetcher("queryUdfFuncListPaging",
                resourcesDataFetchers.queryTypeQueryUdfFuncListPaging());
        typeWiring.dataFetcher("queryUdfFuncList",
                resourcesDataFetchers.queryTypeQueryUdfFuncList());
        typeWiring.dataFetcher("verifyUdfFuncName",
                resourcesDataFetchers.queryTypeVerifyUdfFuncName());
        typeWiring.dataFetcher("authorizedFile",
                resourcesDataFetchers.queryTypeAuthorizedFile());
        typeWiring.dataFetcher("authorizeResourceTree",
                resourcesDataFetchers.queryTypeAuthorizeResourceTree());
        typeWiring.dataFetcher("unauthUDFFunc",
                resourcesDataFetchers.queryTypeUnauthUDFFunc());
        typeWiring.dataFetcher("authorizedUDFFunction",
                resourcesDataFetchers.queryTypeAuthorizedUDFFunction());

        // Schedule Query
        typeWiring.dataFetcher("queryScheduleListPaging",
                scheduleDataFetchers.queryTypeQueryScheduleListPaging());
        typeWiring.dataFetcher("queryScheduleList",
                scheduleDataFetchers.queryTypeQueryScheduleList());
        typeWiring.dataFetcher("previewSchedule",
                scheduleDataFetchers.queryTypePreviewSchedule());

        // TaskInstance Query
        typeWiring.dataFetcher("queryTaskListPaging",
                taskInstanceDataFetchers.queryTypeQueryTaskListPaging());

        // Tenant Query
        typeWiring.dataFetcher("queryTenantlistPaging",
                tenantDataFetchers.queryTypeQueryTenantlistPaging());
        typeWiring.dataFetcher("queryTenantlist",
                tenantDataFetchers.queryTypeQueryTenantlist());
        typeWiring.dataFetcher("verifyTenantCode",
                tenantDataFetchers.queryTypeVerifyTenantCode());

        // UiPlugin Query
        typeWiring.dataFetcher("queryUiPluginsByType",
                uiPluginDataFetchers.queryTypeQueryUiPluginsByType());
        typeWiring.dataFetcher("queryUiPluginDetailById",
                uiPluginDataFetchers.queryTypeQueryUiPluginDetailById());

        // User Query
        typeWiring.dataFetcher("queryUserList",
                userDataFetchers.queryTypeQueryUserList());
        typeWiring.dataFetcher("getUserInfo",
                userDataFetchers.queryTypeGetUserInfo());
        typeWiring.dataFetcher("listUser",
                userDataFetchers.queryTypeListUser());
        typeWiring.dataFetcher("listAll",
                userDataFetchers.queryTypeListAll());
        typeWiring.dataFetcher("verifyUserName",
                userDataFetchers.queryTypeVerifyUserName());
        typeWiring.dataFetcher("unauthorizedUser",
                userDataFetchers.queryTypeUnauthorizedUser());
        typeWiring.dataFetcher("authorizedUser",
                userDataFetchers.queryTypeAuthorizedUser());
        typeWiring.dataFetcher("batchActivateUser",
                userDataFetchers.queryTypeBatchActivateUser());

        return typeWiring;
    }


    protected TypeRuntimeWiring.Builder addWiringForMutationType(TypeRuntimeWiring.Builder typeWiring) {
        // AlertGroup GraphQL Mutation
        typeWiring.dataFetcher("createAlertGroup",
                alertGroupDataFetchers.mutationTypeCreateAlertGroup());
        typeWiring.dataFetcher("delAlertGroupById",
                alertGroupDataFetchers.mutationTypeDelAlertGroupById());
        typeWiring.dataFetcher("updateAlertGroup",
                alertGroupDataFetchers.mutationTypeUpdateAlertGroup());

        // AccessToken GraphQL Mutation
        typeWiring.dataFetcher("createToken",
                accessTokenDataFetchers.mutationTypeCreateToken());
        typeWiring.dataFetcher("delAccessTokenById",
                accessTokenDataFetchers.mutationTypeDelAccessTokenById());
        typeWiring.dataFetcher("updateToken",
                accessTokenDataFetchers.mutationTypeUpdateToken());

        // DataSource GraphQL Mutation
        typeWiring.dataFetcher("createDataSource",
                dataSourceDataFetchers.mutationTypeCreateDataSource());
        typeWiring.dataFetcher("updateDataSource",
                dataSourceDataFetchers.mutationTypeUpdateDataSource());
        typeWiring.dataFetcher("deleteDataSource",
                dataSourceDataFetchers.mutationTypeDeleteDataSource());

        // Executor GraphQL Mutation
        typeWiring.dataFetcher("startProcessInstance",
                executorDataFetchers.mutationTypeStartProcessInstance());
        typeWiring.dataFetcher("execute",
                executorDataFetchers.mutationTypeExecute());

        // AlertPluginInstance Mutation
        typeWiring.dataFetcher("createAlertPluginInstance",
                alertPluginInstanceDataFetchers.mutationTypeCreateAlertPluginInstance());
        typeWiring.dataFetcher("updateAlertPluginInstance",
                alertPluginInstanceDataFetchers.mutationTypeUpdateAlertPluginInstance());
        typeWiring.dataFetcher("deleteAlertPluginInstance",
                alertPluginInstanceDataFetchers.mutationTypeDeleteAlertPluginInstance());

        // Login Mutation
        typeWiring.dataFetcher("signOut",
                loginDataFetchers.mutationTypeLogOut());

        // ProcessDefinition Mutation
        typeWiring.dataFetcher("createProcessDefinition",
                processDefinitionDataFetchers.mutationTypeCreateProcessDefinition());
        typeWiring.dataFetcher("updateProcessDefinition",
                processDefinitionDataFetchers.mutationTypeUpdateProcessDefinition());
        typeWiring.dataFetcher("switchProcessDefinitionVersion",
                processDefinitionDataFetchers.mutationTypeSwitchProcessDefinitionVersion());
        typeWiring.dataFetcher("deleteProcessDefinitionVersion",
                processDefinitionDataFetchers.mutationTypeDeleteProcessDefinitionVersion());
        typeWiring.dataFetcher("releaseProcessDefinition",
                processDefinitionDataFetchers.mutationTypeReleaseProcessDefinition());
        typeWiring.dataFetcher("deleteProcessDefinitionById",
                processDefinitionDataFetchers.mutationTypeDeleteProcessDefinitionById());
        typeWiring.dataFetcher("batchDeleteProcessDefinitionByIds",
                processDefinitionDataFetchers.mutationTypeBatchDeleteProcessDefinitionByIds());

        // ProcessInstance Mutation
        typeWiring.dataFetcher("updateProcessInstance",
                processInstanceDataFetchers.mutationTypeUpdateProcessInstance());
        typeWiring.dataFetcher("deleteProcessInstanceById",
                processInstanceDataFetchers.mutationTypeDeleteProcessInstanceById());
        typeWiring.dataFetcher("batchDeleteProcessInstanceByIds",
                processInstanceDataFetchers.mutationTypeBatchDeleteProcessInstanceByIds());

        // Project Mutation
        typeWiring.dataFetcher("createProject",
                projectDataFetchers.mutationTypeCreateProject());
        typeWiring.dataFetcher("updateProject",
                projectDataFetchers.mutationTypeUpdateProject());
        typeWiring.dataFetcher("deleteProject",
                projectDataFetchers.mutationTypeDeleteProject());

        // Queue Mutation
        typeWiring.dataFetcher("createQueue",
                queueDataFetchers.mutationTypeCreateProject());
        typeWiring.dataFetcher("updateQueue",
                queueDataFetchers.mutationTypeUpdateQueue());

        // Resources Mutation
        typeWiring.dataFetcher("createDirectory",
                resourcesDataFetchers.mutationTypeCreateDirectory());
        typeWiring.dataFetcher("deleteResource",
                resourcesDataFetchers.mutationTypeDeleteResource());
        typeWiring.dataFetcher("onlineCreateResource",
                resourcesDataFetchers.mutationTypeOnlineCreateResource());
        typeWiring.dataFetcher("updateResourceContent",
                resourcesDataFetchers.mutationTypeUpdateResourceContent());
        typeWiring.dataFetcher("createUdfFunc",
                resourcesDataFetchers.mutationTypeCreateUdfFunc());
        typeWiring.dataFetcher("updateUdfFunc",
                resourcesDataFetchers.mutationTypeUpdateUdfFunc());
        typeWiring.dataFetcher("deleteUdfFunc",
                resourcesDataFetchers.mutationTypeDeleteUdfFunc());

        // Schedule Mutation
        typeWiring.dataFetcher("createSchedule",
                scheduleDataFetchers.mutationTypeCreateSchedule());
        typeWiring.dataFetcher("updateSchedule",
                scheduleDataFetchers.mutationTypeUpdateSchedule());
        typeWiring.dataFetcher("online",
                scheduleDataFetchers.mutationTypeOnline());
        typeWiring.dataFetcher("offline",
                scheduleDataFetchers.mutationTypeOffline());
        typeWiring.dataFetcher("deleteScheduleById",
                scheduleDataFetchers.mutationTypeDeleteScheduleById());

        // TaskInstance Mutation
        typeWiring.dataFetcher("forceTaskSuccess",
                taskInstanceDataFetchers.mutationTypeForceTaskSuccess());

        // Tenant Mutation
        typeWiring.dataFetcher("createTenant",
                tenantDataFetchers.mutationTypeCreateTenant());
        typeWiring.dataFetcher("updateTenant",
                tenantDataFetchers.mutationTypeUpdateTenant());
        typeWiring.dataFetcher("deleteTenantById",
                tenantDataFetchers.mutationTypeDeleteTenantById());

        // User Mutation
        typeWiring.dataFetcher("createUser",
                userDataFetchers.mutationTypeQueryCreateUser());
        typeWiring.dataFetcher("updateUser",
                userDataFetchers.mutationTypeUpdateUser());
        typeWiring.dataFetcher("delUserById",
                userDataFetchers.mutationTypeDelUserById());
        typeWiring.dataFetcher("grantProject",
                userDataFetchers.mutationTypeGrantProject());
        typeWiring.dataFetcher("grantResource",
                userDataFetchers.mutationTypeGrantResource());
        typeWiring.dataFetcher("grantUDFFunc",
                userDataFetchers.mutationTypeGrantUDFFunc());
        typeWiring.dataFetcher("grantDataSource",
                userDataFetchers.mutationTypeGrantDataSource());
        typeWiring.dataFetcher("registerUser",
                userDataFetchers.mutationTypeRegisterUser());
        typeWiring.dataFetcher("activateUser",
                userDataFetchers.mutationTypeActivateUser());


        return typeWiring;
    }


    protected TypeRuntimeWiring.Builder addWiringForBaseDataSourceParamDTO(TypeRuntimeWiring.Builder typeWiring) {
        typeWiring.dataFetcher("dbType", dataSourceDataFetchers.BaseDataSourceParamDTOTypeDbType());
        return typeWiring;
    }

    protected TypeRuntimeWiring.Builder addWiringForDataSourceType(TypeRuntimeWiring.Builder typeWiring) {
        typeWiring.dataFetcher("dbType", dataSourceDataFetchers.dataSourceDbType());
        return typeWiring;
    }

}

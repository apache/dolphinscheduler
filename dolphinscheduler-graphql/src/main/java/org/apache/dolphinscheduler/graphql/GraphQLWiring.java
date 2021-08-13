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

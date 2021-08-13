package org.apache.dolphinscheduler.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;


@Component
public class GraphQLProvider {

    @Value("classpath:schema/schema.graphql")
    Resource schemaResource;

    @Value("classpath:schema/AlertGroup.graphql")
    Resource alertGroupResource;

    @Value("classpath:schema/AccessToken.graphql")
    Resource accessTokenResource;

    @Value("classpath:schema/DataAnalysis.graphql")
    Resource dataAnalysisResource;

    @Value("classpath:schema/DataSource.graphql")
    Resource dataSourceResource;

    @Value("classpath:schema/Executor.graphql")
    Resource executorResource;

    @Value("classpath:schema/AlertPluginInstance.graphql")
    Resource alertPluginInstanceResource;

    @Value("classpath:schema/Monitor.graphql")
    Resource monitorResource;

    private GraphQL graphQL;

    @Autowired
    private GraphQLWiring graphQLWiring;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        // get the schema
        File schemaFile = schemaResource.getFile();
        File alertGroupFile = alertGroupResource.getFile();
        File accessTokenFile = accessTokenResource.getFile();
        File dataAnalysisFile = dataAnalysisResource.getFile();
        File dataSourceFile = dataSourceResource.getFile();
        File executorFile = executorResource.getFile();
        File alertPluginInstanceFile = alertPluginInstanceResource.getFile();
        File monitorFile = monitorResource.getFile();

        // parse schema
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();

        typeRegistry.merge(schemaParser.parse(schemaFile));
        typeRegistry.merge(schemaParser.parse(alertGroupFile));
        typeRegistry.merge(schemaParser.parse(accessTokenFile));
        typeRegistry.merge(schemaParser.parse(dataAnalysisFile));
        typeRegistry.merge(schemaParser.parse(dataSourceFile));
        typeRegistry.merge(schemaParser.parse(executorFile));
        typeRegistry.merge(schemaParser.parse(alertPluginInstanceFile));
        typeRegistry.merge(schemaParser.parse(monitorFile));

        RuntimeWiring wiring = graphQLWiring.buildWiring();
        GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
        graphQL = GraphQL.newGraphQL(schema).build();
    }

//    private RuntimeWiring buildWiring() {
//        return RuntimeWiring.newRuntimeWiring()
//                .type(newTypeWiring("Query")
//                        // AlertGroup GraphQL Query
//                        .dataFetcher("queryAllGroupList", alertGroupDataFetchers.getQueryAllGroupListDataFetcher())
//                        .dataFetcher("queryAlertGroupListPaging", alertGroupDataFetchers.getQueryAlertGroupListPagingDataFetcher())
//                        .dataFetcher("verifyGroupName", alertGroupDataFetchers.getVerifyGroupName())
//
//                        // AccessToken GraphQL Mutation
//                        .dataFetcher("generateToken", accessTokenDataFetchers.getGenerateTokenDataFetcher())
//                )
//                .type(newTypeWiring("Mutation")
//                        // AccessToken GraphQL Mutation
//                        .dataFetcher("createAlertGroup", alertGroupDataFetchers.getCreateAlertGroupDataFetcher())
//                        .dataFetcher("delAlertGroupById", alertGroupDataFetchers.getDelAlertGroupByIdDataFetcher())
//                        .dataFetcher("grantUser", alertGroupDataFetchers.getGrantUserDataFetcher())
//                        .dataFetcher("updateAlertGroup", alertGroupDataFetchers.getUpdateAlertGroupDataFetcher())
//
//                        // AccessToken GraphQL Mutation
//                        .dataFetcher("createToken", accessTokenDataFetchers.getCreateTokenDataFetcher())
//                )
//                .type(newTypeWiring("AlertGroupPageInfo")
//                        .dataFetcher("total", environment -> {
//                            PageInfo<AlertGroup> pageInfo = environment.getSource();
//                            return pageInfo.getTotalCount();
//                        })
//                ).build();
//    }

}

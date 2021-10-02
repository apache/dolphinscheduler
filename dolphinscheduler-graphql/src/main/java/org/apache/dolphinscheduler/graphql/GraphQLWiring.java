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


    protected RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                // Wiring every GraphQL type
                .type("Query", this::addWiringForQueryType)
                .type("Mutation", this::addWiringForMutationType)
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



        return typeWiring;
    }
}

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

    @Value("classpath:schema/Schema.graphql")
    Resource schemaResource;

    @Value("classpath:schema/AlertGroup.graphql")
    Resource alertGroupResource;

    @Value("classpath:schema/AccessToken.graphql")
    Resource accessTokenResource;



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

        // parse schema
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();

        typeRegistry.merge(schemaParser.parse(schemaFile));
        typeRegistry.merge(schemaParser.parse(alertGroupFile));
        typeRegistry.merge(schemaParser.parse(accessTokenFile));

        RuntimeWiring wiring = graphQLWiring.buildWiring();
        GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
        graphQL = GraphQL.newGraphQL(schema).build();
    }

}

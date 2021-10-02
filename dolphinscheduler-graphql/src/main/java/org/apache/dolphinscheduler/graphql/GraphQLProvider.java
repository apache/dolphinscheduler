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

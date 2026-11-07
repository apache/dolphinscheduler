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

package org.apache.dolphinscheduler.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PersonalResourceAuthorizationRemovalTest {

    @Test
    void legacyPersonalResourceGrantRoutesAreUnavailable() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new UsersController()).build();
        mvc.perform(post("/users/grant-datasource").param("userId", "1").param("datasourceIds", "1"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/users/grant-namespace").param("userId", "1").param("namespaceIds", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void legacyPersonalResourceQueryRoutesCannotReturnRelations() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DataSourceController(), new K8sNamespaceController()).build();
        mvc.perform(get("/datasources/authed-datasource").param("userId", "1"))
                .andExpect(status().is4xxClientError());
        mvc.perform(get("/datasources/unauth-datasource").param("userId", "1"))
                .andExpect(status().is4xxClientError());
        mvc.perform(get("/k8s-namespace/authed-namespace").param("userId", "1"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/k8s-namespace/unauth-namespace").param("userId", "1"))
                .andExpect(status().isNotFound());
    }
}

/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.e2e.cases;


import static org.assertj.core.api.Assertions.assertThat;

import org.apache.dolphinscheduler.e2e.core.DolphinScheduler;
import org.apache.dolphinscheduler.e2e.pages.LoginPage;
import org.apache.dolphinscheduler.e2e.pages.security.ClusterPage;
import org.apache.dolphinscheduler.e2e.pages.security.SecurityPage;

import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
class ClusterE2ETest {

    private static final String clusterName = "test_cluster_name";
    private static final String clusterConfig = "test_cluster_config";
    private static final String clusterDesc = "test_cluster_desc";

    private static final String editClusterName = "edit_cluster_name";
    private static final String editClusterConfig = "edit_cluster_config";
    private static final String editClusterDesc = "edit_cluster_desc";

    private static RemoteWebDriver browser;

    @BeforeAll
    public static void setup() {
        new LoginPage(browser)
                .login("admin", "dolphinscheduler123")
                .goToNav(SecurityPage.class)
                .goToTab(ClusterPage.class)
        ;
    }

    @Test
    @Order(10)
    void testCreateCluster() {
        final ClusterPage page = new ClusterPage(browser);
        page.create(clusterName, clusterConfig, clusterDesc);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.clusterList())
                    .as("Cluster list should contain newly-created cluster")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(clusterName));
        });
    }

    @Test
    @Order(20)
    void testCreateDuplicateCluster() {
        final ClusterPage page = new ClusterPage(browser);
        page.create(clusterName, clusterConfig, clusterDesc);

        Awaitility.await().untilAsserted(() ->
                assertThat(browser.findElement(By.tagName("body")).getText())
                        .contains("already exists")
        );

        page.createClusterForm().buttonCancel().click();
    }

    @Test
    @Order(30)
    void testEditCluster() {
        final ClusterPage page = new ClusterPage(browser);
        page.update(clusterName, editClusterName, editClusterConfig, editClusterDesc);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();
            assertThat(page.clusterList())
                    .as("Cluster list should contain newly-modified cluster")
                    .extracting(WebElement::getText)
                    .anyMatch(it -> it.contains(editClusterName));
        });
    }

    @Test
    @Order(40)
    void testDeleteCluster() {
        final ClusterPage page = new ClusterPage(browser);

        page.delete(editClusterName);

        Awaitility.await().untilAsserted(() -> {
            browser.navigate().refresh();

            assertThat(
                    page.clusterList()
            )
            .as("Cluster list should not contain deleted cluster")
            .noneMatch(
                    it -> it.getText().contains(clusterName) || it.getText().contains(editClusterName)
            );
        });
    }
}

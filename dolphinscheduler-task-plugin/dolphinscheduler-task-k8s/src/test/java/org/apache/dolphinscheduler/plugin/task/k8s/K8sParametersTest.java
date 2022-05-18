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

package org.apache.dolphinscheduler.plugin.task.k8s;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class K8sParametersTest {
    private K8sTaskParameters k8sTaskParameters = null;
    private final String image = "ds-dev";
    private final String namespace = "{\"name\":\"default\",\"cluster\":\"lab\"}";
    private final double minCpuCores = 2;
    private final double minMemorySpace = 10;

    @Before
    public void before() {
        k8sTaskParameters = new K8sTaskParameters();
        k8sTaskParameters.setImage(image);
        k8sTaskParameters.setNamespace(namespace);
        k8sTaskParameters.setMinCpuCores(minCpuCores);
        k8sTaskParameters.setMinMemorySpace(minMemorySpace);
    }

    @Test
    public void testCheckParameterNormal() {
        Assert.assertTrue(k8sTaskParameters.checkParameters());
    }

    @Test
    public void testGetResourceFilesListNormal() {
       Assert.assertNotNull(k8sTaskParameters.getResourceFilesList());
       Assert.assertEquals(0, k8sTaskParameters.getResourceFilesList().size());
    }

    @Test
    public void testK8sParameters() {
        Assert.assertEquals(image, k8sTaskParameters.getImage());
        Assert.assertEquals(namespace, k8sTaskParameters.getNamespace());
        Assert.assertEquals(0, Double.compare(minCpuCores, k8sTaskParameters.getMinCpuCores()));
        Assert.assertEquals(0,Double.compare(minMemorySpace, k8sTaskParameters.getMinMemorySpace()));
    }

}

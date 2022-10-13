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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Cluster;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class ClusterMapperTest extends BaseDaoTest {

    @Autowired
    ClusterMapper clusterMapper;

    /**
     * insert
     *
     * @return Cluster
     */
    private Cluster insertOne() {
        // insertOne
        Cluster cluster = new Cluster();
        cluster.setName("testCluster");
        cluster.setCode(1L);
        cluster.setOperator(1);
        cluster.setConfig(getConfig());
        cluster.setDescription(getDesc());
        cluster.setCreateTime(new Date());
        cluster.setUpdateTime(new Date());
        clusterMapper.insert(cluster);
        return cluster;
    }

    @BeforeEach
    public void setUp() {
        clearTestData();
    }

    @AfterEach
    public void after() {
        clearTestData();
    }

    public void clearTestData() {
        clusterMapper.queryAllClusterList().stream().forEach(cluster -> {
            clusterMapper.deleteByCode(cluster.getCode());
        });
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOne
        Cluster cluster = insertOne();
        cluster.setDescription("new description info");
        // update
        int update = clusterMapper.updateById(cluster);
        Assertions.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        Cluster cluster = insertOne();
        int delete = clusterMapper.deleteById(cluster.getId());
        Assertions.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        insertOne();
        // query
        List<Cluster> clusters = clusterMapper.selectList(null);
        Assertions.assertEquals(clusters.size(), 1);
    }

    /**
     * test query cluster by name
     */
    @Test
    public void testQueryByClusterName() {
        Cluster entity = insertOne();
        Cluster cluster = clusterMapper.queryByClusterName(entity.getName());
        Assertions.assertEquals(entity.toString(), cluster.toString());
    }

    /**
     * test query cluster by code
     */
    @Test
    public void testQueryByClusterCode() {
        Cluster entity = insertOne();
        Cluster cluster = clusterMapper.queryByClusterCode(entity.getCode());
        Assertions.assertEquals(entity.toString(), cluster.toString());
    }

    /**
     * test query all clusters
     */
    @Test
    public void testQueryAllClusterList() {
        Cluster entity = insertOne();
        List<Cluster> clusters = clusterMapper.queryAllClusterList();
        Assertions.assertEquals(clusters.size(), 1);
        Assertions.assertEquals(entity.toString(), clusters.get(0).toString());
    }

    /**
     * test query cluster list paging
     */
    @Test
    public void testQueryClusterListPaging() {
        Cluster entity = insertOne();
        Page<Cluster> page = new Page<>(1, 10);
        IPage<Cluster> clusterIPage = clusterMapper.queryClusterListPaging(page, "");
        List<Cluster> clusterList = clusterIPage.getRecords();
        Assertions.assertEquals(clusterList.size(), 1);

        clusterIPage = clusterMapper.queryClusterListPaging(page, "abc");
        clusterList = clusterIPage.getRecords();
        Assertions.assertEquals(clusterList.size(), 0);
    }

    /**
     * test query all clusters
     */
    @Test
    public void testDeleteByCode() {
        Cluster entity = insertOne();
        int delete = clusterMapper.deleteByCode(entity.getCode());
        Assertions.assertEquals(delete, 1);
    }

    private String getDesc() {
        return "create an cluster to test ";
    }

    /**
     * create an cluster config
     */
    private String getConfig() {
        return "export HADOOP_HOME=/opt/hadoop-2.6.5\n"
                + "export HADOOP_CONF_DIR=/etc/hadoop/conf\n"
                + "export SPARK_HOME=/opt/soft/spark\n"
                + "export PYTHON_HOME=/opt/soft/python\n"
                + "export JAVA_HOME=/opt/java/jdk1.8.0_181-amd64\n"
                + "export HIVE_HOME=/opt/soft/hive\n"
                + "export FLINK_HOME=/opt/soft/flink\n"
                + "export DATAX_HOME=/opt/soft/datax\n"
                + "export YARN_CONF_DIR=\"/etc/hadoop/conf\"\n"
                + "\n"
                + "export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH\n"
                + "\n"
                + "export HADOOP_CLASSPATH=`hadoop classpath`\n"
                + "\n"
                + "#echo \"HADOOP_CLASSPATH=\"$HADOOP_CLASSPATH";
    }
}

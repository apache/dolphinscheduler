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
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class K8sNamespaceMapperTest extends BaseDaoTest {

    @Autowired
    K8sNamespaceMapper k8sNamespaceMapper;

    /**
     * insert
     *
     * @return K8sNamespace
     */
    private K8sNamespace insertOne() {
        //insertOne
        K8sNamespace k8sNamespace = new K8sNamespace();
        k8sNamespace.setNamespace("testNamespace");
        k8sNamespace.setK8s("ds_null_k8s");
        k8sNamespace.setLimitsCpu(100.0);
        k8sNamespace.setLimitsMemory(100);
        k8sNamespace.setCreateTime(new Date());
        k8sNamespace.setUpdateTime(new Date());
        k8sNamespaceMapper.insert(k8sNamespace);
        return k8sNamespace;
    }

    @Before
    public void setUp() {
        clearTestData();
    }

    @After
    public void after() {
        clearTestData();
    }

    public void clearTestData() {
        k8sNamespaceMapper.selectList(null).stream().forEach(nanespace -> {
            k8sNamespaceMapper.deleteById(nanespace.getId());
        });
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        //insertOne
        K8sNamespace k8sNamespace = insertOne();
        k8sNamespace.setLimitsMemory(200);
        //update
        int update = k8sNamespaceMapper.updateById(k8sNamespace);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        K8sNamespace k8sNamespace = insertOne();
        int delete = k8sNamespaceMapper.deleteById(k8sNamespace.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        insertOne();
        //query
        List<K8sNamespace> k8sNamespaces = k8sNamespaceMapper.selectList(null);
        Assert.assertEquals(k8sNamespaces.size(), 1);
    }


    /**
     * test query k8sNamespaces by id
     */
    @Test
    public void testQueryByK8sNamespaceId() {
        K8sNamespace entity = insertOne();
        K8sNamespace k8sNamespace = k8sNamespaceMapper.selectById(entity.getId());
        Assert.assertEquals(entity.toString(),k8sNamespace.toString());
    }


    /**
     * test query k8sNamespaces list paging
     */
    @Test
    public void testQueryK8sNamespaceListPaging() {
        K8sNamespace entity = insertOne();
        Page<K8sNamespace> page = new Page<>(1, 10);
        IPage<K8sNamespace> k8sNamespaceIPage = k8sNamespaceMapper.queryK8sNamespacePaging(page,"");
        List<K8sNamespace> k8sNamespaceList = k8sNamespaceIPage.getRecords();
        Assert.assertEquals(k8sNamespaceList.size(), 1);

        k8sNamespaceIPage = k8sNamespaceMapper.queryK8sNamespacePaging(page,"abc");
        k8sNamespaceList = k8sNamespaceIPage.getRecords();
        Assert.assertEquals(k8sNamespaceList.size(), 0);
    }

}
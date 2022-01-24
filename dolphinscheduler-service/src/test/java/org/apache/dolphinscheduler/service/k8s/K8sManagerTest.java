package org.apache.dolphinscheduler.service.k8s;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.K8s;
import org.apache.dolphinscheduler.dao.mapper.K8sMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
@RunWith(MockitoJUnitRunner.class)
public class K8sManagerTest {

    @InjectMocks
    private K8sManager k8sManager;

    @Mock
    private K8sMapper k8sMapper;
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getK8sClient() {
        Mockito.when(k8sMapper.selectList(Mockito.any())).thenReturn(getK8sList());

        KubernetesClient result= k8sManager.getK8sClient("must null");
        Assert.assertNull(result);
        result= k8sManager.getK8sClient(null);
        Assert.assertNull(result);
    }

    private K8s getK8s() {
        K8s k8s = new K8s();
        k8s.setId(1);
        k8s.setK8sName("default");
        k8s.setK8sConfig("k8s config");
        return k8s;
    }

    private List<K8s> getK8sList() {
        List<K8s> queueList = new ArrayList<>();
        queueList.add(getK8s());
        return queueList;
    }
}
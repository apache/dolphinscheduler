package org.apache.dolphinscheduler.service.k8s;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.dolphinscheduler.dao.entity.K8s;
import org.apache.dolphinscheduler.dao.mapper.K8sMapper;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * A separate class, because then wait for multiple environment feature, currently using db configuration, later unified
 */
@Component
public class K8sManager {
    /**
     * logger of K8sManager
     */
    private static final Logger logger = LoggerFactory.getLogger(K8sManager.class);
    /**
     * cache k8s client
     */
    private static Map<String, KubernetesClient> clientMap = new Hashtable<>();

    @Autowired
    private K8sMapper k8sMapper;

    public KubernetesClient getK8sClient(String k8sName) {
        if(null == k8sName)
        {
            return null;
        }
        return clientMap.get(k8sName);
    }

    @PostConstruct
    public void buildApiClientAll() throws RemotingException {
        QueryWrapper<K8s> nodeWrapper = new QueryWrapper<>();
        List<K8s> k8sList = k8sMapper.selectList(nodeWrapper);

        if(k8sList!=null) {
            for(K8s k8s : k8sList) {
                DefaultKubernetesClient client =  getClient(k8s.getK8sConfig());
                clientMap.put(k8s.getK8sName(),client);
            }
        }
    }

    private DefaultKubernetesClient getClient(String configYaml) throws RemotingException {
        try {
            Config config = Config.fromKubeconfig(configYaml);
            return new DefaultKubernetesClient(config);
        } catch (Exception e) {
            logger.error("fail to get k8s ApiClient", e);
            throw new RemotingException("fail to get k8s ApiClient:" + e.getMessage());
        }
    }
}

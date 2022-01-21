package org.apache.dolphinscheduler.api.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.K8sNameSpaceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceMapper;
import org.apache.dolphinscheduler.service.k8s.K8sManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class K8sNameSpaceServiceImpl extends BaseServiceImpl implements K8sNameSpaceService {

    private static final Logger logger = LoggerFactory.getLogger(QueueServiceImpl.class);

    @Autowired
    private K8sNamespaceMapper k8sNamespaceMapper;

    @Autowired
    private K8sManager k8SManager;

    private static Yaml yaml = new Yaml();

    private static String resourceYaml = "apiVersion: v1\n"
            + "kind: ResourceQuota\n"
            + "metadata:\n"
            + "  name: ${name}\n"
            + "  namespace: ${namespace}\n"
            + "spec:\n"
            + "  hard:\n"
            + "    ${limitCpu}\n"
            + "    ${limitMemory}\n";
    /**
     * query namespace list
     *
     * @param loginUser login user
     * @return queue list
     */
    @Override
    public Result queryList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Page<K8sNamespace> page = new Page<>(pageNo, pageSize);

        IPage<K8sNamespace> queueList = k8sNamespaceMapper.queryK8sNamespacePaging(page, searchVal);

        Integer count = (int) queueList.getTotal();
        PageInfo<K8sNamespace> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal(count);
        pageInfo.setTotalList(queueList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    @Override
    public Map<String, Object> createK8sNamespace(User loginUser, String namespace, String k8s, String owner, String tag, Double limitsCpu, Integer limitsMemory) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        if (StringUtils.isEmpty(namespace)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.NAMESPACE);
            return result;
        }

        if (StringUtils.isEmpty(k8s)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.K8S);
            return result;
        }

        if(limitsCpu!=null && limitsCpu< 0.0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_CPU);
            return result;
        }

        if(limitsMemory!=null && limitsMemory< 0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_MEMORY);
            return result;
        }

        if (checkNamespaceExistInDb(namespace,k8s)) {
            putMsg(result, Status.K8S_NAMESPACE_EXIST, namespace,k8s);
            return result;
        }

        K8sNamespace k8sNamespaceObj = new K8sNamespace();
        Date now = new Date();

        k8sNamespaceObj.setNamespace(namespace);
        k8sNamespaceObj.setK8s(k8s);
        k8sNamespaceObj.setOwner(owner);
        k8sNamespaceObj.setTag(tag);
        k8sNamespaceObj.setLimitsCpu(limitsCpu);
        k8sNamespaceObj.setLimitsMemory(limitsMemory);
        k8sNamespaceObj.setOnlineJobNum(0);
        k8sNamespaceObj.setPodReplicas(0);
        k8sNamespaceObj.setPodRequestCpu(0.0);
        k8sNamespaceObj.setPodRequestMemory(0);
        k8sNamespaceObj.setCreateTime(now);
        k8sNamespaceObj.setUpdateTime(now);

        try {
            upsertToK8s(k8sNamespaceObj);
        }catch (Exception e){
            putMsg(result, Status.K8S_CLIENT_OPS_ERROR,e.getMessage());
            return result;
        }

        k8sNamespaceMapper.insert(k8sNamespaceObj);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    @Override
    public Map<String, Object> updateK8sNamespace(User loginUser, int id, String owner, String tag, Double limitsCpu, Integer limitsMemory) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        if(limitsCpu!=null && limitsCpu< 0.0)  {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_CPU);
            return result;
        }

        if(limitsMemory!=null && limitsMemory< 0) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.LIMITS_MEMORY);
            return result;
        }

        K8sNamespace k8sNamespaceObj = k8sNamespaceMapper.selectById(id);
        if (k8sNamespaceObj == null) {
            putMsg(result, Status.QUEUE_NOT_EXIST, id);
            return result;
        }

        Date now = new Date();
        k8sNamespaceObj.setTag(tag);
        k8sNamespaceObj.setLimitsCpu(limitsCpu);
        k8sNamespaceObj.setLimitsMemory(limitsMemory);
        k8sNamespaceObj.setUpdateTime(now);
        k8sNamespaceObj.setOwner(owner);
        try {
            upsertToK8s(k8sNamespaceObj);
        }catch (Exception e){
            putMsg(result, Status.K8S_CLIENT_OPS_ERROR,e.getMessage());
            return result;
        }
        // update to db
        k8sNamespaceMapper.updateById(k8sNamespaceObj);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result<Object> verifyNamespaceK8s(String namespace, String k8s) {
        Result<Object> result = new Result<>();
        if (StringUtils.isEmpty(namespace)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.NAMESPACE);
            return result;
        }

        if (StringUtils.isEmpty(k8s)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.K8S);
            return result;
        }

        if (checkNamespaceExistInDb(namespace,k8s)) {
            putMsg(result, Status.K8S_NAMESPACE_EXIST, namespace,k8s);
            return result;
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> deleteNamespaceById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        K8sNamespace k8sNamespaceObj = k8sNamespaceMapper.selectById(id);
        if (k8sNamespaceObj == null) {
            putMsg(result, Status.QUEUE_NOT_EXIST, id);
            return result;
        }

        deleteToK8s(k8sNamespaceObj.getNamespace(),k8sNamespaceObj.getK8s());
        k8sNamespaceMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * check queue name exist
     * if exists return true，not exists return false
     *
     * @param queueName queue name
     * @return true if the queue name not exists, otherwise return false
     */
    private boolean checkNamespaceExistInDb(String queueName,String k8s) {
        return k8sNamespaceMapper.existNamespace(queueName, k8s) == Boolean.TRUE;
    }


    private void upsertToK8s(K8sNamespace k8sNamespace) {
            upsertNamespaceToK8s(k8sNamespace.getNamespace(), k8sNamespace.getK8s());
            upsertNamespacedResourceToK8s(k8sNamespace);
    }

    private void deleteToK8s(String name, String k8s){
        Optional<Namespace> result = getNamespaceFromK8s(name, k8s);
        if (result.isPresent()) {
            KubernetesClient client = k8SManager.getK8sClient(k8s);
            Namespace body = new Namespace();
            ObjectMeta meta = new ObjectMeta();
            meta.setNamespace(name);
            meta.setName(name);
            body.setMetadata(meta);
            client.namespaces().delete(body);
        }
    }

    private ResourceQuota upsertNamespacedResourceToK8s(K8sNamespace k8sNamespace) {
        String cpuStr = null;
        if (k8sNamespace.getLimitsCpu() != null) {
            cpuStr = k8sNamespace.getLimitsCpu() + "";
        }

        String memStr = null;
        if (k8sNamespace.getLimitsMemory() != null) {
            memStr = k8sNamespace.getLimitsMemory() + "Gi";
        }

        KubernetesClient client = k8SManager.getK8sClient(k8sNamespace.getK8s());

        //创建资源
        ResourceQuota queryExist = client.resourceQuotas()
                .inNamespace(k8sNamespace.getNamespace())
                .withName(k8sNamespace.getNamespace())
                .get();


        String yamlStr = genDefaultResourceYaml(k8sNamespace.getNamespace(),
                k8sNamespace.getNamespace(),
                cpuStr, memStr);
        ResourceQuota body = yaml.loadAs(yamlStr, ResourceQuota.class);

        if (queryExist != null) {
            if (k8sNamespace.getLimitsCpu() == null && k8sNamespace.getLimitsMemory() == null) {
                client.resourceQuotas().inNamespace(k8sNamespace.getNamespace())
                        .withName(k8sNamespace.getNamespace())
                        .delete();
                return null;
            }
        }

        return   client.resourceQuotas().inNamespace(k8sNamespace.getNamespace())
                .withName(k8sNamespace.getNamespace())
                .createOrReplace(body);
    }

    private Optional<Namespace> getNamespaceFromK8s(String name, String k8s) {
        NamespaceList listNamespace =
                k8SManager.getK8sClient(k8s).namespaces().list();

        Optional<Namespace> list =
                listNamespace.getItems().stream()
                        .filter((Namespace namespace) ->
                                namespace.getMetadata().getName().equals(name))
                        .findFirst();

        return list;
    }

    private Namespace upsertNamespaceToK8s(String name, String k8s) {
        Optional<Namespace> result = getNamespaceFromK8s(name, k8s);
        //if not exist create
        if (!result.isPresent()) {
            KubernetesClient client = k8SManager.getK8sClient(k8s);
            Namespace body = new Namespace();
            ObjectMeta meta = new ObjectMeta();
            meta.setNamespace(name);
            meta.setName(name);
            body.setMetadata(meta);
            return client.namespaces().create(body);
        }
        return result.get();
    }


    private String genDefaultResourceYaml(String name, String namespace, String limitCpu, String limitMemory) {
        String result = resourceYaml.replace("${name}", name)
                .replace("${namespace}", namespace);
        if (limitCpu == null) {
            result = result.replace("${limitCpu}", "");
        } else {
            result = result.replace("${limitCpu}", "limits.cpu: '" + limitCpu + "'");
        }

        if (limitMemory == null) {
            result = result.replace("${limitMemory}", "");
        } else {
            result = result.replace("${limitMemory}", "limits.memory: " + limitMemory);
        }
        return result;
    }
}

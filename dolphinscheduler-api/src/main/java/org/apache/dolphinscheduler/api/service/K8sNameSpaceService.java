package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

public interface K8sNameSpaceService {
    /**
     * query namespace list paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return queue list
     */
    Result queryList(User loginUser, String searchVal, Integer pageNo, Integer pageSize);



    /**
     *
     * @param loginUser  login user
     * @param namespace namespace
     * @param k8s k8s not null
     * @param owner owner can null
     * @param tag can null,if set means just used for one type job,such as flink,spark
     * @param limitsCpu limits cpu, can null means not limit
     * @param limitsMemory limits memory, can null means not limit
     * @return
     */
    Map<String, Object> createK8sNamespace(User loginUser, String namespace, String k8s, String owner, String tag, Double limitsCpu, Integer limitsMemory);


    /**
     * update K8s Namespace tag and resource limit
     * @param loginUser
     * @param id
     * @param tag
     * @param limitsCpu
     * @param limitsMemory
     * @return
     */
    Map<String, Object> updateK8sNamespace(User loginUser, int id, String owner, String tag, Double limitsCpu, Integer limitsMemory);

    /**
     * verify namespace and k8s
     *
     * @param namespace     namespace
     * @param k8s k8s
     * @return true if the queue name not exists, otherwise return false
     */
    Result<Object> verifyNamespaceK8s(String namespace, String k8s);

    /**
     * delete namespace by id
     * @param loginUser
     * @param id  namespace id
     * @return
     */
    Map<String, Object> deleteNamespaceById(User loginUser, int id);
}

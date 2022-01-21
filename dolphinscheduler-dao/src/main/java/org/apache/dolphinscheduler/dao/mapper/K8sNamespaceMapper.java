package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Draven Li
 * @since 2021-11-30
 */
public interface K8sNamespaceMapper extends BaseMapper<K8sNamespace> {
    /**
     * namespace page
     * @param page page
     * @param searchVal searchVal
     * @return queue IPage
     */
    IPage<K8sNamespace> queryK8sNamespacePaging(IPage<K8sNamespace> page,
                                                @Param("searchVal") String searchVal);

    /**
     * check the target namespace exist
     * @param namespace namespace
     * @param k8s k8s
     * @return true if exist else return null
     */
    Boolean existNamespace(@Param("namespace") String namespace, @Param("k8s") String k8s);
}

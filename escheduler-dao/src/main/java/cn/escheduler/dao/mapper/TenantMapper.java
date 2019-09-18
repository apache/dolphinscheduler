package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.Tenant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

public interface TenantMapper extends BaseMapper<Tenant> {

    Tenant queryById(@Param("tenantId") int tenantId);

    Tenant queryByTenantCode(@Param("tenantCode") String tenantCode);

    IPage<Tenant> queryTenantPaging(IPage<Tenant> page,
                                    @Param("searchVal") String searchVal);
}

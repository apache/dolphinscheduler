package cn.escheduler.api.dto.resources;

import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.mapper.ResourceMapper;
import cn.escheduler.dao.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class ResourceViewDtoTest {
    private static final Logger logger = LoggerFactory.getLogger(ResourceViewDtoTest.class);
    ResourceMapper resourceMapper;

    @Before
    public void before(){

        resourceMapper = ConnectionFactory.getSqlSession().getMapper(ResourceMapper.class);
    }

    @Test
    public void resourceViewTest(){
        List<Resource> resources = resourceMapper.queryResourceCreatedByUser(2,0);
        ResourceViewDto resourceViewDto = new ResourceViewDto();
        for(Resource resource:resources){
            logger.info(resource.toString());
        }
    }

}
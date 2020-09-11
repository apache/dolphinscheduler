package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.ProcessTag;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class ProcessTagMapperTest {

    @Autowired
    ProcessTagMapper processTagMapper;

    /**
     * insert
     * @return ProcessTag
     */
    private ProcessTag inserOne(){
        //insertone
        ProcessTag processTag = new ProcessTag();
        processTag.setProcessID(1001);
        processTag.settagID(110);
        processTagMapper.insert(processTag);
        return processTag;
    }
    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        ProcessTag processTag = inserOne();
        int update = processTagMapper.updateById(processTag);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        ProcessTag processTag = inserOne();
        int delete = processTagMapper.deleteById(processTag.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        ProcessTag processTag = inserOne();
        //query
        List<ProcessTag> processTags = processTagMapper.selectList(null);
        Assert.assertNotEquals(processTags .size(), 0);
    }
    @Test
    public void deleteProcessRelation() {
        ProcessTag processTag = inserOne();
        int delete = processTagMapper.deleteProcessRelation(processTag.getProcessID(),processTag.gettagID());
        assertThat(delete,greaterThanOrEqualTo(1));
    }
}
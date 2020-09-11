package org.apache.dolphinscheduler.dao.mapper;



import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Tag;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class TagMapperTest {

    @Autowired
    TagMapper tagMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProjectMapper projectMapper;

    /**
     * insert
     * @return Tag
     */
    private Tag insertOne(){
        //insertOne
        Tag tag = new Tag();
        tag.setName("ut tag");
        tag.setUserId(111);
        tag.setProjectId(1010);
        tagMapper.insert(tag);
        return tag;
    }
    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        Tag tag = insertOne();
        //update
        int update = tagMapper.updateById(tag);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Tag tag = insertOne();
        int delete = tagMapper.deleteById(tag.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Tag tag = insertOne();
        //query
        List<Tag> tags = tagMapper.selectList(null);
        Assert.assertNotEquals(tags .size(), 0);
    }
    @Test
    public void queryByName() {
        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);

        Project project = new Project();
        project.setName("ut project");
        project.setUserId(user.getId());
        projectMapper.insert(project);

        Tag tag = insertOne();
        tag.setProjectId(project.getId());
        tag.setUserId(user.getId());
        tagMapper.updateById(tag);
        Tag tag1 = tagMapper.queryByName(tag.getName());
        Assert.assertNotEquals(tag1,null);
    }

    @Test
    public void queryTagListPaging() {
        Tag tag = insertOne();
        Page<Tag> page = new Page(1,3);
        IPage<Tag> tagIPage = tagMapper.queryTagListPaging(page,111,1101,null);
        Assert.assertNotEquals(tagIPage.getTotal(), 0);
    }
}
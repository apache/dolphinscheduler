package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.AccessToken;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AccessTokenMapperTest {


    @Resource
    AccessTokenMapper accessTokenMapper;

    @Test
    public void insert(){
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(4);
        accessToken.setToken("你好,hello");
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());
        accessToken.setExpireTime(new Date());
        accessToken.setUserName("apple");
        accessTokenMapper.insert(accessToken);
        Assert.assertNotEquals(accessToken.getId(), 0);
    }

    @Test
    public void queryAll(){
        List<AccessToken> accessTokens = accessTokenMapper.selectList(null);

        for(AccessToken accessToken1 : accessTokens){
            System.out.println(accessToken1.toString());
        }


    }

    @Test
    public void query(){

        Page page = new Page(1, 3);

        String userName = "app";

        IPage<AccessToken> accessTokenPage = accessTokenMapper.selectAccessTokenPage(page, userName, 4);

        System.out.println("total:" + accessTokenPage.getTotal());

        for(AccessToken accessToken1 : accessTokenPage.getRecords()){
            System.out.println(accessToken1.toString());
        }
        System.out.println();


    }


}
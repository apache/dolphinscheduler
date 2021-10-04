package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.AuditLog;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class AuditLogMapperTest extends TestCase {

    @Autowired
    AuditLogMapper logMapper;

    /**
     * insert
     * @return AuditLog
     */
    private AuditLog insertOne() {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserName("name");
        auditLog.setTime(new Date());
        auditLog.setModule(0);
        auditLog.setOperation(0);
        logMapper.insert(auditLog);
        return auditLog;
    }

    /**
     * test page query
     */
    @Test
    public void testQueryAuditLog() {
        AuditLog auditLog = insertOne();
        Page<AuditLog> page = new Page<>(1, 3);
        int[] moduleType = new int[0];
        int[] operationType = new int[0];

        IPage<AuditLog> logIPage = logMapper.queryAuditLog(page, moduleType, operationType, auditLog.getUserName(), "", "", null, null);
        Assert.assertNotEquals(logIPage.getTotal(), 0);
    }
}
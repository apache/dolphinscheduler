package org.apache.dolphinscheduler.api.audit;

import org.apache.dolphinscheduler.common.enums.AuditModuleType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AuditLogMapper;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuditSubscriberTest {

    @Mock
    private AuditLogMapper logMapper;

    @InjectMocks
    private AuditSubscriberImpl auditSubscriber;

    @Test
    public void testExecute() {
        Mockito.when(logMapper.insert(Mockito.any(AuditLog.class))).thenReturn(1);
        auditSubscriber.execute(new AuditMessage(new User(), new Date(), AuditModuleType.USER_MODULE, AuditOperationType.DEFAULT, null, null));
    }
}
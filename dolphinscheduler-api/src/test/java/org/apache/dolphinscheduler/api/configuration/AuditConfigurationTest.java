package org.apache.dolphinscheduler.api.configuration;

import org.apache.dolphinscheduler.api.controller.AbstractControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class AuditConfigurationTest extends AbstractControllerTest {

    @Autowired
    private AuditConfiguration auditConfiguration;

    @Test
    public void isAuditGlobalControlSwitch() {
        Assert.assertTrue(auditConfiguration.isAuditGlobalControlSwitch());
    }
}
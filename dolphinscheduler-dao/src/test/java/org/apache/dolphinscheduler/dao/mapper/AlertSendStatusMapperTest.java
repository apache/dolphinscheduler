package org.apache.dolphinscheduler.dao.mapper;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.AlertSendStatus;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AlertSendStatus mapper test
 */
public class AlertSendStatusMapperTest extends BaseDaoTest {
    @Autowired
    private AlertSendStatusMapper alertSendStatusMapper;

    /**
     * test insert
     */
    @Test
    public void testInsert() {
        AlertSendStatus alertSendStatus = new AlertSendStatus();
        alertSendStatus.setAlertId(1);
        alertSendStatus.setAlertPluginInstanceId(1);
        alertSendStatus.setSendStatus(AlertStatus.EXECUTION_SUCCESS);
        alertSendStatus.setLog("success");
        alertSendStatus.setCreateTime(DateUtils.getCurrentDate());

        alertSendStatusMapper.insert(alertSendStatus);
        assertThat(alertSendStatus.getId(), greaterThan(0));
    }
}

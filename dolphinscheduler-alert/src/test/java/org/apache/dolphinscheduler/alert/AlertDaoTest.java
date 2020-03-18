package org.apache.dolphinscheduler.alert;

import org.apache.dolphinscheduler.alert.utils.MailUtilsTest;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(AlertDaoTest.class);

    @Test
    public void testGetAlertDao() {
        logger.info("testGetAlertDao start");
        AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
        Assert.assertNotNull(alertDao);
        logger.info("testGetAlertDao end");
    }
}

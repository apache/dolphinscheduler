package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * hive conf utils test
 */
public class HiveConfUtilsTest {

    /**
     * test is hive conf var
     */
    @Test
    public void testIsHiveConfVar() {

        String conf = "hive.exec.script.wrapper=123";
        boolean hiveConfVar = HiveConfUtils.isHiveConfVar(conf);
        Assert.assertTrue(hiveConfVar);

        conf = "hive.test.v1=v1";
        hiveConfVar = HiveConfUtils.isHiveConfVar(conf);
        Assert.assertFalse(hiveConfVar);

        conf = "tez.queue.name=tezQueue";
        hiveConfVar = HiveConfUtils.isHiveConfVar(conf);
        Assert.assertTrue(hiveConfVar);

    }
}

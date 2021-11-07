package org.apache.dolphinscheduler.spi.task.dq.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConnectorTypeTest {

    @Test
    public void testGetCode() {
        assertEquals(0, ConnectorType.JDBC.getCode());
        assertEquals(1, ConnectorType.HIVE.getCode());
    }

    @Test
    public void testGetDescription() {
        assertEquals("JDBC", ConnectorType.JDBC.getDescription());
        assertEquals("HIVE", ConnectorType.HIVE.getDescription());
    }

    @Test
    public void testOf() {
        assertEquals(ConnectorType.JDBC, ConnectorType.of(0));
    }
}

package org.apache.dolphinscheduler.plugin.datasource.s3.param;

import org.apache.dolphinscheduler.plugin.datasource.s3.S3DataSourceProcessor;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class S3DataSourceProcessorTest {

    private S3DataSourceProcessor s3DataSourceProcessor = new S3DataSourceProcessor();

    @Test
    public void testCreateConnectionParams() {
	  String connectionJson = "{\"user\":\"accessKey\",\"password\":\"secretKey\",\"address\":\"localhost\"" + ",\"database\":\"endpoint\",\"jdbcUrl\":\"localhost\"}";
	  S3ConnectionParam connectionParams = (S3ConnectionParam) s3DataSourceProcessor.createConnectionParams(connectionJson);
	  Assertions.assertNotNull(connectionJson);
	  Assertions.assertEquals("accessKey", connectionParams.getUser());
    }

    @Test
    public void testGetDbType() {
	  Assertions.assertEquals(DbType.S3, s3DataSourceProcessor.getDbType());
    }

}

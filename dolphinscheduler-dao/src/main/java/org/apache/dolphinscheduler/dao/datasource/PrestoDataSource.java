package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;

public class PrestoDataSource extends BaseDataSource {

  /**
   * @return driver class
   */
  @Override
  public String driverClassSelector() {
    return Constants.COM_PRESTO_JDBC_DRIVER;
  }

  /**
   * @return db type
   */
  @Override
  public DbType dbTypeSelector() {
    return DbType.PRESTO;
  }
}

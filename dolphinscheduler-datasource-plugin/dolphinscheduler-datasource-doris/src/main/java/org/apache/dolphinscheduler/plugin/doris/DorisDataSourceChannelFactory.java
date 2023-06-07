package org.apache.dolphinscheduler.plugin.doris;

import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory;

/**
 * @author xinxing
 * @description 功能描述
 * @create 2023/6/7 14:42
 */
public class DorisDataSourceChannelFactory implements DataSourceChannelFactory {
    @Override
    public DataSourceChannel create() {
        return new DorisDataSourceChannel();
    }

    @Override
    public String getName() {
        return "doris";
    }
}

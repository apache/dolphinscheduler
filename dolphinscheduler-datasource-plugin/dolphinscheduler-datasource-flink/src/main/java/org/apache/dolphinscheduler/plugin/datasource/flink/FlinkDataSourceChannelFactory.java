package org.apache.dolphinscheduler.plugin.datasource.flink;

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannel;
import org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory;

@AutoService(DataSourceChannelFactory.class)
public class FlinkDataSourceChannelFactory implements DataSourceChannelFactory {

    @Override
    public String getName() {
        return "flink";
    }

    @Override
    public DataSourceChannel create() {
        return new FlinkDataSourceChannel();
    }
}
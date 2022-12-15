package org.apache.dolphinscheduler.server.master.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MasterConfigStatic {

    public static MasterConfig masterConfigStatic;

    @Autowired
    public void setMasterConfig(@Qualifier("masterConfig") MasterConfig masterConfig) {
        masterConfigStatic = masterConfig;
    }
}

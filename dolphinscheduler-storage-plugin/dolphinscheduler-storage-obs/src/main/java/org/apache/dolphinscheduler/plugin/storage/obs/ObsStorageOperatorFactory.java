//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.apache.dolphinscheduler.plugin.storage.obs;

import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperateFactory;
import org.apache.dolphinscheduler.plugin.storage.api.StorageType;

import com.google.auto.service.AutoService;

@AutoService({StorageOperateFactory.class})
public class ObsStorageOperatorFactory implements StorageOperateFactory {

    public ObsStorageOperatorFactory() {
    }

    public StorageOperate createStorageOperate() {
        ObsStorageOperator ossOperator = new ObsStorageOperator();
        ossOperator.init();
        return ossOperator;
    }

    public StorageType getStorageOperate() {
        return StorageType.OBS;
    }
}

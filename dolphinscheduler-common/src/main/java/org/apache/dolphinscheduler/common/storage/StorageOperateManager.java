package org.apache.dolphinscheduler.common.storage;

import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Storage Operate Manager
 */
@Component
public class StorageOperateManager {

    /**
      * default storage
     */
    @Autowired
    private HadoopUtils hadoopUtils;

    public static Map<ResUploadType, StorageOperate> storageOperateMap = new HashMap<>(3);

    static {
        ServiceLoader<StorageOperate> load = ServiceLoader.load(StorageOperate.class);
        for (StorageOperate storageOperate : load) {
            storageOperateMap.put(storageOperate.returnStorageType(), storageOperate);
        }
    }

    public StorageOperate storageOperate(ResUploadType resUploadType){
        return storageOperateMap.getOrDefault(resUploadType, hadoopUtils);
    }
}

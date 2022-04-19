package org.apache.dolphinscheduler.common.storage;

import org.apache.dolphinscheduler.common.enums.ResUploadType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
/**
 * @author Storage Operate Manager
 */
public class StorageOperateManager {

    public static Map<ResUploadType, StorageOperate> STORAGE_OPERATE_MAP = new HashMap<>(ResUploadType.values().length);

    static {
        ServiceLoader<StorageOperate> load = ServiceLoader.load(StorageOperate.class);
        for (StorageOperate storageOperate : load) {
            STORAGE_OPERATE_MAP.put(storageOperate.returnStorageType(), storageOperate);
        }
    }

    public static StorageOperate storageOperate(ResUploadType resUploadType) {
        if (Objects.isNull(resUploadType)){
            resUploadType = ResUploadType.HDFS;
        }
        StorageOperate storageOperate = STORAGE_OPERATE_MAP.get(resUploadType);
        if (Objects.isNull(storageOperate)){
            storageOperate = STORAGE_OPERATE_MAP.get(ResUploadType.HDFS);
        }
        return storageOperate;
    }
}

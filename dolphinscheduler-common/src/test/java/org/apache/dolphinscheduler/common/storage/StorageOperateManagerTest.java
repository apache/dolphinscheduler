package org.apache.dolphinscheduler.common.storage;

import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

/**
 * @author StorageOperateManagerTest
 */
@RunWith(MockitoJUnitRunner.class)
public class StorageOperateManagerTest {

    @Mock
    private HadoopUtils hadoopUtils;

    @Test
    public void testManager() {
        Map<ResUploadType, StorageOperate> storageOperateMap = StorageOperateManager.STORAGE_OPERATE_MAP;
        storageOperateMap.put(ResUploadType.HDFS, hadoopUtils);

        StorageOperate storageOperate = StorageOperateManager.storageOperate(ResUploadType.HDFS);
        Assert.assertNotNull(storageOperate);
    }
}

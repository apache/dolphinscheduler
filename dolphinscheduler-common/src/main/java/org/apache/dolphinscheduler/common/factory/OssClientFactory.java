package org.apache.dolphinscheduler.common.factory;

import org.apache.dolphinscheduler.common.model.OssConnection;

import lombok.experimental.UtilityClass;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

@UtilityClass
public class OssClientFactory {

    public OSS buildOssClient(OssConnection ossConnection) {
        return new OSSClientBuilder().build(ossConnection.getEndPoint(),
                ossConnection.getAccessKeyId(), ossConnection.getAccessKeySecret());
    }
}

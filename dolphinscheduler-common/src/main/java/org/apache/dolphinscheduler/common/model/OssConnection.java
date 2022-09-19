package org.apache.dolphinscheduler.common.model;

import lombok.Data;

@Data
public class OssConnection {

    public String accessKeyId;
    public String accessKeySecret;
    public String endPoint;

    public OssConnection(final String accessKeyId, final String accessKeySecret, final String endPoint) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.endPoint = endPoint;
    }
}

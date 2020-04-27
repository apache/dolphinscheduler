package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.common.utils.EncryptionUtils;

public class Md5Test {
    public static void main(String[] args) {
        System.out.println(EncryptionUtils.getMd5("hdfs"));
    }
}

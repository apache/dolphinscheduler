package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.common.utils.EncryptionUtils;

public class Md5Test {
    public static void main(String[] args) {
        // admin 21232f297a57a5a743894a0e4a801fc3
        // hdfs dfccfc63f86ded406799fe52a988831b
        System.out.println(EncryptionUtils.getMd5("hdfs")
        );
    }
}

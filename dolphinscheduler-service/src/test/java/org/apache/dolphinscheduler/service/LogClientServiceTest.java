package org.apache.dolphinscheduler.service;

import org.apache.dolphinscheduler.service.log.LogClientService;

public class LogClientServiceTest {


    public static void main(String[] args) throws Exception {
        LogClientService logClientService = new LogClientService();
        String strs = logClientService.viewLog("192.168.5.129", 50051, "D:\\demo.txt");
        System.out.println(strs);
    }
}

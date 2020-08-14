package org.apache.dolphinscheduler.api;

import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Demo {

    public static void main(String[] args) throws Exception{
        Writer writer = new FileWriter("D:\\demo.txt");

        for (int i = 0 ; i < 3000; i++){
            writer.append("echo 日志打印测试" + (i+1) + "\r\n");
        }
        writer.close();
    }
}

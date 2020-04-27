package org.apache.dolphinscheduler.server.worker.task.connectors;


import java.io.IOException;

import org.apache.dolphinscheduler.common.utils.HttpUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class ConnectorsTest {


    private static final Logger logger = LoggerFactory.getLogger(ConnectorsTest.class);


    private static final  String taskName = "TaskByZL" ;

    @Test
    public void testHandle(){

        String bodyParams = "{\"name\":\""+taskName+"\",\"server\":\"http://192.168.101.42:8083\"," +
                "\"config\":{\"topics\":\"test-topic2\",\"connector.class\":\"org.apache.kafka.connect.file" +
                ".FileStreamSinkConnector\",\"tasks.max\":\"1\",\"file\":\"/data/log2.out\",\"type\":\"offset\"," +
                "\"offsetKey\":[{\"test-topic2-0\":\"22:34\"}]}}";

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(bodyParams);

        ConnectorsTask connectorsTask = new ConnectorsTask(taskExecutionContext,logger);

        connectorsTask.init();

        try {

            connectorsTask.handle();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testDelete(){
        try {
            HttpUtils.request("DELETE","http://192.168.101.42:8083/connectors/"+taskName,null,null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRequest(){
        //success

        String result  = null;


        String bodyParams = "{\"name\":\""+taskName+"\"," +
                "\"config\":{\"topics\":\"test-topic2\",\"connector.class\":\"org.apache.kafka.connect.file" +
                ".FileStreamSinkConnector\",\"tasks.max\":\"1\",\"file\":\"/data/log2.out\",\"type\":\"all\"}}";


        try {
            result = HttpUtils.request("POST","http://192.168.101.42:8083/connectors",null,null,bodyParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);

        try {
            result = HttpUtils.request("GET","http://192.168.101.42:8083/connectors",null,null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(result);


        try {
            result = HttpUtils.request("GET","http://192.168.101.42:8083/offset/"+taskName+"/status",null, null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);



        try {
            result = HttpUtils.request("DELETE","http://192.168.101.42:8083/connectors/"+taskName,null,null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);


    }


}

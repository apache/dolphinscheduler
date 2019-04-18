/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.api;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.escheduler.common.utils.EncryptionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class HttpClientTest {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientTest.class);

    public static void main(String[] args) throws Exception {
//        doGETParamPathVariableAndChinese();
//        doGETParam();
//        doPOSTParam();

        String md5 = EncryptionUtils.getMd5(String.valueOf(System.currentTimeMillis()) + "张三");
        System.out.println(md5);
        System.out.println(md5.length());
    }

    public static void doPOSTParam()throws Exception{
        // create Httpclient
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建http POST请求
        HttpPost httpPost = new HttpPost("http://127.0.0.1:12345/escheduler/projects/create");
        httpPost.setHeader("token", "123");
        // set parameters
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("projectName", "qzw"));
        parameters.add(new BasicNameValuePair("desc", "qzw"));

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(formEntity);


        CloseableHttpResponse response = null;
        try {
            // execute
            response = httpclient.execute(httpPost);
            // eponse status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(content);
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

    /**
     *
     * @throws Exception
     */
    public static void doGETParamPathVariableAndChinese()throws Exception{
        // create HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
//        parameters.add(new BasicNameValuePair("pageSize", "10"));

        // define the parameters of the request
        URI uri = new URIBuilder("http://127.0.0.1:12345/escheduler/projects/%E5%85%A8%E9%83%A8%E6%B5%81%E7%A8%8B%E6%B5%8B%E8%AF%95/process/list")
                .build();

        // create http GET request
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("token","123");
        //response object
        CloseableHttpResponse response = null;
        try {
            // execute http get request
            response = httpclient.execute(httpGet);
            // reponse status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("start--------------->");
                logger.info(content);
                logger.info("end----------------->");
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

    /**
     *
     * @throws Exception
     */
    public static void doGETParam()throws Exception{
        // create HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("processInstanceId", "41415"));

        // define the parameters of the request
        URI uri = new URIBuilder("http://127.0.0.1:12345/escheduler/projects/%E5%85%A8%E9%83%A8%E6%B5%81%E7%A8%8B%E6%B5%8B%E8%AF%95/instance/view-variables")
                 .setParameters(parameters)
                .build();

        // create http GET request
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("token","123");
        //response object
        CloseableHttpResponse response = null;
        try {
            // execute http get request
            response = httpclient.execute(httpGet);
            // reponse status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("start--------------->");
                logger.info(content);
                logger.info("end----------------->");
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

}

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

package org.apache.dolphinscheduler.plugin.task.flink;


import lombok.extern.slf4j.Slf4j;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

//@ExtendWith(MockitoExtension.class)
@Slf4j
public class FlinkMaterializedTableTaskTest {

   public static void main(String[] args) throws Exception {

      String result = "";
      HttpPost post = new HttpPost("http://localhost:8083/v1/sessions");

      // add request parameters or form parameters
//      List<NameValuePair> urlParameters = new ArrayList<>();
//      urlParameters.add(new BasicNameValuePair("username", "abc"));
//      urlParameters.add(new BasicNameValuePair("password", "123"));
//      urlParameters.add(new BasicNameValuePair("custom", "secret"));
//
//      post.setEntity(new UrlEncodedFormEntity(urlParameters));

      try (CloseableHttpClient httpClient = HttpClients.createDefault();
           CloseableHttpResponse response = httpClient.execute(post)){

         result = EntityUtils.toString(response.getEntity());

         Map<String, String> map = new HashMap<String, String>();
         ObjectMapper mapper = new ObjectMapper();

         try {
            //convert JSON string to Map
            map = mapper.readValue(result, Map.class);
            final String sessionHandle = map.get("sessionHandle");
            log.info(sessionHandle);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

   }

}

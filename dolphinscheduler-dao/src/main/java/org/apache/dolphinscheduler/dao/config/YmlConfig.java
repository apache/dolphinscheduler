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
package org.apache.dolphinscheduler.dao.config;



import org.yaml.snakeyaml.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class YmlConfig {

    public static Map<String,String> allMap=new HashMap<String,String>();
    static {
        Yaml yaml = new Yaml();
        InputStream inputStream = YmlConfig.class.getResourceAsStream("/application.yml");
        Iterator<Object> result = yaml.loadAll(inputStream).iterator();
        while(result.hasNext()){
            Map map=(Map)result.next();
            iteratorYml( map,null);
        }
    }

    public static void iteratorYml(Map map,String key){
        Iterator iterator = map.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key2 = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof LinkedHashMap){
                if(key==null){
                    iteratorYml((Map)value,key2.toString());
                }else{
                    iteratorYml((Map)value,key+"."+key2.toString());
                }
            }
            if(value instanceof String){
                if(key==null){
                    allMap.put(key2.toString(), value.toString());
                }
                if(key!=null){
                    allMap.put(key+"."+key2.toString(), value.toString());
                }
            }
        }

    }

}

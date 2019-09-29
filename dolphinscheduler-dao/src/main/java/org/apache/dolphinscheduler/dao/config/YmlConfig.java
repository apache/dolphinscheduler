package org.apache.dolphinscheduler.dao.config;



import org.yaml.snakeyaml.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by qiaozhanwei on 2019/9/17.
 */
public class YmlConfig {

    private static Map<String,String> allMap=new HashMap<String,String>();
    static {
        Yaml yaml = new Yaml();
        InputStream inputStream = YmlConfig.class.getResourceAsStream("/application.yml");
        Iterator<Object> result = yaml.loadAll(inputStream).iterator();
        while(result.hasNext()){
            Map map=(Map)result.next();
            iteratorYml( map,null);
        }
    }

    public static void main(String[] args) {
        String ss = allMap.get("spring.datasource.url");
        System.out.println(ss);
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

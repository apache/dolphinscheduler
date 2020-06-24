package org.apache.dolphinscheduler.spi.params; 

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.util.ArrayList;
import java.util.List;

/** 
* PluginParamsTransfer Tester. 
* 
* @author <Authors name> 
* @since <pre>六月 23, 2020</pre> 
* @version 1.0 
*/ 
public class PluginParamsTransferTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: getAlpacajsJson(List<AbsPluginParams> pluginParamsList) 
* 
*/ 
@Test
public void testGetAlpacajsJson() throws Exception {
    List<AbsPluginParams> paramsList = new ArrayList<>();
    TextParam name = new TextParam("somefie",
            "somefie",
            null,
            "input somefie",
            true,
            false);

    TextParam feedback = new TextParam("feedback",
            "feedback",
            null,
            "input feedback",
            true,
            true);

    List<String> emailShowTypeList = new ArrayList<>();
    emailShowTypeList.add("text");
    emailShowTypeList.add("table");
    RadioParam showType = new RadioParam("showTable",
            "mail.showTable",
            emailShowTypeList,
            "text",
            true,
            false);

    showType.addValue("testAdd");

    PasswordParam passwordParam = new PasswordParam("myPassword",
            "password",
            null,
            null,
            true,
            false);

    paramsList.add(name);
    paramsList.add(feedback);
    paramsList.add(showType);
    paramsList.add(passwordParam);

    String alpacajsJson = PluginParamsTransfer.getAlpacajsJson(paramsList);
    System.out.println(alpacajsJson);

    String paramsJson = "{\"data\":{\"somefie\":null,\"feedback\":null,\"showTable\":\"text\",\"myPassword\":null},\"schema\":{\"type\":\"object\",\"properties\":{\"somefie\":{\"default\":null,\"enum\":null,\"type\":\"string\",\"title\":\"somefie\",\"required\":true},\"feedback\":{\"default\":null,\"enum\":null,\"type\":\"string\",\"title\":\"feedback\",\"required\":true},\"showTable\":{\"default\":\"text\",\"enum\":[\"text\",\"table\",\"testAdd\"],\"type\":\"string\",\"title\":\"mail.showTable\",\"required\":true},\"myPassword\":{\"default\":null,\"enum\":null,\"type\":\"string\",\"title\":\"password\",\"required\":true}}},\"options\":{\"fields\":{\"somefie\":{\"type\":\"text\",\"placeholder\":\"input somefie\",\"readonly\":false},\"feedback\":{\"type\":\"text\",\"placeholder\":\"input feedback\",\"readonly\":true},\"showTable\":{\"type\":\"radio\",\"placeholder\":null,\"readonly\":false},\"myPassword\":{\"type\":\"password\",\"placeholder\":null,\"readonly\":false}}}}";

    Assert.assertEquals(alpacajsJson, paramsJson);
}


} 

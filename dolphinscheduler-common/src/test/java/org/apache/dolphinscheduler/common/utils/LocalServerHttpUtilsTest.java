package org.apache.dolphinscheduler.common.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServerHttpUtilsTest extends TestCase{

    private HadoopUtils hadoopUtils = HadoopUtils.getInstance();
    public static final Logger logger = LoggerFactory.getLogger(LocalServerHttpUtilsTest.class);

    public static Test suite(){
        TestSuite suite=new TestSuite();
        suite.addTestSuite(LocalServerHttpUtilsTest.class);
        return new LocalJettyHttpServer(suite);
    }

    public void testGetTest() throws Exception {
        // success
        String result = null;
        result = HttpUtils.get("http://localhost:8888/test.json");
        Assert.assertNotNull(result);
		ObjectNode jsonObject = JSONUtils.parseObject(result);
		Assert.assertEquals("Github",jsonObject.path("name").asText());
		result = HttpUtils.get("http://123.333.111.33/ccc");
		Assert.assertNull(result);
    }

    public void testGetByKerberos() {
        try {
            String applicationUrl = hadoopUtils.getApplicationUrl("application_1542010131334_0029");
            String responseContent;
            responseContent = HttpUtils.get(applicationUrl);
            Assert.assertNull(responseContent);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void testGetResponseContentString() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://localhost:8888/test.json");
        /** set timeout、request time、socket timeout */
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(Constants.HTTP_CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(Constants.SOCKET_TIMEOUT).setRedirectsEnabled(true).build();
        httpget.setConfig(requestConfig);

        String responseContent = null;
        for (int i = 0; i < 10; i++) {
            responseContent = HttpUtils.getResponseContentString(httpget, httpclient);
            if (responseContent != null) {
                break;
            }
        }
        Assert.assertNotNull(responseContent);
        responseContent = HttpUtils.getResponseContentString(null, httpclient);
        Assert.assertNull(responseContent);
        responseContent = HttpUtils.getResponseContentString(httpget, null);
        Assert.assertNull(responseContent);
    }

    public void testGetHttpClient() {
        CloseableHttpClient httpClient1 = HttpUtils.getInstance();
        CloseableHttpClient httpClient2 = HttpUtils.getInstance();
        Assert.assertEquals(httpClient1, httpClient2);
    }
}

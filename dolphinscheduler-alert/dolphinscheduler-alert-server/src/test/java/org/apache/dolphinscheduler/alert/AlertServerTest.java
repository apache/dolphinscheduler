package org.apache.dolphinscheduler.alert;

import junit.framework.TestCase;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;


@RunWith(MockitoJUnitRunner.class)
public class AlertServerTest extends TestCase {
    
    @InjectMocks
    private AlertServer alertServer;
    
    @Mock
    private PluginDao pluginDao;
    
    @Mock
    private AlertConfig alertConfig;
    
    @Test
    public void testStart() {
        Mockito.when(pluginDao.checkPluginDefineTableExist()).thenReturn(true);
        
        Mockito.when(alertConfig.getPort()).thenReturn(50053);
        
        alertServer.start();
    
        NettyRemotingServer nettyRemotingServer = Whitebox.getInternalState(alertServer, "server");
    
        NettyServerConfig nettyServerConfig = Whitebox.getInternalState(nettyRemotingServer, "serverConfig");
        
        Assert.assertEquals(50053, nettyServerConfig.getListenPort());
    }
}

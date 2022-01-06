package org.apache.dolphinscheduler.alert;

import junit.framework.TestCase;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlertPluginManagerTest extends TestCase {
    
    @InjectMocks
    private AlertPluginManager alertPluginManager;
    
    @Mock
    private PluginDao pluginDao;
    
    @Test
    public void testAlertPluginManager() {
        Mockito.when(pluginDao.addOrUpdatePluginDefine(Mockito.any(PluginDefine.class))).thenReturn(0);
        
        alertPluginManager.installPlugin();
        
        Assert.assertEquals(1, alertPluginManager.size());
        
        Assert.assertNotNull(alertPluginManager.getAlertChannel(0));
    }
}

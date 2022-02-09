package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author: zhuxuetong
 * @date: 2022/2/8 16:06
 * @Description:
 */
public class AbstractDataSourceProcessorTest {

    @Test
    public void checkOtherTest() {
        AbstractDataSourceProcessor mock = mock(AbstractDataSourceProcessor.class);
        Map<String, String> other = new HashMap<>();
        other.put("arg0", "1Aa-_/@.");
        mock.checkOther(other);
        verify(mock, times(1)).checkOther(other);
    }

    @Test
    public void checkOtherExceptionTest() {
        AbstractDataSourceProcessor mock = mock(AbstractDataSourceProcessor.class);
        Map<String, String> other = new HashMap<>();
        other.put("arg0", "%");
        doThrow(new IllegalArgumentException()).when(mock).checkOther(other);
    }
}
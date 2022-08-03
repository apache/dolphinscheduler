package org.apache.dolphinscheduler.plugin.task.api.parser;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author lirongqian
 * @date 2022/8/3
 **/
public class ParameterUtilsTest {

    @Test
    public void replaceListParameter() {
        Map<Integer, Property> params = new HashMap<>();
        params.put(1, new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList("c1", "c2", "c3"))));
        params.put(2, new Property(null, null, DataType.DATE, "2020-06-30"));
        params.put(3, new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList("d1", "d2", "d3"))));
        String sql = ParameterUtils.replaceListParameter(params, "select * from test where col1 in (?) and date=? and col2 in (?)");
        System.out.println(sql);
    }
}

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;

class VarPoolUtilsTest {

    @Test
    void mergeVarPool() {
        List<Property> varpool1 = null;
        List<Property> varpool2 = null;
        Truth.assertThat(VarPoolUtils.mergeVarPool(varpool1, varpool2)).isNull();

        // Override the value of the same property
        // Merge the property with different key.
        varpool1 = Lists.newArrayList(new Property("name", Direct.OUT, DataType.VARCHAR, "tom"));
        varpool2 = Lists.newArrayList(
                new Property("name", Direct.OUT, DataType.VARCHAR, "tim"),
                new Property("age", Direct.OUT, DataType.INTEGER, "10"));

        Truth.assertThat(VarPoolUtils.mergeVarPool(varpool1, varpool2))
                .containsExactly(
                        new Property("name", Direct.OUT, DataType.VARCHAR, "tim"),
                        new Property("age", Direct.OUT, DataType.INTEGER, "10"));

    }

}

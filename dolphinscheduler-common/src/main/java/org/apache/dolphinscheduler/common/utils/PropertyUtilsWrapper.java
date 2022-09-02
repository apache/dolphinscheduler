package org.apache.dolphinscheduler.common.utils;

import lombok.Data;

@Data
public class PropertyUtilsWrapper {

    public String getString(final String key) {
        return PropertyUtils.getString(key);
    }
}

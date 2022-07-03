package org.apache.dolphinscheduler.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public abstract class AbstractBaseEntity implements IBaseEntity{

    public Map<String, Object> toMap() {
        ObjectMapper objMapper = new ObjectMapper();
        return objMapper.convertValue(this, Map.class);
    }
}

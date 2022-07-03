package org.apache.dolphinscheduler.test.core.webstorage;


import java.util.Set;

public interface SeleniumWebStorage {

    public String getAt(String key);

    public void putAt(String key, String value);

    public void remove(String key);

    public Set<String> keySet();

    public int size();

    public void clear();
}
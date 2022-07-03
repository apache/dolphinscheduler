package org.apache.dolphinscheduler.test.core.webstorage;

import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import java.util.Set;

public class SeleniumSessionStorage implements SeleniumWebStorage{

    private final WebStorage webDriver;

    public SeleniumSessionStorage(WebStorage webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public String getAt(String key) {
        return this.getSessionStorage().getItem(key);
    }

    @Override
    public void putAt(String key, String value) {
        this.getSessionStorage().setItem(key, value);
    }

    @Override
    public void remove(String key) {
        this.getSessionStorage().removeItem(key);
    }

    @Override
    public Set<String> keySet() {
        return this.getSessionStorage().keySet();
    }

    @Override
    public int size() {
        return this.getSessionStorage().size();
    }

    @Override
    public void clear() {
        this.getSessionStorage().clear();
    }

    private SessionStorage getSessionStorage() {
        return this.webDriver.getSessionStorage();
    }
}

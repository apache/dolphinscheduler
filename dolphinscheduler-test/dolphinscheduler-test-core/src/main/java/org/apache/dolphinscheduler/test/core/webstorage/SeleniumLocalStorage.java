package org.apache.dolphinscheduler.test.core.webstorage;

import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;

import java.util.Set;

public class SeleniumLocalStorage implements SeleniumWebStorage {

    private final WebStorage webStorage;


    public SeleniumLocalStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }

    @Override
    public String getAt(String key) {
        return this.getLocalStorage().getItem(key);
    }

    @Override
    public void putAt(String key, String value) {
        this.getLocalStorage().setItem(key, value);
    }

    @Override
    public void remove(String key) {
        this.getLocalStorage().removeItem(key);
    }

    @Override
    public Set<String> keySet() {
        return this.getLocalStorage().keySet();
    }

    @Override
    public int size() {
        return this.getLocalStorage().size();
    }

    @Override
    public void clear() {
        this.getLocalStorage().clear();
    }

    private LocalStorage getLocalStorage() {
        return webStorage.getLocalStorage();
    }

}

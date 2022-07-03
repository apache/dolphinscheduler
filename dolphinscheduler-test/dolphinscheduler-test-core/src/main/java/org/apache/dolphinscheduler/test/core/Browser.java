package org.apache.dolphinscheduler.test.core;

import org.apache.dolphinscheduler.test.core.exception.NoBaseUrlDefinedException;
import org.apache.dolphinscheduler.test.core.exception.WebStorageNotSupportedException;
import org.apache.dolphinscheduler.test.core.js.JavascriptInterface;
import org.apache.dolphinscheduler.test.core.webstorage.SeleniumLocalStorage;
import org.apache.dolphinscheduler.test.core.webstorage.SeleniumSessionStorage;
import org.apache.dolphinscheduler.test.core.webstorage.SeleniumWebStorage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.html5.WebStorage;
import java.lang.reflect.InvocationTargetException;

public class Browser {
    private final static PageEventListener NOOP_PAGE_EVENT_LISTENER = new PageEventListenerSupport();
    private String baseUrl = null;
    private Page page;
    private WebDriver driver;

    public Browser(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
    }

    public Browser(WebDriver driver) {
        this.driver = driver;
    }


    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }


    private String getBaseUrlRequired() throws NoBaseUrlDefinedException {
        String baseUrl = getBaseUrl();
        if (baseUrl == null) {
            throw new NoBaseUrlDefinedException();
        }
        return baseUrl;
    }

    private Page makeCurrentPage(Page newPage) {
        if (newPage != page) {
            this.getPageEventListener().pageWillChange(this, page, newPage);
            if (page != null) {
                page.onUnload(newPage);
            }
            Page previousPage = page;
            page = newPage;
            page.get();
            page.onLoad(previousPage);
        }
        return page;
    }

    protected WebDriver switchToWindow(String window) {
        return driver.switchTo().window(window);
    }

    public  <T extends Page> T createPage(Class<T> pageType) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        validatePage(pageType);
        return initialisePage(pageType.getConstructor().newInstance());
    }

    public <T extends Page> T toPage(Class<T> pageClass)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return (T) makeCurrentPage(createPage(pageClass));
    }

    public <T extends Page> T toPage(T page) {
        makeCurrentPage(initialisePage(page));
        return page;
    }

    private void validatePage(Class<?> pageType) {
        if (!Page.class.isAssignableFrom(pageType)) {
            throw new IllegalArgumentException(pageType.getName() + " is not a subclass of " + Page.class.getName());
        }
    }

    private <T extends Page> T initialisePage(T page) {
        if (!this.is(this, page.getBrowser())) {
            page.init(this);
        }
        return page;
    }

    public <T extends Page> T to(T page, String url) throws Exception {
        return via(page, url);
    }

    public <T extends Page> T via(T page, String url) throws Exception {
        initialisePage(page);
        page.to(url);
        return page;
    }

    public void go(String url) {
        driver.get(url);
    }

    public void go() {
        driver.get(baseUrl);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    void quit() {
        driver.quit();
    }

    void close() {
        driver.close();
    }

    public Page getPage() {
        return this.page;
    }

    public JavascriptInterface getJs() {
        return new JavascriptInterface(this);
    }

    public void clearCookies() {
        driver.manage().deleteAllCookies();
    }

    public void clearCookiesQuietly() {
        try {
            clearCookies();
        } catch (WebDriverException e) {
            // ignore
        }
    }

    private WebStorage getSeleniumWebStorage() throws WebStorageNotSupportedException {
        if (driver instanceof WebStorage) {
            return (WebStorage) getDriver();
        } else {
            throw new WebStorageNotSupportedException();
        }
    }

    public SeleniumWebStorage getLocalStorage() throws WebStorageNotSupportedException {
        return new SeleniumLocalStorage(this.getSeleniumWebStorage());
    }

    public SeleniumWebStorage getSessionStorage() throws WebStorageNotSupportedException {
        return new SeleniumSessionStorage(this.getSeleniumWebStorage());
    }


    public void clearWebStorage() throws WebStorageNotSupportedException {
        getLocalStorage().clear();
        getSessionStorage().clear();
    }


    PageEventListener getPageEventListener() {
        return NOOP_PAGE_EVENT_LISTENER;
    }


    public boolean is(Object self, Object other) {
        return self == other;
    }
}

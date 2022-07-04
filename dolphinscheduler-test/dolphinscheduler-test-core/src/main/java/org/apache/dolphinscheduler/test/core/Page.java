package org.apache.dolphinscheduler.test.core;

import org.apache.dolphinscheduler.test.core.exception.PageInstanceNotInitializedException;
import org.apache.dolphinscheduler.test.core.js.JavascriptInterface;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;

public class Page extends LoadableComponent<Page> {
    private final long TIMEOUT = 60;
    private Browser browser;
    private WebDriverWait waitingSupport;

    public WebDriver getDriver() throws Exception {
        return getInitializedBrowser().getDriver();
    }

    public Page init(Browser browser) {
        this.browser = browser;
        this.waitingSupport = new WebDriverWait(this.browser.getDriver(), Duration.ofSeconds(30));
        PageFactory.initElements(browser.getDriver(), this);
        return this;
    }

    public Browser getBrowser() {
        return browser;
    }

    public <T extends Page> T to(T page) throws PageInstanceNotInitializedException {
        return getInitializedBrowser().toPage(page);
    }

    public void to(String url) throws Exception {
        getInitializedBrowser().go(url);
        getInitializedBrowser().toPage(this);
    }

    private Browser getInitializedBrowser() throws PageInstanceNotInitializedException {
        if (browser == null) {
            throw uninitializedException();
        }
        return browser;
    }

    public PageInstanceNotInitializedException uninitializedException() {
        String message = "Instance of page " + this.getClass().toString() + " has not been initialized.";
        return new PageInstanceNotInitializedException(message);
    }

    @Override
    public String toString() {
        return this.getClass().getName().toString();
    }


    /**
     * Lifecycle method called when the page is connected to the browser.
     * <p>
     * This implementation does nothing.
     *
     * @param previousPage The page that was active before this one
     */
    @SuppressWarnings({"UnusedMethodParameter", "EmptyMethod"})
    protected void onLoad(Page previousPage) {
    }

    /**
     * Lifecycle method called when this page is being replaced as the browser's page instance.
     * <p>
     * This implementation does nothing.
     *
     * @param nextPage The page that will be active after this one
     */
    @SuppressWarnings({"UnusedMethodParameter", "EmptyMethod"})
    protected void onUnload(Page nextPage) {
    }

    public JavascriptInterface getJs() throws Exception {
        return getInitializedBrowser().getJs();
    }


    public String getTitle() throws Exception {
        return getInitializedBrowser().getDriver().getTitle();
    }

    public void withRefresh() {
        getBrowser().getDriver().navigate().refresh();
    }

    public WebDriverWait waitFor() {
        return new WebDriverWait(this.browser.getDriver(), Duration.ofSeconds(TIMEOUT));
    }

    public WebDriverWait waitFor(long timeout) {
        return new WebDriverWait(this.browser.getDriver(), Duration.ofSeconds(timeout));
    }

    public WebElement waitFor(long timeout, Function<WebDriver, WebElement> isTrue) {
        return new WebDriverWait(this.browser.getDriver(), Duration.ofSeconds(timeout)).until(isTrue);
    }

    public WebElement waitFor(Function<WebDriver, WebElement> isTrue) {
        return this.waitingSupport.until(isTrue);
    }

    public WebElement withDialog(By by) {
        return getBrowser().getDriver().findElement(by);
    }

    @Override
    protected void load() {

    }

    @Override
    protected void isLoaded() throws Error {

    }
}

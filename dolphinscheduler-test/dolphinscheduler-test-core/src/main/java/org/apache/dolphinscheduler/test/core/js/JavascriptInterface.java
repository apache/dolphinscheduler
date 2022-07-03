package org.apache.dolphinscheduler.test.core.js;

import org.apache.dolphinscheduler.test.core.Browser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class JavascriptInterface {
    private Browser browser = null;

    public JavascriptInterface(Browser browser) {
        this.browser = browser;
    }

    public Object execjs(String script, Object... args) {
        WebDriver driver = browser.getDriver();

        if (!(driver instanceof JavascriptExecutor)) {
            throw new RuntimeException("driver " + driver + " can not execute javascript");
        }
        return ((JavascriptExecutor) driver).executeScript(script, args);

    }
}

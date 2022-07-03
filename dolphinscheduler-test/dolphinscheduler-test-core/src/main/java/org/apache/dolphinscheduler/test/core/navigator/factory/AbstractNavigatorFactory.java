package org.apache.dolphinscheduler.test.core.navigator.factory;

import org.apache.dolphinscheduler.test.core.Browser;
import org.apache.dolphinscheduler.test.core.navigator.Locator;
import org.apache.dolphinscheduler.test.core.navigator.Navigator;
import org.openqa.selenium.WebElement;

public abstract class AbstractNavigatorFactory implements NavigatorFactory{
    private final Browser browser;
    private final InnerNavigatorFactory innerNavigatorFactory;

    public AbstractNavigatorFactory(Browser browser, InnerNavigatorFactory innerNavigatorFactory) {
        this.browser = browser;
        this.innerNavigatorFactory = innerNavigatorFactory;
    }

    @Override
    public Navigator getBase() {
        return null;
    }

    @Override
    public Locator getLocator() {
        return null;
    }

    @Override
    public Navigator createFromWebElements(Iterable<WebElement> elements) {
        return null;
    }

    @Override
    public Navigator createFromNavigators(Iterable<Navigator> navigators) {
        return null;
    }

    @Override
    public NavigatorFactory relativeTo(Navigator newBase) {
        return null;
    }

    public Browser getBrowser() {
        return browser;
    }

    public InnerNavigatorFactory getInnerNavigatorFactory() {
        return innerNavigatorFactory;
    }
}

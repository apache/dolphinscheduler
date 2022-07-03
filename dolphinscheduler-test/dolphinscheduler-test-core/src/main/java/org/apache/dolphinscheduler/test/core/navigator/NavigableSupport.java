package org.apache.dolphinscheduler.test.core.navigator;

import org.apache.dolphinscheduler.test.core.Module;
import org.apache.dolphinscheduler.test.core.navigator.factory.NavigatorFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

public class NavigableSupport implements Navigable{

    private final NavigatorFactory navigatorFactory;
    private final WebDriver.TargetLocator targetLocator;

    NavigableSupport(NavigatorFactory navigatorFactory, WebDriver.TargetLocator targetLocator) {
        this.navigatorFactory = navigatorFactory;
        this.targetLocator = targetLocator;
    }

    @Override
    public Navigator find() {
        return this.getBase();
    }

    @Override
    public Navigator $() {
        return this.getBase();
    }

    @Override
    public Navigator find(int index) {
        return this.getBase().getAt(index);
    }

    @Override
    public Navigator find(List<Integer> range) {
        return getBase().getAt(range);
    }

    @Override
    public Navigator $(int index) {
        return this.getBase().getAt(index);
    }

    @Override
    public Navigator $(List<Integer> range) {
        return getBase().getAt(range);
    }

    @Override
    public Navigator $(Navigator[] navigators) {
        return this.navigatorFactory.createFromNavigators(Arrays.asList(navigators));
    }

    @Override
    public Navigator $(WebElement[] elements) {
        return this.navigatorFactory.createFromWebElements(Arrays.asList(elements));
    }

    @Override
    public Navigator focused() {
        return this.$(new WebElement[]{this.targetLocator.activeElement()});
    }

    @Override
    public <T extends Module> T module(Class<T> moduleClass) {
        return this.getBase().module(moduleClass);
    }

    @Override
    public <T extends Module> T module(T module) {
        return this.getBase().module(module);
    }


    public NavigatorFactory getNavigatorFactory() {
        return this.navigatorFactory;
    }

    public WebDriver.TargetLocator getTargetLocator() {
        return this.targetLocator;
    }

    private Navigator getBase() {
        return this.navigatorFactory.getBase();
    }

    private Locator getLocator() {
        return this.navigatorFactory.getLocator();
    }
}

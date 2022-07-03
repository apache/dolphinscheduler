package org.apache.dolphinscheduler.test.core.navigator.factory;

import org.apache.dolphinscheduler.test.core.navigator.Locator;
import org.apache.dolphinscheduler.test.core.navigator.Navigator;
import org.openqa.selenium.WebElement;

public interface NavigatorFactory {
    /**
     * The base navigator for this factory.
     *
     * @return The base navigator for this factory.
     */
    Navigator getBase();

    /**
     * The locator used for all content lookups from this factory.
     *
     * @return The locator used for all content lookups from this factory.
     */
    Locator getLocator();

    /**
     * Create a navigator, backed by the given web elements.
     *
     * @param elements The web elements to back the navigator.
     * @return The created navigator
     */
    Navigator createFromWebElements(Iterable<WebElement> elements);

    /**
     * Create a navigator, backed by the given navigators.
     *
     * @param navigators The navigators to back the navigator
     * @return The created navigator
     */
    Navigator createFromNavigators(Iterable<Navigator> navigators);

    /**
     * Create a new factory, relative to the given navigator.
     *
     * @param newBase The base to use for the new navigator factory.
     * @return The new navigator factory.
     */
    NavigatorFactory relativeTo(Navigator newBase);
}

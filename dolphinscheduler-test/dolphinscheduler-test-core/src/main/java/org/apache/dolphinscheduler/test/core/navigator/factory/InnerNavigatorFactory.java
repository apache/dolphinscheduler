package org.apache.dolphinscheduler.test.core.navigator.factory;

import org.apache.dolphinscheduler.test.core.Browser;
import org.apache.dolphinscheduler.test.core.navigator.Navigator;
import org.openqa.selenium.WebElement;


public interface InnerNavigatorFactory {
    Navigator createNavigator(Browser browser, Iterable<WebElement> elements);
}

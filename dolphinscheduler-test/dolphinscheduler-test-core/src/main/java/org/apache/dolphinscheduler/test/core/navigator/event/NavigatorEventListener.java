package org.apache.dolphinscheduler.test.core.navigator.event;

import org.apache.dolphinscheduler.test.core.Browser;
import org.apache.dolphinscheduler.test.core.navigator.Navigator;

public interface NavigatorEventListener {
    void beforeClick(Browser browser, Navigator navigator);

    void afterClick(Browser browser, Navigator navigator);

    void beforeValueSet(Browser browser, Navigator navigator, Object value);

    void afterValueSet(Browser browser, Navigator navigator, Object value);

    void beforeSendKeys(Browser browser, Navigator navigator, Object value);

    void afterSendKeys(Browser browser, Navigator navigator, Object value);
}

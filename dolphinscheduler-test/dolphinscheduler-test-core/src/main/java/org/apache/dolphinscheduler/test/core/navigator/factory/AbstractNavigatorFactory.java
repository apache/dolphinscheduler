/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

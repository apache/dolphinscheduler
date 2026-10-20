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

package org.apache.dolphinscheduler.e2e.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public final class UiInteraction {

    private UiInteraction() {
    }

    /**
     * Wait for the modal's enter transition before performing exactly one native click.
     */
    public static void click(WebDriver driver, String selector) {
        AtomicReference<String> lastState = new AtomicReference<>();
        WebElement element = WebDriverWaitFactory.createWebDriverWait(driver)
                .withMessage(() -> "UI target not ready: " + selector + "; state=" + lastState.get())
                .until(ignored -> {
                    List<WebElement> targets = driver.findElements(By.cssSelector(selector)).stream()
                            .filter(WebElement::isDisplayed)
                            .collect(Collectors.toList());
                    if (targets.size() != 1) {
                        lastState.set("visible matches=" + targets.size());
                        return null;
                    }
                    WebElement target = targets.get(0);
                    List<WebElement> modals =
                            target.findElements(By.xpath("ancestor-or-self::*[@data-modal-ready][1]"));
                    String modalReady =
                            modals.isEmpty() ? "outside modal" : modals.get(0).getDomAttribute("data-modal-ready");
                    lastState.set("enabled=" + target.isEnabled() + ", modalReady=" + modalReady);
                    return target.isEnabled() && (modals.isEmpty() || "true".equals(modalReady)) ? target : null;
                });
        log.info("Clicking UI target: selector={}, url={}, location={}, size={}, state={}",
                selector, driver.getCurrentUrl(), element.getLocation(), element.getSize(), lastState.get());
        element.click();
    }
}

package org.apache.dolphinscheduler.test.core.navigator;

import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;

public interface BasicLocator {
    public static final String DYNAMIC_ATTRIBUTE_NAME = "dynamic";

    /**
     * Creates a new Navigator instance containing the elements matching the given <code>By</code> type selector.
     * Any <code>By</code> type capabilities supported by the underlying WebDriver instance are supported.
     *
     * @param bySelector a WebDriver By selector
     * @return new Navigator instance containing the matched elements
     */
    Navigator find(By bySelector);

    /**
     * Creates a new Navigator instance containing the elements matching the given
     * <code>By</code> type selector and index. Any <code>By</code> type capabilities supported by the underlying WebDriver instance are supported.
     *
     * @param bySelector a WebDriver By selector
     * @param index      index of the required element in the selection
     * @return new Navigator instance containing the matched elements
     */
    Navigator find(By bySelector, int index);

    /**
     * Creates a new Navigator instance containing the elements matching the given
     * <code>By</code> type selector and attributes. Any <code>By</code> type capabilities supported by the underlying WebDriver instance are supported.
     *
     * @param bySelector a WebDriver By selector
     * @param attributes a Map with keys representing attributes and values representing required values or patterns
     * @return new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes, By bySelector);

    /**
     * Selects elements by both CSS attributes and index. For example find(name: "firstName", 1) will select
     * second element with the name attribute of "firstName".
     *
     * @param attributes a Map with keys representing attributes and values representing required values or patterns
     * @param index      index of the required element in the selection
     * @return a new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes, int index);

    /**
     * Selects elements by both CSS attributes and range. For example find(name: "firstName", 1..2) will select
     * second and third element with the name attribute of "firstName".
     *
     * @param attributes a Map with keys representing attributes and values representing required values or patterns
     * @param range      range of the required elements in the selection
     * @return a new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes, List<Integer> range);

    /**
     * Creates a new Navigator instance containing the elements matching the given
     * <code>By</code> type selector, attributes and index. Any <code>By</code> type capabilities supported by the underlying WebDriver instance are supported.
     *
     * @param bySelector a WebDriver By selector
     * @param attributes a Map with keys representing attributes and values representing required values or patterns
     * @param index      index of the required element in the selection
     * @return new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes, By bySelector, int index);

    /**
     * Creates a new Navigator instance containing the elements matching the given
     * <code>By</code> type selector, attributes and range. Any <code>By</code> type capabilities supported by the underlying WebDriver instance are supported.
     *
     * @param bySelector a WebDriver By selector
     * @param attributes a Map with keys representing attributes and values representing required values or patterns
     * @param range      range of the required elements in the selection
     * @return new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes, By bySelector, List<Integer> range);

    /**
     * Selects elements by both CSS selector and attributes. For example find("input", name: "firstName") will select
     * all input elements with the name "firstName".
     *
     * @param selector   a CSS selector
     * @param attributes a Map with keys representing attributes and values representing required values or patterns
     * @return a new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes, String selector);

    /**
     * Selects elements by both CSS selector and attributes. For example find("input", name: "firstName", 1) will select
     * second input element with the name "firstName".
     *
     * @param selector   a CSS selector
     * @param attributes a Map with keys representing attributes and values representing required values or patterns
     * @param index      index of the required element in the selection
     * @return a new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes, String selector, int index);

    /**
     * Selects elements by both CSS selector and attributes. For example find("input", name: "firstName", 1..2) will select
     * second and third input element with the name "firstName".
     *
     * @param selector   a CSS selector
     * @param attributes a Map with keys representing attributes and values representing required values or patterns
     * @param range      range of the required elements in the selection
     * @return a new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes, String selector, List<Integer> range);
}

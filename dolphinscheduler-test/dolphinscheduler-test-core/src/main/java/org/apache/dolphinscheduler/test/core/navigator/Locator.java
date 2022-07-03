package org.apache.dolphinscheduler.test.core.navigator;

import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;

public interface Locator extends BasicLocator {
    public static final String MATCH_ALL_SELECTOR = "*";

    /**
     * Shorthand for <code>find(null, selector, null)</code>
     *
     * @return new Navigator
     */
    Navigator find(String selector);

    /**
     * Shorthand for <code>find(selector)</code>
     *
     * @return new Navigator
     * @see #find(java.lang.String)
     */
    Navigator $(String selector);

    /**
     * Creates a new Navigator instance containing the elements whose attributes match the specified values or patterns.
     * The key 'text' can be used to match the text contained in elements. Regular expression Pattern objects may be
     * used as values.
     * <p>Examples:</p>
     * <dl>
     * <dt>find(name: "firstName")</dt>
     * <dd>selects all elements with the name "firstName"</dd>
     * <dt>find(name: "firstName", readonly: "readonly")</dt>
     * <dd>selects all elements with the name "firstName" that are read-only</dd>
     * <dt>find(text: "I can has cheezburger")</dt>
     * <dd>selects all elements containing the exact text</dd>
     * <dt>find(text: ~/I can has.+/)</dt>
     * <dd>selects all elements whose text matches a regular expression</dd>
     * </dl>
     *
     * @return a new Navigator instance containing the matched elements
     */
    Navigator find(Map<String, Object> attributes);

    /**
     * Shorthand for <code>find(selector)[indexOfElement]</code>.
     *
     * @param selector a CSS selector
     * @param index    index of the required element in the selection
     * @return new Navigator instance containing a single element
     */
    Navigator find(String selector, int index);

    /**
     * Shorthand for <code>find(null, selector, range)</code>
     *
     * @param selector The css selector
     * @return new Navigator
     */
    Navigator find(String selector, List<Integer> range);

    /**
     * Shorthand for <code>find(selector, index)</code>.
     *
     * @param selector a CSS selector
     * @param index    index of the required element in the selection
     * @return new Navigator instance containing a single element
     * @see #find(java.lang.String, int)
     */
    Navigator $(String selector, int index);

    /**
     * Shorthand for <code>find(selector, range)</code>
     *
     * @param selector The css selector
     * @return new Navigator
     */
    Navigator $(String selector, List<Integer> range);

    /**
     * Shorthand for <code>find(predicates, bySelector)</code>
     *
     * @param bySelector a WebDriver By selector
     * @return a new Navigator instance containing the matched elements
     */
    Navigator $(Map<String, Object> attributes, By bySelector);

    /**
     * Shorthand for <code>find(predicates, bySelector, index)</code>
     *
     * @param bySelector a WebDriver By selector
     * @return new Navigator
     */
    Navigator $(Map<String, Object> attributes, By bySelector, int index);

    /**
     * Shorthand for <code>find(predicates, bySelector, range)</code>
     *
     * @param bySelector a WebDriver By selector
     * @return new Navigator instance containing the matched elements
     */
    Navigator $(Map<String, Object> attributes, By bySelector, List<Integer> range);


    /**
     * Shorthand for <code>find(bySelector)</code>
     *
     * @param bySelector a WebDriver By selector
     * @return new Navigator
     * @see BasicLocator#find(org.openqa.selenium.By)
     */
    Navigator $(By bySelector);

    /**
     * Shorthand for <code>find(bySelector, index)</code>.
     *
     * @param bySelector a WebDriver By selector
     * @param index      index of the required element in the selection
     * @return new Navigator instance containing a single element
     * @see BasicLocator#find(org.openqa.selenium.By, int)
     */
    Navigator $(By bySelector, int index);

    /**
     * Shorthand for <code>find(bySelector, range)</code>
     *
     * @param bySelector a WebDriver By selector
     * @return new Navigator
     */
    Navigator $(By bySelector, List<Integer> range);

    /**
     * Shorthand for <code>find(null, bySelector, range)</code>
     *
     * @param bySelector a WebDriver By selector
     * @return new Navigator
     */
    Navigator find(By bySelector, List<Integer> range);

    /**
     * Shorthand for <code>find(predicates)</code>
     *
     * @return a new Navigator instance containing the matched elements
     */
    Navigator $(Map<String, Object> attributes);

    /**
     * Shorthand for <code>find(predicates, index)</code>
     *
     * @return new Navigator
     */
    Navigator $(Map<String, Object> attributes, int index);

    /**
     * Shorthand for <code>find(predicates, range)</code>
     *
     * @return new Navigator
     */
    Navigator $(Map<String, Object> attributes, List<Integer> range);

    /**
     * Shorthand for <code>find(predicates, selector)</code>
     *
     * @param selector   a CSS selector
     * @return a new Navigator instance containing the matched elements
     */
    Navigator $(Map<String, Object> attributes, String selector);

    /**
     * Shorthand for <code>find(predicates, selector, index)</code>
     *
     * @return new Navigator
     */
    Navigator $(Map<String, Object> attributes, String selector, int index);

    /**
     * Shorthand for <code>find(predicates, selector, range)</code>
     *
     * @param selector a CSS selector
     * @return new Navigator instance containing the matched elements
     */
    Navigator $(Map<String, Object> attributes, String selector, List<Integer> range);

}

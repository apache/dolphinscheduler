package org.apache.dolphinscheduler.spi.params.checkbox;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

/**
 * front-end checkbox component props attributes
 */
public class CheckboxParamProps extends ParamsProps {

    /**
     * the minimum number of checkboxes that can be checked
     */
    private Integer min;

    /**
     * the maximum number of checkboxes that can be checked
     */
    private Integer max;

    /**
     * the color of the text when the Checkbox in the form of a button is activated
     */
    private String textColor;

    /**
     * the fill color and border color of the Checkbox in the form of a button when activated
     */
    private String fill;

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getFill() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill = fill;
    }
}

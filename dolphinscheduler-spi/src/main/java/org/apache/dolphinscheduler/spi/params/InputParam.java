package org.apache.dolphinscheduler.spi.params;


import org.apache.dolphinscheduler.spi.params.base.FormType;
import org.apache.dolphinscheduler.spi.params.base.ParamsProps;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

/**
 *Text param
 */
public class InputParam extends PluginParams {

    public InputParam(String name, String label) {
        super(name, FormType.INPUT, label);
    }

    public InputParam setPlaceholder(String placeholder) {
        if(this.getProps() == null) {
            this.setProps(new ParamsProps());
        }

        this.getProps().setPlaceholder(placeholder);
        return this;
    }
}

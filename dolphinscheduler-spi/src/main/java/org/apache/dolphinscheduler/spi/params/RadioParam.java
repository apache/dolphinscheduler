package org.apache.dolphinscheduler.spi.params;

import java.util.ArrayList;
import java.util.List;

/**
 * radio
 */
public class RadioParam extends AbsPluginParams {

    /**
     * values can be select
     */
    private List<String> values;

    public RadioParam(String name, String showNameEn, String showNameCh) {
        super(name, showNameEn, showNameCh);
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    /**
     * add one value
     * @param value
     * @return
     */
    public RadioParam addValue(String value) {
        if (this.values == null) {
            this.values = new ArrayList<>();
        }
        this.values.add(value);
        return this;
    }
}

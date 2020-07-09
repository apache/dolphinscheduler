package org.apache.dolphinscheduler.spi.params;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.dolphinscheduler.spi.params.base.FormType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.ArrayList;
import java.util.List;

/**
 * radio
 */
public class RadioParam extends PluginParams {

    private List<ParamsOptions> paramsOptionsList;

    public RadioParam(String name, String label, List<ParamsOptions> paramsOptionsList) {
        super(name, FormType.RADIO, label);
        this.paramsOptionsList = paramsOptionsList;
    }

    public RadioParam(String name, String label) {
        super(name, FormType.RADIO, label);
    }

    @JsonProperty("options")
    public List<ParamsOptions> getParamsOptionsList() {
        return paramsOptionsList;
    }

    public void setParamsOptionsList(List<ParamsOptions> paramsOptionsList) {
        this.paramsOptionsList = paramsOptionsList;
    }

    public RadioParam addParamsOptions(ParamsOptions paramsOptions) {
        if(this.paramsOptionsList == null) {
            this.paramsOptionsList = new ArrayList<>();
        }

        this.paramsOptionsList.add(paramsOptions);
        return this;
    }

}

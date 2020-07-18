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

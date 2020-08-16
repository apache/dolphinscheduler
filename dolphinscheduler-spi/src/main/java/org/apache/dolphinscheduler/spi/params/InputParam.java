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


import org.apache.dolphinscheduler.spi.params.base.FormType;
import org.apache.dolphinscheduler.spi.params.base.ParamsProps;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

/**
 * Text param
 */
public class InputParam extends PluginParams {

    public InputParam(String name, String label) {
        super(name, FormType.INPUT, label);
    }

    public InputParam setPlaceholder(String placeholder) {
        if (this.getProps() == null) {
            this.setProps(new ParamsProps());
        }

        this.getProps().setPlaceholder(placeholder);
        return this;
    }
}

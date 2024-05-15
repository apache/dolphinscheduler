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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class VarPoolUtils {

    /**
     * Merge the given two varpools, and return the merged varpool.
     * If the two varpools have the same property({@link Property#getProp()} and {@link Property#getDirect()} is same), the value of the property in varpool2 will be used.
     * // todo: we may need to consider the datatype of the property
     */
    public List<Property> mergeVarPool(List<Property> varPool1, List<Property> varPool2) {
        if (CollectionUtils.isEmpty(varPool1)) {
            return varPool2;
        }
        if (CollectionUtils.isEmpty(varPool2)) {
            return varPool1;
        }
        List<Property> result = new ArrayList<>();
        for (Property v2 : varPool2) {
            Optional<Property> v1Optional = varPool1
                    .stream()
                    .filter(v1 -> v1.getProp().equals(v2.getProp()) && v1.getDirect().equals(v2.getDirect()))
                    .findFirst();
            if (v1Optional.isPresent()) {
                // todo: clone the property object rather directly change it
                Property v1 = v1Optional.get();
                v1.setValue(v2.getValue());
                result.add(v1);
            } else {
                result.add(v2);
            }
        }
        return result;
    }

}

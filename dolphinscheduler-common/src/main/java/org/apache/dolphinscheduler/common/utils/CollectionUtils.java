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

package org.apache.dolphinscheduler.common.utils;

import org.apache.commons.beanutils.BeanMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides utility methods and decorators for {@link Collection} instances.
 * <p>
 * Various utility methods might put the input objects into a Set/Map/Bag. In case
 * the input objects override {@link Object#equals(Object)}, it is mandatory that
 * the general contract of the {@link Object#hashCode()} method is maintained.
 * <p>
 * NOTE: From 4.0, method parameters will take {@link Iterable} objects when possible.
 *
 * @version $Id: CollectionUtils.java 1686855 2015-06-22 13:00:27Z tn $
 * @since 1.0
 */
public class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("Construct CollectionUtils");
    }

    /**
     * Removes certain attributes of each object in the list
     *
     * @param originList origin list
     * @param exclusionSet exclusion set
     * @param <T> T
     * @return removes certain attributes of each object in the list
     */
    public static <T extends Object> List<Map<String, Object>> getListByExclusion(List<T> originList,
                                                                                  Set<String> exclusionSet) {
        List<Map<String, Object>> instanceList = new ArrayList<>();
        if (originList == null) {
            return instanceList;
        }
        Map<String, Object> instanceMap;
        for (T instance : originList) {
            BeanMap beanMap = new BeanMap(instance);
            instanceMap = new LinkedHashMap<>(16, 0.75f, true);
            for (Map.Entry<Object, Object> entry : beanMap.entrySet()) {
                if (exclusionSet != null && exclusionSet.contains(entry.getKey())) {
                    continue;
                }
                instanceMap.put((String) entry.getKey(), entry.getValue());
            }
            instanceList.add(instanceMap);
        }
        return instanceList;
    }

}

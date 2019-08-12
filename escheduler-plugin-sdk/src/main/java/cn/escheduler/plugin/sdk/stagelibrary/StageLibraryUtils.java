/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.stagelibrary;

import cn.escheduler.plugin.api.impl.LocalizableMessage;

import java.lang.reflect.Method;

public class StageLibraryUtils {
    private static final String LIBRARY_RB = "data-collector-library-bundle";

    private StageLibraryUtils() {}

    public static String getLibraryName(ClassLoader cl) {
        String name;
        try {
            Method method = cl.getClass().getMethod("getName");
            name = (String) method.invoke(cl);
        } catch (NoSuchMethodException ex ) {
            name = "default";
        } catch (Exception ex ) {
            throw new RuntimeException(ex);
        }
        return name;
    }

    public static String getLibraryType(ClassLoader cl) {
        String name = null;
        try {
            Method method = cl.getClass().getMethod("getType");
            method.setAccessible(true);
            name = (String) method.invoke(cl);
        } catch (NoSuchMethodException ex ) {
            // ignore
        } catch (Exception ex ) {
            throw new RuntimeException(ex);
        }
        return name;
    }

    public static String getLibraryLabel(ClassLoader cl) {
        String label = getLibraryName(cl);
        LocalizableMessage lm = new LocalizableMessage(cl, LIBRARY_RB, "library.name", label, new Object[0]);
        return lm.getLocalized();
    }
}

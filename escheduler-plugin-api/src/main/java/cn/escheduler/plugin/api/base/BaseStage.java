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
package cn.escheduler.plugin.api.base;

import cn.escheduler.plugin.api.ConfigIssue;
import cn.escheduler.plugin.api.Stage;
import cn.escheduler.plugin.api.Stage.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage implementation providing empty Data Collector lifecycle methods and convenience methods for subclasses.
 */
public abstract class BaseStage<C extends Context> implements Stage<C> {
    private Info info;
    private C context;
    private boolean requiresSuperInit;
    private boolean superInitCalled;

    /**
     * Initializes the stage.
     * <p/>
     * Stores the <code>Stage.Info</code> and <code>Stage.Context</code> in instance variables and calls the
     * {@link #init()} method.
     * <p/>
     * @param info the stage information.
     * @param context the stage context.
     * @return the list of configuration issues found during initialization, an empty list if none.
     */
    @Override
    public List<ConfigIssue> init(Info info, C context) {
        List<ConfigIssue> issues = new ArrayList<>();
        this.info = info;
        this.context = context;
        issues.addAll(init());
        if (requiresSuperInit && !superInitCalled) {
            issues.add(context.createConfigIssue(null, null, Errors.API_20));
        }
        return issues;
    }

    /**
     * Initializes the stage. Subclasses should override this method for stage initialization.
     * <p/>
     * This implementation is a no-operation.
     *
     * @return the list of configuration issues found during initialization, an empty list if none.
     */
    protected List<ConfigIssue> init() {
        return new ArrayList<>();
    }

    void setRequiresSuperInit() {
        requiresSuperInit = true;
    }

    void setSuperInitCalled() {
        superInitCalled = true;
    }

    /**
     * Returns the stage information passed by the Data Collector during initialization.
     * @return the stage information passed by the Data Collector during initialization.
     */
    protected Info getInfo() {
        return info;
    }

    /**
     * Returns the stage context passed by the Data Collector during initialization.
     * @return the stage context passed by the Data Collector during initialization.
     */
    protected C getContext() {
        return context;
    }

    /**
     * Destroy the stage. Subclasses should override this method for stage cleanup.
     * <p/>
     * This implementation is a no-operation.
     */
    @Override
    public void destroy() {
    }

}

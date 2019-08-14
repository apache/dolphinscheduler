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
package cn.escheduler.plugin.sdk.runner;

import cn.escheduler.plugin.api.Stage;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import cn.escheduler.plugin.api.Configuration;
import cn.escheduler.plugin.sdk.metrics.MetricsConfigurator;

import cn.escheduler.plugin.sdk.validation.Issue;
import cn.escheduler.plugin.api.ConfigIssue;
import cn.escheduler.plugin.api.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Map;

/**
 * Shared context for both Service and Stage.
 */
public abstract class ProtoContext implements Stage.Context {

    private static final Logger LOG = LoggerFactory.getLogger(ProtoContext.class);
    private static final String CUSTOM_METRICS_PREFIX = "custom.";
    protected static final String STAGE_CONF_PREFIX = "stage.conf_";

    private final Configuration configuration;
    protected final MetricRegistry metrics;
    protected final int runnerId;
    protected final String pipelineId;
    protected final String rev;
    protected final String stageInstanceName;

    protected ProtoContext(
            Configuration configuration,
            MetricRegistry metrics,
            String pipelineId,
            String rev,
            int runnerId,
            String stageInstanceName
    ) {
        this.configuration = configuration;
        this.metrics = metrics;
        this.pipelineId = pipelineId;
        this.rev = rev;
        this.runnerId = runnerId;
        this.stageInstanceName = stageInstanceName;
    }

    static class ConfigIssueImpl extends Issue implements ConfigIssue {
        public ConfigIssueImpl(
                String stageName,
                String serviceName,
                String configGroup,
                String configName,
                ErrorCode errorCode,
                Object... args
        ) {
            super(stageName, serviceName, configGroup, configName, errorCode, args);
        }
    }

    static final Object[] NULL_ONE_ARG = {null};

    @Override
    public String getConfig(String configName) {
        return configuration.get(configName, null);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public ConfigIssue createConfigIssue(
            String configGroup,
            String configName,
            ErrorCode errorCode,
            Object... args
    ) {
        Preconditions.checkNotNull(errorCode, "errorCode cannot be null");
        args = (args != null) ? args.clone() : NULL_ONE_ARG;
        return new ConfigIssueImpl(stageInstanceName, "", configGroup, configName, errorCode, args);
    }

    @Override
    public MetricRegistry getMetrics() {
        return metrics;
    }

    @Override
    public Timer createTimer(String name) {
        return MetricsConfigurator.createStageTimer(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId, pipelineId,
                rev);
    }

    public Timer getTimer(String name) {
        return MetricsConfigurator.getTimer(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId);
    }

    @Override
    public Meter createMeter(String name) {
        return MetricsConfigurator.createStageMeter(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId, pipelineId,
                rev);
    }

    public Meter getMeter(String name) {
        return MetricsConfigurator.getMeter(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId);
    }

    @Override
    public Counter createCounter(String name) {
        return MetricsConfigurator.createStageCounter(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId, pipelineId,
                rev);
    }

    public Counter getCounter(String name) {
        return MetricsConfigurator.getCounter(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId);
    }

    @Override
    public Histogram createHistogram(String name) {
        return MetricsConfigurator.createStageHistogram5Min(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId, pipelineId, rev);
    }

    @Override
    public Histogram getHistogram(String name) {
        return MetricsConfigurator.getHistogram(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId);
    }

    @Override
    public Gauge<Map<String, Object>> createGauge(String name) {
        return MetricsConfigurator.createStageGauge(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId, null, pipelineId, rev);
    }

    @Override
    public Gauge<Map<String, Object>> createGauge(String name, Comparator<String> comparator) {
        return MetricsConfigurator.createStageGauge(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId, comparator, pipelineId, rev);
    }

    @Override
    public Gauge<Map<String, Object>> getGauge(String name) {
        return MetricsConfigurator.getGauge(getMetrics(), CUSTOM_METRICS_PREFIX + stageInstanceName + "." + name + "." + runnerId);
    }
}

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
package cn.escheduler.plugin.sdk.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides various helpful tools to work with metric objects. This util class expose several families of
 * methods that can be used for various purposes:
 *
 * createX and deleteX
 *  Will always create a new metric object and throws an exception of metric of given name already exists.
 *
 * createStageX and deleteStageX
 *  Will create or create new or return existing stage specific metric object. Particularly useful for metrics
 *  that are covering all instances of given stage when pipeline is running in multi-threaded fashion.
 */
public class MetricsConfigurator {
    public static final String JMX_PIPELINE_PREFIX = "sdc.pipeline.";
    public static final String JMX_FRAMEWORK_PREFIX = "sdc.pipeline.";

    public static final String METER_SUFFIX = ".meter";
    public static final String COUNTER_SUFFIX = ".counter";
    public static final String HISTOGRAM_M5_SUFFIX = ".histogramM5";
    public static final String TIMER_SUFFIX = ".timer";
    public static final String GAUGE_SUFFIX = ".gauge";

    private static MetricRegistry sdcMetrics;
    private static List<String> runningPipelines = new ArrayList<>();

    private MetricsConfigurator() {}

    private static String metricName(String name, String type) {
        if (name.endsWith(type)) {
            return name;
        }
        return name + type;
    }

    private static String jmxPipelinePrefix(String pipelineName, String pipelineRev) {
        return JMX_PIPELINE_PREFIX + pipelineName + "." + pipelineRev + ".";
    }

    private static <T extends Metric> T create(
            MetricRegistry metrics,
            final T metric,
            final String name,
            final String pipelineName,
            final String pipelineRev
    ) {
        final String jmxNamePrefix = jmxPipelinePrefix(pipelineName, pipelineRev);
        final MetricRegistry metricRegistry = sdcMetrics;
        if (metricRegistry != null && runningPipelines.contains(jmxNamePrefix)) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    String metricName = jmxNamePrefix + name;
                    if(!metricRegistry.getNames().contains(metricName)) {
                        metricRegistry.register(metricName, metric);
                    }
                    return null;
                }
            });
        }
        return metrics.register(name, metric);
    }

    public static Timer createStageTimer(MetricRegistry metrics, String nameSuffix, final String pipelineName, final String pipelineRev) {
        String name = metricName(nameSuffix, TIMER_SUFFIX);
        if(metrics.getTimers().containsKey(name)) {
            return metrics.getTimers().get(name);
        }

        return createTimer(metrics, nameSuffix, pipelineName, pipelineRev);
    }

    public static Timer createTimer(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return create(
                metrics,
                new Timer(new SlidingTimeWindowReservoir(60, TimeUnit.SECONDS)),
                metricName(name, TIMER_SUFFIX),
                pipelineName,
                pipelineRev
        );
    }

    public static Meter createStageMeter(MetricRegistry metrics, String nameSuffix, final String pipelineName, final String pipelineRev) {
        String name = metricName(nameSuffix, METER_SUFFIX);
        if(metrics.getMeters().containsKey(name)) {
            return metrics.getMeters().get(name);
        }

        return createMeter(metrics, nameSuffix, pipelineName, pipelineRev);
    }

    public static Meter createMeter(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return create(
                metrics,
                new ExtendedMeter(),
                metricName(name, METER_SUFFIX),
                pipelineName,
                pipelineRev
        );
    }

    public static Counter createStageCounter(MetricRegistry metrics, String nameSuffix, final String pipelineName, final String pipelineRev) {
        String name = metricName(nameSuffix, COUNTER_SUFFIX);
        if(metrics.getCounters().containsKey(name)) {
            return metrics.getCounters().get(name);
        }

        return createCounter(metrics, nameSuffix, pipelineName, pipelineRev);
    }

    public static Counter createCounter(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return create(
                metrics,
                new Counter(),
                metricName(name, COUNTER_SUFFIX),
                pipelineName,
                pipelineRev
        );
    }

    public static Histogram createStageHistogram5Min(MetricRegistry metrics, String nameSuffix, final String pipelineName, final String pipelineRev) {
        String name = metricName(nameSuffix, HISTOGRAM_M5_SUFFIX);
        if(metrics.getHistograms().containsKey(name)) {
            return metrics.getHistograms().get(name);
        }

        return createHistogram5Min(metrics, nameSuffix, pipelineName, pipelineRev);
    }

    public static Histogram createHistogram5Min(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return create(
                metrics,
                new Histogram(new ExponentiallyDecayingReservoir()),
                metricName(name, HISTOGRAM_M5_SUFFIX),
                pipelineName,
                pipelineRev
        );
    }

    public static Gauge<Map<String, Object>> createFrameworkGauge(MetricRegistry metricRegistry, String componentName, String metricName, Comparator<String> comparator) {
        String fullName = JMX_FRAMEWORK_PREFIX + componentName + "." + metricName + GAUGE_SUFFIX;
        Gauge<Map<String, Object>> gauge = new MapGauge(comparator);
        return metricRegistry.register(fullName, gauge);
    }

    public static Gauge<Map<String, Object>> createStageGauge(MetricRegistry metrics, String nameSuffix, Comparator<String> comparator, final String pipelineName, final String pipelineRev) {
        String name = metricName(nameSuffix, GAUGE_SUFFIX);
        if(metrics.getGauges().containsKey(name)) {
            return metrics.getGauges().get(name);
        }

        return createGauge(metrics, nameSuffix, comparator, pipelineName, pipelineRev);
    }

    public static Gauge<Map<String, Object>> createGauge(MetricRegistry metrics, String name, Comparator<String> comparator, final String pipelineName, final String pipelineRev) {
        return create(
                metrics,
                new MapGauge(comparator),
                metricName(name, GAUGE_SUFFIX),
                pipelineName,
                pipelineRev
        );
    }

    // Kept for backward compatibility with runtime stats, to be removed in future
    public static Gauge<Map<String, Object>> createGauge(MetricRegistry metrics, String name, Gauge gauge, final String pipelineName, final String pipelineRev) {
        return create(
                metrics,
                gauge,
                metricName(name, GAUGE_SUFFIX),
                pipelineName,
                pipelineRev
        );
    }

    public static Counter getCounter(MetricRegistry metrics, String name) {
        return metrics.getCounters().get(metricName(name, COUNTER_SUFFIX));
    }

    public static ExtendedMeter getMeter(MetricRegistry metrics, String name) {
        return (ExtendedMeter) metrics.getMeters().get(metricName(name, METER_SUFFIX));
    }

    public static Histogram getHistogram(MetricRegistry metrics, String name) {
        return metrics.getHistograms().get(metricName(name, HISTOGRAM_M5_SUFFIX));
    }

    public static Timer getTimer(MetricRegistry metrics, String name) {
        return metrics.getTimers().get(metricName(name, TIMER_SUFFIX));
    }

    public static Gauge getGauge(MetricRegistry metrics, String name) {
        return metrics.getGauges().get(metricName(name, GAUGE_SUFFIX));
    }

    /**
     * Remove metric object (regardless of it's type)
     */
    private static boolean remove(final MetricRegistry metrics, final String name, String pipelineName, String pipelineRev) {
        final String jmxNamePrefix = jmxPipelinePrefix(pipelineName, pipelineRev);
        final MetricRegistry metricRegistry = sdcMetrics;
        if (metricRegistry != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    metricRegistry.remove(jmxNamePrefix  + name);
                    return null;
                }
            });
        }
        return metrics.remove(name);
    }

    public static boolean removeGauge(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return remove(metrics, metricName(name, GAUGE_SUFFIX), pipelineName, pipelineRev);
    }

    public static boolean removeMeter(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return remove(metrics, metricName(name, METER_SUFFIX), pipelineName, pipelineRev);
    }

    public static boolean removeCounter(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return remove(metrics, metricName(name, COUNTER_SUFFIX), pipelineName, pipelineRev);
    }

    public static boolean removeStageGauge(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return remove(metrics, metricName(name, GAUGE_SUFFIX), pipelineName, pipelineRev);
    }

    public static boolean removeStageMeter(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return remove(metrics, metricName(name, METER_SUFFIX), pipelineName, pipelineRev);
    }

    public static boolean removeStageCounter(MetricRegistry metrics, String name, final String pipelineName, final String pipelineRev) {
        return remove(metrics, metricName(name, COUNTER_SUFFIX), pipelineName, pipelineRev);
    }

    public static synchronized void registerJmxMetrics(MetricRegistry metrics) {
        sdcMetrics = metrics;
    }

    public static synchronized void registerPipeline(String pipelineName, String pipelineRev) {
        runningPipelines.add(jmxPipelinePrefix(pipelineName, pipelineRev));
    }

    public static synchronized void cleanUpJmxMetrics(final String pipelineName, final String pipelineRev) {
        final MetricRegistry metricRegistry = sdcMetrics;
        final String jmxNamePrefix = jmxPipelinePrefix(pipelineName, pipelineRev);
        runningPipelines.remove(jmxNamePrefix);
        if (metricRegistry != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    for (String name : metricRegistry.getNames()) {
                        if (name.startsWith(jmxNamePrefix)) {
                            metricRegistry.remove(name);
                        }
                    }
                    return null;
                }
            });
        }
    }

    public static boolean resetCounter(MetricRegistry metrics, String name) {
        Counter counter = getCounter(metrics, name);
        boolean result = false;
        if(counter != null) {
            //there could be race condition with observer thread trying to update the counter. This should be ok.
            counter.dec(counter.getCount());
            result = true;
        }
        return result;
    }

}

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
package org.apache.dolphinscheduler.microbench.base;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * BaseBenchMark
 * If you need to test jmh, please extend him first
 */
@Warmup(iterations = AbstractBaseBenchmark.DEFAULT_WARMUP_ITERATIONS)
@Measurement(iterations = AbstractBaseBenchmark.DEFAULT_MEASURE_ITERATIONS)
@State(Scope.Thread)
@Fork(AbstractBaseBenchmark.DEFAULT_FORKS)
public abstract class AbstractBaseBenchmark {

    static final int DEFAULT_WARMUP_ITERATIONS = 10;

    static final int DEFAULT_MEASURE_ITERATIONS = 10;

    static final int DEFAULT_FORKS = 2;

    private static Logger logger = LoggerFactory.getLogger(AbstractBaseBenchmark.class);


    private ChainedOptionsBuilder newOptionsBuilder() {

        String className = getClass().getSimpleName();

        ChainedOptionsBuilder optBuilder = new OptionsBuilder()
                // set benchmark ClassName
                .include(className);

        if (getMeasureIterations() > 0) {
            optBuilder.warmupIterations(getMeasureIterations());
        }

        if (getMeasureIterations() > 0) {
            optBuilder.measurementIterations(getMeasureIterations());
        }

        if (getForks() > 0) {
            optBuilder.forks(getForks());
        }

        String output = getReportDir();
        if (output != null) {
            boolean writeFileStatus;
            String filePath = getReportDir() + className + ".json";
            File file = new File(filePath);

            if (file.exists()) {
                writeFileStatus = file.delete();


            } else {
                writeFileStatus = file.getParentFile().mkdirs();
                try {
                    writeFileStatus = file.createNewFile();
                } catch (IOException e) {
                    logger.warn("jmh test create file error" + e);
                }
            }
            if (writeFileStatus) {
                optBuilder.resultFormat(ResultFormatType.JSON)
                        .result(filePath);
            }
        }
        return optBuilder;
    }

    @Test
    public void run() throws Exception {
        new Runner(newOptionsBuilder().build()).run();
    }

    private int getWarmupIterations() {

        String value = System.getProperty("warmupIterations");
        return null != value ? Integer.parseInt(value) : -1;
    }

    private int getMeasureIterations() {
        String value = System.getProperty("measureIterations");
        return null != value ? Integer.parseInt(value) : -1;
    }

    private static String getReportDir() {
        return System.getProperty("perfReportDir");
    }

    private static int getForks() {
        String value = System.getProperty("forkCount");
        return null != value ? Integer.parseInt(value) : -1;
    }


}


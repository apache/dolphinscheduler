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
package cn.escheduler.plugin.sdk.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import cn.escheduler.plugin.sdk.metrics.ExtendedMeter;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExtendedMeterSerializer extends JsonSerializer<ExtendedMeter> {
    private final String rateUnit;
    private final double rateFactor;

    private static String calculateRateUnit(TimeUnit unit, String name) {
        String s = unit.toString().toLowerCase(Locale.US);
        return name + '/' + s.substring(0, s.length() - 1);
    }

    public ExtendedMeterSerializer(TimeUnit rateUnit) {
        this.rateFactor = (double)rateUnit.toSeconds(1L);
        this.rateUnit = calculateRateUnit(rateUnit, "events");
    }

    @Override
    public void serialize(ExtendedMeter meter, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeNumberField("count", meter.getCount());
        jgen.writeNumberField("m1_rate", meter.getOneMinuteRate() * this.rateFactor);
        jgen.writeNumberField("m5_rate", meter.getFiveMinuteRate() * this.rateFactor);
        jgen.writeNumberField("m15_rate", meter.getFifteenMinuteRate() * this.rateFactor);
        jgen.writeNumberField("m30_rate", meter.getThirtyMinuteRate() * this.rateFactor);
        jgen.writeNumberField("h1_rate", meter.getOneHourRate() * this.rateFactor);
        jgen.writeNumberField("h6_rate", meter.getSixHourRate() * this.rateFactor);
        jgen.writeNumberField("h12_rate", meter.getTwelveHourRate() * this.rateFactor);
        jgen.writeNumberField("h24_rate", meter.getTwentyFourHourRate() * this.rateFactor);
        jgen.writeNumberField("mean_rate", meter.getMeanRate() * this.rateFactor);
        jgen.writeStringField("units", this.rateUnit);
        jgen.writeEndObject();
    }

}

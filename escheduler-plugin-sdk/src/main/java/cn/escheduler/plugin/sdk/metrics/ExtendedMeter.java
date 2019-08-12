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

import com.codahale.metrics.Clock;
import com.codahale.metrics.EWMA;
import com.codahale.metrics.Meter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.exp;

public class ExtendedMeter extends Meter {
    private static final int INTERVAL = 5;
    private static final double SECONDS_PER_MINUTE = 60.0;
    private static final int THIRTY_MINUTES = 30;
    private static final int ONE_HOUR_MINUTES = 60;
    private static final int SIX_HOURS_MINUTES = 6 * 60;
    private static final int TWELVE_HOURS_MINUTES = 12 * 60;
    private static final int TWENTY_HOURS_MINUTES = 24 * 60;

    private static final double M30_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / THIRTY_MINUTES);
    private static final double H1_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / ONE_HOUR_MINUTES);
    private static final double H6_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / SIX_HOURS_MINUTES);
    private static final double H12_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / TWELVE_HOURS_MINUTES);
    private static final double H24_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / TWENTY_HOURS_MINUTES);

    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final EWMA m30Rate;
    private final EWMA h1Rate;
    private final EWMA h6Rate;
    private final EWMA h12Rate;
    private final EWMA h24Rate;

    private final AtomicLong lastTick;
    private final Clock clock;

    public ExtendedMeter() {
        this(Clock.defaultClock());
    }

    public ExtendedMeter(Clock clock) {
        super(clock);
        this.clock = clock;
        this.lastTick = new AtomicLong(this.clock.getTick());
        m30Rate = new EWMA(M30_ALPHA, INTERVAL, TimeUnit.SECONDS);
        h1Rate = new EWMA(H1_ALPHA, INTERVAL, TimeUnit.SECONDS);
        h6Rate = new EWMA(H6_ALPHA, INTERVAL, TimeUnit.SECONDS);
        h12Rate = new EWMA(H12_ALPHA, INTERVAL, TimeUnit.SECONDS);
        h24Rate = new EWMA(H24_ALPHA, INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void mark(long n) {
        super.mark(n);
        tickIfNecessary();
        m30Rate.update(n);
        h1Rate.update(n);
        h6Rate.update(n);
        h12Rate.update(n);
        h24Rate.update(n);
    }

    private void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL) {
            final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
            if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
                final long requiredTicks = age / TICK_INTERVAL;
                for (long i = 0; i < requiredTicks; i++) {
                    m30Rate.tick();
                    h1Rate.tick();
                    h6Rate.tick();
                    h12Rate.tick();
                    h24Rate.tick();
                }
            }
        }
    }

    public double getThirtyMinuteRate() {
        tickIfNecessary();
        return m30Rate.getRate(TimeUnit.SECONDS);
    }

    public double getOneHourRate() {
        tickIfNecessary();
        return h1Rate.getRate(TimeUnit.SECONDS);
    }

    public double getSixHourRate() {
        tickIfNecessary();
        return h6Rate.getRate(TimeUnit.SECONDS);
    }

    public double getTwelveHourRate() {
        tickIfNecessary();
        return h12Rate.getRate(TimeUnit.SECONDS);
    }

    public double getTwentyFourHourRate() {
        tickIfNecessary();
        return h24Rate.getRate(TimeUnit.SECONDS);
    }

}

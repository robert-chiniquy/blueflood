/*
 * Copyright 2013 Rackspace
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rackspacecloud.blueflood.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class BasicRollup extends Rollup {
    private static final Logger log = LoggerFactory.getLogger(BasicRollup.class);
    private Average average;
    private Variance variance;
    private MinValue minValue;
    private MaxValue maxValue;
    private long count;

    public BasicRollup() {
        this.average = new Average();
        this.variance = new Variance();
        this.minValue = new MinValue();
        this.maxValue = new MaxValue();
        this.count = 0;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BasicRollup)) {
            return false;
        }

        BasicRollup otherBasicRollup = (BasicRollup)other;

        return (this.count == otherBasicRollup.getCount())
                && average.equals(otherBasicRollup.getAverage())
                && variance.equals(otherBasicRollup.getVariance())
                && minValue.equals(otherBasicRollup.getMinValue())
                && maxValue.equals(otherBasicRollup.getMaxValue());
    }

    public Average getAverage() {
        return this.average;
    }

    public Variance getVariance() {
        return this.variance;
    }

    public MinValue getMinValue() {
        return this.minValue;
    }

    public MaxValue getMaxValue() {
        return this.maxValue;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }
    
    public String toString() {
        return String.format("cnt:%d, avg:%s, var:%s, min:%s, max:%s", count, average, variance, minValue, maxValue);
    }

    @Override
    public void computeFromSimpleMetrics(Points<SimpleNumber> input) throws IOException {
        if (input == null) {
            throw new IOException("Null input to create rollup from");
        }

        if (input.isEmpty()) {
            return;
        }

        Map<Long, Points.Point<SimpleNumber>> points = input.getPoints();
        for (Map.Entry<Long, Points.Point<SimpleNumber>> item : points.entrySet()) {
            this.count += 1;
            SimpleNumber numericMetric = item.getValue().getData();
            average.handleFullResMetric(numericMetric.getValue());
            variance.handleFullResMetric(numericMetric.getValue());
            minValue.handleFullResMetric(numericMetric.getValue());
            maxValue.handleFullResMetric(numericMetric.getValue());
        }
    }

    @Override
    public void computeFromRollups(Points<BasicRollup> input) throws IOException {
        if (input == null) {
            throw new IOException("Null input to create rollup from");
        }

        if (input.isEmpty()) {
            return;
        }

        Map<Long, Points.Point<BasicRollup>> points = input.getPoints();

        for (Map.Entry<Long, Points.Point<BasicRollup>> item : points.entrySet()) {
            BasicRollup basicRollup = item.getValue().getData();
            this.count += basicRollup.getCount();
            average.handleRollupMetric(basicRollup);
            variance.handleRollupMetric(basicRollup);
            minValue.handleRollupMetric(basicRollup);
            maxValue.handleRollupMetric(basicRollup);
        }
    }

    public static BasicRollup buildRollupFromRawSamples(Points<SimpleNumber> input) throws IOException {
        final BasicRollup basicRollup = new BasicRollup();
        basicRollup.computeFromSimpleMetrics(input);

        return basicRollup;
    }

    public static BasicRollup buildRollupFromRollups(Points<BasicRollup> input) throws IOException {
        final BasicRollup basicRollup = new BasicRollup();
        basicRollup.computeFromRollups(input);

        return basicRollup;
    }
}
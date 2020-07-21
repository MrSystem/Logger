/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package eu.menzani.logger.impl;

import eu.menzani.logger.api.RotationPolicy;
import eu.menzani.logger.Objects;
import eu.menzani.logger.StringFormat;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;

public final class TemporalRotationPolicy implements RotationPolicy {
    private final StringFormat nameFormat;
    private final DateTimeFormatter timestampFormatter;
    private final int stepWidth;
    private final TemporalField stepWidthField;

    public TemporalRotationPolicy(String nameFormat, DateTimeFormatter timestampFormatter,
                                  int stepWidth, TemporalField stepWidthField) {
        if (stepWidth < 1) {
            throw new IllegalArgumentException("stepWidth must be positive.");
        }
        this.nameFormat = new StringFormat(Objects.objectNotNull(nameFormat, "nameFormat"));
        this.timestampFormatter = Objects.objectNotNull(timestampFormatter, "timestampFormatter");
        this.stepWidth = stepWidth;
        this.stepWidthField = Objects.objectNotNull(stepWidthField, "stepWidthField");
    }

    @Override
    public void initialize(Path root, Temporal timestamp) {
        // Do nothing
    }

    @Override
    public Path currentFile(Path root, Temporal timestamp) {
        TemporalAccessor roundedDown = timestamp.with(stepWidthField,
                timestamp.get(stepWidthField) / stepWidth * stepWidth);
        return root.resolve(nameFormat.clone().fill("timestamp", timestampFormatter.format(roundedDown)).toString());
    }
}

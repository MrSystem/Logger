/*
 * Copyright 2020 Francesco Menzani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.menzani.logger.impl;

import eu.menzani.logger.Objects;
import eu.menzani.logger.api.Formatter;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class TimestampFormatter implements Formatter {
    private final DateTimeFormatter formatter;

    public TimestampFormatter() {
        this(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }

    public TimestampFormatter(DateTimeFormatter formatter) {
        this.formatter = Objects.objectNotNull(formatter, "formatter");
    }

    @Override
    public String format(LogEntry entry) {
        return formatter.format(entry.getTimestamp());
    }
}

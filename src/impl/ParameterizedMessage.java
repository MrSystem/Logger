/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package eu.menzani.logger.impl;

import eu.menzani.logger.Nullable;
import eu.menzani.logger.Objects;
import eu.menzani.logger.api.LazyMessage;

public final class ParameterizedMessage implements LazyMessage {
    private volatile String marker = "{}";
    private final String parameterizedString;
    private final Object[] arguments;

    public ParameterizedMessage(String parameterizedString, @Nullable Object... arguments) {
        this.parameterizedString = Objects.objectNotNull(parameterizedString, "parameterizedString");
        this.arguments = Objects.objectNotNull(arguments, "arguments");
    }

    public ParameterizedMessage marker(String marker) {
        this.marker = Objects.objectNotNull(marker, "marker");
        return this;
    }

    public ParameterizedMessage with(LazyMessage... arguments) {
        Objects.objectNotNull(arguments, "arguments");
        int j = 0;
        for (int i = 0; i < this.arguments.length; i++) {
            if (this.arguments[i] == null) {
                if (j == arguments.length) {
                    throw new IllegalArgumentException("Too few arguments.");
                }
                this.arguments[i] = Objects.elementNotNull(arguments[j++], "arguments");
            }
        }
        if (j != arguments.length) {
            throw new IllegalArgumentException("Too many arguments.");
        }
        return this;
    }

    @Override
    public Object evaluate() throws Exception {
        StringBuilder builder = new StringBuilder(parameterizedString);
        int i = 0, j = 0;
        while ((i = builder.indexOf(marker, i)) != -1) {
            if (j == arguments.length) {
                throw newProductionException("too few arguments");
            }
            Object argument = arguments[j++];
            if (argument == null) {
                throw newProductionException("placeholder argument not set");
            }
            if (argument instanceof LazyMessage) {
                argument = ((LazyMessage) argument).evaluate();
            }
            String replacement = argument.toString();
            builder.replace(i, i + 2, replacement);
            i += replacement.length();
        }
        if (j != arguments.length) {
            throw newProductionException("too many arguments");
        }
        return builder;
    }

    private static RuntimeException newProductionException(String cause) {
        return new IllegalStateException("Could not produce parameterized message: " + cause + '.');
    }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package eu.menzani.logger;

public interface Lazy<V> {
    V get() throws Exception;

    @FunctionalInterface
    interface Initializer<T> {
        T newInstance() throws Exception;
    }
}

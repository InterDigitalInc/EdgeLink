/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.net.intent.impl.phase;

import org.onosproject.net.intent.IntentData;

/**
 * Represents a phase where an intent is not compiled.
 * <p>
 * This should be used if a new version of the intent will immediately override
 * this one.
 * </p>
 */
public final class Skipped extends FinalIntentProcessPhase {

    private static final Skipped SINGLETON = new Skipped();

    /**
     * Returns a shared skipped phase.
     *
     * @return skipped phase
     */
    public static Skipped getPhase() {
        return SINGLETON;
    }

    // Prevent object construction; use getPhase()
    private Skipped() {
    }

    @Override
    public IntentData data() {
        return null;
    }
}

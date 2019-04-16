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
package org.onosproject.lisp.ctl;

import org.onlab.packet.IpAddress;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * LISP router factory which returns concrete router object for the physical
 * LISP router in use.
 */
public final class LispRouterFactory {

    private final Logger log = getLogger(getClass());

    private LispRouterAgent agent;

    // non-instantiable (except for our Singleton)
    private LispRouterFactory() {
    }

    /**
     * Configures LISP router agent only if it is not initialized.
     *
     * @param agent reference object of LISP router agent
     */
    public void setAgent(LispRouterAgent agent) {
        synchronized (agent) {
            if (this.agent == null) {
                this.agent = agent;
            } else {
                log.warn("LISP Router Agent has already been set.");
            }
        }
    }

    /**
     * Cleans up LISP router agent.
     */
    public void cleanAgent() {
        synchronized (agent) {
            if (this.agent != null) {
                this.agent = null;
            } else {
                log.warn("LISP Router Agent is not configured.");
            }
        }
    }

    /**
     * Returns a LISP router instance.
     *
     * @param routerId LISP router identifier
     * @return LISP router instance
     */
    public LispRouter getRouterInstance(IpAddress routerId) {
        LispRouter router = new DefaultLispRouter(new LispRouterId(routerId));
        router.setAgent(agent);
        return router;
    }

    /**
     * Returns an instance of LISP router agent factory.
     *
     * @return instance of LISP router agent factory
     */
    public static LispRouterFactory getInstance() {
        return SingletonHelper.INSTANCE;
    }

    /**
     * Prevents object instantiation from external.
     */
    private static final class SingletonHelper {
        private static final String ILLEGAL_ACCESS_MSG = "Should not instantiate this class.";
        private static final LispRouterFactory INSTANCE = new LispRouterFactory();

        private SingletonHelper() {
            throw new IllegalAccessError(ILLEGAL_ACCESS_MSG);
        }
    }
}

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
package org.onosproject.pcerest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

/**
 * Base class for pce rest api tests.  Performs common configuration operations.
 */
public class PceResourceTest extends JerseyTest {

    /**
     * Use first available port.
     *
     * @see TestProperties#CONTAINER_PORT
     */
    protected static final int EPHEMERAL_PORT = 0;

    /**
     * Creates a new web-resource test.
     */
    public PceResourceTest() {
        super(ResourceConfig.forApplicationClass(PceWebApplication.class));
        set(TestProperties.CONTAINER_PORT, EPHEMERAL_PORT);
    }
}

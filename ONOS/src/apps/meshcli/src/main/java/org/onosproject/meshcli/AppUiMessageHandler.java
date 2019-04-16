/* =============================================================================
**
**                     Copyright (c) 2019
**                 InterDigital Communications, Inc.
**
**                      All rights reserved.
**
**  Licensed under the terms and conditions provided in InterDigital Labs Public
**  License v.1 (the “License”). You may not use this file except in compliance 
**  with the License. Unless required by applicable law or agreed to in writing,
**  software distributed under the License is distributed on as “AS IS” BASIS,
**  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
**  the License for the specific language governing permissions and limitations
**  under the License.
**
** =============================================================================
*/
package org.onosproject.meshcli;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Skeletal ONOS UI Custom-View message handler.
 */
public class AppUiMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_CUSTOM_DATA_REQ = "sampleCustomDataRequest";
    private static final String SAMPLE_CUSTOM_DATA_RESP = "sampleCustomDataResponse";

    private static final String NUMBER = "number";
    private static final String SQUARE = "square";
    private static final String CUBE = "cube";
    private static final String MESSAGE = "message";
    private static final String MSG_FORMAT = "Next incrememt is %d units";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private long someNumber = 1;
    private long someIncrement = 1;

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleCustomDataRequestHandler()
        );
    }

    // handler for sample data requests
    private final class SampleCustomDataRequestHandler extends RequestHandler {

        private SampleCustomDataRequestHandler() {
            super(SAMPLE_CUSTOM_DATA_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            someIncrement++;
            someNumber += someIncrement;
            log.debug("Computing data for {}...", someNumber);

            ObjectNode result = objectNode();
            result.put(NUMBER, someNumber);
            result.put(SQUARE, someNumber * someNumber);
            result.put(CUBE, someNumber * someNumber * someNumber);
            result.put(MESSAGE, String.format(MSG_FORMAT, someIncrement + 1));
            sendMessage(SAMPLE_CUSTOM_DATA_RESP, result);
        }
    }
}

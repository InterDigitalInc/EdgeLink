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
package org.onosproject.meshnbi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class NbiUiMessageHandler extends UiMessageHandler {

    private static final String NBI_NOTIFICATION = "nbiNotification";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new NbiNotificationDataHandler()
        );
    }

    // handler for NBI Notifications
    private final class NbiNotificationDataHandler extends RequestHandler {

        private NbiNotificationDataHandler() {
            super(NBI_NOTIFICATION);
        }

        @Override
        public void process(ObjectNode payload) {
            log.info("Sending NBI Notification data to WebSocket client " + payload);
            sendMessage(NBI_NOTIFICATION, payload);
        }
    }
}

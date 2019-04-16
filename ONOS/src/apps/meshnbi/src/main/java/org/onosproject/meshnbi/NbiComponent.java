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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.meshmanager.MeshService;
import org.onosproject.meshmanager.NbiNotificationData;
import org.onosproject.meshmanager.NbiNotificationListener;
import org.onosproject.ui.NbiNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component(immediate = true)
@Service
public class NbiComponent implements NbiNotificationListener {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private MeshService meshService;

    @Activate
    protected void activate() {
        log.info("Started meshnbi app");
        meshService.registerNbiNotificationListener(this);
    }

    @Deactivate
    protected void deactivate() {
        log.info("meshnbi Stopped");
        meshService.unregisterNbiNotificationListener();
        NbiNotificationHandler.getInstance().clearUiMessageHandlerList();
    }

    @Override
    public void onNotificationUpdate(NbiNotificationData nbiNotificationData) {
        log.info("onNotificationUpdate() :: " + nbiNotificationData);
        ObjectMapper mapper1 = new ObjectMapper();
        ObjectNode node1 = mapper1.createObjectNode().put("event", "nbiNotification");
        ObjectNode node2 = mapper1.createObjectNode();

        Short type = nbiNotificationData.getType();
        String status = nbiNotificationData.getStatus();
        List<Integer> idList = nbiNotificationData.getIdList();
        node2.put("type", type);
        node2.put("count", nbiNotificationData.getCount());
        node2.putPOJO("idList", idList);
        if(type == 0 && status != null) {
            node2.put("status", status);
        }

        node1.put("payload", node2);
        NbiNotificationHandler nbiNotificationHandler = NbiNotificationHandler.getInstance();
        nbiNotificationHandler.getNbiNotificationHandlers().forEach(
                (handler) -> {
                    if(handler != null) {
                        log.info("Sending notification");
                        handler.process(node1);
                    }
                    else
                        log.info("Handler is null");
                }
        );
    }
}

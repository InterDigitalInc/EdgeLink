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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.meshmanager.MeshService;
import org.onosproject.meshmanager.api.message.AlertStateE;
import org.onosproject.meshmanager.api.message.AlertTypeE;
import org.onosproject.meshmanager.api.message.AlertsReport;
import org.onosproject.meshmanager.api.message.MeshNBICustomerDeregistrationRsp;
import org.onosproject.meshmanager.api.message.MeshNBIGetAlertsReq;
import org.onosproject.meshmanager.api.message.MeshNBIGetAlertsRsp;
import org.onosproject.meshmanager.api.message.MeshNBIGetMaxAvailableBWReq;
import org.onosproject.meshmanager.api.message.MeshNBIGetMaxAvailableBWRsp;
import org.onosproject.meshmanager.api.message.LinkStats;
import org.onosproject.meshmanager.api.message.MeshNBICreateSliceReq;
import org.onosproject.meshmanager.api.message.MeshNBICustomerDeregistrationReq;
import org.onosproject.meshmanager.api.message.MeshNBICustomerRegistrationReq;
import org.onosproject.meshmanager.api.message.MeshNBIDeleteSliceReq;
import org.onosproject.meshmanager.api.message.MeshNBIGetNodeInfoReq;
import org.onosproject.meshmanager.api.message.MeshNBIGetSliceReq;
import org.onosproject.meshmanager.api.message.MeshNBIRequest;
import org.onosproject.meshmanager.api.message.MeshNBIModifySliceReq;
import org.onosproject.meshmanager.api.message.MeshNBIReadStatsReq;
import org.onosproject.meshmanager.api.message.MeshNBIResponse;
import org.onosproject.meshmanager.api.message.MeshNBIModifySliceRsp;
import org.onosproject.meshmanager.api.message.MeshNBIReadStatsRsp;
import org.onosproject.meshmanager.api.message.ResultCodeE;
import org.onosproject.meshmanager.api.message.StatsReport;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("")
public class NbiWebResource extends AbstractWebResource {

    private final Logger log = LoggerFactory.getLogger(getClass());
    public boolean isUnitTest = false;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MeshService meshService;


    /**
     *
     */
    public NbiWebResource() {
        super();

        isUnitTest = false;

        // Retrieve mesh service instance
        meshService = get(MeshService.class);
    }

    public NbiWebResource(boolean isUnitTest) {
        this.isUnitTest = isUnitTest;
    }

    /**
     * @param responseBuilder
     * @param headers
     * @return
     */
    private Response setResponseHeader(Response.ResponseBuilder responseBuilder, HttpHeaders headers) {
        String origin = "";
        if (!isUnitTest && headers.getRequestHeader("Origin") != null) {
            origin = headers.getRequestHeader("Origin").get(0);
        }
        log.info("RPC: Origin:: " + origin);
        return responseBuilder
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    /**
     * Removes the outer "input" part from json input
     *
     * @param incomingData
     * @return
     */
    private String parseIncomingData(String incomingData) {
        return incomingData.substring(incomingData.indexOf(":") + 1, incomingData.lastIndexOf("}"));
    }

    /**
     * @param headers
     * @param incomingData
     * @param type
     * @return
     */
    private Response processNBIRequest(@Context HttpHeaders headers,
                                       String incomingData, Class<?> type) {
        MeshNBIRequest req;
        ObjectMapper mapper = new ObjectMapper();

        log.info("headers:: " + headers);
        log.info("incomingData::  " + incomingData);

        // Parse JSON request
        try {
            req = (MeshNBIRequest) mapper.readValue(parseIncomingData(incomingData), type);
            log.info("request:: " + req);
        } catch (Exception e) {
            log.error("Req readValue exception:: " + e.getLocalizedMessage());
            return setResponseHeader(Response.status(Response.Status.BAD_REQUEST)
                                             .entity("{\"error\" : \"" + e.getLocalizedMessage() + "\"}"), headers);
        }

        // Validate parsed MeshNBIMessage
        if (!req.validate()) {
            log.error("Missing input field");
            return setResponseHeader(Response.status(Response.Status.BAD_REQUEST)
                                             .entity("{\"error\" : \"Some of the input fields are missing.\"}"), headers);
        }

        // Send request to Mesh Manager and return response
        if (meshService != null) {
            try {
                log.info("Adding to mesh manager queue");
                MeshNBIResponse response = meshService.processNBIRequest(req);
                if (response != null) {

                    log.info("response:: " + response);
                    JsonNode node = mapper.valueToTree(response);
                    // wrap response in "output" json format
                    ObjectNode responseObj = mapper().createObjectNode().putPOJO("output", node);
                    return setResponseHeader(ok(responseObj), headers);
                } else {
                    log.error("response is null");
                }
            } catch (Exception e) {
                log.error("Exception while queuing message");
            }
        } else {
            log.error("Error in fetching meshmanager object");
        }

        return setResponseHeader(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                         .entity("{\"error\" : \"CustomerRegistrationOutput is empty due to internal error.\"}"), headers);
    }


    @OPTIONS
    @Path("/customer/registration")
    public Response customerRegistrationOptions(@Context HttpHeaders headers) {
        log.info("customerRegistrationOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/customer/registration")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response customerRegistration(@Context HttpHeaders headers, String incomingData) {
        log.info("customerRegistration");
        return processNBIRequest(headers, incomingData, MeshNBICustomerRegistrationReq.class);
    }

    @OPTIONS
    @Path("/customer/deregistration")
    public Response customerDeregistrationOptions(@Context HttpHeaders headers) {
        log.info("customerDeregistrationOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/customer/deregistration")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response customerDeregistration(@Context HttpHeaders headers, String incomingData) {
        log.info("customerDeregistration");
        return processNBIRequest(headers, incomingData, MeshNBICustomerDeregistrationReq.class);
    }

    @OPTIONS
    @Path("/slice/create")
    public Response createSliceOptions(@Context HttpHeaders headers) {
        log.info("createSliceOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/slice/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSlice(@Context HttpHeaders headers, String incomingData) {
        log.info("createSlice");
        return processNBIRequest(headers, incomingData, MeshNBICreateSliceReq.class);
    }

    @OPTIONS
    @Path("/slice/modify")
    public Response modifySliceOptions(@Context HttpHeaders headers) {
        log.info("modifySliceOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/slice/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifySlice(@Context HttpHeaders headers, String incomingData) {
        log.info("modifySlice");
        return processNBIRequest(headers, incomingData, MeshNBIModifySliceReq.class);
    }

    @OPTIONS
    @Path("/slice/delete")
    public Response deleteSliceOptions(@Context HttpHeaders headers) {
        log.info("deleteSliceOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/slice/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteSlice(@Context HttpHeaders headers, String incomingData) {
        log.info("deleteSlice");
        return processNBIRequest(headers, incomingData, MeshNBIDeleteSliceReq.class);
    }

    @OPTIONS
    @Path("/slice/config")
    public Response sliceConfigOptions(@Context HttpHeaders headers) {
        log.info("sliceConfigOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/slice/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSliceConfig(@Context HttpHeaders headers, String incomingData) {
        log.info("getSliceConfig");
        return processNBIRequest(headers, incomingData, MeshNBIGetSliceReq.class);
    }

    @OPTIONS
    @Path("/slice/statistics")
    public Response readStatisticsOptions(@Context HttpHeaders headers) {
        log.info("readStatisticsOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/slice/statistics")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response readStatistics(@Context HttpHeaders headers, String incomingData) {
        log.info("readStatistics");
        return processNBIRequest(headers, incomingData, MeshNBIReadStatsReq.class);
    }

    @OPTIONS
    @Path("/node/max-bandwidth")
    public Response maxBandwidthOptions(@Context HttpHeaders headers) {
        log.info("maxBandwidthOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/node/max-bandwidth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getMaxAvailableBandwidth(@Context HttpHeaders headers, String incomingData) {
        log.info("getMaxAvailableBandwidth");
        return processNBIRequest(headers, incomingData, MeshNBIGetMaxAvailableBWReq.class);
    }

    @OPTIONS
    @Path("/alerts")
    public Response alertsOptions(@Context HttpHeaders headers) {
        log.info("alertsOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/alerts")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAlerts(@Context HttpHeaders headers, String incomingData) {
        log.info("getAlerts");
        return processNBIRequest(headers, incomingData, MeshNBIGetAlertsReq.class);
    }

    @OPTIONS
    @Path("/node/info")
    public Response nodeInfoOptions(@Context HttpHeaders headers) {
        log.info("nodeInfoOptions: headers:: " + headers);
        return setResponseHeader(Response.ok(), headers);
    }

    @POST
    @Path("/node/info")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getNodeInfo(@Context HttpHeaders headers, String incomingData) {
        log.info("getNodeInfo");
        return processNBIRequest(headers, incomingData, MeshNBIGetNodeInfoReq.class);
    }

}

/*
 * Copyright 2017-present Open Networking Laboratory
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
package org.onosproject.meshnbi;

import org.easymock.EasyMock;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.osgi.ServiceDirectory;
import org.onlab.osgi.TestServiceDirectory;
import org.onlab.rest.BaseResource;
import org.onosproject.meshmanager.MeshService;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class AppComponentTest extends JerseyTest {

    private NbiComponent component;
    NbiWebResource nbiWebResource;

    final MeshService mockMeshService = EasyMock.createMock(MeshService.class);
    final HttpHeaders mockHttpHeaders = EasyMock.createMock(HttpHeaders.class);

    public AppComponentTest() {
        super(ResourceConfig.forApplicationClass(NbiWebApplication.class));
    }

    @Before
    public void setUpTest() {
        System.out.println("setUpTest :: START");
        nbiWebResource = new NbiWebResource(true);
        nbiWebResource.meshService = mockMeshService;

        ServiceDirectory testDirectory =
                new TestServiceDirectory()
                        .add(MeshService.class, mockMeshService);
        BaseResource.setServiceDirectory(testDirectory);
        System.out.println("setUpTest :: END");
    }

    @After
    public void tearDown() {
        /*component.deactivate();*/
    }

    @Test
    public void nbiRpcTest() {
//        //customerRegistration
//        Object obj = new CustomerRegistrationOutput();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        //customerDeregistration
//        obj = new Object();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        //createSlice
//        obj = new CreateSliceOutput();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        //modifySlice
//        obj = new Object();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        //deleteSlice
//        obj = new DeleteSliceOutput();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        //getSliceConfig
//        obj = new GetSliceOutput();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        //readStatistics
//        obj = new Object();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        //getMaxAvailableBandwidth
//        obj = new Object();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        //getAlerts
//        obj = new Object();
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(obj).times(1);
//
//        mockMeshService.getLastNbiResponse();
//        expectLastCall().andReturn(null).times(1);
//
//        mockMeshService.addToMsgQ(anyObject());
//        expectLastCall().andReturn(true).anyTimes();
//        replay(mockMeshService);

        System.out.println("Running basic test for customerRegistration");

        nbiWebResource.meshService = mockMeshService;
        String s = "{\"input\":{\"customerId\":1,\"password\":\"root\",\"apiVersion\":3}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.customerRegistration(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"customerId\":1,\"password\":\"root\",\"apiVersion\":3}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.customerRegistration(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"customerId\":1,\"password\":\"root\",\"apiVersion\":999}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.customerRegistration(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"customerId\":1,\"password\":\"root\"}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.customerRegistration(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;

        s = "{\"input\":{\"customerId\":1,\"password\":\"root\",\"apiVersion\":3}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.customerRegistration(mockHttpHeaders, s))).getStatus());

        /*************************************************************/
        System.out.println("Running basic test for customerDeregistration");

        nbiWebResource.meshService = mockMeshService;
        s = "{\"input\":{\"sessionId\":1234}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.customerDeregistration(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1234}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.customerDeregistration(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1011111111111111111111111111111111111111}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.customerDeregistration(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.customerDeregistration(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;
        s = "{\"input\":{\"sessionId\":1234}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.customerDeregistration(mockHttpHeaders, s))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for createSlice");

        nbiWebResource.meshService = mockMeshService;
        s = "{\"input\":{\"sessionId\":2,\"poaId\":2236962,\"nodeId\":2,\"sla\":{\"bwReqUl\":{\"cir\":200,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":140,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":7},\"statsConfig\":{\"statsReporting\":0,\"accumulationInterval\":0,\"reportingInterval\":0}}}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.createSlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":2,\"poaId\":2236962,\"nodeId\":2,\"sla\":{\"bwReqUl\":{\"cir\":200,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":140,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":7},\"statsConfig\":{\"statsReporting\":0,\"accumulationInterval\":0,\"reportingInterval\":0}}}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.createSlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":2,\"poaId\":2236962,\"nodeId\":2,\"sla\":{\"bwReqUl\":{\"cir\":200,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":140,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":999},\"statsConfig\":{\"statsReporting\":0,\"accumulationInterval\":0,\"reportingInterval\":0}}}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.createSlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":2,\"poaId\":2236962,\"sla\":{\"bwReqUl\":{\"cir\":200,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":140,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":7},\"statsConfig\":{\"statsReporting\":0,\"accumulationInterval\":0,\"reportingInterval\":0}}}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.createSlice(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;
        s = "{\"input\":{\"sessionId\":2,\"poaId\":2236962,\"nodeId\":2,\"sla\":{\"bwReqUl\":{\"cir\":200,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":140,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":7},\"statsConfig\":{\"statsReporting\":0,\"accumulationInterval\":0,\"reportingInterval\":0}}}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.createSlice(mockHttpHeaders, s))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for modifySlice");

        nbiWebResource.meshService = mockMeshService;
        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":101,\"sla\":{\"bwReqUl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":7},\"statsConfig\":{\"statsReporting\":1,\"accumulationInterval\":60,\"reportingInterval\":60}}}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.modifySlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":101,\"sla\":{\"bwReqUl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":7},\"statsConfig\":{\"statsReporting\":1,\"accumulationInterval\":60,\"reportingInterval\":60}}}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.modifySlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":101,\"sla\":{\"bwReqUl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":999},\"statsConfig\":{\"statsReporting\":1,\"accumulationInterval\":60,\"reportingInterval\":60}}}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.modifySlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":101,\"sla\":{\"bwReqUl\":{\"cir\":100,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":7},\"statsConfig\":{\"statsReporting\":1,\"accumulationInterval\":60,\"reportingInterval\":60}}}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.modifySlice(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;
        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":101,\"sla\":{\"bwReqUl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"bwReqDl\":{\"cir\":100,\"cbs\":0,\"eir\":0,\"ebs\":0,\"commMaxLatency\":0,\"commMaxJitter\":0,\"commMaxFlr\":0},\"priorityConfig\":{\"priority\":7},\"statsConfig\":{\"statsReporting\":1,\"accumulationInterval\":60,\"reportingInterval\":60}}}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.modifySlice(mockHttpHeaders, s))).getStatus());


        /*************************************************************/
        System.out.println("Running basic test for deleteSlice");
        nbiWebResource.meshService = mockMeshService;
        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":101}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.deleteSlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":101}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.deleteSlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":1011111111111111111111111111111111111111}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.deleteSlice(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.deleteSlice(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;
        s = "{\"input\":{\"sessionId\":1231,\"sliceId\":101}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.deleteSlice(mockHttpHeaders, s))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for getSliceConfig :: Correct");
        nbiWebResource.meshService = mockMeshService;
        s = "{\"input\":{\"sessionId\":1231,\"type\":1,\"sliceId\":101}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.getSliceConfig(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"type\":1,\"sliceId\":101}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.getSliceConfig(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"type\":1,\"sliceId\":1011111111111111111111111111111111111111}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.getSliceConfig(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"type\":1,\"sliceId\":101}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.getSliceConfig(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;
        s = "{\"input\":{\"sessionId\":1231,\"type\":1,\"sliceId\":101}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.getSliceConfig(mockHttpHeaders, s))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for readStatistics");
        nbiWebResource.meshService = mockMeshService;
        s = "{\"input\":{\"sessionId\":1231,\"type\":1,\"sliceId\":101}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.readStatistics(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"type\":1,\"sliceId\":101}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.readStatistics(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1231,\"type\":1,\"sliceId\":1011111111111111111111111111111111111111}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.readStatistics(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"type\":1,\"sliceId\":101}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.readStatistics(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;
        s = "{\"input\":{\"sessionId\":1231,\"type\":1,\"sliceId\":101}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.readStatistics(mockHttpHeaders, s))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for getMaxAvailableBandwidth");
        nbiWebResource.meshService = mockMeshService;
        s = "{\"input\":{\"sessionId\":1234,\"nodeId\":2}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.getMaxAvailableBandwidth(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1234,\"nodeId\":2}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.getMaxAvailableBandwidth(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1011111111111111111111111111111111111111,\"nodeId\":2}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.getMaxAvailableBandwidth(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1234}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.getMaxAvailableBandwidth(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;
        s = "{\"input\":{\"sessionId\":1234,\"nodeId\":2}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.getMaxAvailableBandwidth(mockHttpHeaders, s))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for getAlerts");
        nbiWebResource.meshService = mockMeshService;
        s = "{\"input\":{\"sessionId\":1,\"type\":1,\"alertId\":101}}";
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.getAlerts(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1,\"type\":1,\"alertId\":101}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.getAlerts(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1,\"type\":1011111111111111111111111111111111111111,\"alertId\":101}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.getAlerts(mockHttpHeaders, s))).getStatus());

        s = "{\"input\":{\"sessionId\":1,\"alertId\":101}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                     ((Response) (nbiWebResource.getAlerts(mockHttpHeaders, s))).getStatus());

        nbiWebResource.meshService = null;
        s = "{\"input\":{\"sessionId\":1,\"type\":1,\"alertId\":101}}";
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                     ((Response) (nbiWebResource.getAlerts(mockHttpHeaders, s))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for customerRegistration OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.customerRegistrationOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for customerDeregistration OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.customerDeregistrationOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for createSlice OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.createSliceOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for modifySlice OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.modifySliceOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for deleteSlice OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.deleteSliceOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for getSliceConfig :: OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.sliceConfigOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for readStatistics OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.readStatisticsOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for getMaxAvailableBandwidth OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.maxBandwidthOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
        System.out.println("Running basic test for getAlerts OPTIONS");
        assertEquals(Response.Status.OK.getStatusCode(),
                     ((Response) (nbiWebResource.alertsOptions(mockHttpHeaders))).getStatus());
        /*************************************************************/
    }
}
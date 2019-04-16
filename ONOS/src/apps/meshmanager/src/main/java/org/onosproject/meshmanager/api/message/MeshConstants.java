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

package org.onosproject.meshmanager.api.message;

public class MeshConstants {
    public static final int TYPE_SRC_MAC = 0;
    public static final int TYPE_DST_MAC = 1;

    /****** debug levels *******/
    public static final byte ERROR = 0;
    public static final byte WARNING = 1;
    public static final byte INFO = 2;
    public static final byte DEBUG = 3;
    public static final byte TRACE = 4;

    /****** default temporary timers for flow rules *******/
    public static final int DEFAULT_TEMPORARY_TIMER = 15;

    /****** fwd table entries index *******/
    public static final int NB_OF_SIGNALING_ENTRIES = 190;
    public static final int NB_OF_DATA_ENTRIES = 190;
    public static final int NB_OF_ACCESS_NODE_PER_MESH_NODE = 10;
    /****** range 10-199 *******/
    public static final int CATCH_ALL_TO_NC_ENTRY_ID = 10;
    public static final int STARTING_RESERVED_GW_ENTRY_ID = CATCH_ALL_TO_NC_ENTRY_ID + 1;
    public static final int ENDING_RESERVED_GW_ENTRY_ID = STARTING_RESERVED_GW_ENTRY_ID - 2 + NB_OF_SIGNALING_ENTRIES; //starts at entry 11, not 10.. althought values are from 10-199
    /****** range 200-389 *******/
    public static final int STARTING_NON_RESERVED_ENTRY_ID = ENDING_RESERVED_GW_ENTRY_ID + 1;
    public static final int ENDING_NON_RESERVED_ENTRY_ID = STARTING_NON_RESERVED_ENTRY_ID - 1 + NB_OF_SIGNALING_ENTRIES; //allowed nodes in the network is STARTING_NON_RESERVED_ENTRY_ID - STARTING_RESERVED_GW_ENTRY_ID -1
    /****** range 400-409 - reserved for 10 access nodes support per mesh node *******/
    public static final int STARTING_RESERVED_OWN_ACCESS_NODE_UL_ENTRY_ID = ENDING_NON_RESERVED_ENTRY_ID + 1 + NB_OF_ACCESS_NODE_PER_MESH_NODE; //400
    public static final int ENDING_RESERVED_OWN_ACCESS_NODE_UL_ENTRY_ID = STARTING_RESERVED_OWN_ACCESS_NODE_UL_ENTRY_ID -1 + NB_OF_ACCESS_NODE_PER_MESH_NODE; //409
    /****** range 410-599 *******/
    public static final int STARTING_RESERVED_OTHER_ACCESS_NODE_UL_ENTRY_ID = ENDING_RESERVED_OWN_ACCESS_NODE_UL_ENTRY_ID + 1;
    public static final int ENDING_RESERVED_OTHER_ACCESS_NODE_UL_ENTRY_ID = STARTING_RESERVED_OTHER_ACCESS_NODE_UL_ENTRY_ID - 1 + NB_OF_DATA_ENTRIES;
    /****** range 600-789 *******/
    public static final int STARTING_RESERVED_OWN_ACCESS_NODE_DL_ENTRY_ID = ENDING_RESERVED_OTHER_ACCESS_NODE_UL_ENTRY_ID + 1; //600
    public static final int ENDING_RESERVED_OWN_ACCESS_NODE_DL_ENTRY_ID = STARTING_RESERVED_OWN_ACCESS_NODE_DL_ENTRY_ID - 1 + NB_OF_ACCESS_NODE_PER_MESH_NODE; //600
    public static final int STARTING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID = ENDING_RESERVED_OWN_ACCESS_NODE_DL_ENTRY_ID + 1; //610 - assuming at most 10 access nodes per Mesh node
    public static final int ENDING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID = STARTING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID - 1 + NB_OF_DATA_ENTRIES;

    /****** important ranges *******/
    public static final int STARTING_UPLINK_SIGNALING_ENTRY_ID = CATCH_ALL_TO_NC_ENTRY_ID;
    public static final int ENDING_UPLINK_SIGNALING_ENTRY_ID = ENDING_RESERVED_GW_ENTRY_ID;
    public static final int STARTING_DOWNLINK_SIGNALING_ENTRY_ID = STARTING_NON_RESERVED_ENTRY_ID;
    public static final int ENDING_DOWNLINK_SIGNALING_ENTRY_ID = ENDING_NON_RESERVED_ENTRY_ID;
    public static final int STARTING_UPLINK_DATA_ENTRY_ID = STARTING_RESERVED_OTHER_ACCESS_NODE_UL_ENTRY_ID;
    public static final int ENDING_UPLINK_DATA_ENTRY_ID = ENDING_RESERVED_OTHER_ACCESS_NODE_UL_ENTRY_ID;
    public static final int STARTING_DOWNLINK_DATA_ENTRY_ID = STARTING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID;
    public static final int ENDING_DOWNLINK_DATA_ENTRY_ID = ENDING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID;


    /*past values
    public static final int CATCH_ALL_TO_NC_ENTRY_ID = 10;
    public static final int STARTING_RESERVED_GW_ENTRY_ID = 11;
    public static final int ENDING_RESERVED_GW_ENTRY_ID = 199;
    public static final int STARTING_NON_RESERVED_ENTRY_ID = 200;
    public static final int ENDING_NON_RESERVED_ENTRY_ID = 390; //allowed nodes in the network is STARTING_NON_RESERVED_ENTRY_ID - STARTING_RESERVED_GW_ENTRY_ID -1
    public static final int STARTING_UPLINK_ENTRY_ID = CATCH_ALL_TO_NC_ENTRY_ID;
    public static final int ENDING_UPLINK_ENTRY_ID = ENDING_RESERVED_GW_ENTRY_ID;
    public static final int STARTING_DOWNLINK_ENTRY_ID = STARTING_NON_RESERVED_ENTRY_ID;
    public static final int ENDING_DOWNLINK_ENTRY_ID = ENDING_NON_RESERVED_ENTRY_ID;

    public static final int STARTING_RESERVED_OWN_ACCESS_NODE_UL_ENTRY_ID = ENDING_NON_RESERVED_ENTRY_ID + 10; //400
    public static final int STARTING_RESERVED_OTHER_ACCESS_NODE_UL_ENTRY_ID = STARTING_RESERVED_OWN_ACCESS_NODE_UL_ENTRY_ID + 10; // Assuming at most 10 Access Nodes per Mesh Node
    public static final int STARTING_RESERVED_OWN_ACCESS_NODE_DL_ENTRY_ID = STARTING_RESERVED_OWN_ACCESS_NODE_UL_ENTRY_ID + 200; //600
    public static final int STARTING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID = STARTING_RESERVED_OWN_ACCESS_NODE_UL_ENTRY_ID + 210; //610 - assuming at most 10 access nodes per Mesh node
    public static final int ENDING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID = STARTING_RESERVED_OWN_ACCESS_NODE_DL_ENTRY_ID + 200; //800
*/
    /****** fwd table entries cookies *******/
    public static final short ODL_FLOW_COOKIE_SELF = 1;
    public static final short ODL_FLOW_COOKIE_DEFAULT_DROP = 2;
    public static final short ODL_FLOW_COOKIE_ARP = 3;
    public static final short ODL_FLOW_COOKIE_LLDP = 4;
    public static final short ODL_FLOW_COOKIE_TO_OUTSIDE_GW = 5;
    public static final short ODL_FLOW_COOKIE_POP_VLAN_ON_GW = 6;
    public static final short ODL_FLOW_COOKIE_FROM_LEAF_ON_GW = 7;
    public static final short ODL_FLOW_COOKIE_SELF_POP_VLAN = 8;
//    public static final short ODL_FLOW_COOKIE_POP_VLAN_ON_GW_FOR_ACCESS_NODE = 9;
    public static final short ODL_FLOW_COOKIE_GENERIC_PRIORITY = 1000;
    public static final short ODL_FLOW_COOKIE_VLAN_PRIORITY_INCREMENT = 100;
    public static final short ODL_FLOW_COOKIE_GW_FLOOD_PRIORITY = 100;
    public static final short ODL_FLOW_COOKIE_HIGHEST_BASIC_PRIORITY = 10000;
    public static final short ODL_FLOW_COOKIE_LOW_PRIORITY = 5;
    public static final short ODL_FLOW_COOKIE_PACKET_IN_PRIORITY = 4;
    public static final short ODL_FLOW_COOKIE_LOWEST_PRIORITY = 3;
    public static final short ODL_FLOW_COOKIE_ARP_PRIORITY = ODL_FLOW_COOKIE_LOW_PRIORITY;
    public static final short ODL_FLOW_COOKIE_LLDP_PRIORITY = ODL_FLOW_COOKIE_LOW_PRIORITY;
    public static final short ODL_FLOW_COOKIE_SUBNET_SELF_PRIORITY = ODL_FLOW_COOKIE_GENERIC_PRIORITY;
//    public static final short ODL_FLOW_COOKIE_DATAPATH_PRIORITY = ODL_FLOW_COOKIE_GENERIC_PRIORITY;
    public static final short ODL_FLOW_COOKIE_SELF_PRIORITY = 800;
    public static final short ODL_FLOW_COOKIE_SELF_POP_PRIORITY = ODL_FLOW_COOKIE_SELF_PRIORITY + 10;
    public static final short ODL_FLOW_COOKIE_FROM_GW_SELF_PRIORITY = 10000;

    /****** fwd rule direction *******/
    public static final short UPLINK = 0;
    public static final short DOWNLINK = 1;

    public static final short ENTRY_IDLE_TIMEOUTS = 5; //in seconds. Meant for temporary entries that need to be deleted after some time

    public static final byte MISSED_PERIODIC_REPORT_ALLOWED = 100; //seems like awful lot of time, basically, not to ever kick in

    /*** slice manager constants ***/
    public static final short SLICE_MANAGER_FIRST_AVAILABLE_SESSION_ID = 1;
    public static final short SLICE_MANAGER_FIRST_AVAILABLE_SLICE_ID = 1;

    /****** routing manager constants *******/
    public static final short MIN_AVAILABLE_VLAN_ID = 1;
    public static final short MAX_AVAILABLE_VLAN_ID = 4095;

    public static final short NC_NBI_UNDEFINED = 0;
    public static final short NC_NBI_OK = 1;
    public static final short NC_NBI_INVALID_CUSTOMER_ID = 2;
    public static final short NC_NBI_INVALID_PASSWORD = 3;
    public static final short NC_NBI_VERSION_NOT_SUPPORTED = 4;
    public static final short NC_NBI_INVALID_SESSION_ID = 5;
    public static final short NC_NBI_INVALID_NODE_ID = 6;
    public static final short NC_NBI_INVALID_POA_ID = 7;
    public static final short NC_NBI_CANNOT_MEET_SLA = 8;
    public static final short NC_NBI_INVALID_SLICE_ID = 9;
    public static final short NC_NBI_INVALID_SLA = 10;
    public static final short NC_NBI_INVALID_ALERT_ID = 11;
    public static final short NC_NBI_INTERNAL_ERROR_VLAN_ALREADY_ALLOCATED =12;
    public static final short NC_NBI_INTERNAL_ERROR_MEMORY_ALLOCATION =12;

    public MeshConstants() {
    }
}


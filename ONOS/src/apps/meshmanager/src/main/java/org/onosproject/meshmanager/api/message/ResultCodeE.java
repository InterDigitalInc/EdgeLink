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

public enum ResultCodeE {

    NC_NBI_OK(1),

    NC_NBI_INVALID_CUSTOMER_ID(2),

    NC_NBI_INVALID_PASSWORD(3),

    NC_NBI_INVALID_SESSION_ID(4),

    NC_NBI_INVALID_NODE_ID(5),

    NC_NBI_INVALID_POA_ID(6),

    NC_NBI_INVALID_SLICE_ID(7),

    NC_NBI_INVALID_SLA(8),

    NC_NBI_INVALID_ALERT_ID(9),

    NC_NBI_CANNOT_MEET_SLA(10),

    NC_NBI_VERSION_NOT_SUPPORTED(11),

    NC_NBI_OTHER_ERROR(12);

    int value;

    private ResultCodeE(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

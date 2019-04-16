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
package org.onosproject.meshmanager;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

public class MeshAlert {
    private final Logger logger = getLogger(getClass());

    private short alertNumericalValue;
    private String alertStringValue;
    private byte state;
    private long alertInfo;

    /*** alerts type ***/
    public static final short NC_NBI_SLA_NOT_MET_ALERT = 1;

    /*** alerts string ***/
    public static final String NC_NBI_SLA_NOT_MET_ALERT_STRING = "SLA NOT MET";

    /*** alerts states ***/
    public static final byte CLEARED = 0;
    public static final byte RAISED = 1;

    public MeshAlert(short value, String string, byte state, long alertInfo) {
        this.alertNumericalValue = value;
        if(string == null) {
            this.alertStringValue = "Alert";
        }
        else {
            this.alertStringValue = string;
        }
        this.state = state;
        this.alertInfo = alertInfo;
    }

    public Short getAlertValue() {
        return alertNumericalValue;
    }

    public String getAlertStringValue() {
        return alertStringValue;
    }

    public byte getState() {
        return state;
    }

    public long getAlertInfo() {
        return alertInfo;
    }

    public String toString() {
        String myString = this.getAlertStringValue() + "(" + this.alertNumericalValue + ")-";
        switch(this.state) {
        case CLEARED:
            myString += "CLEARED";
            break;
        case RAISED:
            myString += "RAISED";
            break;
        default:
            myString += "UNDEFINED(" + this.state + ")";
            break;
        }
        myString += ":" + alertInfo;
        return myString;
    }

}

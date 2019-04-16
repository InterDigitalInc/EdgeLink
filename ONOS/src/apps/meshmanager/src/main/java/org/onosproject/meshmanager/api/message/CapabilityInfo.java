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

public class CapabilityInfo {
    private byte nodeAttributes;
    private byte mcsSupport;
    private byte nbBuf;
    private int bufferSize;

    public static byte NON_GW_NODEROLE = 0x00; //nodeRoleBit not set
    public static byte GW_NODEROLE = 0x01;
    public static byte TESTMODE = (byte) 0x80;
    public static byte DEPLOYMENTMODE = 0x00; //modeTypeBit not set
    public static byte NODEROLE_MASK = 0x01;
    public static byte MODETYPE_MASK = (byte) 0x80;

    public CapabilityInfo(byte nodeAttributes, byte mcsSupport, byte nbBuf, int bufferSize) {
        this.nodeAttributes = nodeAttributes;
        this.mcsSupport = mcsSupport;
        this.nbBuf = nbBuf;
        this.bufferSize = bufferSize;
    }

    public byte getNodeAttributes() {
        return this.nodeAttributes;
    }

    public byte getMcsSupport() {
        return this.mcsSupport;
    }

    public byte getNbBuf() {
        return this.nbBuf;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public boolean isGwNodeRole()
    {
        if((nodeAttributes & NODEROLE_MASK) == GW_NODEROLE) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isTestMode()
    {
        if((nodeAttributes & MODETYPE_MASK) == TESTMODE) {
            return true;
        } else {
            return false;
        }
    }
}


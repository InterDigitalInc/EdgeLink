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

import java.nio.ByteBuffer;

/**
 *
 */
public class TlvIe {
    public static int IE_HDR_LEN = 3;

    private static int IE_LEN_MAX = 200;
    private static int LONG_SIZE = 8;
    private static int INT_SIZE = 4;
    private static int SHORT_SIZE = 2;
    private static int BYTE_SIZE = 1;

    private byte type;
    private int length; // Payload length
    private ByteBuffer payload;

    /**
     *
     * @param type
     */
    public TlvIe(byte type) {
        this.type = type;
        length = 0;
        payload = ByteBuffer.allocateDirect(IE_LEN_MAX);
    }

    /**
     *
     * @return
     */
    public byte getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public int getLength() {
        return length;
    }

    /**
     *
     * @return
     */
    public ByteBuffer getPayload(){
        return payload;
    }

    /**
     *
     * @param value
     */
    public void append(long value) {
       payload.putLong(value);
       length += LONG_SIZE; //sizeof long
    }

    /**
     *
     * @param value
     */
    public void append(short value) {
       payload.putShort(value);
       length += SHORT_SIZE; //sizeof short
    }

    /**
     *
     * @param value
     */
    public void append(int value) {
       payload.putInt(value);
       length += INT_SIZE; //sizeof int
    }

    /**
     *
     * @param value
     */
    public void append(byte value) {
       payload.put(value);
       length += BYTE_SIZE; //sizeof byte
    }

}


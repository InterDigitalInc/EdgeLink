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

/**
 *
 */
public class BufferReport {
    private short avgPktDelay;
    private byte avgBufferOccupancy;

    protected static final short DEFAULT_AVG_PKT_DELAY = 1000;
    protected static final byte DEFAULT_AVG_BUFFER_OCCUPANCY = 80;

    /**
     *
     */
    public BufferReport() {
    }

    /**
     *
     * @return
     */
    public short getAvgPktDelay() {
        return this.avgPktDelay;
    }

    /**
     *
     * @param value
     */
    public void setAvgPktDelay(short value) {
        this.avgPktDelay = value;
    }

    /**
     *
     * @return
     */
    public byte getAvgBufferOccupancy() {
        return this.avgBufferOccupancy;
    }

    /**
     *
     * @param value
     */
    public void setAvgBufferOccupancy(byte value) {
        this.avgBufferOccupancy = value;
    }

    /**
     * Returns a summary of the bufferReport
     * @return
     */
    public String toString() {
        return "\n\t\tbufferReport" +
            "\n\t\tavgPktDelay=" + avgPktDelay +
            "\n\t\tavgBufferOccupancy=" + avgBufferOccupancy;
    }
}


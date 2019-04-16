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


import org.jboss.netty.buffer.ChannelBuffer;
import org.onlab.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MeshNCConfigConfirm extends MeshSBIMessage {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private List<ResultCode> resultCodes = new ArrayList<>();
    private boolean isCompleteMessage;


    /**
     *
     */
    public MeshNCConfigConfirm() {
        super();
        this.type = MeshSBIMessageType.NC_CONFIG_CONFIRM;
    }

    /**
     *
     * @return
     */
    public ResultCode getFirstResultCode() {
        if (resultCodes.isEmpty()) {
            return null;
        }
        return resultCodes.get(0);
    }

    /**
     *
     * @return
     */
    public List<ResultCode> getResultCodes() {
        return resultCodes;
    }


    /**
     *
     * @return
     */
    @Override
    public boolean isCompleteMessage() {
        return isCompleteMessage;
    }

    /**
     *
     * @param data
     */
    @Override
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);
        int length = getLength() - SBI_HDR_LEN;
        if (length > 0) {
            log.trace("MeshNCConfig::readfrom: payload found payload length:" + length);
            decode(data);
        }
    }

    /**
     *
     * @param payload
     */
    private void decode(ChannelBuffer payload) {
        int sizeRead = SBI_HDR_LEN;
        int msgLength = getLength();
        isCompleteMessage = true;

        while ((payload.readable()) && (sizeRead < msgLength)) {
            sizeRead += decodeNextIE(payload);
        }
        if (!payload.readable() && (sizeRead < msgLength)) {
            log.trace("Incomplete message :: sizeRead:" + sizeRead + " , msgLength:" + msgLength);
            isCompleteMessage = false;
        }
    }

    /**
     *
     * @param payload
     * @return
     */
    private int decodeNextIE(ChannelBuffer payload) {
        byte ieType, result=0;
        short currentIeSize;
        long mac =0;
        ResultCode resultCode;
        ieType = payload.readByte();
        currentIeSize = payload.readShort();
        switch(ieType) {
            case IE_RESULT_CODE:
                result = payload.readByte();
                //read mac address
                mac = payload.readLong();
                resultCode = new ResultCode(result, mac);
                resultCodes.add(resultCode);
                break;
            default:
                //ie not supported.
                break;
        }
        log.trace("decodeNextIE: result:{}, length: {} sectormac {}", result, currentIeSize,
                  HexString.toHexString(mac));
        return (currentIeSize + TlvIe.IE_HDR_LEN); // Ie length excludes header.
    }

}


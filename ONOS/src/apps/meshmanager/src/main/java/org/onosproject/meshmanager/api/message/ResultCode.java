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
public class ResultCode {
    private byte result;
    private Long sectorMac;

    public static final byte RESULT_SUCCESS = 0;
    public static final byte RESULT_MESSAGE_PARSE_FAIL = 1;
    public static final byte RESULT_INVALID_TABLE_CONFIG = 2;
    public static final byte RESULT_INVALID_LINK_REPORT_CONFIG = 3;
    public static final byte RESULT_INVALID_BUFFER_REPORT_CONFIG = 4;
    public static final byte RESULT_TABLE_MEMORY_FULL = 5;
    public static final byte RESULT_ASSOC_FAILED = 6;
    public static final byte RESULT_CODE_MAX = 6;

    /**
     *
     * @param result
     * @param mac
     */
    public ResultCode(byte result, Long mac) {
        this.result = result;
        this.sectorMac = mac;
    }

    /**
     *
     * @return
     */
    public Long getSectMac() {
        return this.sectorMac;
    }

    /**
     *
     * @return
     */
    public String getResultString() {
        switch(this.result) {
        case RESULT_SUCCESS:
            return "SUCCESS";
        case RESULT_MESSAGE_PARSE_FAIL:
            return "MESSAGE_PARSE_FAIL";
        case RESULT_INVALID_TABLE_CONFIG:
            return "INVALID_TABLE_CONFIG";
        case RESULT_INVALID_LINK_REPORT_CONFIG:
            return "INVALID_LINK_REPORT_CONFIG";
        case RESULT_TABLE_MEMORY_FULL:
            return "TABLE_MEMORY_FULL";
        case RESULT_ASSOC_FAILED:
            return "ASSOC_FAILED";
        default:
            return "BEYONG POSSIBLE VALUES";
        }
    }

    /**
     *
     * @return
     */
    public byte getResult() {
        return result;
    }

    /**
     *
     * @param result
     */
    public void setResult(byte result) {
        this.result = result;
    }
}


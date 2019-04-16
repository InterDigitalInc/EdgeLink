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
public class LinkReportInfo {
    private Long ownSectMac;
    private Long peerSectMac;
    private LinkReport linkReport;

    /**
     *
     */
    public LinkReportInfo() {
    }

    /**
     *
     * @return
     */
    public Long getOwnSectMac() {
        return ownSectMac;
    }

    /**
     *
     * @param ownSectMac
     */
    public void setOwnSectorMAC(Long ownSectMac) {
        this.ownSectMac = ownSectMac;
    }

    /**
     *
     * @return
     */
    public Long getPeerSectMac() {
        return peerSectMac;
    }

    /**
     *
     * @param peerSectMac
     */
    public void setPeerSectorMAC(Long peerSectMac) {
        this.peerSectMac = peerSectMac;
    }

    /**
     *
     * @return
     */
    public LinkReport getLinkReport() {
        return linkReport;
    }

    /**
     *
     * @param linkReport
     */
    public void setLinkReport(LinkReport linkReport) {
        this.linkReport = linkReport;
    }

    /**
     *
     * @return Summary of the linkReportInfo
     */
    public String toString() {
        String myString = "\n\t\t\tlinkReportInfo" +
                "\n\t\t\townSectMac=" + ownSectMac +
                "\n\t\t\tpeerSectMac=" + peerSectMac +
                "\n\t\t\tlinkReport=" + linkReport.toString();
        return myString;
    }
}

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

import java.math.BigInteger;

public class SliceInfo {
    private Long   poaId;
    private Short  sliceId;
    private Sla    sla;


    public Long getPoaId() {
        return poaId;
    }

    public void setPoaId(Long poaId) {
        if (poaId < 0) {
            throw new IllegalArgumentException("SliceInfo: poaId should be non-negative.");
        }
        this.poaId = poaId;
    }

    public Short getSliceId() {
        return sliceId;
    }

    public void setSliceId(Short sliceId) {
        if (sliceId < 0) {
            throw new IllegalArgumentException("SliceInfo: sliceId should be non-negative.");
        }
        this.sliceId = sliceId;
    }

    public Sla getSla() {
        return sla;
    }

    public void setSla(Sla sla) {
        this.sla = sla;
    }

    @Override
    public String toString() {
        return "SliceInfo [sliceId=" + sliceId + ", poaId=" + poaId + ", sla=" + sla + "]";
    }
}

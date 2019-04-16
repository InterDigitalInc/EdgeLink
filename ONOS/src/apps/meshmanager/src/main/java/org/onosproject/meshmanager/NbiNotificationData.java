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

import java.util.List;

public class NbiNotificationData {
    private Short type;
    private String status;
    private short count;
    private List<Integer> idList;

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public short getCount() {
        return count;
    }

    public void setCount(short count) {
        this.count = count;
    }

    public List<Integer> getIdList() {
        return idList;
    }

    public void setIdList(List<Integer> idList) {
        this.idList = idList;
    }

    @Override
    public String toString() {
        return "NbiNotificationData [type=" + type + ", status=" + status + ", count=" + count + ", idList=" + idList + "]";
    }
}

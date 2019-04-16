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

import org.onlab.util.HexString;
import java.util.ArrayList;
import java.util.List;

public class NodeInfo {
   private Long nodeId;
   private String nodeLocation;
   private Short numPoa;
   private List<Long> poaList;

   public Long getNodeId() {
       return nodeId;
   }

   public void setNodeId(Long nodeId) {
       this.nodeId = nodeId;
   }

   public String getNodeLocation() {
       return nodeLocation;
   }

   public void setNodeLocation(String nodeLocation) {
       this.nodeLocation = nodeLocation;
   }

   public Short getNumPoa() {
       return numPoa;
   }

   public void setNumPoa(Short numPoa) {
       this.numPoa = numPoa;
   }

   public List<Long> getPoaList() {
       return poaList;
   }

   public void setPoaList(List<Long> poaList) {
       this.poaList = poaList;
   }

   @Override
   public String toString() {
       String string;

       string =  "\n\t\t{ \n\t\t\tnodeId : " + HexString.toHexString(this.nodeId)
               + "\n\t\t\tnodeLocation : " + this.nodeLocation
               + "\n\t\t\tnumPoa : " + this.numPoa;
       if (this.numPoa == 0) {
           string += "\n\t\t\tpoaList: []";
       }
       else {
           string += "\n\t\t\tpoaList: [";
           for (Long poa : this.getPoaList()) {
               string += "\n\t\t\t\t" + poa;
           }
           string += "\n\t\t\t]";
       }
       string += "\n\t\t}";
       return string;
       }
}

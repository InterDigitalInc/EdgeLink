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
package org.onosproject.mesh.controller;

import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkArgument;
import static org.onlab.util.Tools.fromHex;
import static org.onlab.util.Tools.toHex;

/**
 * The class representing a network switch MeshDpid.
 * This class is immutable.
 */
public final class MeshDpid {

    private static final String SCHEME = "mesh";
    private static final long UNKNOWN = 0;
    private final long value;

    /**
     * Default constructor.
     */
    public MeshDpid() {
        this.value = MeshDpid.UNKNOWN;
    }

    /**
     * Constructor from a long value.
     *
     * @param value the value to use.
     */
    public MeshDpid(long value) {
        this.value = value;
    }

    /**
     * Get the value of the MeshDpid.
     *
     * @return the value of the MeshDpid.
     */
    public long value() {
        return value;
    }

    /**
     * Convert the MeshDpid value to a ':' separated hexadecimal string.
     *
     * @return the MeshDpid value as a ':' separated hexadecimal string.
     */
    @Override
    public String toString() {
       return toHex(this.value);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MeshDpid)) {
            return false;
        }
        return value == ((MeshDpid)other).value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    /**
     * Returns MeshDpid created from the given device URI.
     *
     * @param uri device URI
     * @return MeshDpid
     */
    public static MeshDpid meshDpid(URI uri) {
        checkArgument(uri.getScheme().equals(SCHEME), "Unsupported URI scheme");
        String schemeSpecificPart = uri.getSchemeSpecificPart().replaceAll(":", "");
        return new MeshDpid(fromHex(schemeSpecificPart));

    }

    /**
     * Produces device URI from the given MeshDpid.
     *
     * @param MeshDpid device MeshDpid
     * @return device URI
     */
    public static URI uri(MeshDpid MeshDpid) {
        return uri(MeshDpid.value);
    }

    /**
     * Produces device URI from the given MeshDpid long.
     *
     * @param value device MeshDpid as long
     * @return device URI
     */
    public static URI uri(long value) {
        try {
            return new URI(SCHEME, toHex(value), null);
        } catch (URISyntaxException e) {
            return null;
        }
    }

}

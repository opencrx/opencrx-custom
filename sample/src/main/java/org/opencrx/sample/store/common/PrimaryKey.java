/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: PrimaryKey
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openCRX team nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.opencrx.sample.store.common;

import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * Primary Key class is the primary key for all objects. It generates a globally unique
 * PROP_ID using RMI <code>UID</code> class and machines IP address.
 *
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class PrimaryKey implements Serializable, Comparable<Object> {
	
	public PrimaryKey()
    {
        uuid = getUUID();
    }

    public PrimaryKey(
        final String encodedUuid
    ) {
        this(
            encodedUuid,
            true
        );
    }
    
    public PrimaryKey(
        final String uuid,
        boolean decode
    ) {
        if(decode) {
            try {
                this.uuid = URLDecoder.decode(uuid, "UTF-8");
            }
            catch(Exception e) {
                this.uuid = uuid;
            }
        }
        else {
            this.uuid = uuid;
        }
    }

    public final boolean equals(final Object obj)
    {
        return equals((PrimaryKey) obj);
    }

    private boolean equals(final PrimaryKey obj)
    {
        return obj.uuid.equals(this.uuid);
    }

    public final int hashCode()
    {
        return uuid.hashCode();
    }

    public final String toString()
    {
        try {
            return URLEncoder.encode(uuid, "UTF-8");
        } 
        catch(Exception e) {
            return this.uuid;
        }
    }

    public final String getUuid(
    ) {
        return uuid;
    }
    
    private String getUUID()
    {
        return UUIDConversion.toUID(PrimaryKey.uuids.next());
    }
    
    public final int compareTo(final Object o)
    {
        return compareTo((PrimaryKey) o);
    }

    private int compareTo(final PrimaryKey o)
    {
        return (o.toString().compareTo(this.uuid));
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = 2759259048643764941L;

	public static final int LENGTH = 36;
    
    private static final UUIDGenerator uuids = UUIDs.getGenerator();

    private String uuid;
   
}
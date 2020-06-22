/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: Order
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2014, CRIXP Corp., Switzerland
 * All rights reserved.
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
 * * Neither the name of CRIXP Corp. nor the names of the contributors
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
package org.opencrx.sample.store.objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.kernel.account1.jmi1.Account;
import org.opencrx.sample.store.common.IStandardObject;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;
import org.opencrx.sample.store.common.util.Keys;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * Contains one order by a user
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class Order implements IStandardObject
{
	public Order(final PrimaryKey key)
    {
        Key = key;
    }

    public Order()
    {
    }

    public Order(
        org.opencrx.kernel.contract1.jmi1.SalesOrder salesOrder
    ) {
        this.Key = new PrimaryKey(salesOrder.refGetPath().getLastSegment().toString(), false);
        this.StartDate = salesOrder.getActiveOn();
        this.EndDate = salesOrder.getClosedOn();
        // Status
        switch(salesOrder.getContractState()) {
            case 400/* draft / in progress */:
                this.Status = STATUS_BUYING;
                break;
            case 430/* on hold */:
                this.Status = STATUS_PENDING;
                break;
            case 1410/* closed / won or next state */:
                this.Status = STATUS_DELIVERED;
                break;
            case 1450/* closed / cancelled */:
                this.Status = STATUS_CANCELLED;
                break;
        }
        // UserID
        this.UserID = null;
        if(salesOrder.getCustomer() != null) {
            this.UserID = new PrimaryKey(salesOrder.getCustomer().refGetPath().getLastSegment().toString(), false); 
        }
        // Address
        this.Address = "";
        org.opencrx.kernel.contract1.jmi1.PostalAddress postalAddress = null;
        Collection<org.opencrx.kernel.contract1.jmi1.ContractAddress> addresses = salesOrder.getAddress();
        for(org.opencrx.kernel.contract1.jmi1.ContractAddress address: addresses) {
            if(address instanceof org.opencrx.kernel.contract1.jmi1.PostalAddress) {
                postalAddress = (org.opencrx.kernel.contract1.jmi1.PostalAddress)address;
                break;
            }
        }
        if(postalAddress != null) {
            for(int i = 0; i < postalAddress.getPostalAddressLine().size(); i++) {
                this.Address += i == 0 ? "" : "\n";
                this.Address += postalAddress.getPostalAddressLine().get(i);
            }
        }
    }
    
    public void update(
        org.opencrx.kernel.contract1.jmi1.SalesOrder salesOrder,
        ApplicationContext context
    ) {        
    	PersistenceManager pm = JDOHelper.getPersistenceManager(salesOrder);
        UUIDGenerator uuids = UUIDs.getGenerator();
        org.opencrx.kernel.contract1.jmi1.PostalAddress postalAddress = null;
        // Find existing postal address
        if(JDOHelper.isPersistent(salesOrder)) {
        	Collection<org.opencrx.kernel.contract1.jmi1.ContractAddress> addresses = salesOrder.getAddress();
            for(org.opencrx.kernel.contract1.jmi1.ContractAddress address: addresses) {
                if(address instanceof org.opencrx.kernel.contract1.jmi1.PostalAddress) {
                    postalAddress = (org.opencrx.kernel.contract1.jmi1.PostalAddress)address;
                    break;
                }
            }
        }
        // Create
        if(postalAddress == null) {
            postalAddress = pm.newInstance(org.opencrx.kernel.contract1.jmi1.PostalAddress.class);
            salesOrder.addAddress(
                false,
                uuids.next().toString(),
                postalAddress
            );
        }
        postalAddress.getPostalAddressLine().clear();
        StringTokenizer t = new StringTokenizer(this.getAddress(), "\n\r", false);
        int ii = 0;
        while(t.hasMoreTokens() && (ii < 3)) {
            postalAddress.getPostalAddressLine().add(
                t.nextToken()
            );
            ii++;
        }
        // Customer
        Account customer = context.getAccountSegment(pm).getAccount(this.getUserID().toString());
        salesOrder.setCustomer(
            customer
        );
        // Name
        salesOrder.setName(
            Keys.STORE_SCHEMA + customer.getFullName() + "#" + this.getStartDate()
        );        
        // Status
        switch(this.getStatus()) {
            case STATUS_BUYING:
                salesOrder.setContractState((short)400/* draft / in progress */);
                break;
            case STATUS_PENDING:
                salesOrder.setContractState((short)430/* on hold */);
                break;
            case STATUS_DELIVERED:
                salesOrder.setContractState((short)1410/* closed / won or next state */);
                break;
            case STATUS_CANCELLED:
                salesOrder.setContractState((short)1450/* closed / cancelled */);
                break;
        }
        // Dates
        salesOrder.setActiveOn(
            this.getStartDate()
        );
        salesOrder.setClosedOn(
            this.getEndDate()
        );
        // Currency
        salesOrder.setContractCurrency(
            context.getConfiguredCurrencyCode()
        );
        
    }
    
    public final PrimaryKey getKey()
    {
        return Key;
    }

    public final void setKey(
    	final PrimaryKey key
    ) {
        this.Key = key;
    }

    public final String getAddress(
    ) {
        return Address;
    }

    public final void setAddress(
    	final String address
    ) {
        Address = address;
    }

    public final Date getStartDate(
    ) {
        return StartDate;
    }

    public final void setStartDate(
    	final Date startDate
    ) {
        StartDate = startDate;
    }

    public final Date getEndDate(
    ) {
        return EndDate;
    }

    public final void setEndDate(
    	final Date endDate
    ) {
        EndDate = endDate;
    }

    public final int getStatus(
    ) {
        return Status;
    }

    public final String getStatusString(
    ) {
        if( STATUS_BUYING == this.Status ) {
            return "Buying";
        } else if( STATUS_PENDING == this.Status ) {
            return "Pending";
        } else if( STATUS_DELIVERED == this.Status ) {
            return "Delivered";
        } else {
            return "Cancelled";
        }
    }

    public final void setStatus(
    	final int status
    ) {
        Status = status;
    }

    public final PrimaryKey getUserID(
    ) {
        return UserID;
    }

    public final void setUserID(
    	final PrimaryKey userID
    ) {
        UserID = userID;
    }

    /**
     * Returns true if all properties are valid.
     */
    public final boolean isValid(
    ) {
        // All OK
        return true;
    }

    /**
     * Compares the name of the objects
     */
    public final int compareTo(
    	final Object o
    ) {
        return compareTo(o);
    }

    public final int compareTo(
    	final Order o
    ) {
        if (null == this.StartDate) return 1;
        return (this.StartDate.compareTo(o.getStartDate()));
    }

    /**
     * If both objects has the same name then they are equal
     */
    public final boolean equals(
    	final Object obj
    ) {
        if (obj instanceof Order) {
            final Order o = (Order) obj;
            if (o.getStartDate().equals(o.getStartDate())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Save the state of this object to a stream (i.e., serialize it).
     *
     * @serialData Stores all the properties one by one to the stream
     */
    private void writeObject(
    	final ObjectOutputStream s
    ) throws IOException {
        s.writeObject(this.getKey());
        s.writeLong(this.getStartDate().getTime());
        s.writeLong(this.getEndDate().getTime());
        s.writeObject(this.getUserID());
        s.writeInt(this.Status);
        s.writeUTF( this.Address );
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    private void readObject(
    	final ObjectInputStream s
    ) throws IOException, ClassNotFoundException {
        this.Key = (PrimaryKey) s.readObject();
        this.StartDate = new Date(s.readLong());
        this.EndDate = new Date(s.readLong());
        this.UserID = (PrimaryKey) s.readObject();
        this.Status = s.readInt();
        this.Address = s.readUTF();
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = 3246127097462068654L;
 
	public static final int STATUS_BUYING = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_CANCELLED = 3;

    public static final String PROP_START_DATE = "StartDate";
    public static final String PROP_FINISH_DATE = "FinishDate";
    public static final String PROP_STATUS = "Status";
    public static final String PROP_USER_ID = "UserID";
    public static final String PROP_ADDRESS = "Address";

    private PrimaryKey Key = new PrimaryKey();
    private Date StartDate = new Date(System.currentTimeMillis());
    private Date EndDate = new Date(System.currentTimeMillis());
    private int Status = 0;
    private PrimaryKey UserID = new PrimaryKey();
    private String Address = "";
   
}

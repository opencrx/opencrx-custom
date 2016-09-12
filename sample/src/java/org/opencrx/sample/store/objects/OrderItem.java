/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: OrderItem
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
import java.math.BigDecimal;

import org.opencrx.sample.store.common.IStandardObject;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;

/**
 * User: User
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class OrderItem implements IStandardObject {
	
	public OrderItem(
    	final PrimaryKey key
    ) {
        Key = key;
    }

    public OrderItem(
    ) {
    }

    public OrderItem(
        org.opencrx.kernel.contract1.jmi1.SalesOrderPosition position
    ) {
        this.OrderID = new PrimaryKey(
        	position.refGetPath().get(position.refGetPath().size() - 3), 
        	false
        );
        this.Key = new PrimaryKey(
        	this.OrderID.toString() + "*" + position.refGetPath().getBase(), 
        	false
        );
        this.ProductID = position.getProduct() == null ? 
        	new PrimaryKey("", false) : 
        	new PrimaryKey(position.getProduct().refGetPath().getBase(), false);
        this.Quantity = position.getQuantity().intValue();
        this.Price = position.getAmount() == null ? 0.0f : position.getAmount().floatValue();        
    }
    
    public void update(
        org.opencrx.kernel.contract1.jmi1.SalesOrderPosition position,
        ApplicationContext context
    ) {
        position.setQuantity(new BigDecimal(this.getQuantity()));
        // Other fields such as orderID, productID can not be modified
        // Price must be modified manually with openCRX/Core GUI
    }
    
    public final PrimaryKey getKey(
    ) {
        return Key;
    }

    public final void setKey(
    	final PrimaryKey key
    ) {
        this.Key = key;
    }

    public final PrimaryKey getOrderID(
    ) {
        return OrderID;
    }

    public final void setOrderID(
    	final PrimaryKey orderID
    ) {
        OrderID = orderID;
    }

    public final PrimaryKey getProductID(
    ) {
        return ProductID;
    }

    public final int getQuantity(
    ) {
        return Quantity;
    }

    public final void setQuantity(
    	final int quantity
    ) {
        Quantity = quantity;
    }

    public final void setProductID(
    	final PrimaryKey productID
    ) {
        ProductID = productID;
    }

    public final float getPrice(
    ) {
        return Price;
    }

    public final void setPrice(
    	final float price
    ) {
        Price = price;
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
    	final OrderItem o
    ) {
        if (null == this.OrderID || null == this.ProductID) return 1;
        return (this.OrderID.compareTo(o.getOrderID()) & this.ProductID.compareTo(o.getProductID()));
    }

    /**
     * If both objects has the same name then they are equal
     */
    public final boolean equals(
    	final Object obj
    ) {
        if (obj instanceof OrderItem) {
            final OrderItem o = (OrderItem) obj;
            if (this.getOrderID().equals(o.getOrderID()) && this.getProductID().equals(o.getProductID())) {
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
        s.writeObject(this.OrderID);
        s.writeObject(this.ProductID);
        s.writeInt(this.Quantity);
        s.writeFloat(this.Price);
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    private void readObject(
    	final ObjectInputStream s
    ) throws IOException, ClassNotFoundException {
        this.Key = (PrimaryKey) s.readObject();
        this.OrderID = (PrimaryKey) s.readObject();
        this.ProductID = (PrimaryKey) s.readObject();
        this.Quantity = s.readInt();
        this.Price = s.readFloat();
    }
    
    /*
        Local variables for the properties
    */
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = 4653806056649830695L;

	private PrimaryKey Key = new PrimaryKey();
    private PrimaryKey OrderID = new PrimaryKey( );
    private PrimaryKey ProductID = new PrimaryKey( );
    private int Quantity = 1;
    private float Price = 0;
    
}

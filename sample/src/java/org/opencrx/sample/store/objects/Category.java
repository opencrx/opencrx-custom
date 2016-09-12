/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: Category
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

import org.opencrx.sample.store.common.IStandardObject;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;
import org.opencrx.sample.store.common.util.Keys;
import org.opencrx.kernel.product1.jmi1.ProductClassification;
import org.opencrx.kernel.product1.jmi1.ProductClassificationRelationship;

/**
 * Category class. Products are displayed under category
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class Category implements IStandardObject
{
	public Category(final PrimaryKey key)
    {
        Key = key;
    }

    public Category()
    {
    }

    public Category(
        ProductClassification classification
    ) {
        this.Key = new PrimaryKey(classification.refGetPath().getBase(), false);
        this.Title = classification.getDescription();
        this.ParentID = new PrimaryKey("", false);
        Collection<ProductClassificationRelationship> relationships = classification.getRelationship();
        for(ProductClassificationRelationship relationship: relationships) {
            if(relationship.getRelationshipTo() != null) {
                ProductClassification parent = relationship.getRelationshipTo();
                if(!(Keys.STORE_SCHEMA + CATEGORY_NAME_PRODUCTS).equals(parent.getName())) {
                    this.ParentID = new PrimaryKey(parent.refGetPath().getBase(), false);
                    break;
                }
            }
        }
    }
    
    public void update(
        org.opencrx.kernel.product1.jmi1.ProductClassification classification,
        ApplicationContext context
    ) {
        classification.setDescription(
            this.getTitle()
        );
    }
    
    /*
        Getter and Setters for the properties
    */
    public final PrimaryKey getKey()
    {
        return Key;
    }

    public final void setKey(final PrimaryKey key)
    {
        this.Key = key;
    }

    public final String getTitle()
    {
        return Title;
    }

    public final void setTitle(final String title)
    {
        Title = title;
    }

    public final int getProductCount()
    {
        return ProductCount;
    }

    public final void setProductCount(
    	final int productCount
    ) {
        ProductCount = productCount;
    }

    public final PrimaryKey getParentID()
    {
        return ParentID;
    }

    public final void setParentID(
    	final PrimaryKey parentID
    ) {
        ParentID = parentID;
    }

    /**
     * Returns true if all properties are valid.
     */
    public final boolean isValid()
    {
        // Blank checks
        if (this.getTitle().equals(""))
            return false;

        // All OK
        return true;
    }

    /**
     * Compares the name of the objects
     */
    public final int compareTo(final Object o)
    {
        return compareTo(o);
    }


    public final int compareTo(
    	final Category o
    ) {
        if (null == this.Title) return 1;
        return (this.Title.compareTo(o.getTitle()));
    }

    /**
     * If both objects has the same name then they are equal
     */
    public final boolean equals(
    	final Object obj
    ) {
        if (obj instanceof Category)
        {
            final Category o = (Category) obj;

            if (o.getTitle().equals(o.getTitle()))
                return true;
            else
                return false;
        } else
            return false;
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
        s.writeUTF(this.getTitle());
        s.writeInt(this.getProductCount());
        s.writeObject(this.ParentID);
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    private void readObject(
    	final ObjectInputStream s
    ) throws IOException, ClassNotFoundException {
        this.Key = (PrimaryKey) s.readObject();
        this.Title = s.readUTF();
        this.ProductCount = s.readInt();
        this.ParentID = (PrimaryKey)s.readObject();
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = 3659205275778487648L;

	public static final String CATEGORY_NAME_PRODUCTS = "Products";
    
    private PrimaryKey Key = new PrimaryKey();
    private String Title = "";
    private int ProductCount = 0;
    private PrimaryKey ParentID = new PrimaryKey();

}

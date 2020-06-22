/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: ProductManager
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
import java.util.StringTokenizer;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.sample.store.common.IStandardObject;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;
import org.opencrx.sample.store.common.util.Keys;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * User class which stores a registered user information
 *
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class User implements IStandardObject {

	public User(
		final PrimaryKey key
	) {
        Key = key;
    }

    public User(
    ) {
    }

    public User(
        org.opencrx.kernel.account1.jmi1.Contact contact
    ) {
        this.Key = new PrimaryKey(contact.refGetPath().getLastSegment().toString(), false);
        this.Name = contact.getAliasName();
        this.Address = "";
        org.opencrx.kernel.account1.jmi1.PostalAddress postalAddress = null;
        Collection<org.opencrx.kernel.account1.jmi1.AccountAddress> addresses = contact.getAddress();
        for(org.opencrx.kernel.account1.jmi1.AccountAddress address: addresses) {
            if(address instanceof org.opencrx.kernel.account1.jmi1.PostalAddress) {
                postalAddress = (org.opencrx.kernel.account1.jmi1.PostalAddress)address;
                break;
            }
        }
        if(postalAddress != null) {
            for(int i = 0; i < postalAddress.getPostalAddressLine().size(); i++) {
                this.Address += this.Address.isEmpty() ? "" : "\n";
                this.Address += postalAddress.getPostalAddressLine().get(i);
            }
            for(int i = 0; i < postalAddress.getPostalStreet().size(); i++) {
                this.Address += this.Address.isEmpty() ? "" : "\n";
                this.Address += postalAddress.getPostalStreet().get(i);
            }
            if(postalAddress.getPostalCode() != null) {
                this.Address += this.Address.isEmpty() ? "" : "\n";
                this.Address += postalAddress.getPostalCode();            	
            } else {
                this.Address += this.Address.isEmpty() ? "" : "\n";
                this.Address += "";            	
            }
            if(postalAddress.getPostalCity() != null) {
                this.Address += this.Address.isEmpty() ? "" : " ";
                this.Address += postalAddress.getPostalCity();          	
            }
        }
        org.opencrx.kernel.generic.jmi1.PropertySet propertySet = this.getPropertySet(
            contact, 
            Keys.STORE_SCHEMA + "Properties"
        );
        if(propertySet != null) {
            // Password property
            org.opencrx.kernel.base.jmi1.Property passwordProperty = 
                this.getProperty(propertySet, "Password");
            if(passwordProperty != null) {
                this.Password = ((org.opencrx.kernel.base.jmi1.StringProperty)passwordProperty).getStringValue();
            }
            // User type property
            org.opencrx.kernel.base.jmi1.Property userTypeProperty =
                this.getProperty(propertySet, "UserType");
            if(userTypeProperty != null) {
                this.UserType = "Administrator".equals(((org.opencrx.kernel.base.jmi1.StringProperty)userTypeProperty).getStringValue())
                    ? USER_TYPE_ADMINISTRATOR
                    : USER_TYPE_REGULAR;                    
            }
        }
    }
    
    public void update(
        org.opencrx.kernel.account1.jmi1.Contact contact,
        ApplicationContext context
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(contact);
        UUIDGenerator uuids = UUIDs.getGenerator();
        org.opencrx.kernel.account1.jmi1.PostalAddress postalAddress = null;
        // Find existing postal address
        if(JDOHelper.isPersistent(contact)) {
        	Collection<org.opencrx.kernel.account1.jmi1.AccountAddress> addresses = contact.getAddress();
            for(org.opencrx.kernel.account1.jmi1.AccountAddress address: addresses) {
                if(address instanceof org.opencrx.kernel.account1.jmi1.PostalAddress) {
                    postalAddress = (org.opencrx.kernel.account1.jmi1.PostalAddress)address;
                    break;
                }
            }
        }
        // Create
        if(postalAddress == null) {
            postalAddress = pm.newInstance(org.opencrx.kernel.account1.jmi1.PostalAddress.class);
            postalAddress.getUsage().add((short)500); // business
            postalAddress.setMain(true);
            contact.addAddress(
                uuids.next().toString(),
                postalAddress
            );
        }
        postalAddress.getPostalAddressLine().clear();
        StringTokenizer t = new StringTokenizer(this.getAddress(), "\n\r", false);
        while(t.hasMoreTokens()) {
            postalAddress.getPostalAddressLine().add(
                t.nextToken()
            );
        }
        // Alias
        contact.setLastName(
            this.getName()
        );        
        contact.setAliasName(
            this.getName()
        );
        // Store password and account type in store-specific property set
        org.opencrx.kernel.generic.jmi1.PropertySet propertySet = this.getPropertySet(
            contact, 
            Keys.STORE_SCHEMA + "Properties"
        );
        if(propertySet == null) {
            propertySet = pm.newInstance(org.opencrx.kernel.generic.jmi1.PropertySet.class);
            propertySet.setName(Keys.STORE_SCHEMA + "Properties");
            contact.addPropertySet(
                false,
                uuids.next().toString(),
                propertySet
            );
        }
        // Password
        org.opencrx.kernel.base.jmi1.StringProperty passwordProperty = 
            (org.opencrx.kernel.base.jmi1.StringProperty)this.getProperty(propertySet, "Password");
        if(passwordProperty == null) {
            passwordProperty = pm.newInstance(org.opencrx.kernel.base.jmi1.StringProperty.class);
            passwordProperty.setName("Password");
            propertySet.addProperty(
                false,
                uuids.next().toString(),
                passwordProperty
            );
        }
        passwordProperty.setStringValue(this.getPassword());
        // Account type
        org.opencrx.kernel.base.jmi1.StringProperty userTypeProperty = 
            (org.opencrx.kernel.base.jmi1.StringProperty)this.getProperty(propertySet, "UserType");
        if(userTypeProperty == null) {
            userTypeProperty = pm.newInstance(org.opencrx.kernel.base.jmi1.StringProperty.class);
            userTypeProperty.setName("UserType");
            propertySet.addProperty(
                false,
                uuids.next().toString(),
                userTypeProperty
            );
        }
        userTypeProperty.setStringValue(
            this.getUserType() == USER_TYPE_ADMINISTRATOR
                ? "Administrator"
                : "Regular"
        );
    }
    
    public org.opencrx.kernel.generic.jmi1.PropertySet getPropertySet(
        org.opencrx.kernel.generic.jmi1.CrxObject crxObject,
        String name
    ) {
        org.opencrx.kernel.generic.jmi1.PropertySet propertySet = null;
        Collection<org.opencrx.kernel.generic.jmi1.PropertySet> propertySets = crxObject.getPropertySet();
        for(org.opencrx.kernel.generic.jmi1.PropertySet ps: propertySets) {
            if(name.trim().equals(ps.getName().trim())) {
                propertySet = ps;
                break;
            }
        }
        return propertySet;
    }
    
    public org.opencrx.kernel.base.jmi1.Property getProperty(
        org.opencrx.kernel.base.jmi1.PropertySet propertySet,
        String name
    ) {
        org.opencrx.kernel.base.jmi1.Property property = null;
        Collection<org.opencrx.kernel.base.jmi1.Property> properties = propertySet.getProperty();
        for(org.opencrx.kernel.base.jmi1.Property p: properties) {
            if(name.equals(p.getName())) {
                property = p;
                break;
            }
        }
        return property;
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

    public final String getName(
    ) {
        return Name;
    }

    public final void setName( 
    	final String name
    ) {
        if( null == name ) {
            Name = "";
        } else {
            Name = name;
        }
    }

    public final String getPassword(
    ) {
        return Password;
    }

    public final void setPassword(
    	final String password
    ) {
        Password = password;
    }

    public final String getAddress(
    ) {
        return Address;
    }

    public final int getUserType(
    ) {
        return UserType;
    }

    public final void setUserType(
    	final int userType
    ) {
        UserType = userType;
    }

    public final void setAddress(
    	final String address
    ) {
        if( null == address) {
            Address = "";
        } else {
            Address = address;
        }
    }

    /**
     * Returns true if all properties are valid.
     */
    public final boolean isValid(
    ) {
        // Blank checks
        if( this.getName().equals( "" )) {
            return false;
        } else {
	        // All OK
	        return true;
        }
    }

    /**
     * Compares the name of the objects
     */
    public final int compareTo(
    	final Object o
    ) {
        return compareTo( (User) o );
    }

    public final int compareTo(
    	final User o
    ) {
        if( null == this.Name ) return 1;
        return (this.Name.compareTo( o.getName() ));
    }

    /**
     * If both objects has the same name then they are equal
     */
    public final boolean equals(
    	final Object obj
    ) {
        if( obj instanceof User ) {
            final User o = (User) obj;
            if( o.getName().equals( o.getName() ) ) {
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
        s.writeObject(this.getKey() );
        s.writeUTF(this.getAddress() );
        s.writeUTF(this.getName() );
        s.writeUTF(this.getPassword() );
        s.writeInt(this.UserType);
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    private void readObject(
    	final ObjectInputStream s
    ) throws IOException,ClassNotFoundException {
        this.Key = (PrimaryKey) s.readObject();
        this.Address = s.readUTF();
        this.Name = s.readUTF();
        this.Password = s.readUTF();
        this.UserType = s.readInt();
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 4651323221910684892L;

    public static final String PROP_USER_NAME = "UserName";
    public static final String PROP_ADDRESS = "Address";
    public static final String PROP_PASSWORD = "Password";
    public static final String PROP_USER_TYPE = "UserType";

    public static final int USER_TYPE_ADMINISTRATOR = 0;
    public static final int USER_TYPE_REGULAR = 1;

    private PrimaryKey Key = new PrimaryKey();
    private String Name = "";
    private String Address = "";
    private String Password = "\0";
    private int UserType = USER_TYPE_REGULAR;
    
}

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
package org.opencrx.sample.store.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.opencrx.kernel.product1.cci2.ProductQuery;
import org.opencrx.kernel.product1.jmi1.ProductClassification;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;
import org.opencrx.sample.store.objects.Product;
import org.openmdx.base.exception.ServiceException;

/**
 * Product Manager manages product subsystem
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class ProductManager
{
    public ProductManager(
        ApplicationContext context
    ) {
        this.context = context;
    }
    
    public final boolean create(
        final Product newValue
    ) {
    	PersistenceManager pm = null;
    	Transaction tx = null;
        try {
        	pm = this.context.newPersistenceManager();
            tx = pm.currentTransaction();       
            tx.begin();
            org.opencrx.kernel.product1.jmi1.Product product = pm.newInstance(org.opencrx.kernel.product1.jmi1.Product.class);
            newValue.update(
                product, 
                this.context
            );
            this.context.getProductSegment(pm).addProduct(
                false,
                newValue.getKey().getUuid(),
                product
            );
            tx.commit();
            return true;
        } catch(Exception e) {
        	if(tx != null) {
        		try {
        			tx.rollback();
        		}
        		catch(Exception e0) {}
        	}
            new ServiceException(e).log();
            return false;
        } finally {
        	if(pm != null) {
        		pm.close();
        	}
        }
    }

    public final void delete(
        final PrimaryKey key
    ) {
    	// Products must be deleted with standard GUI
//    	Transaction tx = null;
//    	try {
//	        tx = this.context.getPersistenceManager().currentTransaction();       
//	        tx.begin();
//	        org.opencrx.kernel.product1.jmi1.Product product = 
//	            this.context.getProductSegment().getProduct(key.getUuid());
//	        product.refDelete();
//	        tx.commit();
//    	}
//    	catch(Exception e) {
//        	if(tx != null) {
//        		try {
//        			tx.rollback();
//        		}
//        		catch(Exception e0) {}
//        	}
//            new ServiceException(e).log();    		
//    	}
    }

    public final Product get(
        final PrimaryKey key
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        if(key.toString().length() > 0) {
	            org.opencrx.kernel.product1.jmi1.Product product = 
	                (org.opencrx.kernel.product1.jmi1.Product)this.context.getProductSegment(pm).getProduct(key.getUuid());
	            return new Product(
	                product, 
	                this.context
	            );
	        } else {
	            return null;
	        }
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final Map<String,Object> getInCategory(
        final PrimaryKey categoryID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
    		Map<String,Object> productsInCategory = new LinkedHashMap<String,Object>();
	        if(categoryID.toString().length() > 0) {
	            ProductClassification classification = this.context.getProductSegment(pm).getProductClassification(categoryID.getUuid());
	            if(classification != null) {
	                ProductQuery query =  (ProductQuery)pm.newQuery(org.opencrx.kernel.product1.jmi1.Product.class);
	                query.thereExistsClassification().equalTo(classification);
	                List<org.opencrx.kernel.product1.jmi1.Product> products = this.context.getProductSegment(pm).getProduct(query);
	                for(org.opencrx.kernel.product1.jmi1.Product product: products) {
	                    Product prod = new Product(product, this.context);
	                    productsInCategory.put(
	                        prod.getKey().toString(),
	                        prod
	                    );
	                }
	            }
	        }
	        return productsInCategory;
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final Product update(
        final Product newValue
    ) {
    	PersistenceManager pm = null;
    	Transaction tx = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        tx = pm.currentTransaction();       
	        tx.begin();
	        org.opencrx.kernel.product1.jmi1.Product product = 
	        	(org.opencrx.kernel.product1.jmi1.Product)this.context.getProductSegment(pm).getProduct(newValue.getKey().getUuid());
	        newValue.update(
	            product,
	            this.context
	        );
	        tx.commit();
	        return this.get(newValue.getKey());
    	} catch(Exception e) {
        	if(tx != null) {
        		try {
        			tx.rollback();
        		}
        		catch(Exception e0) {}
        	}
            new ServiceException(e).log();
            return null;
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final ApplicationContext context;
    
}

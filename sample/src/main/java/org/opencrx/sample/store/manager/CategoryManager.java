/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: CategoryManager
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
package org.opencrx.sample.store.manager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.opencrx.kernel.product1.cci2.ProductClassificationQuery;
import org.opencrx.kernel.product1.cci2.ProductClassificationRelationshipQuery;
import org.opencrx.kernel.product1.jmi1.ProductClassification;
import org.opencrx.kernel.product1.jmi1.ProductClassificationRelationship;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;
import org.opencrx.sample.store.common.util.Keys;
import org.opencrx.sample.store.objects.Category;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * Manager for Category subsystem
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class CategoryManager
{
    public CategoryManager(
        ApplicationContext context
    ) {
        this.context = context;
    }
    
    public final boolean create(
        final Category newValue
    ) {
    	Transaction tx = null;
    	PersistenceManager pm = null;
        try {
        	pm = this.context.newPersistenceManager();
            UUIDGenerator uuids = UUIDs.getGenerator();
            tx = pm.currentTransaction();       
            tx.begin();
            org.opencrx.kernel.product1.jmi1.Segment productSegment = this.context.getProductSegment(pm);
            org.opencrx.kernel.product1.jmi1.ProductClassification classification = 
                pm.newInstance(org.opencrx.kernel.product1.jmi1.ProductClassification.class);
            newValue.update(
                classification, 
                this.context
            );
            classification.setName(
                Keys.STORE_SCHEMA + newValue.getTitle()
            );
            productSegment.addProductClassification(
                false,
                newValue.getKey().getUuid(),
                classification
            );
            tx.commit();
            // Get parent classification
            ProductClassification parent = null;            
            if(
                (newValue.getParentID() != null) &&
                (newValue.getParentID().getUuid().length() > 0)
            ) {
                parent = productSegment.getProductClassification(newValue.getParentID().getUuid());
            } else {
                // OpenCrxContext.SCHEMA_STORE + CATEGORY_NAME_PRODUCTS as default root classification 
                ProductClassificationQuery query = (ProductClassificationQuery)pm.newQuery(ProductClassification.class);                
                query.name().equalTo(Keys.STORE_SCHEMA + Category.CATEGORY_NAME_PRODUCTS);
                Collection<ProductClassification> classifications = productSegment.getProductClassification(query);
                if(!classifications.isEmpty()) {
                    parent = classifications.iterator().next();
                } else {
                    // Create root classification on demand
                    tx.begin();
                    parent = pm.newInstance(ProductClassification.class);
                    parent.setName(Keys.STORE_SCHEMA + Category.CATEGORY_NAME_PRODUCTS);
                    parent.setDescription(Category.CATEGORY_NAME_PRODUCTS);
                    productSegment.addProductClassification(
                        uuids.next().toString(),
                        parent
                    );
                    tx.commit();
                }
            }
            // Add relationship to parent
            tx.begin();
            ProductClassificationRelationship relationship = pm.newInstance(ProductClassificationRelationship.class);
            relationship.setName(parent.getName());
            relationship.setRelationshipType((short)0);
            relationship.setRelationshipTo(parent);
            classification.addRelationship(
                uuids.next().toString(),
                relationship
            );
            tx.commit();
            return true;
        } catch(Exception e) {
        	if(tx != null) {
        		try {
        			tx.rollback();
        		} catch(Exception e0) {}
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
    	PersistenceManager pm = null;
    	Transaction tx = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        tx = pm.currentTransaction();       
	        tx.begin();
	        org.opencrx.kernel.product1.jmi1.ProductClassification classification = 
	            this.context.getProductSegment(pm).getProductClassification(key.getUuid());
	        classification.refDelete();
	        tx.commit();
    	} catch(Exception e) {
        	if(tx != null) {
        		try {
        			tx.rollback();
        		}
        		catch(Exception e0) {}
        	}
            new ServiceException(e).log();
        } finally {
        	if(pm != null) {
        		pm.close();
        	}
        }
    }

    public final Category get(
        final PrimaryKey key
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        if(key.toString().length() > 0) {
	            org.opencrx.kernel.product1.jmi1.ProductClassification classification = 
	                this.context.getProductSegment(pm).getProductClassification(key.getUuid());
	            return new Category(classification);
	        } else {
	            return null;
	        }
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final Map<String,Object> getChildren(
        final PrimaryKey categoryID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
            org.opencrx.kernel.product1.jmi1.Segment productSegment = this.context.getProductSegment(pm);    		
	        Map<String,Object> children = new LinkedHashMap<String,Object>();
	        org.opencrx.kernel.product1.jmi1.ProductClassification parent = null;
	        if(categoryID.toString().length() == 0) {
	            ProductClassificationQuery query = (ProductClassificationQuery)pm.newQuery(org.opencrx.kernel.product1.jmi1.ProductClassification.class);
	            query.name().equalTo(Keys.STORE_SCHEMA + Category.CATEGORY_NAME_PRODUCTS);
	            Collection<ProductClassification> classifications = productSegment.getProductClassification(query);
	            if(!classifications.isEmpty()) {
	                parent = classifications.iterator().next();
	            }
	        } else {
	            parent = productSegment.getProductClassification(categoryID.getUuid());
	        }
	        if(parent != null) {
	            ProductClassificationRelationshipQuery query = (ProductClassificationRelationshipQuery)pm.newQuery(ProductClassificationRelationship.class);
	            query.thereExistsRelationshipTo().equalTo(parent);
	            query.identity().like(
	            		productSegment.refGetPath().getDescendant("productClassification", ":*", "relationship", ":*").toXRI()
	            );
	            List<ProductClassificationRelationship> relationships = productSegment.getExtent(query);
	            for(ProductClassificationRelationship relationship: relationships) {
	                ProductClassification classification = relationship.getClassification();
	                Category category = new Category(classification);
	                children.put(
	                    category.getKey().toString(), 
	                    category
	                );
	            }
	        }
	        return children;
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final Category update(
        final Category newValue
    ) {
    	Transaction tx = null;
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        tx = pm.currentTransaction();       
	        tx.begin();
	        org.opencrx.kernel.product1.jmi1.ProductClassification classification = 
	            this.context.getProductSegment(pm).getProductClassification(newValue.getKey().getUuid());
	        newValue.update(
	            classification,
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

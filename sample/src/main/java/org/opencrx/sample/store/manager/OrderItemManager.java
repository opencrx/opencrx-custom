/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: ProductManager
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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.opencrx.kernel.contract1.jmi1.AbstractSalesOrderPosition;
import org.opencrx.kernel.contract1.jmi1.SalesOrderPosition;
import org.opencrx.kernel.product1.cci2.SalesTaxTypeQuery;
import org.opencrx.kernel.product1.jmi1.SalesTaxType;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;
import org.opencrx.sample.store.objects.OrderItem;
import org.openmdx.base.exception.ServiceException;
import org.w3c.spi2.Datatypes;
import org.w3c.spi2.Structures;

/**
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class OrderItemManager {

	public OrderItemManager(
        ApplicationContext context
    ) {
        this.context = context;
    }
    
    public final OrderItem get(
        final PrimaryKey orderItemID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        if(orderItemID.toString().length() > 0 && orderItemID.toString().indexOf("*") > 0) {
	        	String orderItemId = orderItemID.toString();
	            SalesOrderPosition position = null;
	            try {
	            	position = (SalesOrderPosition)pm.getObjectById(
	            		this.context.getContractSegment(pm).refGetPath().getDescendant(
	            			"salesOrder", 
	            			orderItemId.substring(0, orderItemId.indexOf("*")), 
	            			"position", 
	            			orderItemId.substring(orderItemId.indexOf("*") + 1)
	            		)            			
	            	);
	            } catch(Exception e) {}
	            return position == null ?
	                null :
	                new OrderItem(position);
	        } else {
	            return null;
	        }
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final void update(
        final OrderItem newValue
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        SalesOrderPosition position = null;
	    	String orderItemId = newValue.getKey().toString();
	    	position = (SalesOrderPosition)pm.getObjectById(
	    		this.context.getContractSegment(pm).refGetPath().getDescendant(
	    			"salesOrder", 
	    			orderItemId.substring(0, orderItemId.indexOf("*")), 
	    			"position", 
	    			orderItemId.substring(orderItemId.indexOf("*") + 1)
	    		)            			
	    	);
	    	Transaction tx = null;
	    	try {
		        tx = pm.currentTransaction();       
	            tx.begin();
	            newValue.update(
	                position,
	                this.context
	            );
	            tx.commit();
	    	} catch(Exception e) {
	        	if(tx != null) {
	        		try {
	        			tx.rollback();
	        		}
	        		catch(Exception e0) {}
	        	}
	            new ServiceException(e).log();    		
	    	}
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final OrderItem getOrderedProduct(
        final PrimaryKey orderID, 
        final PrimaryKey productID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        org.opencrx.kernel.product1.jmi1.Product product = 
	            (org.opencrx.kernel.product1.jmi1.Product)this.context.getProductSegment(pm).getProduct(productID.getUuid());
	        org.opencrx.kernel.contract1.jmi1.SalesOrder salesOrder = 
	            this.context.getContractSegment(pm).getSalesOrder(orderID.getUuid());
	        Collection<SalesOrderPosition> positions = salesOrder.getPosition();
	        for(SalesOrderPosition position: positions) {
	            if(position.getProduct().equals(product)) {
	                return new OrderItem(
	                    position
	                );                    
	            }
	        }
	        return null;
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    private org.opencrx.kernel.product1.jmi1.SalesTaxType getSalesTaxType(
    	PersistenceManager pm
    ) {
        SalesTaxTypeQuery query = (SalesTaxTypeQuery)pm.newQuery(SalesTaxType.class);
        query.name().equalTo(this.context.getConfiguredSalesTaxTypeName());
        Collection<SalesTaxType> salesTaxTypes = this.context.getProductSegment(pm).getSalesTaxType(query);
        return salesTaxTypes.isEmpty() ? 
        	null : 
        	salesTaxTypes.iterator().next();
    }
    
    public final OrderItem newOrder(
        final PrimaryKey orderID, 
        final PrimaryKey productID 
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        final OrderItem existingItem = this.getOrderedProduct(
	            orderID, 
	            productID
	        );
	        if(null != existingItem) {
	            existingItem.setQuantity( existingItem.getQuantity() + 1 );
	            this.update( existingItem );
	            return existingItem;
	        } else {
	        	Transaction tx = null;
	            try {
	                org.opencrx.kernel.contract1.jmi1.SalesOrder salesOrder = 
	                    this.context.getContractSegment(pm).getSalesOrder(orderID.getUuid());
	                org.opencrx.kernel.product1.jmi1.Product product = 
	                	(org.opencrx.kernel.product1.jmi1.Product)this.context.getProductSegment(pm).getProduct(productID.getUuid());            
	                tx = pm.currentTransaction();       
	                tx.begin();
	                org.opencrx.kernel.contract1.jmi1.CreatePositionParams params = Structures.create(
	                	org.opencrx.kernel.contract1.jmi1.CreatePositionParams.class,
	                	Datatypes.member(org.opencrx.kernel.contract1.jmi1.CreatePositionParams.Member.name, product.getName()),
	                	Datatypes.member(org.opencrx.kernel.contract1.jmi1.CreatePositionParams.Member.product, product),
	                	Datatypes.member(org.opencrx.kernel.contract1.jmi1.CreatePositionParams.Member.quantity, new BigDecimal(1))
	                ); 
	                org.opencrx.kernel.contract1.jmi1.CreatePositionResult result = salesOrder.createPosition(
	                    params
	                );
	                tx.commit();
	                org.opencrx.kernel.contract1.jmi1.SalesContractPosition position = 
	                    (org.opencrx.kernel.contract1.jmi1.SalesContractPosition)pm.getObjectById(
	                        result.getPosition().refGetPath()
	                    );
	                org.opencrx.kernel.product1.jmi1.SalesTaxType salesTaxType = this.getSalesTaxType(pm);                
	                if(salesTaxType != null) {
	                    tx.begin();
	                    position.setSalesTaxType(salesTaxType);                    
	                    tx.commit();
	                }
	                return this.get(
	                    new PrimaryKey(
	                    	salesOrder.refGetPath().getLastSegment().toString() + "$" + position.refGetPath().getLastSegment().toString(), 
	                    	false
	                    )
	                );
	            } catch(Exception e) {
	            	if(tx != null) {
	            		try {
	            			tx.rollback();
	            		}
	            		catch(Exception e0) {}
	            	}
	                new ServiceException(e).log();
	                return null;
	            }            
	        }
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final Map<String,Object> getItemsInOrder(
        final PrimaryKey orderID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        org.opencrx.kernel.contract1.jmi1.SalesOrder salesOrder = 
	            this.context.getContractSegment(pm).getSalesOrder(orderID.getUuid());
	        Map<String,Object> orderItems = new LinkedHashMap<String,Object>();
	        Collection<AbstractSalesOrderPosition> positions = salesOrder.getPosition();
	        for(AbstractSalesOrderPosition position: positions) {
	            if(position instanceof org.opencrx.kernel.contract1.jmi1.SalesOrderPosition) {
	                OrderItem orderItem = new OrderItem(
	                    (org.opencrx.kernel.contract1.jmi1.SalesOrderPosition)position
	                );
	                orderItems.put(
	                    orderItem.getKey().toString(), 
	                    orderItem
	                );
	            }
	        }
	        return orderItems;
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final void clearItems(
        final PrimaryKey orderID
    ) {
    	Transaction tx = null;
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        tx = pm.currentTransaction();       
	        tx.begin();
	        org.opencrx.kernel.contract1.jmi1.SalesOrder salesOrder = 
	            this.context.getContractSegment(pm).getSalesOrder(orderID.getUuid());
	        Collection<AbstractSalesOrderPosition> positions = salesOrder.getPosition();        
	        for(AbstractSalesOrderPosition position: positions) {
	            position.refDelete();
	        }
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

    public final void delete(
        final PrimaryKey orderItemID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        if(orderItemID.toString().length() > 0 && orderItemID.toString().indexOf("*") > 0) {
	        	String orderItemId = orderItemID.toString();
	            SalesOrderPosition position = null;
	        	position = (SalesOrderPosition)pm.getObjectById(
	        		this.context.getContractSegment(pm).refGetPath().getDescendant(
	        			"salesOrder", 
	        			orderItemId.substring(0, orderItemId.indexOf("*")), 
	        			"position", 
	        			orderItemId.substring(orderItemId.indexOf("*") + 1)
	        		)            			
	        	);
	            Transaction tx = null;
	            try {
		            tx = pm.currentTransaction();
		            tx.begin();
		            position.refDelete();
		            tx.commit();
	            } catch(Exception e) {
	            	if(tx != null) {
	            		try {
	            			tx.rollback();
	            		}
	            		catch(Exception e0) {}
	            	}
	                new ServiceException(e).log();            	
	            }
	        }
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

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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.opencrx.kernel.contract1.cci2.SalesOrderQuery;
import org.opencrx.kernel.contract1.jmi1.SalesOrder;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;
import org.opencrx.sample.store.common.util.Keys;
import org.opencrx.sample.store.objects.Order;
import org.opencrx.sample.store.objects.User;
import org.openmdx.base.exception.ServiceException;

/**
 * Manager for Order subsystem
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class OrderManager {
	
    public OrderManager(
        ApplicationContext context
    ) {
        this.context = context;
    }
    
    public final Order get(
       final PrimaryKey orderID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        if(orderID.toString().length() > 0) {
	            System.out.println("orderId=" + orderID.getUuid());
	            org.opencrx.kernel.contract1.jmi1.SalesOrder salesOrder = 
	                this.context.getContractSegment(pm).getSalesOrder(orderID.getUuid());     
	            System.out.println("sales order=" + salesOrder);
	            return new Order(salesOrder);
	        } else { 
	            return null;
	        }
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final Order startNew(
        final PrimaryKey userID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        final Order order = new Order();
	        order.setUserID(userID);
	        order.setStatus(Order.STATUS_BUYING);
	        order.setStartDate( new Date(System.currentTimeMillis()));
	        order.setEndDate( new Date(System.currentTimeMillis()));
	        // Get user's default address
	        final UserManager userManager = new UserManager(this.context);
	        final User user = userManager.get( userID );
	        order.setAddress( user.getAddress() );
	        Transaction tx = null;
	        try {
		        tx = pm.currentTransaction();       
		        tx.begin();
		        SalesOrder salesOrder = pm.newInstance(SalesOrder.class);
		        order.update(
		            salesOrder,
		            this.context
		        );
		        this.context.getContractSegment(pm).addSalesOrder(
		            false,
		            order.getKey().getUuid(),
		            salesOrder
		        );
		        tx.commit();
		        return this.get(order.getKey());
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
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final void update(
        final Order newValue
    ) {
    	Transaction tx = null;
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        tx = pm.currentTransaction();       
	        tx.begin();
	        org.opencrx.kernel.contract1.jmi1.SalesOrder salesOrder = 
	            this.context.getContractSegment(pm).getSalesOrder(newValue.getKey().getUuid());
	        newValue.update(
	            salesOrder,
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
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final Order getLastOrder(
        final PrimaryKey userID
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        org.opencrx.kernel.account1.jmi1.Account customer =
	            this.context.getAccountSegment(pm).getAccount(userID.getUuid());
	        SalesOrderQuery query = (SalesOrderQuery)pm.newQuery(SalesOrder.class);
	        query.thereExistsCustomer().equalTo(customer);
	        query.contractState().equalTo(Short.valueOf((short)400)/* buying */);        
	        query.orderByCreatedAt().descending();
	        List<SalesOrder> salesOrders = this.context.getContractSegment(pm).getSalesOrder(query);
	        if(!salesOrders.isEmpty()) {
	            SalesOrder salesOrder = salesOrders.iterator().next();
	            return new Order(salesOrder);
	        } else {
	            return null;
	        }
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    public final Order checkout(
        final PrimaryKey orderID, 
        final String shippingAddress
    ) {
        final Order order = this.get( orderID );
        order.setAddress( shippingAddress );
        order.setStatus( Order.STATUS_PENDING );
        this.update( order );
        return this.get( orderID );
    }

    public final void discardOrder(
        final PrimaryKey orderID
    ) {
        final Order order = this.get( orderID );
        order.setStatus( Order.STATUS_CANCELLED );
        this.update( order );
    }

    public final void deliverOrder(
        final PrimaryKey orderID
    ) {
        final Order order = this.get( orderID );
        order.setStatus( Order.STATUS_DELIVERED );
        this.update( order );
    }

    public final Map<String,Object> getPendingOrders( 
    ) {
    	PersistenceManager pm = null;
    	try {
    		pm = this.context.newPersistenceManager();
	        SalesOrderQuery query = (SalesOrderQuery)pm.newQuery(SalesOrder.class);
	        query.contractState().equalTo(Short.valueOf((short)430)/* on hold */);
	        query.name().like(Keys.STORE_SCHEMA + ".*");
	        Map<String,Object> pendingSalesOrders = new LinkedHashMap<String,Object>();
	        List<SalesOrder> salesOrders = this.context.getContractSegment(pm).getSalesOrder(query);
	        for(SalesOrder salesOrder: salesOrders) {
	            Order order = new Order(salesOrder);
	            pendingSalesOrders.put(
	                order.getKey().toString(),
	                order
	            );            
	        }
	        return pendingSalesOrders;
    	} finally {
    		if(pm != null) {
    			pm.close();
    		}
    	}
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    public static final int STATUS_BUYING = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_CANCELLED = 3;

    private final ApplicationContext context;
    
}

/*
 * ====================================================================
 * Project:     openCRX/Sample
 * Description: GadgetInfoWorkflow
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * Copyright (c) 2008-2017, CRIXP AG, Switzerland
 * All rights reserved.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 * This product includes software developed by contributors to
 * openCRX (http://www.opencrx.org/)
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.opencrx.sample.workflow;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.kernel.backend.Workflows;
import org.opencrx.kernel.home1.jmi1.WfProcessInstance;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.ContextCapable;
import org.openmdx.base.naming.Path;
import org.w3c.spi2.Datatypes;
import org.w3c.spi2.Structures;

import org.opencrx.sample.account1.jmi1.Gadget;
import org.opencrx.sample.account1.jmi1.SendGadgetInfoParams;

/**
 * GadgetInfoWorkflow
 *
 */
public class GadgetInfoWorkflow extends Workflows.AsynchronousWorkflow {

	/* (non-Javadoc)
	 * @see org.opencrx.kernel.backend.Workflows.AsynchronousWorkflow#execute(org.opencrx.kernel.home1.jmi1.WfProcessInstance)
	 */
	@Override
	public void execute(
		WfProcessInstance wfProcessInstance 
	) throws ServiceException {
	    if(wfProcessInstance.getTargetObject() != null) {
	    	PersistenceManager pm = JDOHelper.getPersistenceManager(wfProcessInstance);
	    	ContextCapable target = (ContextCapable)pm.getObjectById(new Path(wfProcessInstance.getTargetObject()));
	    	if(target instanceof Gadget) {
	    		try {
	    			Gadget gadget = (Gadget)target;
		    		org.opencrx.kernel.home1.jmi1.UserHome userHome = 
		    			(org.opencrx.kernel.home1.jmi1.UserHome)pm.getObjectById(wfProcessInstance.refGetPath().getParent().getParent());
	    			SendGadgetInfoParams params = Structures.create(
	    				SendGadgetInfoParams.class,
	    				Datatypes.member(SendGadgetInfoParams.Member.toUsers, userHome.refGetPath().getLastSegment().toString())
	    		    );
	    			pm.currentTransaction().begin();
	    			gadget.sendGadgetInfo(params);
		    		pm.currentTransaction().commit();
	    		} catch(Exception e) {
	    			new ServiceException(e).log();
	    		}	    		
	    	}
	    }
    }

}

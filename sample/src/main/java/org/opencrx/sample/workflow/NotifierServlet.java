/*
 * ====================================================================
 * Project:     openCRX/Sample
 * Description: NotifierServlet
 * Owner:       the original authors.
 * ====================================================================
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opencrx.kernel.admin1.jmi1.Admin1Package;
import org.opencrx.kernel.backend.Accounts;
import org.opencrx.kernel.generic.SecurityKeys;
import org.opencrx.kernel.utils.Utils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;

import org.opencrx.sample.account1.cci2.GadgetQuery;
import org.opencrx.sample.account1.jmi1.Gadget;

/**
 * The NotifyServlet 'listens' for modified 
 * <ul>
 *   <li>gadgets.
 * </ul> 
 */  
public class NotifierServlet 
    extends HttpServlet {

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
    public void init(
        ServletConfig config
    ) throws ServletException {

        super.init(config);
        // provider name
        String providerName = this.getInitParameter("providerName");
        if(providerName.startsWith("provider/")) {
            providerName = providerName.substring(9);
        }        
        // persistenceManagerFactory
        try {
            this.pmf = Utils.getPersistenceManagerFactory();
        }
        catch (Exception e) {
            throw new ServletException("Can not get connection to data provider", e);
        }
        PersistenceManager pm = null;
        try {
            pm = this.pmf.getPersistenceManager(
                SecurityKeys.ROOT_PRINCIPAL,
                null
            );
        }
        catch(Exception e) {
            throw new ServletException("Can not get persistence manager", e);
        }        
        // Get component configuration
        try {
            Admin1Package adminPackage = Utils.getAdminPackage(pm);            
            org.opencrx.kernel.admin1.jmi1.Segment adminSegment = 
                (org.opencrx.kernel.admin1.jmi1.Segment)pm.getObjectById(
                    new Path("xri://@openmdx*org.opencrx.kernel.admin1").getDescendant("provider", providerName, "segment", "Root")
                );
            org.opencrx.kernel.admin1.jmi1.ComponentConfiguration componentConfiguration = null;
            try {
                componentConfiguration = adminSegment.getConfiguration(
                    COMPONENT_CONFIGURATION_ID
                );
            }
            catch(Exception e) {}
            if(componentConfiguration == null) {
                componentConfiguration = adminPackage.getComponentConfiguration().createComponentConfiguration();
                componentConfiguration.setName(COMPONENT_CONFIGURATION_ID);
                pm.currentTransaction().begin();
                adminSegment.addConfiguration(
                    false, 
                    COMPONENT_CONFIGURATION_ID,
                    componentConfiguration
                );
                pm.currentTransaction().commit();
            }
        }
        catch(Exception e) {
            throw new ServletException("Can not get component configuration", e);
        }
    }

    /**
     * Get component config property.
     * 
     * @param name
     * @param componentConfiguration
     * @return
     */
    protected String getComponentConfigProperty(
        String name,
        org.opencrx.kernel.admin1.jmi1.ComponentConfiguration componentConfiguration
    ) {
        String value = null;
        Collection<org.opencrx.kernel.base.jmi1.Property> properties = componentConfiguration.getProperty();
        for(org.opencrx.kernel.base.jmi1.Property p:properties) {
            if(
                p.getName().equals(name) &&
                (p instanceof org.opencrx.kernel.base.jmi1.StringProperty)
            ) {
                value = ((org.opencrx.kernel.base.jmi1.StringProperty)p).getStringValue();
                break;
            }
        }
        return value;
    }
        
    /**
     * Handle gadget update.
     * 
     * @param id
     * @param providerName
     * @param segmentName
     * @param req
     * @param res
     * @throws IOException
     */
    protected void notify(
        String id,
        String providerName,
        String segmentName,
        HttpServletRequest req, 
        HttpServletResponse res        
    ) throws IOException {        
        System.out.println(new Date() + ": " + WORKFLOW_NAME + " " + providerName + "/" + segmentName);
        try {
            PersistenceManager rootPm = null;
            rootPm = this.pmf.getPersistenceManager(
                SecurityKeys.ROOT_PRINCIPAL,
                null
            );
            org.opencrx.kernel.admin1.jmi1.Segment adminSegment = 
                (org.opencrx.kernel.admin1.jmi1.Segment)rootPm.getObjectById(
                    new Path("xri://@openmdx*org.opencrx.kernel.admin1").getDescendant("provider", providerName, "segment", "Root")
                );
            String configurationId = COMPONENT_CONFIGURATION_ID + "." + segmentName;
            org.opencrx.kernel.admin1.jmi1.ComponentConfiguration componentConfiguration = null;
            // Try to get segment-specific component config
            try {
                componentConfiguration = adminSegment.getConfiguration(configurationId);
            } catch(Exception e) {}
            // Fall back to shared config
            if(componentConfiguration == null) {
                componentConfiguration = adminSegment.getConfiguration(COMPONENT_CONFIGURATION_ID);
            }
            Date lastRunAt = componentConfiguration.getModifiedAt();
            // Each run of the NotifierServlet touches the component configuration. The
            // modifiedAt attribute of the component configuration serves as timestamp
            // for the last run of the NotifierServlet. 
            rootPm.currentTransaction().begin();
            componentConfiguration.setDescription(componentConfiguration.getDescription());
            rootPm.currentTransaction().commit();
            componentConfiguration = (org.opencrx.kernel.admin1.jmi1.ComponentConfiguration)rootPm.getObjectById(componentConfiguration.refGetPath());
            Date runAt = componentConfiguration.getModifiedAt();
            // Check for gadgets with modifiedAt in [lastRunAt,runAt]   
            String adminName = SecurityKeys.ADMIN_PRINCIPAL + SecurityKeys.ID_SEPARATOR + segmentName;
            PersistenceManager pm = this.pmf.getPersistenceManager(
            	adminName,
                null
            );
            org.opencrx.kernel.account1.jmi1.Segment accountSegment = Accounts.getInstance().getAccountSegment(pm, providerName, segmentName);
            GadgetQuery gadgetQuery = 
            	(GadgetQuery)PersistenceHelper.newQuery(
            		pm.getExtent(Gadget.class),
            		accountSegment.refGetPath().getDescendant("account", ":*", "gadget", ":*")
            	);
            gadgetQuery.modifiedAt().greaterThanOrEqualTo(lastRunAt);
            gadgetQuery.modifiedAt().lessThan(runAt);
            // Export gadget information to file ModifiedGadgets.log
            Collection<Gadget> gadgets = accountSegment.getExtent(gadgetQuery);
        	File tmpDir = new File("/temp");
        	if(!tmpDir.exists()) {
        		tmpDir = new File("/tmp");
    			if(!tmpDir.exists()) {
    				tmpDir = new File(".");
    			}
        	}
        	File exportFile = new File(tmpDir, "ModifiedGadgets.log");        	
        	PrintStream out = new PrintStream(new FileOutputStream(exportFile, true));
            for(Gadget gadget: gadgets) {
                System.out.println(new Date() + ": " + WORKFLOW_NAME + " " + providerName + "/" + segmentName + ": Export gadget " + gadget.refGetPath().toXRI() + " to " + exportFile);
                try {
                	out.println(new Date() + ";" + gadget.refMofId() + ";" + gadget.getModifiedAt() + ";" + gadget.getModifiedBy() + ";" + gadget.getTitle() + ";" + gadget.getText());
                } catch(Exception e) {
                	try {
                		pm.currentTransaction().rollback();
                	} catch(Exception e0) {}
                    new ServiceException(e).log();
                    System.out.println(new Date() + ": " + WORKFLOW_NAME + " " + providerName + "/" + segmentName + ": exception occured " + e.getMessage() + ". Continuing");
                }                                        
            }
            out.close();
        } catch(Exception e) {
            new ServiceException(e).log();
            System.out.println(new Date() + ": " + WORKFLOW_NAME + " " + providerName + "/" + segmentName + ": exception occured " + e.getMessage() + ". Continuing");
        }        
    }

    /**
     * Handle execute request.
     * 
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    protected void handleRequest(
        HttpServletRequest req, 
        HttpServletResponse res
    ) throws ServletException, IOException {
        if(System.currentTimeMillis() > this.startedAt + STARTUP_DELAY) {
            String segmentName = req.getParameter("segment");
            String providerName = req.getParameter("provider");
            String id = providerName + "/" + segmentName;
            if(COMMAND_EXECUTE.equals(req.getPathInfo())) {
                if(!runningSegments.containsKey(id)) {
	                try {
	                    runningSegments.put(
	                    	id,
	                    	Thread.currentThread()
	                    );
	                    this.notify(
	                        id,
	                        providerName,
	                        segmentName,
	                        req,
	                        res
	                    );
	                } catch(Exception e) {
	                    new ServiceException(e).log();
	                } finally {
	                    runningSegments.remove(id);
	                }
                } else if(
	        		!runningSegments.get(id).isAlive() || 
	        		runningSegments.get(id).isInterrupted()
	        	) {
	            	Thread t = runningSegments.get(id);
	        		System.out.println(new Date() + ": " + WORKFLOW_NAME + " " + providerName + "/" + segmentName + ": workflow " + t.getId() + " is alive=" + t.isAlive() + "; interrupted=" + t.isInterrupted() + ". Skipping execution.");
	        	}
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(
        HttpServletRequest req, 
        HttpServletResponse res
    ) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.flushBuffer();
        this.handleRequest(
            req,
            res
        );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(
        HttpServletRequest req, 
        HttpServletResponse res
    ) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.flushBuffer();
        this.handleRequest(
            req,
            res
        );
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 4441731357561757549L;

    protected static final String COMMAND_EXECUTE = "/execute";
    protected static final String WORKFLOW_NAME = "Notifier";
    protected static final String COMPONENT_CONFIGURATION_ID = "NotifierServlet";
    private static final long STARTUP_DELAY = 30000L;
    
    protected PersistenceManagerFactory pmf = null;
    private static final Map<String,Thread> runningSegments = new ConcurrentHashMap<String,Thread>();
    protected long startedAt = System.currentTimeMillis();
        
}

//--- End of File -----------------------------------------------------------

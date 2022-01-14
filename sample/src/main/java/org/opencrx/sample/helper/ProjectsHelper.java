/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: ProjectsHelper
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
package org.opencrx.sample.helper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.kernel.activity1.cci2.ActivityQuery;
import org.opencrx.kernel.activity1.cci2.ActivityTrackerQuery;
import org.opencrx.kernel.activity1.cci2.ActivityTypeQuery;
import org.opencrx.kernel.activity1.cci2.ActivityWorkRecordQuery;
import org.opencrx.kernel.activity1.cci2.ResourceQuery;
import org.opencrx.kernel.activity1.jmi1.AccountAssignmentActivityGroup;
import org.opencrx.kernel.activity1.jmi1.Activity;
import org.opencrx.kernel.activity1.jmi1.ActivityCreator;
import org.opencrx.kernel.activity1.jmi1.ActivityTracker;
import org.opencrx.kernel.activity1.jmi1.ActivityType;
import org.opencrx.kernel.activity1.jmi1.ActivityWorkRecord;
import org.opencrx.kernel.activity1.jmi1.AddWorkAndExpenseRecordResult;
import org.opencrx.kernel.activity1.jmi1.NewActivityParams;
import org.opencrx.kernel.activity1.jmi1.NewActivityResult;
import org.opencrx.kernel.activity1.jmi1.Resource;
import org.opencrx.kernel.activity1.jmi1.ResourceAddWorkRecordParams;
import org.opencrx.kernel.activity1.jmi1.WorkAndExpenseRecord;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.w3c.spi2.Datatypes;
import org.w3c.spi2.Structures;

/**
 * Sample helpers for project management.
 *
 */
public abstract class ProjectsHelper {

    /**
     * Get customer project groups and restrict to account if specified.
     * 
     * @param activitySegment
     * @param account
     * @return
     */
    public static List<ActivityTracker> getCustomerProjectGroups(
    	org.opencrx.kernel.activity1.jmi1.Segment activitySegment,
    	org.opencrx.kernel.account1.jmi1.Account account
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(activitySegment);
    	ActivityTrackerQuery activityTrackerQuery = 
    		(ActivityTrackerQuery)pm.newQuery(ActivityTracker.class);
    	activityTrackerQuery.forAllDisabled().isFalse();
    	activityTrackerQuery.activityGroupType().equalTo(ACTIVITY_GROUP_TYPE_PROJECT);
    	activityTrackerQuery.thereExistsAssignedAccount().accountRole().equalTo(ACCOUNT_ROLE_CUSTOMER);
    	if(account != null) {
    		activityTrackerQuery.thereExistsAssignedAccount().thereExistsAccount().equalTo(account);
    	}
    	activityTrackerQuery.orderByName().ascending();
    	return activitySegment.getActivityTracker(activityTrackerQuery);
    }

    /**
     * Create a customer project group.
     * 
     * @param activitySegment
     * @param name
     * @param description
     * @param customer
     */
    public static ActivityTracker createCustomerProjectGroup(
    	org.opencrx.kernel.activity1.jmi1.Segment activitySegment,
    	String name,
    	String description,
    	org.opencrx.kernel.account1.jmi1.Account customer
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(activitySegment);
    	try {
    	    pm.currentTransaction().begin();
    	    // Activity tracker
    	    ActivityTracker activityTracker = null;
    	    {
    	        activityTracker = pm.newInstance(ActivityTracker.class);
    	        activityTracker.setName(name);
    	        activityTracker.setDescription(description);
    	        activityTracker.setActivityGroupType(ACTIVITY_GROUP_TYPE_PROJECT);
    	        activitySegment.addActivityTracker(
    	            org.opencrx.kernel.utils.Utils.getUidAsString(), 
    	            activityTracker
    	        );
    	    }
    	    // Activity creator
    	    ActivityCreator activityCreator = null;
    	    {
    	    	ActivityTypeQuery activityTypeQuery = 
    	    		(ActivityTypeQuery)pm.newQuery(ActivityType.class);
    	        activityTypeQuery.name().equalTo("Incidents");
    	        List<ActivityType> activityTypes = activitySegment.getActivityType(activityTypeQuery);
    	        ActivityType incidentType = activityTypes.isEmpty() ? null : activityTypes.iterator().next();
    	    	activityCreator = pm.newInstance(ActivityCreator.class);
    	    	activityCreator.setName(name);
    	    	activityCreator.setDescription(description);
    	    	activityCreator.getActivityGroup().add(activityTracker);
    	    	activityCreator.setActivityType(incidentType);
    	    	activityCreator.setIcalClass(ICAL_CLASS_NA);
    	    	activityCreator.setIcalType(ICAL_TYPE_VEVENT);
    	    	activityCreator.setPriority((short)0);
    	    	activitySegment.addActivityCreator(
    	    		org.opencrx.kernel.utils.Utils.getUidAsString(),
    	    		activityCreator
    	    	);
    	    }
    	    // Account assignment
    	    {
    	    	AccountAssignmentActivityGroup accountAssignment = pm.newInstance(AccountAssignmentActivityGroup.class);
    	    	accountAssignment.setName(customer.getFullName());
    	    	accountAssignment.setAccount(customer);
    	    	accountAssignment.setAccountRole(ACCOUNT_ROLE_CUSTOMER);
    	    	activityTracker.addAssignedAccount(
    	    		org.opencrx.kernel.utils.Utils.getUidAsString(),
    	    		accountAssignment
    	    	);
    	    }
    	    pm.currentTransaction().commit();
        	return activityTracker;
    	} catch(Exception e) {
			new ServiceException(e).log();
    	    try {
    	        pm.currentTransaction().rollback();
    	    } catch(Exception ignore) {}
    	}
    	return null;
    }
    
    /**
     * Get customer projects for the given customer project group.
     * 
     * @param customerProjectGroup
     * @return
     */
    public static List<Activity> getCustomerProjects(
    	ActivityTracker customerProjectGroup
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(customerProjectGroup);
    	ActivityQuery activityQuery = 
    		(ActivityQuery)pm.newQuery(Activity.class);
    	activityQuery.forAllDisabled().isFalse();
    	activityQuery.orderByName().ascending();
    	return customerProjectGroup.getFilteredActivity(activityQuery);
    }

	/**
	 * Create a customer project.
	 * 
	 * @param customerProjectGroup
	 * @return
	 */
    public static Activity createCustomerProject(
		ActivityTracker customerProjectGroup,
		String name,
		String description,
		String detailedDescription,
		Date scheduledStart,
		Date scheduledEnd,
		short priority
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(customerProjectGroup);
		ActivityCreator customerProjectCreator = null;
		for(ActivityCreator activityCreator: customerProjectGroup.<ActivityCreator>getActivityCreator()) {
			if(activityCreator.getActivityType().getActivityClass() == ACTIVITY_CLASS_INCIDENT) {
				customerProjectCreator = activityCreator;
				break;
			}
		}
		if(customerProjectCreator != null) {
			try {
				pm.currentTransaction().begin();
				NewActivityParams newActivityParams = Structures.create(
					NewActivityParams.class,
					Datatypes.member(NewActivityParams.Member.name, name),
					Datatypes.member(NewActivityParams.Member.description, description),
					Datatypes.member(NewActivityParams.Member.detailedDescription, detailedDescription),
					Datatypes.member(NewActivityParams.Member.scheduledStart, scheduledStart),
					Datatypes.member(NewActivityParams.Member.scheduledEnd, scheduledEnd),
					Datatypes.member(NewActivityParams.Member.priority, priority),
					Datatypes.member(NewActivityParams.Member.icalType, ICAL_TYPE_NA)
				);
				NewActivityResult newActivityResult = 
					customerProjectCreator.newActivity(newActivityParams);
				pm.currentTransaction().commit();
				return newActivityResult.getActivity();
			} catch(Exception e) {
				new ServiceException(e).log();
				try {
					pm.currentTransaction().rollback();
				} catch(Exception ignore) {}
			}
		}
		return null;
    }

    /**
     * Get project resources.
     * 
     * @param activitySegment
     * @return
     */
    public static List<Resource> getProjectResources(
    	org.opencrx.kernel.activity1.jmi1.Segment activitySegment
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(activitySegment);
    	ResourceQuery resourceQuery =
    		(ResourceQuery)pm.newQuery(Resource.class);
    	resourceQuery.forAllDisabled().isFalse();
    	resourceQuery.orderByName().ascending();
    	return activitySegment.getResource(resourceQuery);
    }

    /**
     * Create project resource for given contact.
     * 
     * @param contact
     * @return
     */
    public static Resource createProjectResource(
    	org.opencrx.kernel.account1.jmi1.Contact contact
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(contact);
    	String providerName = contact.refGetPath().getSegment(2).toClassicRepresentation();
    	String segmentName = contact.refGetPath().getSegment(4).toClassicRepresentation();
    	org.opencrx.kernel.activity1.jmi1.Segment activitySegment =
    		(org.opencrx.kernel.activity1.jmi1.Segment)pm.getObjectById(
    			new Path("xri://@openmdx*org.opencrx.kernel.account1").getDescendant("provider", providerName, "segment", segmentName)
    		);
    	try {
    		pm.currentTransaction().begin();
    		Resource resource = pm.newInstance(Resource.class);
    		resource.setName(contact.getFullName());
    		resource.setContact(contact);
    		activitySegment.addResource(
				org.opencrx.kernel.utils.Utils.getUidAsString(),
				resource
    		);
    		pm.currentTransaction().commit();
    		return resource;
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
		}
    	return null;
    }

    /**
     * Get activity work records for given resource.
     * 
     * @param resource
     * @return
     */
    public static List<WorkAndExpenseRecord> getWorkRecords(
    	Resource resource
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(resource);
    	ActivityWorkRecordQuery workRecordQuery =
    		(ActivityWorkRecordQuery)pm.newQuery(ActivityWorkRecord.class);
    	workRecordQuery.orderByStartedAt().ascending();
    	return resource.getWorkReportEntry(workRecordQuery);
    }

    /**
     * Create work record for given resource and activity.
     * 
     * @param resource
     * @param name
     * @param description
     * @param startAt
     * @param durationHours
     * @param durationMinutes
     * @param isBillable
     * @param rate
     * @param rateCurrency
     * @param activity
     * @return
     */
    public static WorkAndExpenseRecord createWorkRecord(
    	Resource resource,
    	String name,
    	String description,
    	Date startAt,
    	Integer durationHours,
    	Integer durationMinutes,
    	Boolean isBillable,
    	BigDecimal rate,
    	short rateCurrency,
    	Activity activity
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(resource);
    	try {
    		pm.currentTransaction().begin();
    		ResourceAddWorkRecordParams addWorkRecordParams = Structures.create(
    			ResourceAddWorkRecordParams.class,
    			Datatypes.member(ResourceAddWorkRecordParams.Member.activity, activity),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.name, name),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.description, description),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.startAt, startAt),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.durationHours, durationHours),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.durationMinutes, durationMinutes),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.isBillable, isBillable),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.rate, rate),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.rateCurrency, rateCurrency),
    			Datatypes.member(ResourceAddWorkRecordParams.Member.recordType, (short)0)        			
    		);
    		AddWorkAndExpenseRecordResult addWorkRecordResult = resource.addWorkRecord(addWorkRecordParams);
    		pm.currentTransaction().commit();
    		return addWorkRecordResult.getWorkRecord();
    	} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
    	}
    	return null;
    }
    
	public static final short ICAL_CLASS_NA = 0;
	public static final short ICAL_TYPE_VEVENT = 1;
	public static final short ACTIVITY_GROUP_TYPE_PROJECT = 40;
	public static final short ACCOUNT_ROLE_CUSTOMER = 100;
	public static final short ACTIVITY_CLASS_INCIDENT = 2;
	public static final short ICAL_TYPE_NA = 0;
    
}

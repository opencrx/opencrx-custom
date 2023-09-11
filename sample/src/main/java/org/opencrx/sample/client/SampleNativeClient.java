/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: SampleNativeClient
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
package org.opencrx.sample.client;

import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.opencrx.kernel.account1.cci2.PostalAddressQuery;
import org.opencrx.kernel.account1.jmi1.PostalAddress;
import org.opencrx.sample.client1.jmi1.AccountT;
import org.opencrx.sample.client1.jmi1.QueryAccountParams;
import org.opencrx.sample.client1.jmi1.QueryAccountResult;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.lightweight.naming.LightweightInitialContextFactoryBuilder;
import org.w3c.spi2.Datatypes;
import org.w3c.spi2.Structures;

/**
 * Sample native openCRX client.
 *
 */
public class SampleNativeClient {

	public static void main(
		String[] args
	) throws NamingException, ServiceException {
		final String providerName = "CRX";
		final String segmentName = "Standard";
		final String userName = "admin-" + segmentName;
		// Disable when running on J2EE application server
		if(true) {
			// Required when deployed with openMDX lightweight container
			if(!NamingManager.hasInitialContextFactoryBuilder()) {
				LightweightInitialContextFactoryBuilder.install(
					Collections.singletonMap(
						"org.openmdx.comp.env.jdbc_opencrx_CRX",
						"jdbc:postgresql:\\/\\/localhost:5432\\/CRX?user=...&password=..."
					)
				);
			}
		}
		PersistenceManagerFactory pmf = org.opencrx.kernel.utils.Utils.getPersistenceManagerFactory();
		PersistenceManager pm = pmf.getPersistenceManager(userName, null);
		// Query contacts
		String fullName = null;
		String postalCity = null;
		{
			org.opencrx.kernel.account1.jmi1.Segment accountSegment =
				(org.opencrx.kernel.account1.jmi1.Segment)pm.getObjectById(
					new Path("xri://@openmdx*org.opencrx.kernel.account1").getDescendant("provider", providerName, "segment", segmentName)
				);
			org.opencrx.kernel.account1.cci2.ContactQuery contactQuery =
				(org.opencrx.kernel.account1.cci2.ContactQuery)pm.newQuery(org.opencrx.kernel.account1.jmi1.Contact.class);
			contactQuery.orderByFullName().ascending();
			contactQuery.thereExistsFullName().like(".*");
			int count = 0;
	    	PostalAddressQuery postalAddressQuery = (PostalAddressQuery) pm.newQuery(PostalAddress.class);
			for (org.opencrx.kernel.account1.jmi1.Contact contact : accountSegment.<org.opencrx.kernel.account1.jmi1.Contact> getAccount(contactQuery)) {
				System.out.println(contact.refGetPath().toXRI() + ": " + contact.getFullName());
	    		List<PostalAddress> postalAddresses = contact.getAddress(postalAddressQuery);
	    		for(PostalAddress postalAddress : postalAddresses) {
	    			if(postalAddress.isMain() && postalAddress.getPostalCity() != null) {
	    				fullName = contact.getFullName();
		    			postalCity = postalAddress.getPostalCity();
	    				break;
	    			}
	    		}
				count++;
				if(count > 100) {
					break;
				}
			}
		}
		// Create client segment if it does not exist
		{
			try {
				pm.getObjectById(
					new Path("xri://@openmdx*org.opencrx.sample.client1").getDescendant("provider", providerName, "segment", segmentName)
				);
			} catch(Exception e) {
				org.openmdx.base.jmi1.Provider provider =
					(org.openmdx.base.jmi1.Provider)pm.getObjectById(
						new Path("xri://@openmdx*org.opencrx.sample.client1").getDescendant("provider", "CRX")
					);
				pm.currentTransaction().begin();
				org.opencrx.sample.client1.jmi1.Segment clientSegment = pm.newInstance(org.opencrx.sample.client1.jmi1.Segment.class);
				provider.addSegment(segmentName, clientSegment);
				pm.currentTransaction().commit();
			}
		}
		// Invoke operation
		{
			org.opencrx.sample.client1.jmi1.Segment clientSegment =
				(org.opencrx.sample.client1.jmi1.Segment)pm.getObjectById(
					new Path("xri://@openmdx*org.opencrx.sample.client1").getDescendant("provider", providerName, "segment", segmentName)
				);
			QueryAccountParams params = Structures.create(
				QueryAccountParams.class,
				Datatypes.member(QueryAccountParams.Member.name, fullName),
				Datatypes.member(QueryAccountParams.Member.postalCity, postalCity)
			);
			QueryAccountResult result = clientSegment.queryAccounts(params);
			for(AccountT accountT: result.<AccountT>getAccounts()) {
				System.out.println("fullName: " + accountT.getFullName() + "; postalCity: " + accountT.getPostalCity());
			}
		}
		pm.close();
	}

}

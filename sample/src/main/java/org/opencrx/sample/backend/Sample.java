/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: Sample backend
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
package org.opencrx.sample.backend;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;

import org.opencrx.kernel.account1.cci2.AccountQuery;
import org.opencrx.kernel.account1.cci2.PostalAddressQuery;
import org.opencrx.kernel.account1.jmi1.Account;
import org.opencrx.kernel.account1.jmi1.PostalAddress;
import org.opencrx.kernel.backend.AbstractImpl;
import org.opencrx.kernel.backend.Base;
import org.opencrx.sample.account1.jmi1.Gadget;
import org.opencrx.sample.client1.jmi1.AccountT;
import org.opencrx.sample.client1.jmi1.QueryAccountResult;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.resource.Records;
import org.w3c.spi2.Datatypes;
import org.w3c.spi2.Structures;

/**
 * Sample backend.
 *
 */
public class Sample extends AbstractImpl {

	/**
	 * Register Sample backend.
	 * 
	 */
	public static void register(
	) {
		System.out.println(new Date() + ": Sample.register()");
		registerImpl(new Sample());
	}
	
	/**
	 * Get instance of Sample backend.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public static Sample getInstance(
	) throws ServiceException {
		return getInstance(Sample.class);
	}
	
    /**
     * Constructor.
     * 
     */
    protected Sample(
    ) {
    }

    /**
     * Implementation of Operation Gadget:sendGadgetInfo.
     * 
     * @param gadget
     * @param toUsers
     * @throws ServiceException
     */
    public void sendGadgetInfo(
        Gadget gadget,
        String toUsers
    ) throws ServiceException {
    	try {
    		// Collect info about gadget	    			
    		URL queryUrl = new URL("http://www.bing.com/search?q=" + URLEncoder.encode(gadget.getTitle(), "UTF-8"));
    		System.out.println(new Date() + ": sendGadgetInfo " + queryUrl);
    		URLConnection conn = queryUrl.openConnection();
    		// Get response
    		InputStream in = conn.getInputStream();
    		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    		int b;
    		while ((b = in.read()) != -1) {
    			bytes.write(b);
    			if(bytes.size() > 1000000) break;
    		}
    		in.close();
    		String response = new String(bytes.toByteArray(), "UTF-8");
    		// Get search results
    		StringBuffer links = new StringBuffer();
    		int pos = 0;
    		while((pos = response.indexOf("<h2><a href=", pos)) > 0) {
    			StringBuffer link = new StringBuffer();
    			char c;
    			while((c = response.charAt(pos + 13)) != '"') {
    				link.append(c);
    				pos++;
    			}
    			System.out.println(new Date() + ": link=" + link);
    			links.append(link.toString()).append("\n");
    		}
			// ... and send alert to users
			Base.getInstance().sendAlert(
				gadget,
				toUsers, 
				"Gadget information for " + gadget.getTitle(), 
				links.toString(),
				(short)0, 
				30,
				null
			);
    	} catch(Exception e) {
    		throw new ServiceException(e);
    	}
    }

    /**
     * Backend implementations used by service1 operations.
     * 
     * @param queryFullName
     * @param queryCity
     * @param serviceSegment
     * @return
     * @throws ServiceException
     * @throws ResourceException
     */
    public QueryAccountResult queryAccounts(
		String queryFullName,
		String queryCity,
		org.opencrx.sample.client1.jmi1.Segment serviceSegment
    ) throws ServiceException, ResourceException {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(serviceSegment);
    	String providerName = serviceSegment.refGetPath().get(2);
    	String segmentName = serviceSegment.refGetPath().get(4);
    	AccountQuery accountQuery = (AccountQuery) pm.newQuery(Account.class);
    	if(queryFullName != null && queryFullName.length() > 0) {
    		accountQuery.thereExistsFullName().like("(?i).*" + queryFullName + ".*");
    	}
    	if(queryCity != null && queryCity.length() > 0) {
    		PostalAddressQuery addressQuery = (PostalAddressQuery) pm.newQuery(PostalAddress.class);
    		addressQuery.thereExistsPostalCity().like("(?i)" + queryCity + ".*");
    		accountQuery.thereExistsAddress().elementOf(PersistenceHelper.asSubquery(addressQuery));
    	}
    	accountQuery.orderByFullName().ascending();
    	List<Account> accounts = Accounts.getInstance().getAccountSegment(
    		pm,
    		providerName,
    		segmentName
    	).getAccount(accountQuery);
    	PostalAddressQuery pQuery = (PostalAddressQuery) pm.newQuery(PostalAddress.class);
    	@SuppressWarnings("unchecked")
    	List<AccountT> resultingAccounts = Records.getRecordFactory().createIndexedRecord(Multiplicity.LIST.toString());
    	for(Account account : accounts) {
    		List<Structures.Member<AccountT.Member>> members = new ArrayList<Structures.Member<AccountT.Member>>();
    		members.add(
    			Datatypes.member(
    				AccountT.Member.fullName, account.getFullName()
    			)
    		);
    		String city = null;
    		List<PostalAddress> postalAddresses = account.getAddress(pQuery);
    		for(PostalAddress postalAddress : postalAddresses) {
    			city = postalAddress.getPostalCity();
    			if (postalAddress.isMain()) {
    				break;
    			}
    		}
    		if(city != null) {
    			members.add(
    				Datatypes.member(AccountT.Member.postalCity, city)
    			);
    		}
    		AccountT resultingAcount = Structures.create(AccountT.class, members);
    		resultingAccounts.add(resultingAcount);
    	}
    	return Structures.create(
    		QueryAccountResult.class,
    		Datatypes.member(QueryAccountResult.Member.accounts, resultingAccounts)
    	);
    }

}

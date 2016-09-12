/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: AddressBookHelper
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2015, CRIXP Corp., Switzerland
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
package org.opencrx.sample.helper;

import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.kernel.account1.cci2.AccountQuery;
import org.opencrx.kernel.account1.cci2.GroupQuery;
import org.opencrx.kernel.account1.cci2.MemberQuery;
import org.opencrx.kernel.account1.jmi1.Account;
import org.opencrx.kernel.account1.jmi1.Contact;
import org.opencrx.kernel.account1.jmi1.Group;
import org.opencrx.kernel.account1.jmi1.LegalEntity;
import org.opencrx.kernel.account1.jmi1.Member;
import org.opencrx.kernel.utils.Utils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Sample helpers for address book management.
 *
 */
public abstract class AddressBookHelper {

	/**
	 * Create a new contact.
	 * 
	 * @param accountSegment
	 * @param firstName
	 * @param lastName
	 * @return
	 */
	public static Contact createContact(
		org.opencrx.kernel.account1.jmi1.Segment accountSegment,
		String firstName,
		String lastName
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(accountSegment);
		try {
			pm.currentTransaction().begin();
			Contact contact = pm.newInstance(Contact.class);
			contact.setFirstName(firstName);
			contact.setLastName(lastName);
			accountSegment.addAccount(
				Utils.getUidAsString(),
				contact
			);
			pm.currentTransaction().commit();
			return contact;
		} catch(Exception e) {
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
		}
		return null;
	}

	/**
	 * Create a new legal entity.
	 * 
	 * @param accountSegment
	 * @param name
	 * @return
	 */
	public static LegalEntity createLegalEntity(
		org.opencrx.kernel.account1.jmi1.Segment accountSegment,
		String name
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(accountSegment);
		try {
			pm.currentTransaction().begin();
			LegalEntity legalEntity = pm.newInstance(LegalEntity.class);
			legalEntity.setName(name);
			accountSegment.addAccount(
				Utils.getUidAsString(),
				legalEntity
			);
			pm.currentTransaction().commit();
			return legalEntity;
		} catch(Exception e) {
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
		}
		return null;
	}

	/**
	 * Get list of address books, i.e. groups with account type = 100.
	 * 
	 * @param accountSegment
	 * @return
	 */
	public static final List<Group> getAddressBooks(
		org.opencrx.kernel.account1.jmi1.Segment accountSegment
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(accountSegment);
		GroupQuery addressBookQuery = (GroupQuery)pm.newQuery(Group.class);
		addressBookQuery.forAllDisabled().isFalse();
		addressBookQuery.thereExistsAccountType().equalTo(ACCOUNT_TYPE_ADDRESS_BOOK);
		return accountSegment.getAccount(addressBookQuery);
	}
	
	/**
	 * Create a new address book.
	 * 
	 * @param accountSegment
	 * @param name
	 * @param description
	 * @return
	 */
	public static Group createAddressBook(
		org.opencrx.kernel.account1.jmi1.Segment accountSegment,
		String name,
		String description
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(accountSegment);
		try {
			pm.currentTransaction().begin();
			Group addressBook = pm.newInstance(Group.class);
			addressBook.setName(name);
			addressBook.setDescription(description);
			accountSegment.addAccount(
				Utils.getUidAsString(),
				addressBook
			);
			pm.currentTransaction().commit();
			return addressBook;
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
		}
		return null;
	}

	/**
	 * Get active members for given address book.
	 * 
	 * @param addressBook
	 * @return
	 */
	public static List<Member> getAddressBookMembers(
		Group addressBook
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(addressBook);
		MemberQuery memberQuery = (MemberQuery)pm.newQuery(Member.class);
		memberQuery.forAllDisabled().isFalse();
		memberQuery.thereExistsAccount().forAllDisabled().isFalse();
		return addressBook.getMember(memberQuery);
	}
	
	/**
	 * Get accounts for given address book.
	 * 
	 * @param addressBook
	 * @return
	 */
	public static List<Account> getAddressBookAccounts(
		Group addressBook
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(addressBook);
		String providerName = addressBook.refGetPath().getSegment(2).toClassicRepresentation();
		String segmentName = addressBook.refGetPath().getSegment(4).toClassicRepresentation();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = 
			(org.opencrx.kernel.account1.jmi1.Segment)pm.getObjectById(
				new Path("xri://openmdx*org.opencrx.kernel.account1").getDescendant("provider", providerName, "segment", segmentName)
			);
		AccountQuery accountQuery = (AccountQuery)pm.newQuery(Account.class);
		accountQuery.forAllDisabled().isFalse();
		accountQuery.thereExistsAccountMembership().forAllDisabled().isFalse();
		accountQuery.thereExistsAccountMembership().thereExistsAccountFrom().equalTo(addressBook);
		return accountSegment.getAccount(accountQuery);
	}
	
	/**
	 * Add account as member to given address book.
	 * 
	 * @param addressBook
	 * @param account
	 * @param validFrom
	 * @param validTo
	 * @param roles
	 * @return
	 */
	public static Member addAccountToAddressBook(
		Group addressBook,
		Account account,
		Date validFrom,
		Date validTo,
		List<Short> roles
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(addressBook);
		try {
			pm.currentTransaction().begin();
			Member member = pm.newInstance(Member.class);
			member.setName(account.getFullName());
			member.setAccount(account);
			member.setValidFrom(validFrom);
			member.setValidTo(validTo);
			if(roles != null) {
				member.getMemberRole().addAll(roles);
			}
			addressBook.addMember(
				Utils.getUidAsString(),
				member
			);
			pm.currentTransaction().commit();
			return member;
		} catch(Exception e) {
			try {
				pm.currentTransaction().rollback();
			} catch(Exception igore) {}
		}
		return null;
	}
	
	public static final short ACCOUNT_TYPE_ADDRESS_BOOK = 100;

}

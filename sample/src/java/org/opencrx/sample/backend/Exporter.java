/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: Custom Exporter
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2010-2014, CRIXP Corp., Switzerland
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
package org.opencrx.sample.backend;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.kernel.account1.cci2.AccountAddressQuery;
import org.opencrx.kernel.account1.jmi1.AccountAddress;
import org.opencrx.kernel.account1.jmi1.Contact;
import org.opencrx.kernel.account1.jmi1.EMailAddress;
import org.opencrx.kernel.account1.jmi1.PhoneNumber;
import org.opencrx.kernel.address1.jmi1.PostalAddressable;
import org.opencrx.kernel.backend.Base;
import org.opencrx.kernel.backend.Contracts;
import org.opencrx.kernel.base.jmi1.ExportProfile;
import org.opencrx.kernel.contract1.jmi1.ContractAddress;
import org.opencrx.kernel.contract1.jmi1.SalesContract;
import org.opencrx.kernel.contract1.jmi1.SalesContractPosition;
import org.opencrx.kernel.document1.jmi1.DocumentRevision;
import org.opencrx.kernel.document1.jmi1.MediaContent;
import org.opencrx.kernel.product1.jmi1.ConfiguredProduct;
import org.opencrx.kernel.utils.Utils;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.portal.servlet.Codes;

/**
 * Custom-specific exporter backend.
 *
 */
public class Exporter extends org.opencrx.kernel.backend.Exporter {

	/**
	 * Register custom-specific Exporter backend.
	 */
	public static void register(
	) {
		registerImpl(new org.opencrx.sample.backend.Exporter());
	}
	
    /**
     * Constructor.
     */
    protected Exporter(
    ) {
    }

	/* (non-Javadoc)
	 * @see org.opencrx.kernel.backend.Exporter#exportItem(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.opencrx.kernel.base.jmi1.ExportProfile, java.lang.String, java.lang.String)
	 */
	@Override
	public Object[] exportItem(
		RefObject_1_0 object, 
		ExportProfile exportProfile,
		String referenceFilter, 
		String itemMimeType
	) throws ServiceException {
		PersistenceManager pm = JDOHelper.getPersistenceManager(object);
		// Render SalesContract as RTF
		if(
			object instanceof SalesContract &&
			exportProfile != null &&
			exportProfile.getTemplate() != null
		) {
			try {
		        org.opencrx.kernel.code1.jmi1.Segment codeSegment = null;
		        try {
		        	codeSegment = (org.opencrx.kernel.code1.jmi1.Segment)pm.getObjectById(
		        		new Path("xri://@openmdx*org.opencrx.kernel.code1").getDescendant("provider", object.refGetPath().get(2), "segment", "Root")        		
		        	);
		        } catch(Exception e) {}
			    final Codes codes = new Codes(codeSegment);					    
				short locale = exportProfile.getLocale();
				String localeAsString = (String)codes.getShortText(
					"locale", 
					(short)0, 
					true, 
					true
				).get(locale);
				String language = "en";
				if(localeAsString.indexOf("_") > 0) {
					language = localeAsString.substring(0, 2);
				}
				Base.CodeMapper codeMapper = new Base.CodeMapper() {					
					@Override
					public String getLocaleText(short locale) {
						return (String)codes.getShortText("locale", (short)0, true, true).get(locale);
					}
					@Override
					public String getCurrencyText(short currency, short locale) {
						return (String)codes.getLongText("currency", locale, true, true).get(currency);
					}
					@Override
					public String getCountryText(short country, short locale) {
						return (String)codes.getLongText("org:opencrx:kernel:address1:PostalAddressable:postalCountry", locale, true, true).get(country);
					}
				};
				DecimalFormat decimalFormatter = Utils.getDecimalFormat(language);
				DateFormat dateFormat = Utils.getDateFormat(language);
				SalesContract contract = (SalesContract)object;
				List<SalesContractPosition> positions = Contracts.getInstance().getSalesContractPositions(contract);
			    org.opencrx.kernel.utils.rtf.RTFTemplate document = new org.opencrx.kernel.utils.rtf.RTFTemplate();
			    DocumentRevision headRevision = exportProfile.getTemplate().getHeadRevision();
			    document.readFrom(
			    	new InputStreamReader(
			    	    ((MediaContent)headRevision).getContent().getContent()
			    	),
			    	true
			   	);
				// Positions
			    int positionCount = 0;
				int nPositions = positions.size();
			    for(SalesContractPosition position: positions) {
					if(positionCount < nPositions - 1) {
				       	document.appendTableRow("contractPositions");
					}
					document.setBookmarkContent("quantity", decimalFormatter.format(position.getQuantity()), true);
			  	    document.setBookmarkContent("product", Base.getInstance().getTitle(((ConfiguredProduct)position).getProduct(), codeMapper, locale, true), true);
					document.setBookmarkContent("uom", Base.getInstance().getTitle(position.getUom(), codeMapper, locale, true), true);
					document.setBookmarkContent("pricePerUnit", decimalFormatter.format(position.getPricePerUnit() == null ? BigDecimal.ZERO : position.getPricePerUnit()), true);
					document.setBookmarkContent("discountAmount", decimalFormatter.format(position.getDiscountAmount()), true);
					document.setBookmarkContent("salesTaxType", decimalFormatter.format(position.getSalesTaxType() == null ? BigDecimal.ZERO : position.getSalesTaxType().getRate()), true);
					document.setBookmarkContent("positionAmount", decimalFormatter.format(position.getAmount()), true);
			   	    positionCount++;
			    }
			    PostalAddressable billingAddress = null;
			    PostalAddressable shippingAddress = null;
			    Collection<ContractAddress> contractAddresses = contract.getAddress();
			    for(ContractAddress address: contractAddresses) {
			        if(
			            address.getUsage().contains(Short.valueOf((short)10200)) &&
			            (address instanceof PostalAddressable)
			        ) {
			            shippingAddress = (PostalAddressable)address;
			        }
			        if(
			            address.getUsage().contains(Short.valueOf((short)10000)) &&
			            (address instanceof PostalAddressable)
			        ) {
			            billingAddress = (PostalAddressable)address;
			        }
			    }
			    if(contract.getCustomer() != null) {
			    	Collection<AccountAddress> customerAddresses = contract.getCustomer().getAddress();
				    for(AccountAddress address: customerAddresses) {
				        if(
				            address.getUsage().contains(Short.valueOf((short)10200)) &&
				            (address instanceof PostalAddressable)
				        ) {
				            if(shippingAddress == null) {
				            	shippingAddress = (PostalAddressable)address;
				            }
				        }
				        if(
				            address.getUsage().contains(Short.valueOf((short)10000)) &&
				            (address instanceof PostalAddressable)
				        ) {
				            if(billingAddress == null) {
				            	billingAddress = (PostalAddressable)address;
				            }
				        }
				    }
			    }
			    String billingAddressTitle = Base.getInstance().getTitle(billingAddress, codeMapper, locale, true);
			    String[] addressLines = billingAddressTitle.split("<br />");
			    for(int i = 0; i < 8; i++) {
				    document.setBookmarkContent(
				   		"billingAddress_" + i,
				   		i < addressLines.length ? addressLines[i] : ""
				   	);
			    }
			    String shippingAddressTitle = Base.getInstance().getTitle(shippingAddress, codeMapper, locale, true);
			    addressLines = shippingAddressTitle.split("<br />");
			    for(int i = 0; i < 8; i++) {
				    document.setBookmarkContent(
				   		"shippingAddress_" + i,
				   		i < addressLines.length ? addressLines[i] : ""
				   	);
			    }
			    document.setBookmarkContent(
			       	"contractNumber",
			       	contract.getContractNumber() == null ? "" : contract.getContractNumber()
			   	);
				document.setBookmarkContent(
					"activeOn",
					contract.getActiveOn() == null ? "" : dateFormat.format(contract.getActiveOn())
				);
			    document.setBookmarkContent(
			    	"expiresOn",
			    	contract.getExpiresOn() == null ? "" : dateFormat.format(contract.getExpiresOn())
			   	);
			    document.setBookmarkContent(
			      	"description",
			       	contract.getDescription() == null ? "" : contract.getDescription()
			    );
			    document.setBookmarkContent(
			    	"contractCurrency",
			    	(String)(codes.getShortText("currency", locale, true, true).get(new Short(contract.getContractCurrency())))
			    );
			    document.setBookmarkContent(
			    	"paymentTerms",
			    	(String)(codes.getShortText("paymentterms", locale, true, true).get(new Short(contract.getPaymentTerms())))
			    );
			    document.setBookmarkContent(
					"shippingMethod",
					(String)(codes.getShortText("shippingMethod", locale, true, true).get(new Short(contract.getShippingMethod())))
				);
			    document.setBookmarkContent(
			       	"totalAmountExcludingTax",
			       	contract.getTotalAmount() == null ? "" : decimalFormatter.format(contract.getTotalAmount())
			    );
			    document.setBookmarkContent(
			           "totalSalesTax",
			           contract.getTotalTaxAmount() == null ? "" : decimalFormatter.format(contract.getTotalTaxAmount())
			       );
			    document.setBookmarkContent(
			        "totalAmount",
			        contract.getTotalAmountIncludingTax() == null ? "" : decimalFormatter.format(contract.getTotalAmountIncludingTax())
			    );
			    // SalesRep
			    if(
			    	(contract.getSalesRep() != null) && 
			    	(contract.getSalesRep() instanceof Contact)
			    ) {
					Contact salesRep = (Contact)contract.getSalesRep();
					document.setBookmarkContent("salesRep", salesRep.getFullName());
					EMailAddress salesRepEMailAddress = null;
					PhoneNumber salesRepPhoneNumber = null;
					AccountAddressQuery addressQuery = (AccountAddressQuery)pm.newQuery(AccountAddress.class);
			        addressQuery.thereExistsUsage().equalTo(new Short((short)500));
			        addressQuery.forAllDisabled().isFalse();
			        List<AccountAddress> salesRepAddresses = salesRep.getAddress(addressQuery);
			        for(AccountAddress address: salesRepAddresses) {
						if(address instanceof EMailAddress) {
						    salesRepEMailAddress = (EMailAddress)address;
			          	}
			          	if(address instanceof PhoneNumber) {
			          	    salesRepPhoneNumber = (PhoneNumber)address;
				        }
			      	}
			        document.setBookmarkContent(
			        	"salesRepEMail",
			        	salesRepEMailAddress == null ? "" : salesRepEMailAddress.getEmailAddress()
			       	);
			    	document.setBookmarkContent(
			    	    "salesRepTel",
			    	    salesRepPhoneNumber == null ? "" : salesRepPhoneNumber.getPhoneNumberFull()
			    	);
			    }
			    ByteArrayOutputStream renderedDocument = new ByteArrayOutputStream();
			    document.writeTo(renderedDocument);
			    renderedDocument.close();
			    return new Object[]{
			    	((MediaContent)headRevision).getContentName(),
				    exportProfile.getMimeType(),
				    renderedDocument.toByteArray()
			    };
			}
			catch(Exception e) {
				throw new ServiceException(e);
			}
		}
		// Default rendering
		else {
			return super.exportItem(
				object, 
				exportProfile, 
				referenceFilter, 
				itemMimeType
			);
		}
	}
	
}

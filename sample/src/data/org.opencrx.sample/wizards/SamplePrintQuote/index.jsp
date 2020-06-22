<%@  page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %><%
/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: Sample rendering of a quote
 * Owner:       CRIXP Corp., Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2014-2016, CRIXP Corp., Switzerland
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
%><%@ page session="true" import="
java.util.*,
java.io.*,
java.text.*,
java.math.*,
org.opencrx.kernel.backend.*,
org.openmdx.base.accessor.jmi.cci.*,
org.openmdx.base.exception.*,
org.openmdx.kernel.id.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.attribute.*,
org.openmdx.portal.servlet.component.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.wizards.*,
org.openmdx.base.naming.*,
org.openmdx.base.query.*,
org.openmdx.base.text.conversion.*,
org.openmdx.kernel.log.*
" %>
<%!

	public static String renderPostalAddress(
		org.opencrx.kernel.account1.jmi1.PostalAddress postalAddress			
	) {
		String renderedAddress = "";
		for(String postalAddressLine: postalAddress.getPostalAddressLine()) {
			renderedAddress += postalAddressLine + "\n";
		}
		for(String postalStreet: postalAddress.getPostalStreet()) {
			renderedAddress += postalStreet + "\n";
		}
		if(postalAddress.getPostalCode() != null) {
			renderedAddress += postalAddress.getPostalCode() + " ";
		}
		if(postalAddress.getPostalCity() != null) {
			renderedAddress += postalAddress.getPostalCity() + "\n";
		}
		return renderedAddress;
	}

	public static Action getDownloadFileAction(
		String location,
		String downloadFileName,
		String mimeType,
		ApplicationContext app
	) {
		return  new Action(
			Action.EVENT_DOWNLOAD_FROM_LOCATION,
			new Action.Parameter[]{
				new Action.Parameter(Action.PARAMETER_LOCATION, location),
				new Action.Parameter(Action.PARAMETER_NAME, downloadFileName),
				new Action.Parameter(Action.PARAMETER_MIME_TYPE, mimeType)
			},
			app.getTexts().getClickToDownloadText() + " " + downloadFileName,
			true
		);		
	}

%>
<%
	request.setCharacterEncoding("UTF-8");
	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	String requestId =  request.getParameter(Action.PARAMETER_REQUEST_ID);
	String objectXri = request.getParameter(Action.PARAMETER_OBJECTXRI);
 	if(objectXri == null || app == null || viewsCache.getView(requestId) == null) {
      response.sendRedirect(
         request.getContextPath() + "/" + WebKeys.SERVLET_NAME
      );
      return;
	}
	Texts_1_0 texts = app.getTexts();
	javax.jdo.PersistenceManager pm = app.getNewPmData();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title><%= app.getApplicationName() %> - Print Quote</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<script language="javascript" type="text/javascript" src="../../js/prototype.js"></script>
	<link rel="stylesheet" href="../../js/bootstrap/css/bootstrap.min.css">	
	<link rel="stylesheet" href="../../_style/colors.css">
	<link rel="stylesheet" href="../../_style/n2default.css">
	<link rel="stylesheet" href="../../_style/ssf.css">
	<link href="../../_style/calendar-small.css" rel="stylesheet" type="text/css">
	<link rel='shortcut icon' href='../../images/favicon.ico' />
</head>
<%
try {
	RefObject_1_0 obj = (RefObject_1_0)pm.getObjectById(new Path(objectXri));
	Path objPath = new Path(obj.refMofId());
	String providerName = objPath.get(2);
	String segmentName = objPath.get(4);
   	if(obj instanceof org.opencrx.kernel.contract1.jmi1.Quote) {
   		org.opencrx.kernel.contract1.jmi1.Quote quote = (org.opencrx.kernel.contract1.jmi1.Quote)obj;
   		String renderedQuote = "";
   		renderedQuote += "SALES QUOTATION\n\n";
   		// FROM
   		renderedQuote += "FROM:\n";
   		if(quote.getSupplier() != null) {
   			org.opencrx.kernel.account1.jmi1.Account supplier = quote.getSupplier();
   			org.opencrx.kernel.account1.jmi1.AccountAddress[] mainAddresses = Accounts.getInstance().getMainAddresses(supplier);
   			if(mainAddresses[Accounts.POSTAL_BUSINESS] != null) {
   				org.opencrx.kernel.account1.jmi1.PostalAddress mainPostalBusiness = (org.opencrx.kernel.account1.jmi1.PostalAddress)mainAddresses[Accounts.POSTAL_BUSINESS];
   				renderedQuote += renderPostalAddress(mainPostalBusiness);
   			}
   		} else {
   			renderedQuote += "---\n";
   		}
   		renderedQuote += "\n";
   		// TO
   		renderedQuote += "TO:\n";
   		if(quote.getCustomer() != null) {
   			org.opencrx.kernel.account1.jmi1.Account customer = quote.getCustomer();
   			org.opencrx.kernel.account1.jmi1.AccountAddress[] mainAddresses = Accounts.getInstance().getMainAddresses(customer);
   			if(mainAddresses[Accounts.POSTAL_BUSINESS] != null) {
   				org.opencrx.kernel.account1.jmi1.PostalAddress mainPostalBusiness = (org.opencrx.kernel.account1.jmi1.PostalAddress)mainAddresses[Accounts.POSTAL_BUSINESS];
   				renderedQuote += renderPostalAddress(mainPostalBusiness);
   			}
   		} else {
   			renderedQuote = "---\n";
   		}
   		renderedQuote += "\n";
		// INQUIRY
		renderedQuote += "INQUIRY: " + (quote.getPricingDate() == null ? "---" : quote.getPricingDate()) + "\n";
		// NUMBER
		renderedQuote += "NUMBER:  " + (quote.getContractNumber() == null ? "---" : quote.getContractNumber()) + "\n";
		// EXPIRES
		renderedQuote += "EXPIRES: " + (quote.getExpiresOn() == null ? "---" : quote.getExpiresOn()) + "\n";
		renderedQuote += "\n\n";
		// POSITIONS
		renderedQuote += "ITEM          QUANTITY  PRODUCT               UNIT PRICE  SALES TAX          TOTAL\n";
		org.opencrx.kernel.contract1.cci2.QuotePositionQuery quotePositionQuery = (org.opencrx.kernel.contract1.cci2.QuotePositionQuery)pm.newQuery(org.opencrx.kernel.contract1.jmi1.QuotePosition.class);
		quotePositionQuery.orderByPositionNumber().ascending();
		for(org.opencrx.kernel.contract1.jmi1.QuotePosition quotePosition: quote.<org.opencrx.kernel.contract1.jmi1.QuotePosition>getPosition(quotePositionQuery)) {
			renderedQuote += String.format("%-10s  ", quotePosition.getName());
			renderedQuote += String.format("%10.2f  ", quotePosition.getQuantity());
			renderedQuote += String.format("%-20s  ", (quotePosition.getProduct().getName() == null ? "--none--" : quotePosition.getProduct().getName()));
			renderedQuote += String.format("%10.2f  ", quotePosition.getPricePerUnit());
			renderedQuote += String.format("%-12s  ", (quotePosition.getSalesTaxType() == null ? "--none--" : quotePosition.getSalesTaxType().getName()));
			renderedQuote += String.format("%10.2f", quotePosition.getAmount());
		}
		renderedQuote += "\n\n";
		renderedQuote += "We will be happy to supply any further information you may need and trust that you\n";
		renderedQuote += "call on us to fill your order, which will receive our prompt and careful attention.";			
		// Save as file and render download link
        String downloadFileName = quote.getName() == null ? quote.refGetPath().getLastSegment().toString() : quote.getName() + ".txt";
        String mimeType = "text/plain";	        
        String location = org.opencrx.kernel.utils.Utils.getUidAsString();
		OutputStream fileos = new FileOutputStream(app.getTempFileName(location, ""));
		fileos.write(renderedQuote.getBytes("UTF-8"));
		fileos.close();
		Action downloadFileAction = getDownloadFileAction(location, downloadFileName, mimeType, app);
%>
		<br />
		<div class="OperationDialogTitle">Sample - Print Quote</div>
		<br />
		<a href="<%= downloadFileAction.getEncodedHRef(requestId) %>"><%= downloadFileAction.getTitle() %></a>		
		<br />
<%			
   	}
} catch(Exception e) {
	new ServiceException(e).log();
}
%>

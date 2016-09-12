<%@page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%
/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: SegmentSetup
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2005-2016, CRIXP Corp., Switzerland
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
%>
<%@page session="true" import="
java.util.*,
java.io.*,
java.text.*,
org.opencrx.kernel.backend.*,
org.opencrx.kernel.generic.*,
org.openmdx.kernel.id.cci.*,
org.openmdx.kernel.id.*,
org.openmdx.base.exception.*,
org.openmdx.base.accessor.jmi.cci.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.attribute.*,
org.openmdx.portal.servlet.component.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.wizards.*,
org.openmdx.base.naming.*
" %>
<%!

	public static class SegmentSetupController extends org.opencrx.kernel.portal.wizard.SegmentSetupController {
	
		public SegmentSetupController(
		) {
			super();
		}
		
		public org.opencrx.kernel.home1.jmi1.ExportProfile findExportProfile(
			String exportProfileName,
			org.opencrx.kernel.home1.jmi1.UserHome userHome,
			javax.jdo.PersistenceManager pm
		) {
			org.opencrx.kernel.home1.cci2.ExportProfileQuery query =
			    (org.opencrx.kernel.home1.cci2.ExportProfileQuery)pm.newQuery(org.opencrx.kernel.home1.jmi1.ExportProfile.class);
			query.name().equalTo(exportProfileName);
			Collection exportProfiles = userHome.getExportProfile(query);
			if(!exportProfiles.isEmpty()) {
				return (org.opencrx.kernel.home1.jmi1.ExportProfile)exportProfiles.iterator().next();
			}
			return null;
		}

		public org.opencrx.kernel.document1.jmi1.Document findDocument(
			String documentName,
			org.opencrx.kernel.document1.jmi1.Segment segment
		) throws ServiceException {
			return Documents.getInstance().findDocument(
				documentName,
				segment
			);
		}

		public org.opencrx.kernel.document1.jmi1.DocumentFolder findDocumentFolder(
			String documentFolderName,
			org.opencrx.kernel.document1.jmi1.Segment segment
		) throws ServiceException {
			return Documents.getInstance().findDocumentFolder(
				documentFolderName,
				segment
			);
		}

		public org.opencrx.kernel.document1.jmi1.DocumentFolder initDocumentFolder(
			String documentFolderName,
			org.opencrx.kernel.document1.jmi1.Segment segment,
			List<org.opencrx.security.realm1.jmi1.PrincipalGroup> allUsers
		) throws ServiceException {
			return Documents.getInstance().initDocumentFolder(
				documentFolderName,
				segment,
				allUsers
			);
		}

		public org.opencrx.kernel.document1.jmi1.Document initDocument(
			String documentName,
			java.net.URL revisionURL,
			String revisionMimeType,
			String revisionName,
			org.opencrx.kernel.document1.jmi1.DocumentFolder documentFolder,
			org.opencrx.kernel.document1.jmi1.Segment segment,
			List<org.opencrx.security.realm1.jmi1.PrincipalGroup> allUsers
		) throws ServiceException {
			return Documents.getInstance().initDocument(
				documentName,
				documentName,
				revisionURL,
				revisionMimeType,
				revisionName,
				documentFolder,
				segment,
				allUsers
			);
		}

		public org.opencrx.kernel.home1.jmi1.ExportProfile initExportProfile(
			String exportProfileName,
			String[] forClass,
			String mimeType,
			String exportParams,
			org.opencrx.kernel.document1.jmi1.Document template,
			org.opencrx.kernel.home1.jmi1.UserHome userHome,
			List allUsers
		) {
			javax.jdo.PersistenceManager pm = javax.jdo.JDOHelper.getPersistenceManager(userHome);
			org.opencrx.kernel.home1.jmi1.ExportProfile exportProfile = findExportProfile(
				exportProfileName,
				userHome,
				pm
			);
			if(exportProfile != null) return exportProfile;
			try {
				pm.currentTransaction().begin();
				exportProfile = pm.newInstance(org.opencrx.kernel.home1.jmi1.ExportProfile.class);
				exportProfile.refInitialize(false, false);
				exportProfile.setName(exportProfileName);
				exportProfile.getForClass().addAll(
					Arrays.asList(forClass)
				);
				exportProfile.setMimeType(mimeType);
				exportProfile.setExportParams(exportParams);
				exportProfile.setTemplate(template);
				exportProfile.getOwningGroup().addAll(allUsers);
				userHome.addExportProfile(
					false,
					org.opencrx.kernel.backend.Activities.getInstance().getUidAsString(),
					exportProfile
				);
				pm.currentTransaction().commit();
			}
			catch(Exception e) {
				try {
					pm.currentTransaction().rollback();
				} catch(Exception e0) {}
			}
			return exportProfile;
		}

		public void customSetup(
		) throws ServiceException {
			javax.jdo.PersistenceManager pm = this.getPm();
			try {
				org.openmdx.security.realm1.jmi1.Realm realm = org.opencrx.kernel.backend.SecureObject.getInstance().getRealm(pm, this.getProviderName(), this.getSegmentName());
				org.opencrx.security.realm1.jmi1.PrincipalGroup usersPrincipalGroup =
					(org.opencrx.security.realm1.jmi1.PrincipalGroup)org.opencrx.kernel.backend.SecureObject.getInstance().findPrincipal(
						"Users",
						realm
					);
				org.opencrx.security.realm1.jmi1.PrincipalGroup administratorsPrincipalGroup =
					(org.opencrx.security.realm1.jmi1.PrincipalGroup)org.opencrx.kernel.backend.SecureObject.getInstance().findPrincipal(
						"Administrators",
						realm
					);
				List<org.opencrx.security.realm1.jmi1.PrincipalGroup> allUsers = new ArrayList<org.opencrx.security.realm1.jmi1.PrincipalGroup>();
				allUsers.add(usersPrincipalGroup);
				allUsers.add(administratorsPrincipalGroup);			

				// Opportunity export profile
				{
					org.opencrx.kernel.document1.jmi1.DocumentFolder templateFolderOpportunity = initDocumentFolder(
						TEMPLATE_FOLDER_NAME_OPPORTUNITY,
						this.getDocumentSegment(),
						allUsers
					);
					org.opencrx.kernel.document1.jmi1.Document templateOpportunity = initDocument(
						TEMPLATE_NAME_OPPORTUNITY,
						this.getSession().getServletContext().getResource("/documents/Template_Opportunity.rtf"),
						"text/rtf",
						TEMPLATE_NAME_OPPORTUNITY,
						templateFolderOpportunity,
						this.getDocumentSegment(),
						allUsers
					);
					initExportProfile(
						TEMPLATE_NAME_OPPORTUNITY,
						new String[]{
							"org:opencrx:kernel:contract1:Opportunity"
						},
						"text/rtf",
						null, // referenceFilter
						templateOpportunity,
						this.getUserHome(),
						allUsers
					);
				}
				// Quote export profile
				{
					org.opencrx.kernel.document1.jmi1.DocumentFolder templateFolderQuote = initDocumentFolder(
						TEMPLATE_FOLDER_NAME_QUOTE,
						this.getDocumentSegment(),
						allUsers
					);
					org.opencrx.kernel.document1.jmi1.Document templateQuote = initDocument(
						TEMPLATE_NAME_QUOTE,
						this.getSession().getServletContext().getResource("/documents/Template_Quote.rtf"),
						"text/rtf",
						TEMPLATE_NAME_QUOTE,
						templateFolderQuote,
						this.getDocumentSegment(),
						allUsers
					);
					initExportProfile(
						TEMPLATE_NAME_QUOTE,
						new String[]{
							"org:opencrx:kernel:contract1:Quote"
						},
						"text/rtf",
						null, // referenceFilter
						templateQuote,
						this.getUserHome(),
						allUsers
					);
				}
				// Sales order export profile
				{
					org.opencrx.kernel.document1.jmi1.DocumentFolder templateFolderSalesOrder = initDocumentFolder(
						TEMPLATE_FOLDER_NAME_SALESORDER,
						this.getDocumentSegment(),
						allUsers
					);
					org.opencrx.kernel.document1.jmi1.Document templateSalesOrder = initDocument(
						TEMPLATE_NAME_SALESORDER,
						this.getSession().getServletContext().getResource("/documents/Template_SalesOrder.rtf"),
						"text/rtf",
						TEMPLATE_NAME_SALESORDER,
						templateFolderSalesOrder,
						this.getDocumentSegment(),
						allUsers
					);
					initExportProfile(
						TEMPLATE_NAME_SALESORDER,
						new String[]{
							"org:opencrx:kernel:contract1:SalesOrder"
						},
						"text/rtf",
						null, // referenceFilter
						templateSalesOrder,
						this.getUserHome(),
						allUsers
					);
				}
				// Invoice export profile
				{
					org.opencrx.kernel.document1.jmi1.DocumentFolder templateFolderInvoice = initDocumentFolder(
						TEMPLATE_FOLDER_NAME_INVOICE,
						this.getDocumentSegment(),
						allUsers
					);
					org.opencrx.kernel.document1.jmi1.Document templateInvoice = initDocument(
						TEMPLATE_NAME_INVOICE,
						this.getSession().getServletContext().getResource("/documents/Template_Invoice.rtf"),
						"text/rtf",
						TEMPLATE_NAME_INVOICE,
						templateFolderInvoice,
						this.getDocumentSegment(),
						allUsers
					);
					initExportProfile(
						TEMPLATE_NAME_INVOICE,
						new String[]{
							"org:opencrx:kernel:contract1:Invoice"
						},
						"text/rtf",
						null, // referenceFilter
						templateInvoice,
						this.getUserHome(),
						allUsers
					);
				}
			} catch(Exception e) {
				try {
					pm.currentTransaction().rollback();
				} catch(Exception e0) {}
				new ServiceException(e).log();
			}
		}

		@Override
		public void doSetup(
		) throws ServiceException {
			super.doSetup();
			this.customSetup();
		}

		public void customRenderSetupReport(
			Writer out
		) throws ServiceException, IOException {
			javax.jdo.PersistenceManager pm = this.getPm();
			org.opencrx.kernel.document1.jmi1.Segment documentSegment = this.getDocumentSegment();
			org.opencrx.kernel.home1.jmi1.UserHome userHome = this.getUserHome();
			out.append("<div class=\"col2\">");
			out.append("<fieldset>");
			out.append("	<legend>Sample-specific configuration</legend>");
			out.append("	<table>");
			out.append("		<tr>");
			out.append("			<td colspan=\"2\"><h2>Export Profiles</h2></td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td style=\"width:400px;\">" + TEMPLATE_NAME_OPPORTUNITY + "</td>");
			out.append("			<td>" + (findExportProfile(TEMPLATE_NAME_OPPORTUNITY, userHome, pm) == null ? MISSING : OK) + "</td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td>" + TEMPLATE_NAME_QUOTE + "</td>");
			out.append("			<td>" + (findExportProfile(TEMPLATE_NAME_QUOTE, userHome, pm) == null ? MISSING : OK) + "</td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td>" + TEMPLATE_NAME_SALESORDER + "</td>");
			out.append("			<td>" + (findExportProfile(TEMPLATE_NAME_SALESORDER, userHome, pm) == null ? MISSING : OK) + "</td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td>" + TEMPLATE_NAME_INVOICE + "</td>");
			out.append("			<td>" + (findExportProfile(TEMPLATE_NAME_INVOICE, userHome, pm) == null ? MISSING : OK) + "</td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td colspan=\"2\"><h2>Contract Templates</h2></td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td>" + TEMPLATE_NAME_OPPORTUNITY + "</td>");
			out.append("			<td>" + (findDocument(TEMPLATE_NAME_OPPORTUNITY, documentSegment) == null ? MISSING : OK) + "</td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td>" + TEMPLATE_NAME_QUOTE + "</td>");
			out.append("			<td>" + (findDocument(TEMPLATE_NAME_QUOTE, documentSegment) == null ? MISSING : OK) + "</td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td>" + TEMPLATE_NAME_SALESORDER + "</td>");
			out.append("			<td>" + (findDocument(TEMPLATE_NAME_SALESORDER, documentSegment) == null ? MISSING : OK) + "</td>");
			out.append("		</tr>");
			out.append("		<tr>");
			out.append("			<td>" + TEMPLATE_NAME_INVOICE + "</td>");
			out.append("			<td>" + (findDocument(TEMPLATE_NAME_INVOICE, documentSegment) == null ? MISSING : OK) + "</td>");
			out.append("		</tr>");
			out.append("	</table>");
			out.append("</fieldset>");
			out.append("</div>");
		}

		@Override
		public void renderSetupReport(
			Writer out
		) throws ServiceException, IOException {
			super.renderSetupReport(out);
			this.customRenderSetupReport(out);
		}
	
		public static final String TEMPLATE_FOLDER_NAME_OPPORTUNITY = "Opportunity Templates";
		public static final String TEMPLATE_NAME_OPPORTUNITY = "Opportunity with Positions (RTF)";
		public static final String TEMPLATE_FOLDER_NAME_QUOTE = "Quote Templates";
		public static final String TEMPLATE_NAME_QUOTE = "Quote with Positions (RTF)";
		public static final String TEMPLATE_FOLDER_NAME_SALESORDER = "Sales Order Templates";
		public static final String TEMPLATE_NAME_SALESORDER = "Sales Order with Positions (RTF)";
		public static final String TEMPLATE_FOLDER_NAME_INVOICE = "Invoice Templates";
		public static final String TEMPLATE_NAME_INVOICE = "Invoice with Positions (RTF)";

		public static final String REPORT_TEMPLATE_FOLDER_NAME = "Report Templates";
		
	}
%>
<%
	final String WIZARD_NAME = "SegmentSetup.jsp";
	SegmentSetupController wc = new SegmentSetupController();
%>
	<t:wizardHandleCommand controller='<%= wc %>' defaultCommand='Refresh' />
<%
	if(response.getStatus() != HttpServletResponse.SC_OK) {
		wc.close();
		return;
	}
	org.openmdx.portal.servlet.ApplicationContext app = wc.getApp();
	javax.jdo.PersistenceManager pm = wc.getPm();
	String requestIdParam = Action.PARAMETER_REQUEST_ID + "=" + wc.getRequestId();
	String xriParam = Action.PARAMETER_OBJECTXRI + "=" + java.net.URLEncoder.encode(wc.getObjectIdentity().toXRI(), "UTF-8");
%>
<head>
	<style type="text/css" media="all">
		body{
		  font-family: "Open Sans", "DejaVu Sans Condensed", "lucida sans", tahoma, verdana, arial, sans-serif;
			padding: 0; margin:0;}
		h1{ margin: 0.5em 0em; font-size: 150%;}
		h2{ font-size: 130%; margin: 0.5em 0em; text-align: left;}
    textarea,
    input[type='text'],
    input[type='password']{
    	width: 100%;
    	margin: 0; border: 1px solid silver;
    	padding: 0;
    	font-size: 100%;
		  font-family: "Open Sans", "DejaVu Sans Condensed", "lucida sans", tahoma, verdana, arial, sans-serif;
    }
    input.button{
    	-moz-border-radius: 4px;
    	-webkit-border-radius: 4px;
    	width: 120px;
    	border: 1px solid silver;
    }
		.col1,
		.col2{float: left; width: 49.5%;}
	</style>
	<title>openCRX - Segment Setup Wizard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link rel="stylesheet" href="../../js/bootstrap/css/bootstrap.min.css">	
	<link rel="stylesheet" href="../../_style/colors.css">
	<link rel="stylesheet" href="../../_style/n2default.css">
	<link rel="stylesheet" href="../../_style/ssf.css">	
	<link rel='shortcut icon' href='../../images/favicon.ico' />
	<script type="text/javascript" src="../../js/portal-all.js"></script>	
</head>
<body>
<div id="container">
	<div id="wrap">
		<div id="header" style="height:90px;">
      <div id="logoTable">
        <table id="headerlayout">
          <tr id="headRow">
            <td id="head" colspan="2">
              <table id="info">
                <tr>
                  <td id="headerCellLeft"><img id="logoLeft" src="../../images/logoLeft.gif" alt="openCRX" title="" /></td>
                  <td id="headerCellSpacerLeft"></td>
                  <td id="headerCellMiddle">&nbsp;</td>
                  <td id="headerCellRight"><img id="logoRight" src="../../images/logoRight.gif" alt="" title="" /></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </div>
    </div>
    <div id="content-wrap">
    	<div id="content" style="padding:10px 0.5em 0px 0.5em;">
			<form method="post" action="<%= WIZARD_NAME %>">
				<% wc.renderSetupReport(out); %>
				<br />
				<div class="buttons">
					<input type="hidden" name="<%= Action.PARAMETER_REQUEST_ID %>" value="<%= wc.getRequestId() %>" />
					<input type="hidden" name="<%= Action.PARAMETER_OBJECTXRI %>" value="<%= wc.getObjectIdentity().toXRI() %>" />
					<input type="hidden" id="Command" name="Command" value="" /> 
<%
					if(wc.isCurrentUserIsAdmin()) {
%>
						<input type="Submit" name="Setup" value="<%= app.getTexts().getSaveTitle() %>" onclick="javascript:$('Command').value=this.name;" />
<%
					}
%>
					<input type="Submit" name="Cancel" value="<%= app.getTexts().getCloseText() %>" onclick="javascript:$('Command').value=this.name;" />
					<%= wc.isCurrentUserIsAdmin() ? "" : "<h2>This wizard requires admin permissions.</h2>" %>
					<br />
				</div>
			</form>
			<br />
      </div> <!-- content -->
    </div> <!-- content-wrap -->
  </div> <!-- wrap -->
</div> <!-- container -->
</body>
</html>
<t:wizardClose controller="<%= wc %>" />

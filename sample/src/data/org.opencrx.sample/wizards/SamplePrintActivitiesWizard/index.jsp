<%@page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%
/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Name:        SamplePrintActivitiesWizard.jsp
 * Description: Create report with list of activities
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
%>
<%@ page session="true" import="
java.util.*,
java.util.regex.*,
java.io.*,
java.text.*,
java.math.*,
java.net.URL,
java.net.URLEncoder,
java.net.MalformedURLException,
java.io.UnsupportedEncodingException,
org.opencrx.kernel.backend.*,
org.opencrx.kernel.portal.*,
org.openmdx.kernel.id.*,
org.openmdx.portal.servlet.databinding.*,
org.openmdx.base.accessor.jmi.cci.*,
org.openmdx.base.exception.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.attribute.*,
org.openmdx.portal.servlet.component.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.wizards.*,
org.openmdx.base.naming.*,
org.openmdx.base.query.*,
org.openmdx.kernel.log.*
" %>
<%
	final String FORM_NAME = "SamplePrintActivities";	
	org.opencrx.sample.wizard.SamplePrintActivitiesWizardController wc = new org.opencrx.sample.wizard.SamplePrintActivitiesWizardController();
%>
	<t:wizardHandleCommand controller='<%= wc %>' defaultCommand='Refresh' />
<%
	if(response.getStatus() != HttpServletResponse.SC_OK) {
		wc.close();
		return;
	}
	ApplicationContext app = wc.getApp();
	javax.jdo.PersistenceManager pm = wc.getPm();
	RefObject_1_0 obj = wc.getObject();
%>
<div class="OperationDialogTitle"><%= wc.getToolTip() %></div>
<form id="<%= FORM_NAME %>" name="<%= FORM_NAME %>" accept-charset="UTF-8" method="POST" action="<%= wc.getServletPath() %>">
	<input type="hidden" name="<%= Action.PARAMETER_REQUEST_ID %>" value="<%= wc.getRequestId() %>" />
	<input type="hidden" name="<%= Action.PARAMETER_OBJECTXRI %>" value="<%= wc.getObjectIdentity().toXRI() %>" />
	<input type="hidden" id="Command" name="Command" value="" />
<%
	if(wc.getDownloadFileAction() != null) {
		Action downloadFileAction = wc.getDownloadFileAction();
%>
		<br />
		<a href="<%= downloadFileAction.getEncodedHRef(wc.getRequestId()) %>"><%= downloadFileAction.getTitle() %></a>		
		<br />
<%
	} else {
%>		
		<div id="WaitIndicator" style="width:50px;height:24px;" class="wait">&nbsp;</div>
		<div id="SubmitArea" style="display:none;">
			<input type="submit" name="OK" id="OK.Button" class="<%= CssClass.btn.toString() %> <%=CssClass.btn_light.toString()%>" tabindex="9010" value="<%=app.getTexts().getOkTitle()%>" onclick="javascript:$('WaitIndicator').style.display='block';$('SubmitArea').style.display='none'; $('Command').value=this.name;this.name='---';" />
			<input type="submit" name="Cancel" class="<%=CssClass.btn.toString()%> <%=CssClass.btn_light.toString()%>" tabindex="9020" value="<%= app.getTexts().getCancelTitle() %>" onclick="javascript:$('WaitIndicator').style.display='block';$('SubmitArea').style.display='none'; $('Command').value=this.name;" />
		</div>
<%					
	}
%>
</form>
<br />
<script type="text/javascript">
	Event.observe('<%= FORM_NAME %>', 'submit', function(event) {
		$('<%= FORM_NAME %>').request({
			onFailure: function() { },
			onSuccess: function(t) {
				$('UserDialog').update(t.responseText);
			}
		});
		Event.stop(event);
	});
	$('WaitIndicator').style.display='none';
	$('SubmitArea').style.display='block';	
</script>
<t:wizardClose controller="<%= wc %>" />

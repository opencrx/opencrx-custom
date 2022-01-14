<%@ page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8"%><%
/*
 * ====================================================================
 * Project:     openCRX/Workshop, http://www.opencrx.org/
 * Description: Update vcards of accounts
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
%><%@ page session="true" import="
java.util.Enumeration,
java.io.PrintWriter"
%>
<%  request.setCharacterEncoding("UTF-8"); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
  <title>openCRX Snoop Wizard</title>
  <meta name="label" content="openCRX Snoop Wizard">
  <meta name="toolTip" content="openCRX Snoop Wizard">
  <meta name="targetType" content="_blank">
  <meta name="forClass" content="org:opencrx:kernel:home1:UserHome">
  <meta name="order" content="9999">
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link rel="stylesheet" href="../../javascript/bootstrap/css/bootstrap.min.css">	
	<link rel="stylesheet" href="../../_style/colors.css">
	<link rel="stylesheet" href="../../_style/n2default.css">
	<link rel="stylesheet" href="../../_style/ssf.css">  
  <link rel='shortcut icon' href='../../images/favicon.ico' />
</head>

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
    	<div id="content" style="padding:100px 0.5em 0px 0.5em;">

<table cellspacing="8" class="tableLayout">
  <tr>
    <td style="padding-left:15px;">
      <table class="objectTitle">
        <tr>
          <td>
            <div>
              openCRX Snoop Wizard
            </div>
          </td>
        </tr>
      </table>
      <br />

<%
  try {
%>
      The Snoop wizard returns information about the HTTP request.
      <h3>Servlet Spec Version Implemented</h3>
      <pre style="font-family:Courier;">
<%= getServletConfig().getServletContext().getMajorVersion() + "." +
      getServletConfig().getServletContext().getMinorVersion() %></pre>
      <h3>Requested URL</h3>
      <pre style="font-family:Courier;"><%= request.getRequestURL().toString() %></pre>
      <h3>Request parameters</h3>
      <pre style="font-family:Courier;">
<%
    Enumeration e = request.getParameterNames();
    while(e.hasMoreElements()){
      String key = (String)e.nextElement();
      String[] paramValues = request.getParameterValues(key);
      for(int i=0;i < paramValues.length;i++){
        out.println(key + " : "  + paramValues[i]);
      }
    }
%>
      </pre>
      <h3>Request information</h3>
      <pre style="font-family:Courier;">Request Method:   <%= request.getMethod() %>
Request URI:      <%= request.getRequestURI() %>
Request Protocol: <%= request.getProtocol() %>
Servlet Path:     <%= request.getServletPath() %>
Context Path:     <%= request.getContextPath() %>
Path Info:        <%= request.getPathInfo() %>
Path Translated:  <%= request.getPathTranslated() %>
Query String:     <%= request.getQueryString() %>
Content Length:   <%= request.getContentLength() %>
Content Type:     <%= request.getContentType() %>
Server Name:      <%= request.getServerName() %>
Server Port:      <%= request.getServerPort() %>
Remote User:      <%= request.getRemoteUser() %>
Remote Address:   <%= request.getRemoteAddr() %>
Remote Host:      <%= request.getRemoteHost() %>
Auth. Scheme:     <%= request.getAuthType() %>
</pre>
      <h3>Certificate Information</h3>
      <pre style="font-family:Courier;">
<%
      java.security.cert.X509Certificate certs [] = (java.security.cert.X509Certificate [])
        request.getAttribute("javax.servlet.request.X509Certificate");

      if ((certs != null) && (certs.length > 0)) {
%>
Subject Name : <%= certs[0].getSubjectDN().getName() %> <br>
Issuer Name :<%= certs[0].getIssuerDN().getName() %> <br>
Certificate Chain Length : <%= certs.length %> <br>
<%
        // List the Certificate chain
        for (int i=0; i<certs.length;i++) {
%>
Certificate[<%= i %>] : <%= certs[i].toString() %>
<%
        } // end of for loop
      }
      else // certs==null
      {
%>Not using SSL or client certificate not required.<%
      }
%>
      </pre>
      <h3>Request headers</h3>
      <pre style="font-family:Courier;">
<%
    e = request.getHeaderNames();
    while (e.hasMoreElements()) {
      String name = (String)e.nextElement();
      out.println(name + ": " + request.getHeader(name));
    }
%>
      </pre>
<%
  }
  catch (Exception ex) {
    out.println("<p><b>!! Snoop Failed !!<br><br>The following exception occur:</b><br><br>");
    ex.printStackTrace(new PrintWriter(out));
  }
%>

      </td>
    </tr>
  </table>
      </div> <!-- content -->
    </div> <!-- content-wrap -->
  </div> <!-- wrap -->
</div> <!-- container -->
</body>
</html>

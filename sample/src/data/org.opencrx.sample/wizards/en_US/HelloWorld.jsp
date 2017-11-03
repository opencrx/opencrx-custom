<%@ page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openCRX/Workshop, http://www.opencrx.org/
 * Description: HelloWorld
 * Owner:       CRIXP Corp., Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2010-2013, CRIXP Corp., Switzerland
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
<%  request.setCharacterEncoding("UTF-8"); %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
  <title>Hello World</title>
  <meta name="label" content="openCRX HelloWorld Wizard">
  <meta name="toolTip" content="openCRX HelloWorld Wizard">
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

<body>
  <div id="container">
  	<div id="wrap">
  		<div id="fixheader" style="height:90px;">
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

        <table cellspacing="8" class="tableLayout">
          <tr>
            <td style="padding-left:15px;">
              <table class="objectTitle">
                <tr>
                  <td>
                    <div>
                      openCRX Hello World Wizard
                    </div>
                  </td>
                </tr>
              </table>
              <br />
              <% out.print("<p> Hello World!"); %>
            </td>
          </tr>
        </table>

        </div> <!-- content -->
      </div> <!-- content-wrap -->
  	</div> <!-- wrap -->
  </div> <!-- container -->

</body>

</html>

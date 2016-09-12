/*
 * ====================================================================
 * Project:     opencCRX/Apps, http://www.opencrx.org/
 * Name:        $Id: ApplicationContextFactory.java,v 1.3 2010/08/30 15:35:41 wfro Exp $
 * Description: openCRX Context Factory
 * Revision:    $Revision: 1.3 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2010/08/30 15:35:41 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2009, CRIXP Corp., Switzerland
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
package org.opencrx.sample.store.common.util;

import java.util.Locale;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpSession;

import org.opencrx.kernel.utils.Utils;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;

public class ApplicationContextFactory {
    
    public static ApplicationContext createContext(
        HttpSession session
    ) throws Exception {
        // Get model
        if(ApplicationContextFactory.model == null) {
            ApplicationContextFactory.model = Utils.getModel();
        }        
        // Get persistence manager factory
        if(ApplicationContextFactory.pmf == null) {
            ApplicationContextFactory.pmf = Utils.getPersistenceManagerFactoryProxy(
            	session.getServletContext().getInitParameter("url"), 
            	session.getServletContext().getInitParameter("userName"), 
            	session.getServletContext().getInitParameter("password"), 
            	session.getServletContext().getInitParameter("mimeType")
            );
        }
        // Locale
        String localeAsString = session.getServletContext().getInitParameter("locale");
        Locale locale = localeAsString == null ?
            null :
            new Locale(localeAsString.substring(0,2), localeAsString.substring(3,5));
        Converter.setLocale(locale);
        // Currency
        String currencyCodeAsString = session.getServletContext().getInitParameter("currencyCode");
        Short currencyCode = currencyCodeAsString == null ?
            null :
            Short.valueOf(currencyCodeAsString);
        // SalesTax
        String salesTaxTypeName = session.getServletContext().getInitParameter("salesTaxTypeName");
        Path rootPath = new Path("xri://@openmdx*authority/" + session.getServletContext().getInitParameter("root"));
        String providerName = rootPath.get(2);
        String segmentName = rootPath.get(4);
        return new ApplicationContext(
            ApplicationContextFactory.pmf,
            providerName,
            segmentName,
            currencyCode,
            locale,
            salesTaxTypeName
        );
    }
        
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static PersistenceManagerFactory pmf = null;
    private static Model_1_0 model = null;
    
}

/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: ProductManager
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2014, CRIXP Corp., Switzerland
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

import javax.servlet.http.HttpServletRequest;

import org.opencrx.sample.store.common.PrimaryKey;

/**
 * Helper class for handling common operations with the request
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class RequestHelper
{
    public static final String CATEGORYID = "CategoryID";
    private final HttpServletRequest _request;

    public static final String ACTION = "action";
    public static final String ACTION_TYPE = "actionType";

    public static final String ACTION_LOGIN = "Login";
    public static final String ACTION_LOGOUT = "Logout";

    public static final String ACTION_ADD = "Add";
    public static final String ACTION_EDIT = "Edit";
    public static final String ACTION_DELETE = "Delete";
    public static final String ACTION_CHECKOUT = "Checkout";
    public static final String ACTION_REGISTER = "Register";
    public static final String ACTION_DELIVERED = "Delivered";
    public static final String ACTION_CANCEL = "Cancel" ;

    public final String actionType( final String type )
    {
        return "<input type=\"hidden\" name=\"" + RequestHelper.ACTION_TYPE + "\" value=\"" + type + "\">";
    }

    public final boolean isActionPerformed( final String actionType )
    {
        return this.getActionType().equals( actionType );
    }

    public final String getActionType()
    {
        return this.getParameter( RequestHelper.ACTION_TYPE );
    }
    public final String getAction()
    {
        return this.getParameter( RequestHelper.ACTION );
    }
    public final String getParameter( final String name )
    {
        final String action = this._request.getParameter( name );
        if( null == action )
            return "";
        else
            return action;
    }

    public RequestHelper( final HttpServletRequest request )
    {
        this._request = request;
    }

    public final PrimaryKey getCurrentCategoryID()
    {
        final String categoryID = this._request.getParameter( RequestHelper.CATEGORYID );
        if( null == categoryID )
            return new PrimaryKey("", false);
        else
            return new PrimaryKey(categoryID, false);
    }


}

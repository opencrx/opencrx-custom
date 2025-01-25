/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: ProductManager
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
package org.opencrx.sample.store.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.opencrx.sample.store.objects.Order;
import org.opencrx.sample.store.objects.User;

/**
 * Utility class for handling all Session stuff
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class SessionHelper
{
    public static final String SESSION_USER = "User";
    public static final String SESSION_ORDER = "Order";
    
    private final HttpServletRequest _request;

    public SessionHelper( final HttpServletRequest request )
    {
        this._request = request;
    }

    /**
     * Checks if there is any user currently logged in
     * @return
     */
    public final boolean isUserLoggedIn()
    {
        final HttpSession session = this._request.getSession(false);
        if( null == session )
            return false;
        else
        {
            final User user = (User) session.getAttribute( SessionHelper.SESSION_USER );
            if( null == user )
                return false;
            else
                return true;
        }
    }

    public final User getCurrentUser()
    {
        final User user = (User) this.getSession().getAttribute( SessionHelper.SESSION_USER );
        return user;
    }

    public final HttpSession getSession()
    {
        return this._request.getSession( true );
    }

    public final void setUser( final User user )
    {
        final HttpSession session = getSession();
        session.setAttribute( SessionHelper.SESSION_USER, user );
    }

    public final Order getOrder()
    {
        final Order order = (Order) this.getSession().getAttribute( SessionHelper.SESSION_ORDER );
        return order;
    }

    public final void setOrder( final Order order )
    {
        this.getSession().setAttribute( SessionHelper.SESSION_ORDER, order );
    }

    public final ApplicationContext getApplicationContext()
    {
        final ApplicationContext context = (ApplicationContext) this.getSession().getAttribute( ApplicationContext.ID );
        return context;
    }

    public final void setApplicationContext( final ApplicationContext context )
    {
        this.getSession().setAttribute( ApplicationContext.ID, context );
    }
    
    public final boolean isAdmin()
    {
        final User user = this.getCurrentUser();
        if( null == user )
            return false;

        return (User.USER_TYPE_ADMINISTRATOR == user.getUserType());
    }
}

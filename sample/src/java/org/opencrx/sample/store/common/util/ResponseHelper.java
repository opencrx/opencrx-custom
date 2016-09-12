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

import javax.servlet.http.HttpServletResponse;

import org.opencrx.sample.store.common.IStandardObject;
import org.opencrx.sample.store.common.PrimaryKey;

/**
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class ResponseHelper
{
    public static final String CATEGORYID = "CategoryID";
    public static final String PRODUCTID = "ProductID";

    public static final String SHOP_JSP = "Store.jsp";
    public static final String DELETE_PRODUCT_JSP = "DeleteProduct.jsp";
    public static final String DROP_ORDER_ITEM_JSP = "DropOrderItem.jsp";
    public static final String ADD_ORDER_ITEM_JSP = "AddOrderItem.jsp";
    public static final String CHECKOUT_JSP = "Checkout.jsp";
    public static final String ORDER_DETAILS_JSP = "OrderDetails.jsp";
    public static final String CHANGE_QUANTITY_JSP = "ChangeQuantity.jsp";

    public static final String TOP_10_PRODUCTS_REPORT_JSP = "Top10Products.jsp";
    public static final String MONTHLY_SALE__REPORT_JSP = "MonthlySale.jsp";
    public static final String CATEGORY_SALE_REPORT_JSP = "CategorySale.jsp";

    public ResponseHelper( final HttpServletResponse _response )
    {
    }

    public final String shopUrl( final PrimaryKey categoryID )
    {
        return ResponseHelper.SHOP_JSP + "?" +
                ResponseHelper.CATEGORYID + "=" + categoryID.toString();
    }

    public final String shopUrl( final PrimaryKey categoryID, final PrimaryKey productID )
    {
        return ResponseHelper.SHOP_JSP + "?" +
                ResponseHelper.CATEGORYID + "=" + categoryID.toString() + "&" +
                ResponseHelper.PRODUCTID + "=" + productID.toString() + "&";
    }

    public final String categoryHyperLink( final PrimaryKey categoryID,final String title )
    {
        return  this.makeHyperLink( shopUrl( categoryID ), title );
    }

    public final String makeHyperLink( final String url, final String title )
    {
        return "<a href=\"" + url + "\" >" + title + "</a>";
    }

    public final String makeHyperLink( final String url, final String title, final String className )
    {
        return "<a class=\"" + className + "\" href=\"" + url + "\" >" + title + "</a>";
    }

    public final String redirect( final String url )
    {
        return "<script>document.location.href=\"" + url + "\";</script>";
    }

    public final String productDeleteUrl( final PrimaryKey key )
    {
        return ResponseHelper.DELETE_PRODUCT_JSP + "?" + IStandardObject.PRIMARY_KEY + "=" + key.toString();
    }

    public final String dropOrderItemUrl( final PrimaryKey key )
    {
        return ResponseHelper.DROP_ORDER_ITEM_JSP + "?" + IStandardObject.PRIMARY_KEY + "=" + key.toString();
    }

    public final String addOrderItemUrl( final PrimaryKey key )
    {
        return ResponseHelper.ADD_ORDER_ITEM_JSP + "?" + IStandardObject.PRIMARY_KEY + "=" + key.toString();
    }

    public final String orderDetailsUrl( final PrimaryKey key )
    {
        return ResponseHelper.ORDER_DETAILS_JSP + "?" + IStandardObject.PRIMARY_KEY + "=" + key.toString();
    }

    public final String makeInputBox( final String name, final String value, final int columns )
    {
        return "<input name=\"" + name + "\" value=\"" + value + "\" size=\"" + Integer.toString(columns) + "\" maxlength=\"" + Integer.toString(columns) + "\"" + ">";
    }
}

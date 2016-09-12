<%@ page import="
org.opencrx.sample.store.common.PrimaryKey,
org.opencrx.sample.store.common.util.*,
org.opencrx.sample.store.manager.ProductManager,
java.util.Iterator,
org.opencrx.sample.store.objects.Product"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!private static final String ACTION_TYPE = "ProductAction";%>
<%
    request.setCharacterEncoding("UTF-8");
    final RequestHelper requestHelper = new RequestHelper(request);
    final ResponseHelper responseHelper = new ResponseHelper(response );
    final SessionHelper sessionHelper = new SessionHelper( request );

    // 1. Get the current category ID
    final PrimaryKey categoryID = requestHelper.getCurrentCategoryID();
    final ProductManager productManager = new ProductManager(sessionHelper.getApplicationContext());

    // 1a. If any product ID specified in the URL, then admin is editing the product
    final PrimaryKey productID = new PrimaryKey(
        Converter.getString( requestHelper.getParameter( ResponseHelper.PRODUCTID ) ) ,
		false
	);

    // 2. Process actions on product list
    if( requestHelper.isActionPerformed( ACTION_TYPE ) )
    {
        final String action = requestHelper.getAction();
        
        if( action.equals( RequestHelper.ACTION_ADD ) || action.equals( RequestHelper.ACTION_EDIT ) )
        {
    final String title = Converter.getString( requestHelper.getParameter( Product.PROP_TITLE ) );
    final String details = Converter.getString( requestHelper.getParameter( Product.PROP_DETAILS ) );
    final boolean isAvailable = Converter.getBoolean( requestHelper.getParameter( Product.PROP_IS_AVAILABLE ) );
    final float price = Converter.getCurrency( requestHelper.getParameter( Product.PROP_UNIT_PRICE ) );
    final String pictureFile = Converter.getString( requestHelper.getParameter( Product.PROP_PICTURE_FILE ));

    final boolean isAddNew = action.equals( RequestHelper.ACTION_ADD );
    // 2a. If ADD mode, then create a new product, otherwise get the existing product
    final Product product = ( isAddNew ? new Product( ) : productManager.get( productID ) );
    
    product.setCategoryID( categoryID );
    product.setTitle( title );
    product.setDetails( details );
    product.setAvailable( isAvailable );
    product.setUnitPrice( price );
    product.setPictureFile( pictureFile );

    if( isAddNew )
        productManager.create( product );
    else
        productManager.update( product );
        }
    }

    // 2. Get the products in the category
    final java.util.Map<String,Object> collection = productManager.getInCategory( categoryID );

    // 3. Render product list
    if( collection.values().size() >  0 )
    {
%>
        <div style="width:440px">
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
        <tr style="background-color:gainsboro">
            <th width="100">&nbsp;&nbsp;</th>
            <th align="left" width="200">Title</th>
            <th width="50">Price</th>
            <th width="50">Avilability</th>
        </tr>
<%
    final Iterator iterator = collection.values().iterator();
        while (iterator.hasNext())
        {
    final Product product = (Product) iterator.next();
%>
            <tr>
                <td align="left" valign="top" rowspan="2"><img src="pics/<%=product.getPictureFile()%>" border="0"></td>
                <td align="left" valign="top" ><%=product.getTitle()%></td>
                <td align="center" valign="top" ><%=Converter.getString( product.getUnitPrice() )%>/-</td>
                <td align="center" valign="top" ><%=( product.isAvailable() ? "Available" : "Unavailable" )%></td>
            </tr>

            <tr>
                <td colspan="2" valign="top" align="left"><small><%=product.getDetails()%></small></td>
                <td align="center" valign="top" >
                <%
                    if( sessionHelper.isUserLoggedIn() )
                            {
                                out.println( responseHelper.makeHyperLink( responseHelper.addOrderItemUrl( product.getKey() ), "Buy" ) );

                                // Allow admin to delete/edit the product
                                if( sessionHelper.isAdmin() )
                                {
                                    out.println( " | " + responseHelper.makeHyperLink( responseHelper.productDeleteUrl( product.getKey() ), "Delete", "smallLink" ) );
                                    out.println( " | " + responseHelper.makeHyperLink( responseHelper.shopUrl( product.getCategoryID(), product.getKey() ), "Edit", "smallLink" ) );
                                }


                            }
                %>
                </td>
            </tr>
<%
    }
%>
        </table>
        </div>
<%
    }
    else
    {
%>
        No products available in this category.
<%
    }

    // ---------------- Product Console -------------------
%>
<%
    if( sessionHelper.isAdmin() )
    {

        // 1. Check if there is any product ID in the URL. If there is then
        // the product needs to be edited
        String productTitle = "";
        String isAvailable = "on";
        String unitPrice = "";
        String details = "";
        String pictureFileName = "";
        String action = RequestHelper.ACTION_ADD;

        // 2. Get the product, if the product exists, then it is in edit mode
        Product editProduct = productManager.get( productID );
        if( null != editProduct )
        {
    productTitle = editProduct.getTitle();
    isAvailable = ( editProduct.isAvailable() ? "on" : "" );
    unitPrice = Converter.getString( editProduct.getUnitPrice() );
    details = editProduct.getDetails();
    pictureFileName = editProduct.getPictureFile();
    action = RequestHelper.ACTION_EDIT;
        }
%>
<div class="admin-console">
<form method="post" accept-charset="utf-8">
<%= requestHelper.actionType( ACTION_TYPE ) %>
Add/Edit Product:<br>
Title: <input type="text" name="<%= Product.PROP_TITLE %>" value="<%= productTitle%>" ><br>
Available: <input type="checkbox" name="<%= Product.PROP_IS_AVAILABLE %>" value="<%= isAvailable %>" ><br>
Price: <input type="text" name="<%= Product.PROP_UNIT_PRICE %>" value="<%= unitPrice %>" ><br>
Description:<br>
<textarea name="<%= Product.PROP_DETAILS %>" ><%= details %></textarea><br>
Picture File Name: <input type="text" name="<%= Product.PROP_PICTURE_FILE %>" value="<%= pictureFileName %>" ><br>
<input type="submit" name="<%= RequestHelper.ACTION %>" value="<%= action %>" >
</form>
</div>
<%
    }
%>
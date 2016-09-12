<%@ page import="
org.opencrx.sample.store.common.PrimaryKey,
org.opencrx.sample.store.common.util.RequestHelper,
org.opencrx.sample.store.common.IStandardObject,
org.opencrx.sample.store.manager.ProductManager,
org.opencrx.sample.store.common.util.SessionHelper,
org.opencrx.sample.store.common.util.ResponseHelper,
org.opencrx.sample.store.objects.Product"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 1. Get the product ID from URL
    RequestHelper requestHelper = new RequestHelper( request );
	SessionHelper sessionHelper = new SessionHelper(request);
    PrimaryKey key = new PrimaryKey( requestHelper.getParameter( IStandardObject.PRIMARY_KEY ) );

    // 2. Delete the product
    ProductManager manager = new ProductManager(sessionHelper.getApplicationContext());
    Product product = manager.get( key );
    manager.delete( key );

    // 3. Return to shop page
    ResponseHelper responseHelper = new ResponseHelper( response );
    out.println( responseHelper.redirect( responseHelper.shopUrl( product.getCategoryID() )) );
%>
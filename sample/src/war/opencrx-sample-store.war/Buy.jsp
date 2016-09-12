<%@ page import="
org.opencrx.sample.store.common.util.RequestHelper,
org.opencrx.sample.store.common.PrimaryKey,
org.opencrx.sample.store.common.IStandardObject,
org.opencrx.sample.store.manager.ProductManager,
org.opencrx.sample.store.objects.Product,
org.opencrx.sample.store.common.util.ResponseHelper,
org.opencrx.sample.store.common.util.SessionHelper,
org.opencrx.sample.store.objects.Order,
org.opencrx.sample.store.objects.OrderItem,
org.opencrx.sample.store.manager.OrderItemManager,
org.opencrx.sample.store.manager.OrderManager"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 1. Get the product ID from URL
    final RequestHelper requestHelper = new RequestHelper( request );
	final SessionHelper sessionHelper = new SessionHelper(request);
    final PrimaryKey productID = new PrimaryKey( requestHelper.getParameter( IStandardObject.PRIMARY_KEY ) );

    // 2. Get the product
    final ProductManager productManager = new ProductManager(sessionHelper.getApplicationContext());
    Product product = productManager.get( productID );

    // 3. Get the cart
    Order order = sessionHelper.getOrder();

    // 4. If no order started yet, initiate an order
    final OrderManager orderManager = new OrderManager(sessionHelper.getApplicationContext());
    if( null == order )
    {
        // 4.1 Initiate a new order
        order = orderManager.startNew( sessionHelper.getCurrentUser().getKey() );
        sessionHelper.setOrder( order );
    }

    // 5. Add the current product in the order
    final OrderItemManager itemManager = new OrderItemManager(sessionHelper.getApplicationContext());
    final OrderItem item = itemManager.newOrder( order.getKey(), productID );

    // 6. Go back to shop page
    final ResponseHelper responseHelper = new ResponseHelper( response );
    out.println( responseHelper.redirect( responseHelper.shopUrl( product.getCategoryID() ) ) );
%>
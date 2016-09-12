<%@ page import="
java.util.Enumeration,
org.opencrx.sample.store.manager.OrderItemManager,
org.opencrx.sample.store.objects.OrderItem,
org.opencrx.sample.store.common.PrimaryKey,
org.opencrx.sample.store.common.util.*,
org.opencrx.sample.store.manager.ProductManager,
org.opencrx.sample.store.objects.Product"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    final SessionHelper sessionHelper = new SessionHelper(request);
    final OrderItemManager orderItemManager = new OrderItemManager(sessionHelper.getApplicationContext());
    final ProductManager productManager = new ProductManager(sessionHelper.getApplicationContext());

    // 1. Get the posted order items
    Enumeration enu = request.getParameterNames();
    while( enu.hasMoreElements() )
    {
        String name = (String) enu.nextElement();
		if(!"CategoryID".equals(name)) {
			String value = request.getParameter( name );
			System.out.println("name=" + name);
			System.out.println("value=" + value);
	
			// 1.2 Get the order item
			OrderItem item = orderItemManager.get( new PrimaryKey( name ) );
	
			// 1.3 If found, update the quantity
			if( null != item )
			{
				// 1.3.1 the value is the quantity
				int quantity = Integer.valueOf( value );
	
				// 1.3.2 Get the product in order to get the unit price of the product
				Product product = productManager.get( item.getProductID() );
	
				// 1.3.2 update the order item with the new quantity
				item.setQuantity( quantity );
				item.setPrice( quantity * product.getUnitPrice() );
	
				orderItemManager.update( item );
			}
		}
    }
	System.out.println("< update");
    final ResponseHelper responseHelper = new ResponseHelper( response );
    final RequestHelper requestHelper = new RequestHelper( request );

    // 2. Return to the shop page on the same category from where it came
    final String categoryID = requestHelper.getParameter( RequestHelper.CATEGORYID  );
    out.println( responseHelper.redirect( responseHelper.shopUrl( new PrimaryKey( categoryID ) ) ) );
%>
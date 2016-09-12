<%@ page import="
org.opencrx.sample.store.common.util.RequestHelper,
org.opencrx.sample.store.common.util.SessionHelper,
org.opencrx.sample.store.objects.Order,
org.opencrx.sample.store.manager.OrderManager,
org.opencrx.sample.store.manager.OrderItemManager,
java.util.Iterator,
org.opencrx.sample.store.objects.OrderItem,
org.opencrx.sample.store.manager.ProductManager,
org.opencrx.sample.store.objects.Product,
org.opencrx.sample.store.common.util.*,
org.opencrx.sample.store.common.util.*"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!private static final String ACTION_TYPE = "Checkout";%>
<!-- Show user cart -->
<h2 class="sidebar-title">Cart</h2>
<div class="sidebar-block">

<%
    final RequestHelper requestHelper = new RequestHelper( request );
    final ResponseHelper responseHelper = new ResponseHelper( response );
    final SessionHelper sessionHelper = new SessionHelper( request );

    // 1. Process postback
    if( requestHelper.getActionType().equals( ACTION_TYPE ) )
    {
        if( requestHelper.getAction().equals( RequestHelper.ACTION_CHECKOUT ) )
        {
    final OrderManager manager = new OrderManager(sessionHelper.getApplicationContext());
    final Order order = sessionHelper.getOrder();

    // 1.1 Get shipping address
    final String shippingAddress = Converter.getString( requestHelper.getParameter( Order.PROP_ADDRESS ) );

    // 1.2 Checkout
    final Order updatedOrder = manager.checkout( order.getKey(), shippingAddress );

    // 1.3 Store the updated order in the session
    final Order newOrder = manager.startNew( sessionHelper.getCurrentUser().getKey() );
    sessionHelper.setOrder( newOrder );

    out.println("<p class=\"information\">Order placed successfully.</p>");
        }
    }


    if( sessionHelper.isUserLoggedIn() )
    {
        // 1. Show current order
        Order order = sessionHelper.getOrder();
        if( null == order )
        {
    // 1.1 No order currently being processed, check if user has any last pending
    // order
    final OrderManager orderManager = new OrderManager(sessionHelper.getApplicationContext());
    // 1.2 Get the last order of the current user and see if it was pending
    final Order lastOrder = orderManager.getLastOrder( sessionHelper.getCurrentUser().getKey() );

    // 1.3 If there is no last order, then this is the first time the user is buying in his life
    if( null != lastOrder )
    {
        if( lastOrder.getStatus() == Order.STATUS_BUYING )
        {
            // 1.2.1 User did not finish the last order. So, set it as current order
            sessionHelper.setOrder( lastOrder );
            order = lastOrder;
        }
        else
        {
            // 1.3 User has no pending order nor any current order.
        }
    }
    else
    {
        // 1.4 No last order either
    }
        }

        // 2. Now check if any order is found, if found, show the order items
        if( null != order && ( order.getStatus() == Order.STATUS_BUYING ) )
        {
    // 2.1 Get the items in the order
    final OrderItemManager itemManager = new OrderItemManager(sessionHelper.getApplicationContext());
    final java.util.Map<String,Object> items = itemManager.getItemsInOrder( order.getKey() );

    final ProductManager productManager = new ProductManager(sessionHelper.getApplicationContext());
%>
<form method="POST" action="<%=ResponseHelper.CHANGE_QUANTITY_JSP%>" >
            <%
                float totalPrice = 0f;
                // 2.2 Render the items
                final Iterator iterator = items.values().iterator();
                while( iterator.hasNext() )
                {
                    final OrderItem item = (OrderItem) iterator.next();

                    Product product = productManager.get( item.getProductID() );

                    // 2.2.1 Show product title
                    out.println("<li><b>");
                    out.println( product.getTitle() );
                    out.println("</b><br>");

                    // 2.2.2 Show price of the product
                    out.println( Converter.getString( product.getUnitPrice() ) );
                    out.println("X");

                    /// 2.2.3 Show quantity of the products ordered and price of the product
                    out.println( responseHelper.makeInputBox( item.getKey().toString(), Converter.getString( item.getQuantity() ), 3 ) );
                    out.println("= ");
                    out.println( Converter.getString( item.getPrice() ) );
                    out.println("<br>");
                    out.println( responseHelper.makeHyperLink( responseHelper.dropOrderItemUrl( item.getKey() ), "Drop" ));

                    out.println("</li>");

                    totalPrice += item.getPrice();

                }

                if( totalPrice > 0 )
                {
            %>

            <br>
            <input type="hidden" name="<%=RequestHelper.CATEGORYID%>" value="<%=requestHelper.getCurrentCategoryID()%>">
            <p><input type="submit" value="Update" ></p>
            <%
                }
            %>
</form>
            <%
                // 2.3 Show total price
                if( totalPrice > 0 )
                {
                    out.println("<p><b>Total Price: " + Converter.getString( totalPrice ) + "</b></p>" );

                    // ---------------- Checkout form --------------------
            %>

                <form method="POST">
                <p>

                <%= requestHelper.actionType( ACTION_TYPE ) %>
                Shipping Address:<br>
                <textarea name="<%= Order.PROP_ADDRESS %>"><%= order.getAddress() %></textarea><br>
                <input type="submit" name="<%= RequestHelper.ACTION %>" value="<%= RequestHelper.ACTION_CHECKOUT %>" >
                </p>

                </form>

                <%
            }
            else
            {
                out.println("No product ordered.");
            }
        }
    }
    else
    {
        out.println("<p>Please login to purchase items.</p>");
    }

%>

</div>

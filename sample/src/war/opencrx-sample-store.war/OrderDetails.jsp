<%@ page import="
org.opencrx.sample.store.common.util.*,
org.opencrx.sample.store.common.PrimaryKey,
org.opencrx.sample.store.common.IStandardObject,
org.opencrx.sample.store.manager.OrderManager,
org.opencrx.sample.store.objects.Order,
org.opencrx.sample.store.manager.OrderItemManager,
java.util.Iterator,
org.opencrx.sample.store.objects.OrderItem,
org.opencrx.sample.store.objects.Product,
org.opencrx.sample.store.manager.UserManager,
org.opencrx.sample.store.objects.User,
org.opencrx.sample.store.manager.ProductManager"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!private static final String ACTION_TYPE = "OrderAction";%>
<%
    final RequestHelper requestHelper = new RequestHelper( request );
    final ResponseHelper responseHelper = new ResponseHelper( response );
    final SessionHelper sessionHelper = new SessionHelper( request );

    // 1. Get the Order ID from URL
    final PrimaryKey orderID = new PrimaryKey( Converter.getString( requestHelper.getParameter( IStandardObject.PRIMARY_KEY )), false);

    final OrderManager orderManager = new OrderManager(sessionHelper.getApplicationContext());
    final UserManager userManager = new UserManager(sessionHelper.getApplicationContext());

    // 1a. Perform postback
    if( requestHelper.getActionType().equals( ACTION_TYPE ) )
    {
        if( requestHelper.getAction().equals( RequestHelper.ACTION_DELIVERED ) )
        {
    orderManager.deliverOrder( orderID );
        }
        else if( requestHelper.getAction().equals( RequestHelper.ACTION_CANCEL ) )
        {
    orderManager.discardOrder( orderID );
        }
    }
    // 2. Load order details
    final Order order = orderManager.get( orderID );

    // 2a. Get the user who ordered
    final User buyer = userManager.get( order.getUserID() );

    // 2b. We will need rpoduct manager to get product details
    final ProductManager productManager = new ProductManager(sessionHelper.getApplicationContext());

    // 3. Load order items
    final OrderItemManager orderItemManager = new OrderItemManager(sessionHelper.getApplicationContext());
    final java.util.Map<String,Object> orderItems = orderItemManager.getItemsInOrder( orderID );

    // 4. Render order details
%>

<jsp:include page="TemplateBegin.jsp" />

  <form method="POST">
  <%=requestHelper.actionType( ACTION_TYPE )%>

						<!-- Begin .post -->
                        <a name="header"></a>
						<H3 class="post-title">Online store for you daily needs</H3>

                        <div class="post">
                        <H4 class="post-title">Order Details</H4>
                        <ul>
                        <li>Ordered by: <%=buyer.getName()%></li>
                        <li>Start date: <%=Converter.getString( order.getStartDate() )%></li>
                        <li>End date: <%=Converter.getString( order.getEndDate() )%></li>
                        <li>Status: <b><%=order.getStatusString()%></b></li>
                        </ul>
                        <p/>
                        Products:
                        <ul>
                        <%
                            float totalPrice = 0f;
                                            final Iterator iterator = orderItems.values().iterator();
                                            while( iterator.hasNext() )
                                            {
                                                OrderItem orderItem = (OrderItem) iterator.next();
                                                Product product = productManager.get( orderItem.getProductID() );

                                                out.println( "<li>" );
                                                out.println( product.getTitle() );
                                                out.println( " " + Converter.getString( orderItem.getQuantity() ) );
                                                out.println( " X " + Converter.getString( product.getUnitPrice() ) );
                                                out.println( " = " + Converter.getString( orderItem.getPrice() ) );
                                                out.println( "</li>" );

                                                totalPrice += orderItem.getPrice();
                                            }
                        %>
                        </ul>

                        <p>Total price: <%=Converter.getString( totalPrice )%></p>

                        <input type="submit" name="<%= RequestHelper.ACTION %>" value="<%= RequestHelper.ACTION_DELIVERED %>" >
                        |
                        <input type="submit" name="<%= RequestHelper.ACTION %>" value="<%= RequestHelper.ACTION_CANCEL %>">

                        <p />
                        </div>

</form>

<jsp:include page="TemplateMiddle.jsp"/>

                    <h2 class="sidebar-title">Control Panel</h2>
                    <div class="sidebar-block">
                    <p><a href="<%= ResponseHelper.SHOP_JSP %>">Back to shop</a></p>
                    </div>

<jsp:include page="TemplateEnd.jsp" />
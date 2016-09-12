<%@ page import="
org.opencrx.sample.store.common.util.RequestHelper,
org.opencrx.sample.store.common.util.ResponseHelper,
org.opencrx.sample.store.common.util.SessionHelper,
java.util.*,
org.opencrx.sample.store.common.PrimaryKey"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    final RequestHelper requestHelper = new RequestHelper( request );
    final ResponseHelper responseHelper = new ResponseHelper( response );
    final SessionHelper sessionHelper = new SessionHelper( request );

    if( sessionHelper.isAdmin() )
    {
        // 1. Load the orders placed within the specified date range
        List items = Collections.EMPTY_LIST;
        request.setAttribute( "ProductList", items );
        %>
        <h1>Sale by category</h1>

        <jsp:forward page="ProductSaleList.jsp" />
        <%
    }
    else
    {
%>
Admin view only
<%
    }
%>
